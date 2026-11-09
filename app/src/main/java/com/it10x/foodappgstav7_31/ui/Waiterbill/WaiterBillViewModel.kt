package com.it10x.foodappgstav7_31.com.it10x.foodappgstav7_31.ui.Waiterbill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.online.repository.CashierOrderSyncRepository
import com.it10x.foodappgstav7_31.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_31.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_31.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_31.data.pos.dao.OutletDao
import com.it10x.foodappgstav7_31.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_31.data.pos.repository.OutletRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSPaymentRepository
import com.it10x.foodappgstav7_31.printer.PrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import com.it10x.foodappgstav7_31.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_31.data.pos.dao.PosCustomerLedgerDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_31.data.pos.repository.KotRepository

import com.it10x.foodappgstav7_31.ui.Waiterbill.BillingItemUi
import kotlinx.coroutines.flow.update
import kotlin.math.pow

class WaiterBillViewModel(
    private val kotItemDao: KotItemDao,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val orderSequenceRepository: OrderSequenceRepository,
    private val outletDao: OutletDao,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager,
    private val outletRepository: OutletRepository,
    private val paymentRepository: POSPaymentRepository,
    private val customerDao: PosCustomerDao,
    private val ledgerDao: PosCustomerLedgerDao,
    private val kotRepository: KotRepository,
    private val cashierOrderSyncRepository: CashierOrderSyncRepository
) : ViewModel() {

    // --------------------------------------------------------
    // UI State + Delivery Address
    // --------------------------------------------------------
    private val _deliveryAddress = MutableStateFlow<DeliveryAddressUiState?>(null)

    private val _loading = MutableStateFlow(false)
    val deliveryAddress: DeliveryAddressUiState? get() = _deliveryAddress.value

    private val _uiState = MutableStateFlow(BillUiState(loading = true))
    val uiState: StateFlow<BillUiState> = _uiState

    private val _currencySymbol = MutableStateFlow("₹") // fallback
    val currencySymbol: StateFlow<String> = _currencySymbol

    private val _discountFlat = MutableStateFlow(0.0)
    private val _discountPercent = MutableStateFlow(0.0)

    private val _customerSuggestions = MutableStateFlow<List<PosCustomerEntity>>(emptyList())
    val customerSuggestions: StateFlow<List<PosCustomerEntity>> = _customerSuggestions


    // ---------------- PAYMENT PROTECTION ----------------

    private val _event = MutableStateFlow<String?>(null)
    val event: StateFlow<String?> = _event

    private val _isProcessing = MutableStateFlow(false)



    val orderTypePublic: String
        get() = orderType

    init {
       // Log.d("BILL_INIT", "Initialized | table=$tableId")

        observeBill()
        loadCurrency()

    }

    // --------------------------------------------------------
    // Observe Bill (Live billing snapshot)
    // --------------------------------------------------------

    private fun observeBill() {
        viewModelScope.launch {
            combine(
                kotItemDao.getItemsForTable(tableId),
                _discountFlat,
                _discountPercent
            ) { kotItems, flat, percent ->
                Triple(kotItems, flat, percent)
            }.collectLatest { (kotItems, flat, percent) ->

                val doneItems = kotItems.filter { it.status == "DONE" }

                val billingItems = doneItems
                    .groupBy {
                        listOf(
                            it.productId,
                            it.basePrice,
                            it.taxRate,
                            it.note,
                            it.modifiersJson
                        )
                    }
                    .map { (_, group) ->

                        val first = group.first()
                        val quantity = group.sumOf { it.quantity }
                        val modifierPricePerItem =
                            ModifierJsonHelper.fromJson(first.modifiersJson)
                                .flatMap { it.items }
                                .sumOf { it.price }

// ✅ base + modifier
                        val basePlusModifier = first.basePrice + modifierPricePerItem

// ✅ totals
                        val itemTotal = (basePlusModifier * quantity).round(2)

                        val taxPerItem =
                            if (first.taxType == "exclusive")
                                (basePlusModifier * (first.taxRate / 100)).round(2)
                            else 0.0

                        val taxTotal = (taxPerItem * quantity).round(2)

                        BillingItemUi(
                            id = first.id,
                            productId = first.productId,
                            name = first.name,
                            basePrice = first.basePrice,
                            taxRate = first.taxRate,
                            quantity = quantity,
                            finalTotal = itemTotal + taxTotal,
                            itemtotal = itemTotal,
                            taxTotal = taxTotal,
                            note = first.note ?: "",
                            modifiersJson = first.modifiersJson ?: ""
                        )

                    }



                val subtotal = billingItems.sumOf { it.itemtotal }
                val totalTax = billingItems.sumOf { it.taxTotal }

                val percentValue = subtotal * (percent / 100.0)
                val discount = if (flat > 0) flat else percentValue

                val safeDiscount = discount.coerceAtMost(subtotal)

                val taxAfterDiscount =
                    if (subtotal == 0.0) 0.0
                    else totalTax * (1 - safeDiscount / subtotal)

                val deliveryBase = 0.0
                val deliveryTax = 0.0

                val finalTotal =
                    (subtotal - safeDiscount) + taxAfterDiscount

                _uiState.update { old ->

                    old.copy(
                        loading = false,
                        items = billingItems,
                        subtotal = subtotal,
                        tax = taxAfterDiscount,
                        deliveryFee = deliveryBase,
                        deliveryTax = deliveryTax,
                        discountFlat = flat,
                        discountPercent = percent,
                        discountApplied = safeDiscount,
                        total = finalTotal
                    )
                }



            }
        }
    }


    private fun loadCurrency() {
        viewModelScope.launch {
            val outletInfo = outletRepository.getOutletInfo()
            _currencySymbol.value = outletInfo.currencyCode
        }
    }

    fun Double.round(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(this * factor) / factor
    }


}
