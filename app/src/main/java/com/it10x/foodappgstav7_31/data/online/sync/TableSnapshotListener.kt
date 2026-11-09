package com.it10x.foodappgstav7_31.data.online.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.it10x.foodappgstav7_31.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class TableSnapshotListener(
    private val firestore: FirebaseFirestore,
    private val kotItemDao: KotItemDao
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var listener: ListenerRegistration? = null

    fun startListening(tableId: String) {
        stopListening()

        Log.d("TABLE_SYNC", "👂 Listening for table snapshot: $tableId")

        listener = firestore
            .collection("table_kot_snapshots")
            .document(tableId)
            .collection("items")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    Log.e("TABLE_SYNC", "Listener error", error)
                    return@addSnapshotListener
                }

                scope.launch {
                    try {
                        val newItems = snapshot.documents.map { doc ->

                            PosKotItemEntity(
                                id = doc.id,
                                kotNumber = "",
                                sessionId = doc.getString("sessionId") ?: "",
                                kotBatchId = "SNAPSHOT",
                                tableNo = doc.getString("tableName") ?: "",
                                tableName = doc.getString("tableNo") ?: "",
                                productId = doc.getString("productId") ?: "",
                                name = doc.getString("productName") ?: "",
                                productMode =
                                    doc.getString("productMode")
                                        ?: "raw_stock",
                                currentStock =
                                    doc.getDouble("currentStock")
                                        ?: 0.0,
                                categoryId = doc.getString("categoryId") ?: "",
                                categoryName = doc.getString("categoryName") ?: "",
                                parentId = null,
                                isVariant = false,
                                basePrice = 0.0,
                                finalPrice = 0.0,
                                modifierTotal = 0.0,
                                quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                                taxCategory = doc.getString("taxCategory") ?: "",
                                taxRate = 0.0,
                                taxType = "exclusive",
                                taxRate1 = 0.0,
                                taxType1 = "exclusive",
                                note = doc.getString("note") ?: "",
                                modifiersJson = "",
                                kitchenPrintReq = true,
                                kitchenPrinted = true,
                                status = "DONE",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        }

                        if (newItems.isEmpty()) {
                            Log.w("TABLE_SYNC", "⚠️ No items in snapshot")
                            return@launch
                        }

                        // 🔥 Replace local table state (IMPORTANT)
                     //   kotItemDao.clearTable(newItems.first().tableNo)
                        kotItemDao.insertAll(newItems)

                        Log.d("TABLE_SYNC", "✅ Table synced from snapshot")

                    } catch (e: Exception) {
                        Log.e("TABLE_SYNC", "❌ Failed to sync snapshot", e)
                    }
                }
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }
}