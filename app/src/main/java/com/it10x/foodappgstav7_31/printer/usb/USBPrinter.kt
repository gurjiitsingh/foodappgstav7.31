package com.it10x.foodappgstav7_31.printer.usb

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.*
import android.util.Log
import com.it10x.foodappgstav7_31.printer.common.EscPosImagePrinter
import com.it10x.foodappgstav7_31.printer.common.EscPosText
import com.it10x.foodappgstav7_31.printer.escpos.EscPosQr
import com.it10x.foodappgstav7_31.usb.USBPermissionHelper
import kotlinx.coroutines.*

object USBPrinter {

    private const val TAG = "USBPrinter"
    const val ACTION_USB_PERMISSION = "com.it10x.foodappgstav7_31.USB_PERMISSION"

    private var usbManager: UsbManager? = null
    private var usbDevice: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var outEndpoint: UsbEndpoint? = null

    // =================================================
    // INIT + PERMISSION
    // =================================================
    fun init(
        context: Context,
        device: UsbDevice,
        onReady: (Boolean) -> Unit
    ) {
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        usbDevice = device

        USBPermissionHelper.requestPermission(context, device) {
            try {
                setupConnection(device)
                onReady(true)
            } catch (e: Exception) {
                Log.e(TAG, "USB setup failed", e)
                onReady(false)
            }
        }
    }

    // =================================================
    // USB CONNECTION SETUP
    // =================================================
    private fun setupConnection(device: UsbDevice) {
        val iface = device.getInterface(0)
        outEndpoint = null

        for (i in 0 until iface.endpointCount) {
            val ep = iface.getEndpoint(i)
            if (
                ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                ep.direction == UsbConstants.USB_DIR_OUT
            ) {
                outEndpoint = ep
                break
            }
        }

        if (outEndpoint == null) {
            throw IllegalStateException("No OUT endpoint found")
        }

        connection = usbManager?.openDevice(device)
            ?: throw IllegalStateException("Unable to open USB device")

        connection?.claimInterface(iface, true)
        Log.d(TAG, "USB printer connected: ${device.deviceName}")
    }

    // =================================================
    // TEST PRINT
    // =================================================
    fun printBitmap(
        context: Context,
        device: UsbDevice,
        bitmap: Bitmap,
        onResult: (Boolean) -> Unit
    ) {

        init(context, device) { ready ->

            if (!ready) {
                onResult(false)
                return@init
            }

            val ep = outEndpoint
            val conn = connection

            if (ep == null || conn == null) {

                Log.e(TAG, "USB connection missing")

                onResult(false)
                return@init
            }

            CoroutineScope(Dispatchers.IO).launch {

                try {

                    // =============================
                    // INIT
                    // =============================

                    val initCommand = byteArrayOf(
                        0x1B,
                        0x40
                    )

                    conn.bulkTransfer(
                        ep,
                        initCommand,
                        initCommand.size,
                        1000
                    )

                    delay(50)


                    // =============================
                    // BEEP
                    // =============================

                    val beepCommand = byteArrayOf(
                        0x1B,
                        0x42,
                        0x03,
                        0x02
                    )

                    conn.bulkTransfer(
                        ep,
                        beepCommand,
                        beepCommand.size,
                        1000
                    )

                    delay(50)


                    // =============================
                    // CENTER
                    // =============================

                    val centerCommand = byteArrayOf(
                        0x1B,
                        0x61,
                        0x01
                    )

                    conn.bulkTransfer(
                        ep,
                        centerCommand,
                        centerCommand.size,
                        1000
                    )

                    delay(50)


                    // =============================
                    // COMMON IMAGE FORMAT
                    // =============================

                    printBitmapInChunksUSB(
                        conn = conn,
                        ep = ep,
                        bitmap = bitmap
                    )

                    delay(100)


                    // =============================
                    // FEED + CUT
                    // =============================

                    val feedCutCommand = byteArrayOf(
                        0x0A,
                        0x0A,
                        0x1D,
                        0x56,
                        0x01
                    )

                    conn.bulkTransfer(
                        ep,
                        feedCutCommand,
                        feedCutCommand.size,
                        1000
                    )

                    delay(100)

                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }

                } catch (e: Exception) {

                    Log.e(
                        TAG,
                        "USB bitmap print failed",
                        e
                    )

                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            }
        }
    }

    // =================================================
    // TEST PRINT
    // =================================================
    fun printTest(
        context: Context,
        device: UsbDevice,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    ) {
        init(context, device) { ready ->
            if (!ready) {
                onResult(false)
                return@init
            }

            val testText = """
            ****************************
                 TEST PRINT
            ****************************
            Printer Role : $roleLabel
            Connection   : USB
            Device Name  : ${device.deviceName}
            Status       : OK
            ----------------------------


        """.trimIndent()

//            printText(testText) { success ->
//                onResult(success)
//            }
        }
    }

