package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.models.waiter.WaiterOrder
import com.it10x.foodappgstav7_31.data.online.models.waiter.WaiterOrderItem
import com.it10x.foodappgstav7_31.data.pos.entities.PosCartEntity
import kotlinx.coroutines.tasks.await


class WaiterKitchenRepository(
    private val firestore: FirebaseFirestore
) {

    //WAITERTOFIRESTORE
    suspend fun sendOrderToFireStore(
        cartList: List<PosCartEntity>,
        tableNo: String,
        tableName: String,
        sessionId: String,
        orderType: String,
        deviceId: String,
        deviceName: String?,

    ): Boolean {

        return try {

            if (cartList.isEmpty()) return false
            Log.d("WAITER_FIRESTORE", "sendOrderToFireStore() started")
            Log.d("KOT_DEBUG","WaiterKitchenRepository is sending Waiter kot--------------")

            val orderId = firestore.collection("waiter_orders").document().id
            val batch = firestore.batch()

            val orderRef = firestore
                .collection("waiter_orders")
                .document(orderId)

            val order = WaiterOrder(
                orderId = orderId,
                tableNo = tableNo,
                tableName = tableName,
                sessionId = sessionId,
                orderType = orderType,
                deviceId = deviceId,
                deviceName = deviceName,
                status = "PENDING",
                createdAt = System.currentTimeMillis(),
            )

            batch.set(orderRef, order)

            cartList.forEach { cartItem ->

                val itemRef = orderRef
                    .collection("items")
                    .document()

                val orderItem = WaiterOrderItem(
                    productId = cartItem.productId,
                    productName = cartItem.name,
                    categoryId = cartItem.categoryId,
                    categoryName = cartItem.categoryName,
                    quantity = cartItem.quantity,
                    discountEligible = cartItem.discountEligible,
                    price = cartItem.basePrice,
                    taxRate = cartItem.taxRate,
                    tableNo = tableNo,
                    tableName = tableName,
                    sessionId = sessionId,

                    createdById = cartItem.createdById,
                    createdByName = cartItem.createdByName,

                    note = cartItem.note,
                    modifiersJson = cartItem.modifiersJson,
                    kitchenPrintReq = cartItem.kitchenPrintReq,
                    kitchenPrinted = false
                )

                Log.d(
                    "WAITER_SEND",
                    """
    Sending firestore betch:
    product=${orderItem.productName}
    createdById=${orderItem.createdById}
    createdByName=${orderItem.createdByName}
    """.trimIndent()
                )

                batch.set(itemRef, orderItem)
            }

            batch.commit().await()

        //    Log.d("WAITER_FIRESTORE", "Order uploaded with ${cartList.size} items")

            true

        } catch (e: Exception) {
            Log.e("WAITER_FIRESTORE", "Upload failed", e)
            false
        }
    }

}

