package com.it10x.foodappgstav7_31.printer.queue

import android.util.Log
import com.it10x.foodappgstav7_31.data.printqueue.PrintQueueEntity
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.printqueue.PrintQueueDao

import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.printer.common.PrintDocument
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File

class PrintQueueManager private constructor(
    private val dao: PrintQueueDao,
    private val printerManager: PrinterManager
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    private val channels = mutableMapOf<PrinterRole, Channel<PrintQueueEntity>>()

    companion object {

        @Volatile
        private var INSTANCE: PrintQueueManager? = null

        private const val STATUS_PENDING = "PENDING"
        private const val STATUS_QUEUED = "QUEUED"
        private const val STATUS_PRINTING = "PRINTING"
        private const val STATUS_FAILED = "FAILED"


        fun getInstance(
            dao: PrintQueueDao,
            printerManager: PrinterManager
        ): PrintQueueManager {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: PrintQueueManager(
                    dao,
                    printerManager
                ).also {
                    INSTANCE = it
                }
            }
        }
    }

    init {

        Log.e(
            "QUEUE_INIT",
            "🔥 PrintQueueManager CREATED ${hashCode()}"
        )

        PrinterRole.values().forEach { role ->
            val channel = Channel<PrintQueueEntity>(Channel.UNLIMITED)
            channels[role] = channel
            startWorker(channel)
        }

       // Log.e("QUEUE_INIT", "🔥 PrintQueueManager CREATED ${System.currentTimeMillis()}")

        scope.launch {
            loadPendingJobs()
        }
    }

    suspend fun enqueue(
        role: PrinterRole,
        document: PrintDocument,
        text: String,
        paymentMode: String? = null,
        grandTotal: Double? = null,
        referenceId: String,
        earnedPoints: Int,
    ) {
        enqueueJob(
            role = role,
            referenceId = referenceId,
            earnedPoints,
            jobType = "TEXT",
            text = text,
            imagePath = null,
            logoImagePath = null,
            qrData = document.qrData,
            paymentMode = paymentMode,
            grandTotal = grandTotal
        )
    }


    suspend fun enqueueImage(
        referenceId: String,
        earnedPoints: Int,
        role: PrinterRole,
        imagePath: String,
        paymentMode: String? = null,
        grandTotal: Double? = null
    ) {
        enqueueJob(
            role = role,
            referenceId = referenceId,
            earnedPoints = earnedPoints,
            jobType = "IMAGE",
            text = null,
            imagePath = imagePath,
            paymentMode = paymentMode,
            grandTotal = grandTotal
        )
    }


    private suspend fun enqueueJob(
        role: PrinterRole,
        referenceId: String,
        earnedPoints: Int,
        jobType: String,
        text: String? = null,
        // Existing image bill
        imagePath: String? = null,
        // New common bill logo
        logoImagePath: String? = null,
        // New payment QR
        qrData: String? = null,
        paymentMode: String? = null,
        grandTotal: Double? = null
    )
    {
        Log.d(
            "PRINT_QUEUE",
            "QR DATA = ${qrData?.take(80)}"
        )
        // --------------------------------------------------
        // DUPLICATE CHECK
        // --------------------------------------------------

        if (dao.existsByReferenceId(referenceId)) {

            Log.e(
                "PRINT_QUEUE",
                "Duplicate blocked referenceId=$referenceId jobType=$jobType"
            )

            return
        }

        // --------------------------------------------------
        // CREATE PRINT JOB
        // --------------------------------------------------

        val job = PrintQueueEntity(
            id = UUID.randomUUID().toString(),

            referenceId = referenceId,
            earnedPoints = earnedPoints,
            role = role.name,

            jobType = jobType,

            // Existing text bill
            text = text,

            // Existing image bill - DO NOT CHANGE
            imagePath = imagePath,

            // New logo
            logoImagePath = logoImagePath,

            // New payment QR
            qrData = qrData,

            paymentMode = paymentMode,
            grandTotal = grandTotal,

            status = "PENDING",
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )

        // --------------------------------------------------
        // DATABASE INSERT
        // --------------------------------------------------

        try {

            dao.insert(job)

        } catch (e: Exception) {

            Log.e(
                "PRINT_QUEUE",
                "Failed to insert print job referenceId=$referenceId",
                e
            )

            return
        }

        // --------------------------------------------------
        // SEND TO ROLE WORKER
        // --------------------------------------------------

        channels[role]?.send(job)

        Log.d(
            "PRINT_QUEUE",
            "ENQUEUED " +
                    "id=${job.id} " +
                    "referenceId=${job.referenceId} " +
                    "role=${job.role} " +
                    "jobType=${job.jobType}"
        )
    }

    private fun startWorker(channel: Channel<PrintQueueEntity>) {
        scope.launch {
            for (job in channel) {
                processJob(job)
            }
        }
    }


    private suspend fun processJob(
        job: PrintQueueEntity
    )
    {

        val role = PrinterRole.valueOf(job.role)

        Log.d(
            "PRINT_PROCESS",
            """
        START PRINT
        jobId=${job.id}
        referenceId=${job.referenceId}
        role=${job.role}
        jobType=${job.jobType}
        retry=${job.retryCount}
        image=${job.imagePath}
        """.trimIndent()
        )

        // --------------------------------------------------
        // MARK JOB AS PRINTING
        // --------------------------------------------------

        dao.updateStatus(
            id = job.id,
            status = STATUS_PRINTING,
            retry = job.retryCount
        )

        try {

            // --------------------------------------------------
            // WAIT FOR PRINTER RESULT
            // --------------------------------------------------

            withTimeout(30000) {

                suspendCancellableCoroutine<Unit> { cont ->

                    when (job.jobType) {

                        // ======================================
                        // TEXT PRINT
                        // ======================================

                        "TEXT" -> {

                            val text = requireNotNull(job.text) {
                                "TEXT print job has no text. jobId=${job.id}"
                            }

                            printerManager.printText(
                                role = role,
                                text = text,
                                paymentMode = job.paymentMode,
                                grandTotal = job.grandTotal,
                                qrData = job.qrData
                            ) {

                                if (cont.isActive) {
                                    cont.resume(Unit)
                                }
                            }
                        }

                        // ======================================
                        // IMAGE PRINT
                        // ======================================

                        "IMAGE" -> {

                            val imagePath = requireNotNull(job.imagePath) {
                                "IMAGE print job has no imagePath. jobId=${job.id}"
                            }

                            printerManager.printBitmap(
                                role = role,
                                imagePath = imagePath
                            ) {

                                if (cont.isActive) {
                                    cont.resume(Unit)
                                }
                            }
                        }

                        // ======================================
                        // UNKNOWN JOB
                        // ======================================

                        else -> {

                            throw IllegalArgumentException(
                                "Unknown print job type: ${job.jobType}"
                            )
                        }
                    }
                }
            }

            // --------------------------------------------------
            // PRINT SUCCESS
            // --------------------------------------------------

            dao.delete(job.id)

            // --------------------------------------------------
            // DELETE IMAGE FILE
            // TEXT jobs have imagePath = null
            // so nothing happens for TEXT.
            // --------------------------------------------------

            job.imagePath?.let { path ->

                try {

                    val file = File(path)

                    if (file.exists()) {

                        val deleted = file.delete()

                        Log.d(
                            "PRINT_QUEUE",
                            "IMAGE DELETE path=$path success=$deleted"
                        )

                    } else {

                        Log.d(
                            "PRINT_QUEUE",
                            "IMAGE FILE ALREADY MISSING path=$path"
                        )
                    }

                } catch (e: Exception) {

                    Log.e(
                        "PRINT_QUEUE",
                        "Failed to delete image file $path",
                        e
                    )
                }
            }

            Log.d(
                "PRINT_QUEUE",
                "SUCCESS jobId=${job.id}"
            )

        } catch (e: Exception) {

            // --------------------------------------------------
            // PRINT FAILED
            // --------------------------------------------------

            Log.e(
                "PRINT_QUEUE",
                "FAILED jobId=${job.id} " +
                        "type=${job.jobType} " +
                        "error=${e.message}",
                e
            )

            val newRetry = job.retryCount + 1

            // --------------------------------------------------
            // RETRY
            // Currently: 1 retry
            // --------------------------------------------------

            if (newRetry <= 1) {

                delay(3000)

                dao.updateStatus(
                    id = job.id,
                    status = STATUS_PENDING,
                    retry = newRetry
                )

                channels[role]?.send(
                    job.copy(
                        status = STATUS_PENDING,
                        retryCount = newRetry
                    )
                )

                Log.d(
                    "PRINT_QUEUE",
                    "RETRY scheduled " +
                            "jobId=${job.id} " +
                            "retry=$newRetry"
                )

            } else {

                // --------------------------------------------------
                // PERMANENT FAILURE
                // --------------------------------------------------

                dao.updateStatus(
                    id = job.id,
                    status = STATUS_FAILED,
                    retry = newRetry
                )

                Log.e(
                    "PRINT_QUEUE",
                    "GAVE UP jobId=${job.id}"
                )
            }
        }

        Log.d(
            "PRINT_QUEUE",
            "END jobId=${job.id}"
        )
    }

    private suspend fun processJob_old(job: PrintQueueEntity) {

        val role = PrinterRole.valueOf(job.role)

//        Log.e(
//            "PRINT_PROCESS",
//            """
//    START PRINT
//    jobId=${job.id}
//    referenceId=${job.referenceId}
//    role=${job.role}
//    retry=${job.retryCount}
//    image=${job.imagePath}
//    """.trimIndent()
//        )

//        dao.updateStatus(job.id, "PRINTING", job.retryCount)
        dao.updateStatus(
            job.id,
            STATUS_PRINTING,
            job.retryCount
        )
        try {

            // ⏱ Prevent infinite waiting if printer never responds
//            withTimeout(15000) {
//
//                suspendCancellableCoroutine<Unit> { cont ->
//
//                    when (job.jobType) {
//
//                        "TEXT" -> {
//                            printerManager.printText(
//                                role,
//                                requireNotNull(job.text) {
//                                    "Text job has no text."
//                                },
//                                job.paymentMode,
//                                job.grandTotal
//                            ) {
//                                if (cont.isActive) cont.resume(Unit)
//                            }
//                        }
//
//                        "IMAGE" -> {
//                            printerManager.printBitmap(
//                                role = role,
//                                imagePath = requireNotNull(job.imagePath) {
//                                    "Image job has no image path."
//                                }
//                            ) {
//                                if (cont.isActive) cont.resume(Unit)
//                            }
//                        }
//
//                        else -> {
//                            throw IllegalArgumentException(
//                                "Unknown job type ${job.jobType}"
//                            )
//                        }
//                    }
//                }
//            }

            withTimeout(30000) {

                suspendCancellableCoroutine<Unit> { cont ->

                    when (job.jobType) {

                        "TEXT" -> {
                            printerManager.printText(
                                role,
                                requireNotNull(job.text),
                                job.paymentMode,
                                job.grandTotal
                            ) {
                                if (cont.isActive) {
                                    cont.resume(Unit)
                                }
                            }
                        }

                        "IMAGE" -> {
                            printerManager.printBitmap(
                                role = role,
                                imagePath = requireNotNull(job.imagePath)
                            ) {
                                if (cont.isActive) {
                                    cont.resume(Unit)
                                }
                            }
                        }
                    }
                }
            }

            // ✅ Success → remove from queue
            // ✅ Success → remove from queue
            dao.delete(job.id)

// ✅ Delete printed image file
            job.imagePath?.let { path ->
                try {
                    val deleted = File(path).delete()

                    Log.d(
                        "PRINT_QUEUE",
                        "IMAGE DELETE path=$path success=$deleted"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "PRINT_QUEUE",
                        "Failed to delete image file $path",
                        e
                    )
                }
            }

            Log.d("PRINT_QUEUE", "SUCCESS ${job.id}")



        } catch (e: Exception) {

            Log.e("PRINT_QUEUE", "FAILED ${job.id}: ${e.message}")

            val newRetry = job.retryCount + 1

            // 🔁 Retry logic (max 3 attempts)
            if (newRetry <= 1) {

                delay(3000)

//                dao.updateStatus(
//                    job.id,
//                    "PENDING",
//                    newRetry
//                )
                dao.updateStatus(
                    job.id,
                    STATUS_PENDING,
                    job.retryCount
                )

                channels[role]?.send(
                    job.copy(
                        retryCount = newRetry
                    )
                )

            } else {

                // ❌ Permanent failure
               // dao.updateStatus(job.id, "FAILED", newRetry)
                dao.updateStatus(
                    job.id,
                    STATUS_FAILED,
                    newRetry
                )

                Log.e("PRINT_QUEUE", "GAVE UP ${job.id}")
            }
        }

        Log.d("PRINT_QUEUE", "END ${job.id}")
    }




//    private suspend fun loadPendingJobs() {
//        val jobs = dao.getPending()
//
//        jobs.forEach { job ->
//            val role = PrinterRole.valueOf(job.role)
//            channels[role]?.send(job)
//        }
//    }


    private suspend fun loadPendingJobs() {

        val jobs = dao.getPending()

        jobs.forEach { job ->

            val updated = dao.updateStatusIfPending(
                id = job.id,
                oldStatus = STATUS_PENDING,
                newStatus = STATUS_QUEUED
            )

            if (updated == 1) {

                val role = PrinterRole.valueOf(job.role)

                channels[role]?.send(
                    job.copy(status = STATUS_QUEUED)
                )
            }
        }
    }



}