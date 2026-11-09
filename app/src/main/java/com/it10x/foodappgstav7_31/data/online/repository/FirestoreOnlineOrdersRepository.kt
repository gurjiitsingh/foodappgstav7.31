package com.it10x.foodappgstav7_31.data.online.repository

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData

//Called BY FirestoreOnlineOrderListener
class FirestoreOnlineOrdersRepository {

    private val db = FirebaseFirestore.getInstance()

    private var listener: ListenerRegistration? = null

    private val ordersCollection
        get() = db.collection("orderMaster")

    // =====================================================
    // START CONTINUOUS LISTENER
    // =====================================================

    fun startListening(
        onNewOrder: (OrderMasterData) -> Unit
    ) {
        Log.d("REALTIME_ORDER", "🔥 STARTING FIRESTORE LISTENER")

        // Remove any existing listener owned by this instance.
        listener?.remove()
        listener = null

        listener = ordersCollection
          //  .whereEqualTo("syncStatus", "NEWONLINE")
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Log.e(
                        "REALTIME_ORDER",
                        "❌ FIRESTORE LISTENER ERROR",
                        error
                    )
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.e(
                        "REALTIME_ORDER",
                        "❌ SNAPSHOT IS NULL"
                    )
                    return@addSnapshotListener
                }

                Log.d(
                    "REALTIME_ORDER",
                    "📡 Snapshot: documents=${snapshots.size()}, " +
                            "changes=${snapshots.documentChanges.size}"
                )

                for (change in snapshots.documentChanges) {
                    val document = change.document

                    Log.d(
                        "REALTIME_ORDER",
                        "Change=${change.type}, " +
                                "id=${document.id}, " +
                                "source=${document.getString("source")}, " +
                                "syncStatus=${document.getString("syncStatus")}"
                    )

                    // Process only documents newly entering this query.
                    if (change.type != DocumentChange.Type.ADDED) {
                        continue
                    }

                    try {
                        val order = document
                            .toObject(OrderMasterData::class.java)
                            .copy(id = document.id)

                        if (order.source != "WEB" &&
                            order.source != "APP"
                        ) {
                            continue
                        }

                        if (order.syncStatus != "NEWONLINE") {
                            continue
                        }

                        Log.d(
                            "ONLINE_RECEIVER",
                            "Incoming order: documentId=${document.id}, " +
                                    "orderId=${order.id}, " +
                                    "source=${order.source}, " +
                                    "syncStatus=${order.syncStatus}, " +
                                    "srno=${order.srno}"
                        )

                        onNewOrder(order)

                    } catch (e: Exception) {
                        Log.e(
                            "REALTIME_ORDER",
                            "❌ Failed to convert order: ${document.id}",
                            e
                        )
                    }
                }
            }
    }

    // =====================================================
    // FETCH ONE ORDER FROM FIRESTORE
    // =====================================================

    fun fetchOrder(
        orderId: String,
        onSuccess: (OrderMasterData) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        ordersCollection
            .document(orderId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    onFailure(
                        IllegalStateException(
                            "Order does not exist: $orderId"
                        )
                    )
                    return@addOnSuccessListener
                }

                try {
                    val order = document
                        .toObject(OrderMasterData::class.java)
                        ?.copy(id = document.id)

                    if (order == null) {
                        onFailure(
                            IllegalStateException(
                                "Could not convert order: $orderId"
                            )
                        )
                        return@addOnSuccessListener
                    }

                    Log.d(
                        "REALTIME_ORDER",
                        "✅ Order fetched: ${order.id}"
                    )

                    onSuccess(order)

                } catch (e: Exception) {
                    onFailure(e)
                }
            }
            .addOnFailureListener { error ->
                Log.e(
                    "REALTIME_ORDER",
                    "❌ Failed to fetch order: $orderId",
                    error
                )
                onFailure(error)
            }
    }

    // =====================================================
    // UPDATE CLOUD SYNC STATUS
    // Call only after the Room save is verified.
    // =====================================================

    fun updateCloudSyncStatus(
        orderId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        ordersCollection
            .document(orderId)
            .update("syncStatus", "SYNCED")
            .addOnSuccessListener {
                Log.d(
                    "REALTIME_ORDER",
                    "✅ Cloud syncStatus updated to SYNCED: $orderId"
                )
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.e(
                    "REALTIME_ORDER",
                    "❌ Failed to update syncStatus: $orderId",
                    error
                )
                onFailure(error)
            }
    }

    // =====================================================
    // STOP LISTENER
    // =====================================================

    fun stopListening() {
        Log.d("REALTIME_ORDER", "Stopping Firestore listener")

        listener?.remove()
        listener = null
    }
}
