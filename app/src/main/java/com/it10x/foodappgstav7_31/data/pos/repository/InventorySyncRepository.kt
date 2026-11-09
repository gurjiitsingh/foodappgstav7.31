package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.InventorySyncEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.collections.component1

class InventorySyncRepository(
    private val db: AppDatabase
) {

    private val firestore =
        FirebaseFirestore.getInstance()



    suspend fun syncInventoryFromSales() = withContext(Dispatchers.IO) {

        try {

            // =====================================================
            // 1. LOAD DATA FROM ROOM
            // =====================================================

            val recipes =
                db.productRecipeDao().getAll()

            val paidItems =
                db.orderProductDao().getPaidItems()

            val syncedIds =
                db.inventorySyncDao()
                    .getSyncedIds()
                    .toSet()

            val pendingItems =
                paidItems.filter {
                    !syncedIds.contains(it.id)
                }

            if (pendingItems.isEmpty()) {
                Log.d("INV_SYNC", "No pending inventory updates")
                return@withContext
            }

            // =====================================================
            // 2. CALCULATE USAGE MAP
            // =====================================================

            val inventoryUsage = mutableMapOf<String, Double>()
            val finishedUsage = mutableMapOf<String, Double>()

// ✅ PUT IT HERE (before loop)
            val recipeMap = recipes.groupBy { it.productId }

            pendingItems.forEach { orderItem ->

                Log.d(
                    "INV_SYNC",
                    "ORDER ITEM -> ${orderItem.name} | mode=${orderItem.productMode} | qty=${orderItem.quantity}"
                )

                when(orderItem.productMode ?: "unknown") {

                    "raw_stock" -> {

                        Log.d("INV_SYNC", "RAW STOCK -> ${orderItem.name}")

                        // ✅ REPLACE FILTER WITH LOOKUP
                        val productRecipes =
                            recipeMap[orderItem.productId] ?: emptyList()

                        if (productRecipes.isEmpty()) {
                            Log.d("INV_SYNC", "NO RECIPES FOUND -> ${orderItem.name}")
                        }

                        productRecipes.forEach { recipe ->

                            val usedQty =
                                recipe.quantity * orderItem.quantity

                            inventoryUsage.merge(
                                recipe.inventoryItemId,
                                usedQty,
                                Double::plus
                            )
                        }
                    }

                    "finished_stock" -> {

                        Log.d(
                            "INV_SYNC",
                            "FINISHED STOCK -> ${orderItem.name} | qty=${orderItem.quantity}"
                        )

                        val qty = orderItem.quantity.toDouble()

                        finishedUsage.merge(
                            orderItem.productId,
                            qty,
                            Double::plus
                        )
                    }

//                    "finished_stock" -> {
//
//                        Log.d(
//                            "INV_SYNC",
//                            "FINISHED STOCK -> ${orderItem.name} | qty=${orderItem.quantity}"
//                        )
//
//                        val qty = orderItem.quantity.toDouble()
//
//                        finishedUsage.merge(
//                            orderItem.productId,
//                            qty,
//                            Double::plus
//                        )
//                    }

                    else -> {
                        Log.d(
                            "INV_SYNC",
                            "UNKNOWN MODE -> ${orderItem.name} | mode=${orderItem.productMode}"
                        )
                    }
                }
            }



            // =====================================================
            // 3. FIRESTORE BATCH UPDATE
            // =====================================================

              inventoryUsage.forEach { (inventoryId, qty) ->

                val inventoryRef = firestore.collection("inventoryItems").document(inventoryId)
                val ledgerRef = firestore.collection("stockLedgerInventory").document()

                firestore.runTransaction { tx ->

                    val snap = tx.get(inventoryRef)

                    val currentStock =
                        snap.getDouble("currentStock")
                            ?: snap.getLong("currentStock")?.toDouble()
                            ?: 0.0
                    val inventoryName = snap.getString("name") ?: ""


                    val consumptionUnit =
                        snap.getString("consumptionUnit") ?: "pcs"

                    val purchaseUnit =
                        snap.getString("purchaseUnit") ?: consumptionUnit

                    val conversionFactor =
                        snap.getDouble("conversionFactor")
                            ?: snap.getLong("conversionFactor")?.toDouble()
                            ?: 1.0

                    val newStock = currentStock - qty

                    val purchaseQuantity =
                        if (conversionFactor > 0)
                            qty / conversionFactor
                        else
                            qty

                    // 1. UPDATE INVENTORY CURRENT STOCK (CACHE)
                    tx.update(inventoryRef, mapOf(
                        "currentStock" to newStock,
                        "stockStatus" to if (newStock > 0) "in_stock" else "out_of_stock"
                    ))

                    // 2. WRITE LEDGER ENTRY
                    tx.set(
                        ledgerRef,
                        mapOf(

                            "inventoryItemId" to inventoryId,
                            "inventoryItemName" to inventoryName,

                            "type" to "CONSUMPTION",
                            "direction" to "OUT",

                            // Consumption
                            "quantity" to qty,
                            "unit" to consumptionUnit,

                            // Purchase equivalent
                            "purchaseQuantity" to purchaseQuantity,
                            "purchaseUnit" to purchaseUnit,
                            "conversionFactor" to conversionFactor,

                            "beforeStock" to currentStock,
                            "afterStock" to newStock,

                            "referenceType" to "SALE",
                            "referenceId" to "orderId",

                            "note" to "Recipe consumption",

                            "createdBy" to "POS",//cashierName,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "source" to "ANDROID"
                        )
                    )
                }.await()
            }

            val finishedLedger = firestore.collection("stockLedgerFinished")


            finishedUsage.forEach { (productId, qty) ->

                val ref = finishedLedger.document()
             //   val productRef = firestore.collection("productStock").document(productId)
                val productRef = FirebaseFirestore.getInstance()
                    .collection("products")
                    .document(productId)

                FirebaseFirestore.getInstance().runTransaction { tx ->

                    val snap = tx.get(productRef)

                    val productName = snap.getString("name") ?: ""
                    val currentStock =
                        snap.getDouble("currentStock")
                            ?: snap.getLong("currentStock")?.toDouble()
                            ?: 0.0

                    val newStock = currentStock - qty

                    // 1. UPDATE PRODUCT CACHE STOCK
                    tx.update(
                        productRef,
                        mapOf(
                            "currentStock" to newStock,
                            "stockStatus" to if (newStock > 0) "in_stock" else "out_of_stock"
                        )
                    )

                    // 2. WRITE LEDGER ENTRY
                    tx.set(
                        ref,
                        mapOf(
                            "productId" to productId,
                            "productName" to productName,

                            "type" to "SALE",
                            "direction" to "OUT",

                            "qty" to qty,

                            "beforeStock" to currentStock,
                            "afterStock" to newStock,

                            // Optional but recommended
                            "referenceType" to "ORDER",
                            "referenceId" to "orderId",      // if available "referenceId" to orderItem.orderId

                            "note" to "Finished product sold",

                            "createdBy" to "POS",//"cashierName"    // if available

                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "source" to "ANDROID"
                        )
                    )
                }.await()
            }


            // =====================================================
            // 4. MARK ITEMS AS SYNCED
            // =====================================================

            db.inventorySyncDao().insertAll(
                pendingItems.map {

                    InventorySyncEntity(
                        orderItemId = it.id,
                        syncedAt = System.currentTimeMillis()
                    )
                }
            )

            Log.d(
                "INV_SYNC",
                "Inventory sync successful"
            )

        } catch (e: Exception) {

            Log.e(
                "INV_SYNC",
                "Inventory sync failed: ${e.message}",
                e
            )

            throw e
        }
    }
}



//@Entity(tableName = "product_stock")
//data class ProductStockEntity(
//    @PrimaryKey val productId: String,
//
//    val name: String,
//    val productMode: String,
//
//    val currentStock: Double,
//    val minStock: Double?,
//
//    val inventoryItemId: String?,
//
//    val lastUpdated: Long
//)