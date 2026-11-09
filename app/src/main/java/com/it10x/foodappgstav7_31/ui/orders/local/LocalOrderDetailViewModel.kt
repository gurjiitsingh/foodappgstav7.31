package com.it10x.foodappgstav7_31.ui.orders.local

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_31.data.pos.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav7_31.printer.PrinterManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocalOrderDetailViewModel(
    private val orderId: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val _orderInfo = MutableStateFlow<PosOrderMasterEntity?>(null)
    val orderInfo: StateFlow<PosOrderMasterEntity?> = _orderInfo

    val products: StateFlow<List<PosOrderItemEntity>> =
        flow { emit(repository.getOrderItems(orderId)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subtotal = products.map { it.sumOf { p -> p.itemSubtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)


    val discount = orderInfo
        .map { it?.discountTotal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

//    val taxTotal = orderInfo
//        .map { it?.taxTotal ?: 0.0 }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val taxTotal = orderInfo
        .map { it?.itemTax ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val grandTotal = orderInfo
        .map { it?.grandTotal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalPaid = orderInfo
        .map { it?.paidAmount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val dueAmount = orderInfo
        .map { it?.dueAmount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val deliveryFee = orderInfo
        .map { it?.deliveryFee ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val deliveryTax = orderInfo
        .map { it?.deliveryTax ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

//    val paymentStatus = combine(totalPaid, dueAmount) { paid, due ->
//        when {
//            paid == 0.0 -> "CREDIT"
//            due > 0.0 -> "PARTIAL"
//            else -> "PAID"
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "CREDIT")

    val paymentStatus = orderInfo
        .map { order ->
            Log.d(
                "ORDER_DETAIL",
                "orderId=$orderId | orderStatus=${order?.orderStatus} | " +
                        "paymentMode=${order?.paymentMode} | " +
                        "paymentStatus=${order?.paymentStatus} | " +
                        "paidAmount=${order?.paidAmount} | " +
                        "dueAmount=${order?.dueAmount}"
            )

            order?.paymentStatus ?: "CREDIT"
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            "CREDIT"
        )

    init {
        viewModelScope.launch {
            val loadedOrder = repository.getOrderById(orderId)

            Log.d(
                "ORDER_DETAIL",
                "LOADED ORDER: id=${loadedOrder?.id} | " +
                        "srno=${loadedOrder?.srno} | " +
                        "orderStatus=${loadedOrder?.orderStatus} | " +
                        "paymentMode=${loadedOrder?.paymentMode} | " +
                        "paymentStatus=${loadedOrder?.paymentStatus} | " +
                        "paidAmount=${loadedOrder?.paidAmount} | " +
                        "dueAmount=${loadedOrder?.dueAmount}"
            )

            _orderInfo.value = loadedOrder
        }
    }
//    init {
//        viewModelScope.launch {
//            _orderInfo.value = repository.getOrderById(orderId)
//        }
//    }

    fun updateGrandTotal(newTotal: Double) {
        val current = _orderInfo.value ?: return
        viewModelScope.launch {
            repository.updateGrandTotal(current.id, newTotal)

            // ✅ update local state immediately for UI refresh
            _orderInfo.value = current.copy(grandTotal = newTotal, updatedAt = System.currentTimeMillis())
        }
    }
}