    // =================================================
    // CORE PRINT (ORDER / AUTO)
    // =================================================
    fun printText(
        context: Context,
        device: UsbDevice,
        text: String,
        onResult: (Boolean) -> Unit,
        qrData: String? = null,
    ) {
        init(context, device) { ready ->

            if (!ready) {
                onResult(false)
                return@init
            }

            val ep = outEndpoint
            val conn = connection

            if (ep == null || conn == null) {
                Log.e(TAG, "USB connection missing")
                onResult(false)
                return@init
            }

            CoroutineScope(Dispatchers.IO).launch {

                try {

                    // =========================================
                    // INIT
                    // =========================================

                    val initCommand = byteArrayOf(
                        0x1B,
                        0x40
                    )

                    conn.bulkTransfer(
                        ep,
                        initCommand,
                        initCommand.size,
                        1000
                    )

                    delay(50)

                    // =========================================
                    // BEEP
                    // =========================================

                    val beepCommand = byteArrayOf(
                        0x1B,
                        0x42,
                        0x03,
                        0x02
                    )

                    conn.bulkTransfer(
                        ep,
                        beepCommand,
                        beepCommand.size,
                        1000
                    )

                    delay(30)

                    // =========================================
                    // TEXT
                    // =========================================

                    val textBytes =
                        EscPosText.build(text)

                    conn.bulkTransfer(
                        ep,
                        textBytes,
                        textBytes.size,
                        5000
                    )

                    delay(100)

                    // =========================================
                    // PAYMENT QR
                    // =========================================

                    if (!qrData.isNullOrBlank()) {

                        Log.d(
                            TAG,
                            "USB QR DATA = ${qrData.take(100)}"
                        )

                        val qrBytes =
                            EscPosQr.build(
                                data = qrData
                            )

                        Log.d(
                            TAG,
                            "USB QR BYTES = ${qrBytes.size}"
                        )

                        conn.bulkTransfer(
                            ep,
                            qrBytes,
                            qrBytes.size,
                            5000
                        )

                        delay(100)
                    }

                    // =========================================
                    // SPACE AFTER QR
                    // =========================================

                    val feedAfterQr = byteArrayOf(
                        0x0A,
                        0x0A,
                        0x0A
                    )

                    conn.bulkTransfer(
                        ep,
                        feedAfterQr,
                        feedAfterQr.size,
                        1000
                    )

                    delay(50)

                    // =========================================
                    // CUT AFTER QR
                    // =========================================

                    val cutCommand = byteArrayOf(
                        0x1D,
                        0x56,
                        0x01
                    )

                    conn.bulkTransfer(
                        ep,
                        cutCommand,
                        cutCommand.size,
                        1000
                    )

                    delay(50)

                    // =========================================
                    // SUCCESS
                    // =========================================

                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }

                } catch (e: Exception) {

                    Log.e(
                        TAG,
                        "USB text print failed",
                        e
                    )

                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            }
        }
    }


    fun printText_oldwithoutqr(
        context: Context,
        device: UsbDevice,
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        init(context, device) { ready ->

            if (!ready) {
                onResult(false)
                return@init
            }

            val ep = outEndpoint
            val conn = connection

            if (ep == null || conn == null) {
                Log.e(TAG, "USB connection missing")
                onResult(false)
                return@init
            }

            CoroutineScope(Dispatchers.IO).launch {

                try {

                    // =========================================
                    // INIT
                    // =========================================

                    val initCommand = byteArrayOf(
                        0x1B,
                        0x40
                    )

                    conn.bulkTransfer(
                        ep,
                        initCommand,
                        initCommand.size,
                        1000
                    )

                    delay(50)


                    // =========================================
                    // BEEP
                    // Same behaviour as Bluetooth
                    // =========================================

                    val beepCommand = byteArrayOf(
                        0x1B,
                        0x42,
                        0x03,
                        0x02
                    )

                    conn.bulkTransfer(
                        ep,
                        beepCommand,
                        beepCommand.size,
                        1000
                    )

                    delay(30)


                    // =========================================
                    // TEXT
                    // =========================================

                    val safeText = text
                        .replace("\n", "\r\n")
                        .toByteArray(Charsets.US_ASCII)

                    conn.bulkTransfer(
                        ep,
                        safeText,
                        safeText.size,
                        5000
                    )

                    delay(100)


                    // =========================================
                    // FEED + CUT
                    // Same behaviour as Bluetooth
                    // =========================================

                    val feedAndCut = byteArrayOf(
                        0x1B,
                        0x64,
                        0x03,
                        0x1D,
                        0x56,
                        0x01
                    )

                    conn.bulkTransfer(
                        ep,
                        feedAndCut,
                        feedAndCut.size,
                        1000
                    )

                    delay(50)


                    // =========================================
                    // SUCCESS
                    // =========================================

                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }

                } catch (e: Exception) {

                    Log.e(
                        TAG,
                        "USB text print failed",
                        e
                    )

                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            }
        }
    }




