package com.it10x.foodappgstav7_31.data.online.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.pos.dao.KotItemDao
import kotlinx.coroutines.tasks.await
import okio.Source
import java.util.UUID

class TableKotSyncService(
    private val firestore: FirebaseFirestore,
    private val kotItemDao: KotItemDao
) {




  //USED CLEAR TABLE FROM QUE
    suspend fun syncTableSnapshot(
        tableId: String,
        source: String,
    ) {
        try {
            val tableRef = firestore
                .collection("pos_tables")
                .document(tableId)

            // 🔥 GET ITEMS FROM ROOM
            val items = kotItemDao.getItemsForTableSync(tableId)
                .filter { it.status == "DONE" }

            // 🔥 CONVERT TO FIRESTORE MAP
            // 🔥 CONVERT TO FIRESTORE MAP
            val itemList = items.map {
                mapOf(
                    "sessionId" to it.sessionId,
                    "tableName" to it.tableName,

                    "productId" to it.productId,
                    "name" to it.name,
                    "quantity" to it.quantity,
                    "price" to it.basePrice,

                    "note" to (it.note ?: ""),
                    "category" to it.categoryName,

                    "createdByName" to (it.createdByName ?: ""),
                    "modifiersJson" to (it.modifiersJson ?: "")
                )
            }
            val cartCount = items.sumOf { it.quantity }
            val data = mapOf(
                "tableId" to tableId,
                "source" to source,
                "status" to if (itemList.isEmpty()) "EMPTY" else "RUNNING",
                "active" to itemList.isNotEmpty(),
                "cartCount" to cartCount,
                "items" to itemList,
                "updatedAt" to System.currentTimeMillis()
            )

            tableRef.set(data).await()

        } catch (e: Exception) {
            Log.e("TABLE_SYNC", "❌ syncTableSnapshot failed", e)
        }
    }


    suspend fun clearTableSnapshot(tableNo: String,source: String) {

        try {
            val tableRef = firestore
                .collection("pos_tables")
                .document(tableNo)

            val updateMap = mapOf(
                "tableId" to tableNo,
                "source" to source,
                "status" to "CLOSED",
                "active" to false,
                "cartCount" to 0,
                "items" to emptyList<Map<String, Any>>(),
                "updatedAt" to System.currentTimeMillis()
            )

            tableRef.set(updateMap).await()

            Log.d("TABLE_SYNC", "✅ Firestore table cleared")
        } catch (e: Exception) {

            Log.e(
                "TABLE_SYNC",
                "❌ Failed to clear table snapshot",
                e
            )
        }
    }


    suspend fun cleanupStaleFirestoreTables() {

        try {

            val snapshot = firestore
                .collection("pos_tables")
                .get()
                .await()

            for (doc in snapshot.documents) {

                val tableId =
                    doc.getString("tableId")
                        ?: continue

                val active =
                    doc.getBoolean("active")
                        ?: false

                if (!active) continue

                val localItems =
                    kotItemDao.getItemsByTable(tableId)

                if (localItems.isEmpty()) {

                    Log.d(
                        "TABLE_CLEANUP",
                        "Found stale table $tableId"
                    )

                    clearTableSnapshot(tableId, source="STALE_CLEANUP")
                }
            }

        } catch (e: Exception) {

            Log.e(
                "TABLE_CLEANUP",
                "Cleanup failed",
                e
            )
        }
    }
}