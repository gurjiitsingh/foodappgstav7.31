package com.it10x.foodappgstav7_31.data.online.sync

import android.util.Log
import kotlinx.coroutines.*
import java.util.UUID

class SyncQueueManager(
    private val dao: SyncQueueDao,
    private val tableService: TableKotSyncService
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {

        startWorker()
    }


    fun addTableUpdate(tableId: String) {
        scope.launch {
            dao.insert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    type = "TABLE_UPDATE",
                    tableId = tableId,
                    status = "PENDING",
                    createdAt = System.currentTimeMillis()
                )
            )
            Log.d("SYNC_QUEUE", "Added TABLE_UPDATE for $tableId")
        }
    }

    suspend fun addClearTable(tableId: String) {
        scope.launch {
            dao.insert(
                SyncQueueEntity(
                    id = UUID.randomUUID().toString(),
                    type = "TABLE_CLEAR",
                    tableId = tableId,
                    status = "PENDING",
                    createdAt = System.currentTimeMillis()
                )
            )
            Log.d("SYNC_QUEUE", "Added TABLE_CLEAR for $tableId")
        }
    }

    private fun startWorker() {
        scope.launch {
            while (true) {
                try {
                    val job = dao.getNext()

                    if (job != null) {
                        Log.d("SYNC_QUEUE", "Processing ${job.type}")

                        when (job.type) {
                            "TABLE_UPDATE" -> {
                                tableService.syncTableSnapshot(job.tableId, "POS")
                            }

                            "TABLE_CLEAR" -> {
                                tableService.clearTableSnapshot(
                                    job.tableId,
                                    "QUEUE_CLEAR"
                                )
                            }
                        }

                        dao.deleteJob(job.id)
                    }

                } catch (e: Exception) {
                    Log.e("SYNC_QUEUE", "Worker error", e)
                }

                delay(3000)
            }
        }
    }
}