    fun printLogoTextQrUSB(
        context: Context,
        device: UsbDevice,
        logoBitmap: Bitmap?,   // nullable
        qrBitmap: Bitmap?,     // nullable
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        init(context, device) { ready ->

            if (!ready) {
                onResult(false)
                return@init
            }

            val ep = outEndpoint
            val conn = connection

            if (ep == null || conn == null) {
                onResult(false)
                return@init
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // =============================
                    // INIT
                    // =============================
                    conn.bulkTransfer(ep, byteArrayOf(0x1B, 0x40), 2, 1000)
                    delay(50)

                    // =============================
                    // CENTER ALIGN
                    // =============================
                    conn.bulkTransfer(ep, byteArrayOf(0x1B, 0x61, 0x01), 3, 1000)
                    delay(50)

                    // =============================
                    // PRINT LOGO
                    // =============================
                    if (logoBitmap != null) {
                        printBitmapInChunksUSB(conn, ep, logoBitmap)
                        delay(100)
                    }

                    // =============================
                    // PRINT QR (🔥 THIS WAS MISSING)
                    // =============================
                    if (qrBitmap != null) {

                        // 🔥 VERY IMPORTANT
                        delay(200)

                        printBitmapInChunksUSB(conn, ep, qrBitmap)

                        delay(200)

                        conn.bulkTransfer(ep, byteArrayOf(0x0A), 1, 1000)
                    }

                    // =============================
                    // LEFT ALIGN
                    // =============================
                    conn.bulkTransfer(ep, byteArrayOf(0x1B, 0x61, 0x00), 3, 1000)
                    delay(50)

                    // =============================
                    // TEXT
                    // =============================
                    val safeText = text
                        .replace("\n", "\r\n")
                        .toByteArray(Charsets.US_ASCII)

                    conn.bulkTransfer(ep, safeText, safeText.size, 5000)

                    // small spacing
                    conn.bulkTransfer(ep, byteArrayOf(0x0A, 0x0A), 2, 1000)

                    delay(100)

                    // =============================
                    // CUT
                    // =============================
                    conn.bulkTransfer(
                        ep,
                        byteArrayOf(0x1D, 0x56, 0x01),
                        3,
                        1000
                    )

                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "USB logo + QR print failed", e)
                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            }
        }
    }

    private suspend fun printBitmapInChunksUSB(
        conn: UsbDeviceConnection,
        ep: UsbEndpoint,
        bitmap: Bitmap
    ) {

        val chunkHeight = 48
        val packetSize = 1024

        var y = 0

        while (y < bitmap.height) {

            val height = minOf(
                chunkHeight,
                bitmap.height - y
            )

            val chunk = Bitmap.createBitmap(
                bitmap,
                0,
                y,
                bitmap.width,
                height
            )

            try {

                // ==========================================
                // COMMON IMAGE CONVERSION
                // Same raster format used by BT + LAN
                // ==========================================

                val bytes =
                    EscPosImagePrinter.convertBitmapToRaster(chunk)

                // ==========================================
                // SEND IMAGE IN USB PACKETS
                // ==========================================

                var offset = 0

                while (offset < bytes.size) {

                    val end = minOf(
                        offset + packetSize,
                        bytes.size
                    )

                    val length = end - offset

                    val packet = bytes.copyOfRange(
                        offset,
                        end
                    )

                    val result = conn.bulkTransfer(
                        ep,
                        packet,
                        length,
                        3000
                    )

                    if (result < 0) {
                        throw Exception(
                            "USB image transfer failed at offset=$offset"
                        )
                    }

                    offset = end

                    // USB printer buffer protection
                    delay(20)
                }

                // Give printer time to process this raster chunk
                delay(40)

            } finally {
                chunk.recycle()
            }

            y += height
        }
    }







    // =================================================
    // RELEASE
    // =================================================
    fun release() {
        try {
            connection?.close()
        } catch (_: Exception) {}
        connection = null
        outEndpoint = null
        usbDevice = null
    }
}
