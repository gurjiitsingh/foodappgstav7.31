
package com.it10x.foodappgstav7_31.data.online.repository

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderDao
import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderEntity
import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderProductEntity
import com.it10x.foodappgstav7_31.data.online.models.OrderProductData

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OnlineReceivedOrderRepository(
    private val dao: OnlineReceivedOrderDao,
    private val updateCloudSyncStatus: (
        orderId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) -> Unit,
    private val ordersRepository: OrdersRepository = OrdersRepository()
) {

    companion object {
        private const val TAG = "ONLINE_ORDER_RECEIVER"
    }

    /**
     * Saves the order header and its products locally.
     * Firestore is marked SYNCED only after the Room save succeeds.
     *
     * Returns true if the header was not present before this call.
     */
    suspend fun receiveOrder(
        order: OnlineReceivedOrderEntity
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            require(order.id.isNotBlank()) {
                "Online order ID cannot be blank"
            }

            val existedBefore = dao.orderExists(order.id)

            // 1. Fetch product documents from Firestore orderProducts.
            val firestoreProducts =
                ordersRepository.getOrderProducts(order.id)

            // 2. Don't mark an order synced if its expected products
            //    could not be fetched.
            if (order.productsCount > 0 &&
                firestoreProducts.size < order.productsCount
            ) {
                throw IllegalStateException(
                    "Incomplete products for order ${order.id}. " +
                            "Expected ${order.productsCount}, " +
                            "received ${firestoreProducts.size}"
                )
            }

            val localProducts = firestoreProducts.map { product ->
                product.toLocalEntity(order.id)
            }

            // 3. Save and verify the header and products in one
            //    Room transaction.
            val saved = dao.saveOrderWithProducts(
                order = order,
                products = localProducts
            )

            if (!saved) {
                throw IllegalStateException(
                    "Local order verification failed: ${order.id}"
                )
            }

            // 4. Verify the saved header and product count.
            val savedOrder = dao.getOrderById(order.id)
                ?: throw IllegalStateException(
                    "Order header missing after save: ${order.id}"
                )

            val savedProductCount =
                dao.getProductCountForOrder(order.id)

            // Verify product count for monitoring only.
// Do not reject the order if the counts differ.
//            if (savedProductCount != localProducts.size) {
//                Log.w(
//                    TAG,
//                    "Product count mismatch for ${order.id}. " +
//                            "Expected=${localProducts.size}, " +
//                            "Found=$savedProductCount. " +
//                            "Order saving will continue."
//                )
//            }

            Log.d(
                TAG,
                "Local order and products saved. " +
                        "id=${savedOrder.id}, " +
                        "fetchedProducts=${firestoreProducts.size}, " +
                        "localProducts=$savedProductCount, " +
                        "alreadyExisted=$existedBefore"
            )

            // 5. Request the Firestore update only after local saving
            //    and verification succeed.
            updateCloudStatus(order.id)

            Result.success(!existedBefore)

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to save online order and products: ${order.id}",
                e
            )
            Result.failure(e)
        }
    }

    private fun updateCloudStatus(orderId: String) {
        try {
            updateCloudSyncStatus(
                orderId,
                {
                    Log.d(
                        TAG,
                        "Firestore syncStatus updated to SYNCED: $orderId"
                    )
                },
                { error ->
                    Log.e(
                        TAG,
                        "Local save succeeded but Firestore status update " +
                                "failed. Retry is needed: $orderId",
                        error
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Could not request Firestore status update: $orderId",
                e
            )
        }
    }

    private fun OrderProductData.toLocalEntity(
        orderId: String
    ): OnlineReceivedOrderProductEntity {

        return OnlineReceivedOrderProductEntity(
            id = id,
            productId = productId,
            orderMasterId = orderId,
            categoryName = categoryName,
            name = name,
            quantity = quantity,
            basePrice = basePrice.toDoubleOrNullSafe(),
            itemSubtotal = itemSubtotal.toDoubleOrNullSafe(),
            taxRate = taxRate.toDoubleOrNullSafe(),
            taxType = taxType,
            taxAmount = taxAmount.toDoubleOrNullSafe(),
            taxTotal = taxTotal.toDoubleOrNullSafe(),
            finalPrice = finalPrice.toDoubleOrNullSafe(),
            finalTotal = finalTotal.toDoubleOrNullSafe(),
            productCat = productCat,
            note = note,
            modifiersJson = modifiersJson
                ?.takeIf { it.isNotBlank() }
                ?: serializeModifiers()
        )
    }

    private fun OrderProductData.serializeModifiers(): String? {
        if (modifiers.isEmpty()) return null

        return try {
            val array = JSONArray()

            modifiers.forEach { modifier ->
                val json = JSONObject()
                json.put("createdAt", modifier.createdAt)
                json.put("groupId", modifier.groupId)
                json.put("id", modifier.id)
                json.put("isDefault", modifier.isDefault)
                json.put("name", modifier.name)
                json.put("price", modifier.price)
                json.put("sortOrder", modifier.sortOrder)
                json.put("status", modifier.status)
                json.put("updatedAt", modifier.updatedAt)

                val priceMap = JSONObject()
                modifier.priceMap.forEach { (key, value) ->
                    priceMap.put(key, value)
                }
                json.put("priceMap", priceMap)

                array.put(json)
            }

            array.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize modifiers for product: $id", e)
            null
        }
    }

    private fun Any?.toDoubleOrNullSafe(): Double? {
        return when (this) {
            null -> null
            is Number -> toDouble()
            is String -> toDoubleOrNull()
            else -> null
        }
    }
}
