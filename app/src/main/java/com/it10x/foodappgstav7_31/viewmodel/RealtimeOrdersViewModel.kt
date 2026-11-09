package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.online.repository.FirestoreOnlineOrdersRepository
import com.it10x.foodappgstav7_31.printer.AutoPrintManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Realtime orders used ONLY by the Online Orders screen.
 *
 * IMPORTANT:
 * - This ViewModel does NOT ring.
 * - This ViewModel does NOT print.
 * - OrderListenerService handles bell + auto printing separately.
 * - This ViewModel only receives orders and exposes them to Compose UI.
 */
class RealtimeOrdersViewModel(
    application: Application,
    private val autoPrintManager: AutoPrintManager
) : AndroidViewModel(application) {

    private val repo = FirestoreOnlineOrdersRepository()

    // =====================================================
    // REALTIME ORDERS FOR UI
    // =====================================================

    private val _realtimeOrders =
        MutableStateFlow<List<OrderMasterData>>(emptyList())

    val realtimeOrders: StateFlow<List<OrderMasterData>> =
        _realtimeOrders


    // =====================================================
    // LISTENER STATE
    // =====================================================

    private var isListening = false


    // =====================================================
    // START LISTENING
    // =====================================================

    fun startListening() {

        android.util.Log.e(
            "UI_REALTIME_ORDER",
            "🔥 RealtimeOrdersViewModel.startListening() CALLED"
        )

        if (isListening) {
            android.util.Log.e(
                "UI_REALTIME_ORDER",
                "⛔ Already listening"
            )
            return
        }

        isListening = true

        repo.startListening { newOrder ->

            android.util.Log.e(
                "UI_REALTIME_ORDER",
                "📥 UI received: " +
                        "id=${newOrder.id}, " +
                        "source=${newOrder.source}, " +
                        "srno=${newOrder.srno}"
            )

            // =================================================
            // ONLY ONLINE ORDERS
            // =================================================

            if (
                newOrder.source != "WEB" &&
                newOrder.source != "APP"
            ) {
                        return@startListening
            }


            // =================================================
            // DUPLICATE CHECK
            // =================================================

            if (_realtimeOrders.value.any { it.id == newOrder.id }) {

                android.util.Log.d(
                    "UI_REALTIME_ORDER",
                    "⛔ Duplicate order ignored: ${newOrder.id}"
                )

                return@startListening
            }


            // =================================================
            // ADD TO UI LIST
            // =================================================

            _realtimeOrders.value =
                listOf(newOrder) + _realtimeOrders.value

            android.util.Log.e(
                "UI_REALTIME_ORDER",
                "✅ Added to UI list. " +
                        "Count=${_realtimeOrders.value.size}"
            )
        }
    }


    // =====================================================
    // ACKNOWLEDGE ORDER
    // =====================================================

    fun acknowledgeOrder(orderId: String) {

        FirebaseFirestore.getInstance()
            .collection("orderMaster")
            .document(orderId)
            .update("acknowledged", true)
            .addOnSuccessListener {

                android.util.Log.d(
                    "ACK_ORDER",
                    "Order acknowledged: $orderId"
                )
            }
            .addOnFailureListener { e ->

                android.util.Log.e(
                    "ACK_ORDER",
                    "Failed to acknowledge order",
                    e
                )
            }
    }


    fun updateOrderStatus(
        orderId: String,
        status: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("orderMaster")
            .document(orderId)
            .update("orderStatus", status)
            .addOnSuccessListener {
                android.util.Log.d(
                    "ORDER_STATUS",
                    "Order $orderId updated to $status"
                )
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                android.util.Log.e(
                    "ORDER_STATUS",
                    "Failed to update order $orderId",
                    e
                )
                onResult(false, e.message)
            }
    }
    // =====================================================
    // STOP LISTENING
    // =====================================================

    fun stopListening() {

        android.util.Log.d(
            "UI_REALTIME_ORDER",
            "🛑 RealtimeOrdersViewModel.stopListening()"
        )

        repo.stopListening()

        isListening = false
    }


    // =====================================================
    // CLEANUP
    // =====================================================

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}