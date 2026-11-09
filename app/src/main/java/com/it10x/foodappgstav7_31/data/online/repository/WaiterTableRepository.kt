package com.it10x.foodappgstav7_31.data.online.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.it10x.foodappgstav7_31.viewmodel.PosTableViewModel
import com.it10x.foodappgstav7_31.data.pos.entities.TableEntity

class WaiterTableRepository(
    private val firestore: FirebaseFirestore
) {

    private var listener: ListenerRegistration? = null

    fun startListening(
        onUpdate: (List<PosTableViewModel.TableUiState>) -> Unit
    ) {
        stopListening()

        listener = firestore.collection("pos_tables")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.map { doc ->

                    val tableId = doc.id
                    val tableName = doc.getString("tableName") ?: tableId
                    val area = doc.getString("area") ?: "Waiter"
                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()

                    val cartCount = items.sumOf {
                        (it["quantity"] as? Long ?: 1L).toInt()
                    }

                    val table = TableEntity(
                        id = tableId,
                        tableName = tableName,
                        status = if (items.isEmpty()) "AVAILABLE" else "OCCUPIED",
                        area = area,
                        sortOrder = 0,
                        cartCount = cartCount,
                        kitchenCount = 0,
                        billCount = 0,
                        billAmount = 0.0
                    )

                    val color = when {
                        table.billCount > 0 -> PosTableViewModel.TableColor.RED
                        table.kitchenCount > 0 -> PosTableViewModel.TableColor.GREEN
                        table.cartCount > 0 -> PosTableViewModel.TableColor.BLUE
                        else -> PosTableViewModel.TableColor.GRAY
                    }

                    PosTableViewModel.TableUiState(
                        table = table,
                        color = color,
                        isBilled = false
                    )
                }

                onUpdate(list)
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }
}