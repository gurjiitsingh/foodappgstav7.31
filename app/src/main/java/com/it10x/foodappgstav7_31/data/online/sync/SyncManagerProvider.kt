package com.it10x.foodappgstav7_31.data.online.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.core.AppContextProvider
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider

object SyncManagerProvider {

    private var instance: SyncQueueManager? = null

    fun get(): SyncQueueManager {

        return instance ?: synchronized(this) {

            val context = AppContextProvider.context
            val db = AppDatabaseProvider.get(context)

            val service = TableKotSyncService(
                firestore = FirebaseFirestore.getInstance(),
                kotItemDao = db.kotItemDao()
            )

            val manager = SyncQueueManager(
                dao = db.syncQueueDao(),
                tableService = service
            )

            instance = manager
            manager
        }
    }
}