
package com.it10x.foodappgstav7_31.service

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.Log

import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderEntity
import com.it10x.foodappgstav7_31.data.online.repository.OnlineReceivedOrderRepository
import com.it10x.foodappgstav7_31.data.online.repository.FirestoreOnlineOrdersRepository
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.printer.AutoPrintManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// PRINT, RING AND SAVE ONLINE ORDERS
// Called from OnlineOrderMonitoringService.

class FirestoreOnlineOrderListener(
    private val context: Context,
    private val autoPrintManager: AutoPrintManager
) {

    private val repo = FirestoreOnlineOrdersRepository()

    // Background scope for saving incoming orders.
    private val saveScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Existing Room database: pos.db
    private val receivedOrderRepository by lazy {
        OnlineReceivedOrderRepository(
            dao = AppDatabaseProvider
                .get(context.applicationContext)
                .onlineReceivedOrderDao(),

            updateCloudSyncStatus = { orderId, onSuccess, onFailure ->
                repo.updateCloudSyncStatus(
                    orderId = orderId,
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }
        )
    }

    private var ringtone: Ringtone? = null

    private val ringingLock = Any()

    // Prevent duplicate ringtone and printing.
    private val processedOrderIds = mutableSetOf<String>()

    fun startListening() {

        Log.d(
            "ONLINE_ORDER",
            "🔥 Online order listener STARTED"
        )

        repo.startListening { order ->

            Log.d(
                "ONLINE_ORDER",
                "📥 Firestore order received: " +
                        "id=${order.id}, " +
                        "source=${order.source}, " +
                        "srno=${order.srno}"
            )


            // =====================================================
// 1. ACCEPT ALL ONLINE ORDER SOURCES
// WEB, APP, and other supported online sources
// =====================================================

            val orderSource = order.source
                ?.trim()
                ?.uppercase()
                .orEmpty()

            if (orderSource !in setOf("WEB", "APP", "ONLINE")) {
                Log.d(
                    "ONLINE_ORDER",
                    "⛔ Ignoring unsupported order source: " +
                            "id=${order.id}, source=${order.source}"
                )
                return@startListening
            }




            // 2. IGNORE ALREADY PRINTED ORDERS
            if (order.printed == true) {
                Log.d(
                    "ONLINE_ORDER",
                    "⛔ Order already printed: ${order.id}"
                )
                return@startListening
            }

            // 3. PREVENT DUPLICATE PROCESSING
            synchronized(processedOrderIds) {
                if (!processedOrderIds.add(order.id)) {
                    Log.d(
                        "ONLINE_ORDER",
                        "⛔ Duplicate WEB order ignored: ${order.id}"
                    )
                    return@startListening
                }
            }

            Log.e(
                "ONLINE_ORDER",
                "🚨 NEW WEB ORDER: ${order.id}"
            )

            // 4. RING — existing behavior preserved.
            playRingtone()

            // Save and verify the order and all expected products first.
// Printing will happen only after this succeeds.
            saveScope.launch {
                saveOrderLocally(order)
            }
            // 5. AUTO PRINT — existing behavior preserved.
            Log.d(
                "ONLINE_ORDER",
                "🖨 Sending WEB order to AutoPrintManager: ${order.id}"
            )

            try {
                autoPrintManager.onNewOrder(order)
            } catch (e: Exception) {
                Log.e(
                    "ONLINE_ORDER",
                    "❌ Auto-print failed: ${order.id}",
                    e
                )
            }


        }
    }

    private suspend fun saveOrderLocally(order: OrderMasterData) {

        val localOrder = order.toLocalEntity()

        for (attempt in 1..3) {
            try {
                Log.d(
                    "ONLINE_ORDER",
                    "💾 Saving order to Room: " +
                            "id=${order.id}, attempt=$attempt/3"
                )

                val result =
                    receivedOrderRepository.receiveOrder(localOrder)

                if (result.isSuccess) {
                    Log.d(
                        "ONLINE_ORDER",
                        "✅ Room save verified: " +
                                "id=${order.id}, srno=${order.srno}"
                    )

                    // receiveOrder() requests the Firestore SYNCED
                    // update only after local save verification.
                    return
                }

                val error = result.exceptionOrNull()

                Log.e(
                    "ONLINE_ORDER",
                    "❌ Room save failed: " +
                            "id=${order.id}, attempt=$attempt/3",
                    error
                )

            } catch (e: Exception) {
                Log.e(
                    "ONLINE_ORDER",
                    "❌ Unexpected local-save error: " +
                            "id=${order.id}, attempt=$attempt/3",
                    e
                )
            }

            if (attempt < 3) {
                delay(1500L)
            }
        }

        Log.e(
            "ONLINE_ORDER",
            "❌ Order was not saved after 3 attempts: ${order.id}. " +
                    "Firestore must remain NEWONLINE."
        )

        // Allow a later listener event to try this order again.
        synchronized(processedOrderIds) {
            processedOrderIds.remove(order.id)
        }
    }

    // Convert Firestore model into the Room-compatible entity.
    private fun OrderMasterData.toLocalEntity(): OnlineReceivedOrderEntity {

        val orderCreatedAtMillis =
            createdAt?.toDate()?.time
                ?: createdAtMillis.takeIf { it > 0L }
                ?: System.currentTimeMillis()

        return OnlineReceivedOrderEntity(
            id = id,
            srno = srno,
            source = source,
            orderNumber = orderNumber,

            outletId = outletId,
            outletName = outletName,

            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            email = email,
            addressId = addressId,

            dAddressLine1 = dAddressLine1,
            dAddressLine2 = dAddressLine2,
            dCity = dCity,
            dState = dState,
            dZipcode = dZipcode,
            dLandmark = dLandmark,

            orderType = orderType,
            tableNo = tableNo,

            itemTotal = itemTotal,
            discountTotal = discountTotal ?: 0.0,
            subTotal = subTotal ?: 0.0,
            taxTotal = taxTotal ?: 0.0,
            deliveryFee = deliveryFee ?: 0.0,
            grandTotal = grandTotal ?: 0.0,

            paymentMode = paymentType
                .takeIf { it.isNotBlank() },

            paymentMethod = paymentType
                .takeIf { it.isNotBlank() },

            paymentStatus = paymentStatus,

            orderStatus = orderStatus,
            notes = notes,
            productsCount = productsCount ?: 0,

            createdAtMillis = orderCreatedAtMillis,

            printed = printed ?: false,
            acknowledged = acknowledged ?: false,

            syncStatus = syncStatus,

            couponFlat = couponFlat ?: 0.0,
            pickUpDiscount = pickUpDiscount ?: 0.0,
            couponPercent = couponPercent ?: 0.0
        )
    }

    private fun playRingtone() {
        synchronized(ringingLock) {

            if (ringtone?.isPlaying == true) {
                Log.d(
                    "ONLINE_ORDER",
                    "🔔 Bell already ringing"
                )
                return
            }

            Log.e(
                "ONLINE_ORDER",
                "🔔 START RINGING"
            )

            val alarmUri =
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
                )

            ringtone =
                RingtoneManager.getRingtone(
                    context,
                    alarmUri
                ).apply {

                    audioAttributes =
                        AudioAttributes.Builder()
                            .setUsage(
                                AudioAttributes.USAGE_ALARM
                            )
                            .setContentType(
                                AudioAttributes.CONTENT_TYPE_SONIFICATION
                            )
                            .build()

                    if (
                        android.os.Build.VERSION.SDK_INT >=
                        android.os.Build.VERSION_CODES.P
                    ) {
                        isLooping = true
                    }

                    play()
                }
        }
    }

    fun stopRingtone() {
        synchronized(ringingLock) {

            Log.d(
                "ONLINE_ORDER",
                "🔕 STOP RINGING"
            )

            try {
                ringtone?.stop()
            } catch (e: Exception) {
                Log.e(
                    "ONLINE_ORDER",
                    "Error stopping ringtone",
                    e
                )
            }

            ringtone = null
        }
    }

    fun stopListening() {

        Log.d(
            "ONLINE_ORDER",
            "🛑 Online order listener STOPPED"
        )

        repo.stopListening()

        stopRingtone()

        synchronized(processedOrderIds) {
            processedOrderIds.clear()
        }
    }
}
