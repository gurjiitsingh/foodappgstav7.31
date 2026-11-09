package com.it10x.foodappgstav7_31.ui.bill

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.auth.PosSessionManager
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.online.repository.CashierOrderSyncRepository
import com.it10x.foodappgstav7_31.data.online.sync.SyncManagerProvider
import com.it10x.foodappgstav7_31.data.online.sync.TableKotSyncService
import com.it10x.foodappgstav7_31.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_31.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_31.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_31.data.pos.dao.OutletDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderPaymentEntity
import com.it10x.foodappgstav7_31.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_31.data.pos.repository.OutletRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSPaymentRepository
import com.it10x.foodappgstav7_31.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.printer.ReceiptFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.data.print.OutletMapper
import com.it10x.foodappgstav7_31.ui.payment.PaymentInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.it10x.foodappgstav7_31.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_31.data.pos.dao.PosCustomerLedgerDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosCustomerLedgerEntity
import com.it10x.foodappgstav7_31.data.pos.repository.KotRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import com.it10x.foodappgstav7_31.data.pos.manager.TableSyncManager
import com.it10x.foodappgstav7_31.fiscal.FiscalContext
import com.it10x.foodappgstav7_31.fiscal.FiscalService
import com.it10x.foodappgstav7_31.fiscal.getFiscalService
import com.it10x.foodappgstav7_31.network.fiskaly.FiskalyClient
import com.it10x.foodappgstav7_31.fiskaly.FiskalyRepository
import com.it10x.foodappgstav7_31.utils.MoneyUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.String
import android.app.Application
import com.it10x.foodappgstav7_31.data.ReceiptPrintMode
import com.it10x.foodappgstav7_31.data.pos.repository.BusinessDayRepository
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.pos.repository.InvoiceCounterRepository
import com.it10x.foodappgstav7_31.data.pos.repository.VirtualTableRepository
import com.it10x.foodappgstav7_31.utils.bill.BillCalculator
import kotlin.Double
import com.it10x.foodappgstav7_31.ui.pos.PosSessionViewModel
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import androidx.room.withTransaction
import com.it10x.foodappgstav7_31.data.pos.repository.loyalty.LoyaltyRepository

class BillViewModel(
    private val app: Application,
    private val db: AppDatabase,
    private val kotItemDao: KotItemDao,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val orderSequenceRepository: OrderSequenceRepository,
    private val invoiceCounterRepository: InvoiceCounterRepository,
    private val outletDao: OutletDao,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager,
    private val prefs: PrinterPreferences,
    private val outletRepository: OutletRepository,
    private val paymentRepository: POSPaymentRepository,
    private val customerDao: PosCustomerDao,
    private val ledgerDao: PosCustomerLedgerDao,
    private val kotRepository: KotRepository,
    private val cashierOrderSyncRepository: CashierOrderSyncRepository,
    private val tableSyncManager: TableSyncManager,
    private val businessDayRepository: BusinessDayRepository,
    private val fiskalyRepository: FiskalyRepository,
    private val virtualTableRepository: VirtualTableRepository,
    private val posSessionViewModel: PosSessionViewModel,
) : ViewModel() {

    // --------------------------------------------------------
    // UI State + Delivery Address
    // --------------------------------------------------------
    private var invoiceCounterInitialized = false

    init {

//        Log.d(
//            "POS_SESSION",
//            "BillViewModel received PosSession VM = ${
//                System.identityHashCode(posSessionViewModel)
//            }"
//        )
//
//        Log.d(
//            "POS_SESSION",
//            "BillViewModel received tableId = $tableId, tableName = $tableName"
//        )

        initializeInvoiceCounter()

    }


    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()
    private var pendingOrderId: String? = null
    private val _deliveryAddress = MutableStateFlow<DeliveryAddressUiState?>(null)

    private val _loading = MutableStateFlow(false)
    val deliveryAddress: DeliveryAddressUiState? get() = _deliveryAddress.value

    private val _uiState = MutableStateFlow(BillUiState(loading = true))
    val uiState: StateFlow<BillUiState> = _uiState

    private val _currencySymbol = MutableStateFlow("₹") // fallback
    val currencySymbol: StateFlow<String> = _currencySymbol

    private val _discountFlat = MutableStateFlow(0.0)
    private val _deliveryFee = MutableStateFlow(0.0)
    private val _deliveryTaxPercent = MutableStateFlow(0.0)
    private val _discountPercent = MutableStateFlow(0.0)

    // ============================================================
// LOYALTY REDEMPTION DISCOUNT
// Stored in paise
// ============================================================
    private val _loyaltyDiscountPaise = MutableStateFlow(0L)

    fun setLoyaltyDiscountPaise(value: Long) {
        _loyaltyDiscountPaise.value = value.coerceAtLeast(0L)
    }

    private val _customerSuggestions = MutableStateFlow<List<PosCustomerEntity>>(emptyList())
    val customerSuggestions: StateFlow<List<PosCustomerEntity>> = _customerSuggestions

    private val _redeemPoints = MutableStateFlow(0)

    // ============================================================
// CASHIER CONTROL — EARN POINTS ON DISCOUNTED BILLS
// ============================================================

    private val _allowPointsOnDiscountedBill =
        MutableStateFlow(true)

    val allowPointsOnDiscountedBill: StateFlow<Boolean> =
        _allowPointsOnDiscountedBill

    fun setAllowPointsOnDiscountedBill(allow: Boolean) {
        _allowPointsOnDiscountedBill.value = allow
    }

    // ---------------- PAYMENT PROTECTION ----------------

//    private val _event = MutableStateFlow<String?>(null)
//    val event: StateFlow<String?> = _event

    private val _event = MutableSharedFlow<String>()
    val event = _event.asSharedFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    private val firestore = FirebaseFirestore.getInstance()



    val totalPaise: Long
        get() = MoneyUtils.toPaise(_uiState.value.total)
    val unpaidModes = setOf("CREDIT", "DELIVERY_PENDING", "WAITER_PENDING")

    private val _creditPaise = MutableStateFlow(0L)
    val creditPaise: StateFlow<Long> = _creditPaise

    private val loyaltyRepository by lazy {
        LoyaltyRepository(db)
    }

    // ============================================================
// CUSTOMER LOYALTY POINTS
// ============================================================

    private val _customerLoyaltyPoints = MutableStateFlow(0)
    val customerLoyaltyPoints: StateFlow<Int> = _customerLoyaltyPoints


    fun setCreditAmountRaw(input: String) {
        val clean = input.trim()

        if (clean.isEmpty()) {
            _creditPaise.value = 0L
            return
        }

        val parts = clean.split(".")

        val rupees = parts.getOrNull(0)?.toLongOrNull() ?: 0L
        val paisePart = parts.getOrNull(1)?.padEnd(2, '0')?.take(2) ?: "00"
        val paise = paisePart.toLongOrNull() ?: 0L

        val finalPaise = rupees * 100 + paise

        Log.d("BILL_DEBUG", "RAW INPUT: $input → $finalPaise paise")

        _creditPaise.value = finalPaise
    }

    fun clearCredit() {
        Log.d("BILL_DEBUG", "CLEAR CREDIT")
        _creditPaise.value = 0L   // ✅ correct source of truth
    }


    fun setDeliveryFee(value: Double) {
        _deliveryFee.value = value.coerceAtLeast(0.0)
    }

    fun setDeliveryTaxPercent(value: Double) {
        _deliveryTaxPercent.value = value.coerceAtLeast(0.0)
    }

    // ✅ FINAL remaining flow (AUTO updates)
    val remainingPaise: StateFlow<Long> =
        combine(_uiState, _creditPaise) { ui, credit ->

            val totalPaise = MoneyUtils.toPaise(ui.total)

            val remaining = (totalPaise - credit).coerceAtLeast(0)

            Log.d("BILL_DEBUG", "Total: $totalPaise | Credit: $credit | Remaining: $remaining")

            remaining
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0L
        )





    private val tableKotSyncService = TableKotSyncService(
        firestore,
        kotItemDao
    )
//    private fun sendEvent(message: String) {
//        _event.value = message
//    }
    private fun sendEvent(message: String) {
        viewModelScope.launch {
            _event.emit(message)
        }
    }

//    fun clearEvent() {
//        _event.value = null
//    }

    fun setRedeemPoints(points: Int) {
        _redeemPoints.value = points.coerceAtLeast(0)
    }
    fun setFlatDiscount(value: Double) {
        _discountFlat.value = value.coerceAtLeast(0.0)
        _discountPercent.value = 0.0 // reset percent
    }

    fun setPercentDiscount(value: Double) {
        _discountPercent.value = value.coerceAtLeast(0.0)
        _discountFlat.value = 0.0 // reset flat
    }

    fun setCustomerPhone(phone: String) {

        _uiState.update {
            it.copy(customerPhone = phone)
        }

        // Reset old customer's points immediately.
        _customerLoyaltyPoints.value = 0

        val cleanPhone = phone.trim()

        if (cleanPhone.isBlank()) return

        viewModelScope.launch {
            try {
                val outlet = outletDao.getOutlet() ?: return@launch

                val program = loyaltyRepository.ensureDefaultProgram(
                    ownerId = outlet.ownerId,
                    outletId = outlet.outletId
                )

                if (!program.isActive) return@launch

                // Look up an existing account only.
                // Do not create an account just to display points.
                val account = loyaltyRepository.getAccount(
                    customerId = cleanPhone,
                    programId = program.id,
                    outletId = outlet.outletId
                )

                val points = if (account != null) {
                    loyaltyRepository.getAvailablePoints(account.id)
                } else {
                    0
                }

                // Prevent an older lookup from replacing the
                // balance of a newly selected customer.
                if (_uiState.value.customerPhone.trim() == cleanPhone) {
                    _customerLoyaltyPoints.value = points
                }

                Log.d(
                    "CUSTOMER_LOYALTY",
                    "Phone: $cleanPhone | Available points: $points"
                )

            } catch (e: Exception) {
                Log.e(
                    "CUSTOMER_LOYALTY",
                    "Failed to load customer points",
                    e
                )
                if (_uiState.value.customerPhone.trim() == cleanPhone) {
                    _customerLoyaltyPoints.value = 0
                }
            }
        }
    }



  //  val outletInfo: StateFlow<OutletInfo> = outletRepository.outletInfo
    // ✅ Expose orderType safely for Compose UI
    val orderTypePublic: String
        get() = orderType

    init {
        resetBillUi()
        observeBill()
        loadCurrency()
        setDeliveryTaxPercent(5.0)
    }


    // --------------------------------------------------------
    // Observe Bill (Live billing snapshot)
    // --------------------------------------------------------

    private fun observeBill() {
        viewModelScope.launch {

            val billInputs = combine(
                kotItemDao.getItemsForTable(tableId),
                _discountFlat,
                _discountPercent,
                _deliveryFee,
                _deliveryTaxPercent
            ) { kotItems, flat, percent, deliveryFeeFlow, deliveryTaxPercentFlow ->

                BillCombine(
                    kotItems,
                    flat,
                    percent,
                    deliveryFeeFlow,
                    deliveryTaxPercentFlow
                )
            }

            combine(
                billInputs,
                _loyaltyDiscountPaise
            ) { bill, loyaltyDiscountPaise ->

                Pair(bill, loyaltyDiscountPaise)

            }.collectLatest { (bill, loyaltyDiscountPaise) ->

                val (kotItems, flat, percent, deliveryFeeFlow, deliveryTaxPercentFlow) = bill

                val outlet = outletDao.getOutlet()
                if (outlet == null) return@collectLatest

                // KEEP THE REST OF YOUR EXISTING observeBill CODE HERE
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

                            // ✅ FIXED
                            finalTotal = itemTotal + taxTotal,
                            itemtotal = itemTotal,
                            taxTotal = taxTotal,

                            note = first.note ?: "",
                            modifiersJson = first.modifiersJson ?: ""
                        )

                    }




//==================
                val calculation = BillCalculator.calculate(
                    items = doneItems,
                    taxMode = outlet.taxMode,
                    discountFlat = flat,
                    discountPercent = percent,
                    deliveryFee = deliveryFeeFlow,
                    deliveryTaxPercent = deliveryTaxPercentFlow  ?: 0.0,
                    loyaltyDiscount = loyaltyDiscountPaise / 100.0
                )

                val subtotal = MoneyUtils.fromPaise(calculation.itemSubtotalPaise)

                val itemTax = MoneyUtils.fromPaise(
                    calculation.exclusiveTaxPaise +
                            calculation.inclusiveTaxPaise
                )

                val deliveryFee = MoneyUtils.fromPaise(calculation.deliveryFeePaise)

                val deliveryTax = MoneyUtils.fromPaise(calculation.deliveryTaxPaise)

                val safeDiscount = MoneyUtils.fromPaise(calculation.discountPaise)

                val finalTotal = MoneyUtils.fromPaise(calculation.grandTotalPaise)
//==============================
                _uiState.update { old ->
                    old.copy(
                        loading = false,
                        items = billingItems,
                        subtotal = subtotal,
                        deliveryFee = deliveryFee,
                        deliveryTax = deliveryTax,
                        tax = itemTax,
                        discountFlat = flat,
                        discountPercent = percent,
                        discountApplied = safeDiscount,
                        total = finalTotal,
                        loyaltyDiscount = MoneyUtils.fromPaise(calculation.loyaltyDiscountPaise),
                    )
                }

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        resetBillUi()
    }

    fun resetBillUi() {
        _discountFlat.value = 0.0
        _discountPercent.value = 0.0

        _deliveryFee.value = 0.0
        _deliveryTaxPercent.value = 5.0   // or 0.0 if you want

        _creditPaise.value = 0L

        _customerLoyaltyPoints.value = 0

        _loyaltyDiscountPaise.value = 0L

        _redeemPoints.value = 0
        _allowPointsOnDiscountedBill.value = true
        _deliveryAddress.value = null

        _uiState.update {
            it.copy(
                customerPhone = ""
            )
        }
    }

    private fun loadCurrency() {
        viewModelScope.launch {
            val outletInfo = outletRepository.getOutletInfo()
            _currencySymbol.value = outletInfo.currencyCode
        }
    }


    // --------------------------------------------------------
    // Payment + Order Creation
    // --------------------------------------------------------

    private val paymentMutex = kotlinx.coroutines.sync.Mutex()


    //  ====================================== SAVE ORDER ======================
    fun payBill(
        payments: List<PaymentInput>,
        name: String,
        phone: String
    ) {
// UPDATE NEEDED ############################
//        Move the customer credit update inside the database transaction
//        Currently, you update currentDue and insert the customer ledger entry before db.withTransaction.
//        If the bill save fails afterward, the customer could owe money even though no bill was saved.

        val creditPaiseInput = _creditPaise.value
        val paidModes = setOf("CASH", "CARD", "UPI", "WALLET")

        val hasPaid = payments.any { it.mode in paidModes }
        val hasUnpaid = payments.any { it.mode in unpaidModes }

        viewModelScope.launch {

            if (!paymentMutex.tryLock()) {
                sendEvent("Payment already in progress")
                return@launch
            }
            _isProcessing.value = true

            val outlet = outletDao.getOutlet()
            if (outlet == null) {

                _toastEvent.emit("Outlet not configured")
                _isProcessing.value = false
                return@launch
            }


            lateinit var fiscalService: FiscalService
            lateinit var fiscalContext: FiscalContext

            try {
                val inputPhone = phone.trim()
                val inputName = name.trim().ifBlank { "Customer" }
                val kotItems = kotItemDao.getDoneItemsForTableOnce(tableId)
                val orderDetailItems =
                    kotItemDao.getDoneAndDeletedItemsForOrderDetail(tableId)
                if (kotItems.isEmpty()) {
                    sendEvent("No items to bill")
                    return@launch
                }


                val now = System.currentTimeMillis()
                val orderDate = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(Date(now))
                val currentBusinessDay =
                    businessDayRepository.getCurrentBusinessDay()

                val businessDate = currentBusinessDay.businessDate
                val orderId = pendingOrderId ?: UUID.randomUUID().toString()
                pendingOrderId = orderId

                val mapping = orderSequenceRepository.getOrCreateOrderNo(
                    mapKey = orderId,
                    deviceCode = "P1",
                    financialYear = getFinancialYearCode()
                )

                val srNo = mapping.srno
                //================================
                // BILL CALCULATION
                //==================================
                val calculation = BillCalculator.calculate(
                    items = kotItems,
                    taxMode = outlet.taxMode,
                    discountFlat = _discountFlat.value,
                    discountPercent = _discountPercent.value,
                    deliveryFee = _deliveryFee.value,
                    deliveryTaxPercent = _deliveryTaxPercent.value,
                    loyaltyDiscount = _loyaltyDiscountPaise.value / 100.0
                )

                val itemSubtotalPaise = calculation.itemSubtotalPaise

                val validationKotItems = kotItemDao
                    .getDoneItemsForTableOnce(tableId)


                val validationItemSubtotalPaise = validationKotItems.sumOf { item ->

                    val modifierPrice =
                        ModifierJsonHelper.fromJson(item.modifiersJson)
                            .flatMap { group -> group.items }
                            .sumOf { modifier -> modifier.price }

                    val basePlusModifier =
                        item.basePrice + modifierPrice

                    MoneyUtils.toPaise(basePlusModifier) * item.quantity
                }

                val firstTotalQuantity = kotItems.sumOf { it.quantity }
                val validationTotalQuantity = validationKotItems.sumOf { it.quantity }

                if (
                    itemSubtotalPaise != validationItemSubtotalPaise ||
                    firstTotalQuantity != validationTotalQuantity
                ) {
                    withContext(Dispatchers.Main) {
                        sendEvent("Something went wrong. Please try again.")
                    }
                    return@launch
                }

                val safeDiscountPaise = calculation.discountPaise + calculation.loyaltyDiscountPaise
                val loyaltyDiscountPaise =
                    calculation.loyaltyDiscountPaise
                val exclusiveTaxPaise = calculation.exclusiveTaxPaise
                val inclusiveTaxPaise = calculation.inclusiveTaxPaise
// Total GST on items (used for saving/reporting)
                val itemTaxPaise = exclusiveTaxPaise + inclusiveTaxPaise
                val deliveryFeePaise = calculation.deliveryFeePaise
                val deliveryTaxPaise = calculation.deliveryTaxPaise
                val totalTaxPaise = calculation.totalTaxPaise
                val taxableAmountPaise = calculation.taxableAmountPaise
                val roundOffPaise = calculation.roundOffPaise
                val grandTotalPaise = calculation.grandTotalPaise

                // ===========================
                // PAYMENT CALCULATION
                // ===========================
                val totalPaidPaise = payments
                    .filter { it.mode in paidModes }
                    .sumOf { it.amount }

// 👇 FIRST define paymentStatus
                val hasCredit = creditPaiseInput > 0

                val paymentStatus = when {
                    payments.any { it.mode == "WAITER_PENDING" } -> "WAITER_PENDING"
                    payments.any { it.mode == "DELIVERY_PENDING" } -> "DELIVERY_PENDING"
                    hasPaid && hasCredit -> "PARTIAL"
                    hasCredit -> "CREDIT"
                    else -> "PAID"
                }
// 👇 THEN use it
                val duePaise = creditPaiseInput
                val dueAmount = BigDecimal.valueOf(creditPaiseInput, 2)
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .toDouble()

                // ===========================
                // PHONE VALIDATION
                // ===========================

                if (paymentStatus in listOf("CREDIT", "PARTIAL") && inputPhone.isBlank())
                {
                    sendEvent("Phone required for credit sale")
                    return@launch
                }

// ===========================
// ENSURE CUSTOMER EXISTS (IF PHONE ENTERED)
// ===========================

                var resolvedCustomerId: String? = null

                if (inputPhone.isNotBlank()) {

                    resolvedCustomerId = inputPhone

                    val existingCustomer = customerDao.getCustomerByPhone(inputPhone)

                    if (existingCustomer == null) {

                        val customer = PosCustomerEntity(
                            id = inputPhone,
                            ownerId = outlet.ownerId,
                            outletId = outlet.outletId,
                            name = inputName,
                            phone = inputPhone,
                            addressLine1 = null,
                            addressLine2 = null,
                            city = null,
                            state = null,
                            zipcode = null,
                            landmark = null,
                            creditLimit = 0.0,
                            currentDue = 0.0,   // 🔥 important
                            createdAt = now,
                            updatedAt = null
                        )

                        customerDao.insert(customer)
                    }
                }
                // ===========================
                // CUSTOMER CREDIT HANDLING
                // ===========================

                if (paymentStatus == "CREDIT" || paymentStatus == "PARTIAL") {
                    val cleanPhone = inputPhone.trim()
                    resolvedCustomerId = cleanPhone
                    val creditToAdd = duePaise / 100.0
                    val existingCustomer = customerDao.getCustomerByPhone(cleanPhone)
                    if (existingCustomer == null) {
                        Log.e("CREDIT", "❌ Customer NOT FOUND for phone = $cleanPhone")
                        return@launch
                    }
                    // ✅ FIX: use ID (NOT phone)
                    customerDao.increaseDue(existingCustomer.id, creditToAdd)
                    val updatedCustomer = customerDao.getCustomerById(existingCustomer.id)
                    val lastBalance = ledgerDao.getLastBalance(existingCustomer.id) ?: 0.0
                    val newBalance = lastBalance + creditToAdd
                    val ledgerEntry = PosCustomerLedgerEntity(
                        id = UUID.randomUUID().toString(),
                        ownerId = outlet.ownerId,
                        outletId = outlet.outletId,
                        customerId = existingCustomer.id,   // ✅ FIX
                        orderId = orderId,
                        paymentId = null,
                        type = "ORDER",
                        debitAmount = creditToAdd,
                        creditAmount = 0.0,
                        balanceAfter = newBalance,
                        note = "Credit sale Order ",
                        createdAt = now,
                        deviceId = "POS"
                    )

                    ledgerDao.insert(ledgerEntry)

                }

                val paymentMode =
                    if (payments.size > 1) "MIXED"
                    else payments.firstOrNull()?.mode ?: "CREDIT"

                val finalizedById = PosSessionManager.getUserId(app) ?: ""
                val finalizedByName = PosSessionManager.getFullName(app) ?: ""

                val stewardName = kotItems
                    .mapNotNull { it.createdByName }
                    .firstOrNull { it.isNotBlank() } ?: "NONAME"

                val stewardId = kotItems
                    .mapNotNull { it.createdById }
                    .firstOrNull { it.isNotBlank() } ?: ""
                // ===========================
                // ORDER MASTER
                // ===========================
//                Log.d(
//                    "POS_ORDER",
//                    "INSERT OREDER in billviewmodel orderType=${orderType}, TableName=${tableName} ,TableId=${tableId}"
//                )

                val orderMaster = PosOrderMasterEntity(
                    id = orderId,
                    srno = srNo,
                    orderType = orderType,
                    tableNo = tableName,
                    customerName = inputName,
                    customerPhone = inputPhone,
                    customerId = resolvedCustomerId,
                    dAddressLine1 = deliveryAddress?.line1,
                    dAddressLine2 = deliveryAddress?.line2,
                    dCity = deliveryAddress?.city,
                    dState = deliveryAddress?.state,
                    dZipcode = deliveryAddress?.zipcode,
                    dLandmark = deliveryAddress?.landmark,
                    itemTotal = MoneyUtils.fromPaise(itemSubtotalPaise),
                    deliveryFee = _deliveryFee.value,
                    deliveryTax = MoneyUtils.fromPaise(deliveryTaxPaise),
                    itemTax = MoneyUtils.fromPaise(itemTaxPaise),
                    taxTotal = MoneyUtils.fromPaise(totalTaxPaise),
                    discountTotal = MoneyUtils.fromPaise(safeDiscountPaise),
                    grandTotal = MoneyUtils.fromPaise(grandTotalPaise),
                    paymentMode = paymentMode,
                    paymentStatus = paymentStatus,
                    paidAmount = MoneyUtils.fromPaise(totalPaidPaise),
                    dueAmount =dueAmount,
                    orderStatus = "COMPLETED",
                    deviceId = "POS",
                    deviceName = "POS",
                    appVersion = "1.0",
                    createdById =stewardId,
                    createdByName=stewardName,
                    finalizedById=finalizedById,
                    finalizedByName=finalizedByName,
                    orderDate=orderDate,
                    businessDate = businessDate,
                    createdAt = now,
                    updatedAt = now,
                    syncStatus = if (paymentStatus == "WAITER_PENDING") "SYNCED" else "PENDING",
                    lastSyncedAt = null,
                    notes = null
                )


                //   val orderItems = kotItems
                val orderItems = orderDetailItems
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



                        val basePlusModifier = first.basePrice + modifierPricePerItem
                        val itemGrossAmount = (basePlusModifier * quantity).round(2)
                        val taxPerItemPlusModifier =
                            if (first.taxType == "exclusive")
                                (basePlusModifier * (first.taxRate / 100))
                            else 0.0
                        val finalPricePerItemPlusModifier =
                            (basePlusModifier + taxPerItemPlusModifier).round(2)

                        val finalPriceTotalItemPlusModifier =
                            (finalPricePerItemPlusModifier * quantity).round(2)

                        val taxTotalItemPlusModifier =
                            (taxPerItemPlusModifier * quantity).round(2)

                        val modifierTotal =
                            (modifierPricePerItem * quantity).round(2)

                        PosOrderItemEntity(
                            id = UUID.randomUUID().toString(),
                            // 🔹 SNAPSHOT CATEGORY NAME (enterprise safe)
                            categoryName = first.categoryName,
                            orderMasterId = orderId,
                            productId = first.productId,
                            name = first.name,
                            productMode = first.productMode,
                            currentStock = first.currentStock,
                            categoryId = first.categoryId,
                            createdById =first.createdById,
                            createdByName=first.createdByName,
                            parentId = first.parentId,
                            isVariant = first.isVariant,
                            basePrice = first.basePrice,
                            modifierPrice = modifierTotal,
                            quantity = quantity,
                            itemSubtotal = itemGrossAmount ,
                            // 🔹 Currency snapshot (important for audit)
                            currency = _currencySymbol.value,
                            // 🔹 Payment snapshot (do NOT rely on join later)
                            status = first.status,
                            taxRate = first.taxRate,
                            taxType = first.taxType,
                            taxAmountPerItem = taxPerItemPlusModifier,
                            taxTotal = taxTotalItemPlusModifier,
                            note = first.note,
                            modifiersJson = first.modifiersJson,
                            finalPricePerItem = finalPricePerItemPlusModifier,
                            finalTotal = finalPriceTotalItemPlusModifier,
                            createdAt = now
                        )
                    }

                // =====================================================
// ORDER ITEM SUBTOTAL VALIDATION
// =====================================================

                val kotSubtotalPaise = kotItems.sumOf { item ->
                    MoneyUtils.toPaise(item.basePrice) * item.quantity
                }

                val orderItemsSubtotalPaise = orderItems.sumOf { item ->
                    MoneyUtils.toPaise(item.itemSubtotal)
                }


                // val (txId, clientId) = fiskalyRepository.startTransaction()
                //fiscalService = getFiscalService(outlet.countryName!!, fiskalyRepository)
                fiscalService = getFiscalService("IN", fiskalyRepository)
                fiscalContext = withContext(Dispatchers.IO) {
                    fiscalService.start()
                }
                withContext(Dispatchers.IO) {

                    db.withTransaction {

                        // ==========================================
                        // 1. READ CURRENT DONE KOT ITEMS
                        // ==========================================

                        val currentKotItems = kotItemDao
                            .getDoneItemsForTableOnce(tableId)


                        if (currentKotItems.isEmpty()) {
                            throw IllegalStateException("No items to bill")
                        }

                        // ==========================================
                        // 2. VALIDATE CURRENT KOT DATA
                        // ==========================================

                        val currentItemSubtotalPaise =
                            currentKotItems.sumOf { item ->

                                val modifierPrice =
                                    ModifierJsonHelper.fromJson(item.modifiersJson)
                                        .flatMap { group -> group.items }
                                        .sumOf { modifier -> modifier.price }

                                val basePlusModifier =
                                    item.basePrice + modifierPrice

                                MoneyUtils.toPaise(basePlusModifier) * item.quantity
                            }

                        val currentTotalQuantity =
                            currentKotItems.sumOf {
                                it.quantity
                            }

                        if (
                            currentItemSubtotalPaise != itemSubtotalPaise ||
                            currentTotalQuantity != firstTotalQuantity
                        ) {
                            throw IllegalStateException("Bill data changed")
                        }

                        // ==========================================
                        // 3. SAVE ORDER MASTER
                        // ==========================================

                        orderMasterDao.insert(orderMaster)

                        // ==========================================
                        // 4. VERIFY SAVED ORDER MASTER
                        // ==========================================

                        val savedOrder =
                            orderMasterDao.getByIdSync(orderId)

                        if (
                            savedOrder == null ||
                            MoneyUtils.toPaise(savedOrder.itemTotal) !=
                            itemSubtotalPaise
                        ) {
                            throw IllegalStateException("Order validation failed")
                        }

                        // ==========================================
                        // 5. SAVE ORDER ITEMS
                        // ==========================================

                        orderProductDao.insertAll(orderItems)


                        // ==========================================
                        // 6. SAVE ACTUAL PAYMENTS ONLY
                        // ==========================================

                        val actualPayments = payments.filter {
                            it.mode in paidModes && it.amount > 0
                        }

                        if (actualPayments.isNotEmpty()) {
                            val paymentEntities = actualPayments.map {
                                PosOrderPaymentEntity(
                                    id = UUID.randomUUID().toString(),
                                    orderId = orderId,
                                    ownerId = outlet.ownerId,
                                    outletId = outlet.outletId,
                                    amount = MoneyUtils.fromPaise(it.amount),
                                    mode = it.mode,
                                    provider = null,
                                    method = null,
                                    status = "SUCCESS",
                                    deviceId = "POS",
                                    createdAt = now,
                                    businessDate = businessDate,
                                    syncStatus = "PENDING"
                                )
                            }

                            paymentRepository.insertPayments(paymentEntities)
                        }


// ============================================================
// LOYALTY POINTS — AWARD ONLY WHEN CUSTOMER PHONE IS PROVIDED
// ============================================================


                        if (
                            !inputPhone.isBlank() &&
                            resolvedCustomerId != null &&
                            _redeemPoints.value > 0
                        ) {
                            val program = loyaltyRepository.ensureDefaultProgram(
                                ownerId = outlet.ownerId,
                                outletId = outlet.outletId
                            )

                            if (!program.isActive) {
                                throw IllegalStateException("Loyalty programme is not active")
                            }

                            val pointsToRedeem = _redeemPoints.value

                            require(program.redeemPoints > 0) {
                                "Invalid loyalty redemption configuration"
                            }

                            val expectedDiscountPaise =
                                pointsToRedeem.toLong() * program.redeemValueMinor /
                                        program.redeemPoints.toLong()

                            require(expectedDiscountPaise == calculation.loyaltyDiscountPaise) {
                                "Loyalty discount and redemption points do not match"
                            }

                            val account = loyaltyRepository.getOrCreateAccount(
                                customerId = resolvedCustomerId,
                                programId = program.id,
                                outletId = outlet.outletId
                            )

                            loyaltyRepository.redeemPoints(
                                accountId = account.id,
                                points = pointsToRedeem,
                                idempotencyKey = "ORDER_REDEEM:$orderId",
                                orderId = orderId,
                                orderNo = srNo,
                                outletId = outlet.outletId,
                                businessDate = businessDate,
                                createdBy = finalizedById,
                                description = "Points redeemed on order $srNo"
                            )

                            Log.d(
                                "LOYALTY",
                                "Redeemed $pointsToRedeem points from customer $resolvedCustomerId"
                            )
                        }

                        if (!inputPhone.isBlank() && resolvedCustomerId != null) {
                            val program = loyaltyRepository.ensureDefaultProgram(
                                ownerId = outlet.ownerId,
                                outletId = outlet.outletId
                            )

                            if (program.isActive) {

                                // Food/product amount after discount.
                                // Tax and delivery fee are excluded.
//                                val eligibleSpendPaise =
//                                    (itemSubtotalPaise - safeDiscountPaise).coerceAtLeast(0L)
//
//                                // Respect the programme's discounted-bill setting.
//                                val canEarn = safeDiscountPaise <= 0L ||
//                                        program.allowEarnOnDiscountedBills


// ============================================================
// EARNING BASIS — ACTUAL AMOUNT AFTER DISCOUNTS
// ============================================================

// Normal discount follows the programme setting.
// Loyalty redemption does NOT block earning on the remaining amount.

                                val normalDiscountPaise = calculation.discountPaise
                                val loyaltyDiscountPaise = calculation.loyaltyDiscountPaise

                                val eligibleSpendPaise = (
                                        itemSubtotalPaise -
                                                normalDiscountPaise -
                                                loyaltyDiscountPaise
                                        ).coerceAtLeast(0L)

//                                val canEarn = normalDiscountPaise <= 0L ||
//                                        _allowPointsOnDiscountedBill.value
                                val canEarn = _allowPointsOnDiscountedBill.value

                                val points = if (
                                    canEarn &&
                                    eligibleSpendPaise > 0L &&
                                    program.earnSpendMinor > 0L &&
                                    program.earnPoints > 0
                                ) {
                                    val earningBlocks =
                                        eligibleSpendPaise / program.earnSpendMinor

                                    val calculatedPoints =
                                        earningBlocks * program.earnPoints.toLong()

                                    calculatedPoints
                                        .coerceAtMost(Int.MAX_VALUE.toLong())
                                        .toInt()
                                } else {
                                    0
                                }

                                if (points > 0) {
                                    val account = loyaltyRepository.getOrCreateAccount(
                                        customerId = resolvedCustomerId,
                                        programId = program.id,
                                        outletId = outlet.outletId
                                    )

                                    loyaltyRepository.earnPoints(
                                        accountId = account.id,
                                        points = points,
                                        spendMinor = eligibleSpendPaise,
                                        idempotencyKey = "ORDER_EARN:$orderId",
                                        orderId = orderId,
                                        orderNo = srNo,
                                        outletId = outlet.outletId,
                                        businessDate = businessDate,
                                        createdBy = finalizedById,
                                        description = "Points earned on order $srNo"
                                    )

                                    Log.d(
                                        "LOYALTY",
                                        "Awarded $points points to customer $resolvedCustomerId"
                                    )
                                } else {
                                    Log.d(
                                        "LOYALTY",
                                        "No points awarded. Eligible spend: $eligibleSpendPaise"
                                    )
                                }
                            }
                        }


                        // ==========================================
                        // 7. UPDATE KOT HISTORY
                        // ==========================================

                        kotRepository.markHistoryPaid(
                            tableNo = tableId,
                            orderId = orderId
                        )

                        // ==========================================
                        // 8. FINALIZE TABLE
                        // ==========================================

                        repository.finalizeTableAfterPayment(
                            tableNo = tableId,
                            orderType = orderType
                        )

                        // ==========================================
                        // 9. DELETE LOCAL KOT
                        // ==========================================

                        kotRepository.deleteKotByTable(
                            tableId
                        )

                        // ==========================================
                        // 10. CLEAR ORDER SEQUENCE
                        // ==========================================

                        orderSequenceRepository.clearOrder(
                            orderId
                        )
                    } // END TRANSACTION

                    // ==============================================
                    // ROOM TRANSACTION SUCCESSFULLY COMMITTED
                    // ==============================================

                    if (
                        orderType == "TAKEAWAY" ||
                        orderType == "DELIVERY"
                    ) {

                        val nextVirtualTable =
                            virtualTableRepository.markCompleted(
                                tableId = tableId,
                                tableName = tableName,
                                orderType = orderType
                            )

                        Log.d("VIRTUAL_ORDER", "Completed: $tableName")
                        Log.d("VIRTUAL_ORDER", "Next: ${nextVirtualTable?.tableName}")

                        if (nextVirtualTable != null) {

                            posSessionViewModel.setNextVirtualTable(
                                tableId = nextVirtualTable.id,
                                tableName = nextVirtualTable.tableName,
                                orderType = nextVirtualTable.orderType
                            )
                        }
                    }

                    // ==============================================
                    // FIRESTORE
                    // ==============================================

                    tableKotSyncService.clearTableSnapshot(
                        tableId,
                        source = "PAYMENT_CLEAR"
                    )

                    SyncManagerProvider.get()
                        .addClearTable(tableId)
                }


                //FISKLAY CODE
                withContext(Dispatchers.IO) {
                    fiscalService.finish(
                        fiscalContext,
                        payments,
                        kotItems
                    )
                }

                // ==========================================
// VERIFY COMMITTED BILL + PAYMENTS
// ==========================================


                sendEvent("Payment successful")
                pendingOrderId = null
                resetBillUi()
            } catch (e: Exception) {

//                if (::fiscalService.isInitialized && ::fiscalContext.isInitialized) {
//                    withContext(Dispatchers.IO) {
//                        fiscalService.cancel(fiscalContext)
//                    }
//                }

                // if (!isFinished) {
                fiscalService.cancel(fiscalContext)
                // }



                Log.e("PAY_ERROR", "Payment failed", e)
                sendEvent("Payment failed")
            }finally {
                _isProcessing.value = false
                if (paymentMutex.isLocked) {
                    paymentMutex.unlock()
                }
            }

        }
    }

    fun printCurrentBill() {

        viewModelScope.launch {

            if (!paymentMutex.tryLock()) {
                sendEvent("Already processing")
                return@launch
            }

            _isProcessing.value = true
            val now = System.currentTimeMillis()
            val orderDate = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date(now))
            try {

                val outlet = outletDao.getOutlet()
                if (outlet == null) {
                    sendEvent("Outlet not configured")
                    return@launch
                }

                val kotItems = kotItemDao
                    .getDoneItemsForTableOnce(tableId)


                val kotNumbers = kotItems
                    .map { it.kotNumber }
                    .filter { it.isNotBlank() }
                    .distinct()

                val kotNumberText = kotNumbers.joinToString(", ")

//                kotItems.forEach {
//                    Log.d(
//                        "CREATED_BY",
//                        "Item=${it.name}, createdById=${it.createdById}, createdByName=${it.createdByName}"
//                    )
//                }

                if (kotItems.isEmpty()) {
                    sendEvent("No items to print")
                    return@launch
                }

                val currentBusinessDay =
                    businessDayRepository.getCurrentBusinessDay()

                val businessDate = currentBusinessDay.businessDate
                val orderId = pendingOrderId ?: UUID.randomUUID().toString()
                pendingOrderId = orderId

                val mapping = orderSequenceRepository.getOrCreateOrderNo(
                    mapKey = orderId,
                    deviceCode = "P1",
                    financialYear = getFinancialYearCode()
                )

                val srNo = mapping.srno

                // =========================
                // CALCULATE TOTAL
                // =========================
                val calculation = BillCalculator.calculate(
                    items = kotItems,
                    taxMode = outlet.taxMode,
                    discountFlat = _discountFlat.value,
                    discountPercent = _discountPercent.value,
                    deliveryFee = _deliveryFee.value,
                    deliveryTaxPercent = _deliveryTaxPercent.value,
                    loyaltyDiscount = _loyaltyDiscountPaise.value / 100.0
                )


                // ============================================================
// LOYALTY POINTS — PROJECTED EARNING FOR PRINT
// ============================================================

                var earnedPoints = 0

                val customerPhone = _uiState.value.customerPhone.trim()

                if (customerPhone.isNotBlank()) {

                    val program = loyaltyRepository.ensureDefaultProgram(
                        ownerId = outlet.ownerId,
                        outletId = outlet.outletId
                    )

                    if (program.isActive) {

                        val normalDiscountPaise = calculation.discountPaise
                        val loyaltyDiscountPaise = calculation.loyaltyDiscountPaise

                        // Actual amount after both discounts.
                        // Tax and delivery are excluded.
                        val eligibleSpendPaise = (
                                calculation.itemSubtotalPaise -
                                        normalDiscountPaise -
                                        loyaltyDiscountPaise
                                ).coerceAtLeast(0L)

                        Log.d(
                            "TEXT_PRINT",
                            "Normal discount = $normalDiscountPaise | " +
                                    "Cashier switch = ${_allowPointsOnDiscountedBill.value} | " +
                                    "Can earn = ${normalDiscountPaise <= 0L || _allowPointsOnDiscountedBill.value}"
                        )

//                        val canEarn = normalDiscountPaise <= 0L ||
//                                _allowPointsOnDiscountedBill.value
                        val canEarn = _allowPointsOnDiscountedBill.value

                        earnedPoints = if (
                            canEarn &&
                            eligibleSpendPaise > 0L &&
                            program.earnSpendMinor > 0L &&
                            program.earnPoints > 0
                        ) {
                            val earningBlocks =
                                eligibleSpendPaise / program.earnSpendMinor

                            (earningBlocks * program.earnPoints.toLong())
                                .coerceAtMost(Int.MAX_VALUE.toLong())
                                .toInt()
                        } else {
                            0
                        }
                    }
                }

                Log.d("TEXT_PRINT", "Projected earned points = $earnedPoints")


                val itemSubtotalPaise = calculation.itemSubtotalPaise

                // =========================
                // INDEPENDENT SUBTOTAL VALIDATION
                // =========================
                val validationKotItems = kotItemDao
                    .getDoneItemsForTableOnce(tableId)


                val validationItemSubtotalPaise = validationKotItems.sumOf { item ->

                    val modifierPrice =
                        ModifierJsonHelper.fromJson(item.modifiersJson)
                            .flatMap { group -> group.items }
                            .sumOf { modifier -> modifier.price }

                    val basePlusModifier =
                        item.basePrice + modifierPrice

                    MoneyUtils.toPaise(basePlusModifier) * item.quantity
                }

                val firstTotalQuantity = kotItems.sumOf { it.quantity }
                val validationTotalQuantity = validationKotItems.sumOf { it.quantity }



                if (
                    itemSubtotalPaise != validationItemSubtotalPaise ||
                    firstTotalQuantity != validationTotalQuantity
                ) {
                    sendEvent("Something went wrong. Please try again.")
                    return@launch
                }

                val safeDiscountPaise =
                    calculation.discountPaise
                val loyaltyDiscountPaise = calculation.loyaltyDiscountPaise
                Log.d(
                    "TEXT_PRINT",
                    "Loyal PT $loyaltyDiscountPaise"
                )
                val deliveryFeePaise = calculation.deliveryFeePaise

                val deliveryTaxPaise = calculation.deliveryTaxPaise

                val totalTaxPaise = calculation.totalTaxPaise

                val taxableAmountPaise = calculation.taxableAmountPaise
                val roundOffPaise = calculation.roundOffPaise

                val grandTotalPaise = calculation.grandTotalPaise





                val finalizedById = PosSessionManager.getUserId(app) ?: ""
                val finalizedByName = PosSessionManager.getFullName(app) ?: ""
                val stewardName = kotItems
                    .mapNotNull { it.createdByName }
                    .firstOrNull { it.isNotBlank() } ?: "NONAME"

                val stewardId = kotItems
                    .mapNotNull { it.createdById }
                    .firstOrNull { it.isNotBlank() } ?: ""
                // =========================
                // ORDER MASTER (MINIMAL)
                // =========================
                val orderMaster = PosOrderMasterEntity(
                    id = orderId,
                    srno = srNo,
                    orderType = orderType,
                    tableNo = tableName,
                    customerName = "Customer",
                    customerPhone = "",
                    customerId = null,

                    itemTotal = MoneyUtils.fromPaise(itemSubtotalPaise),

                    deliveryFee = _deliveryFee.value,
                    deliveryTax = MoneyUtils.fromPaise(deliveryTaxPaise),

                    itemTax = MoneyUtils.fromPaise(
                        calculation.exclusiveTaxPaise +
                                calculation.inclusiveTaxPaise
                    ),
                    taxTotal = MoneyUtils.fromPaise(totalTaxPaise),

                    discountTotal = MoneyUtils.fromPaise(safeDiscountPaise),
                    loyaltyDiscountPaise =MoneyUtils.fromPaise(loyaltyDiscountPaise),
                    roundOff =  MoneyUtils.fromPaise(roundOffPaise),
                    taxableAmount =  MoneyUtils.fromPaise(taxableAmountPaise),

                    grandTotal = MoneyUtils.fromPaise(grandTotalPaise),

                    paymentMode = "NONE",
                    paymentStatus = "UNPAID",
                    paidAmount = 0.0,
                    dueAmount = MoneyUtils.fromPaise(grandTotalPaise),

                    orderStatus = "PRINTED",

                    deviceId = "POS",
                    deviceName = "POS",
                    appVersion = "1.0",

                    createdById =stewardId,
                    createdByName=stewardName,
                    finalizedById= finalizedById,
                    finalizedByName=finalizedByName,
                    orderDate=orderDate,
                    businessDate = businessDate,
                    createdAt = now,
                    updatedAt = now,

                    syncStatus = "PENDING",
                    lastSyncedAt = null,
                    notes = null
                )



                val orderItems = kotItems
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

                        val basePlusModifier =
                            first.basePrice + modifierPricePerItem

                        val itemGrossAmount =
                            (basePlusModifier * quantity).round(2)

                        val taxPerItemPlusModifier =
                            if (first.taxType == "exclusive") {
                                basePlusModifier * (first.taxRate / 100)
                            } else {
                                0.0
                            }

                        val finalPricePerItemPlusModifier =
                            (basePlusModifier + taxPerItemPlusModifier).round(2)

                        val finalPriceTotalItemPlusModifier =
                            (finalPricePerItemPlusModifier * quantity).round(2)

                        val taxTotalItemPlusModifier =
                            (taxPerItemPlusModifier * quantity).round(2)

                        val modifierTotal =
                            (modifierPricePerItem * quantity).round(2)

                        PosOrderItemEntity(
                            id = UUID.randomUUID().toString(),

                            categoryName = first.categoryName,
                            productMode = first.productMode,
                            currentStock = first.currentStock,
                            categoryId = first.categoryId,
                            parentId = first.parentId,
                            isVariant = first.isVariant,

                            createdById = first.createdById,
                            createdByName = first.createdByName,

                            orderMasterId = orderId,

                            productId = first.productId,
                            name = first.name,

                            basePrice = first.basePrice,
                            modifierPrice = modifierTotal,

                            quantity = quantity,

                            itemSubtotal = itemGrossAmount,

                            currency = _currencySymbol.value,
                            status = first.status,

                            taxRate = first.taxRate,
                            taxType = first.taxType,

                            taxAmountPerItem = taxPerItemPlusModifier,
                            taxTotal = taxTotalItemPlusModifier,

                            note = first.note,
                            modifiersJson = first.modifiersJson,

                            finalPricePerItem = finalPricePerItemPlusModifier,
                            finalTotal = finalPriceTotalItemPlusModifier,

                            createdAt = now
                        )
                    }

                // =========================
                // SAVE (optional but good)
                // =========================
                withContext(Dispatchers.IO) {
                  //  orderMasterDao.insert(orderMaster)
                  //  orderProductDao.insertAll(orderItems)
                }

                // =========================
                // PRINT
                // =========================
              printOrder(orderMaster, orderItems, kotNumberText, stewardName,referenceId = orderId, earnedPoints = earnedPoints)

              //  sendEvent("Printed successfully")

            } catch (e: Exception) {

                Log.e("PRINT_ERROR", "Print failed", e)
                sendEvent("Print failed")

            } finally {
                _isProcessing.value = false
                if (paymentMutex.isLocked) paymentMutex.unlock()
            }
        }
    }






    fun complimentaryOrder(
        payments: List < PaymentInput >,
        name: String,
        phone: String,
        reason: String,   // DRIVER, FAMILY, FRIEND, STAFF, OTHER
        note: String ? = null,

        ) {
        Log.d("PAY_DEBUG", "creditPaise BEFORE payment = ${_creditPaise.value}")
        val creditPaiseInput = _creditPaise.value
        val paidModes = setOf("CASH", "CARD", "UPI", "WALLET")


        val hasPaid = payments.any { it.mode in paidModes }
        val hasUnpaid = payments.any { it.mode in unpaidModes }



        viewModelScope.launch {

            if (!paymentMutex.tryLock()) {
                sendEvent("Payment already in progress")
                return@launch
            }
            _isProcessing.value = true

            val outlet = outletDao.getOutlet()

            if (outlet == null) {

                _toastEvent.emit("Outlet not configured")
                _isProcessing.value = false
                return@launch
            }


            lateinit var fiscalService: FiscalService
            lateinit var fiscalContext: FiscalContext

            try {

                val inputPhone = phone.trim()
                val inputName = name.trim().ifBlank { "Customer" }

                val kotItems = kotItemDao
                    .getDoneItemsForTableOnce(tableId)


                kotItems.forEach {
                    Log.d(
                        "CREATED_BY",
                        "Item=${it.name}, createdById=${it.createdById}, createdByName=${it.createdByName}"
                    )
                }
                if (kotItems.isEmpty()) {
                    sendEvent("No items to bill")
                    return@launch
                }

                val now = System.currentTimeMillis()
                val orderDate = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(Date(now))
                val currentBusinessDay =
                    businessDayRepository.getCurrentBusinessDay()

                val businessDate = currentBusinessDay.businessDate
                val orderId = pendingOrderId ?: UUID.randomUUID().toString()
                pendingOrderId = orderId

//================================
// BILL CALCULATION
//================================
                val calculation = BillCalculator.calculate(
                    items = kotItems,
                    taxMode = outlet.taxMode,
                    discountFlat = _discountFlat.value,
                    discountPercent = _discountPercent.value,
                    deliveryFee = _deliveryFee.value,
                    deliveryTaxPercent = _deliveryTaxPercent.value
                )

                val itemSubtotalPaise = calculation.itemSubtotalPaise

                val itemTaxPaise =
                    calculation.exclusiveTaxPaise +
                            calculation.inclusiveTaxPaise

                val safeDiscountPaise = calculation.discountPaise

                val deliveryFeePaise = calculation.deliveryFeePaise

                val deliveryTaxPaise = calculation.deliveryTaxPaise

                val totalTaxPaise = calculation.totalTaxPaise

                val grandTotalPaise = calculation.grandTotalPaise
                //=======================================================================
                // ===========================
                // PAYMENT CALCULATION
                // ===========================
                val totalPaidPaise = payments
                    .filter { it.mode in paidModes }
                    .sumOf { it.amount }

// 👇 FIRST define paymentStatus
                val hasCredit = creditPaiseInput > 0

                val paymentStatus = when {

                    payments.any { it.mode == "WAITER_PENDING" } -> "WAITER_PENDING"

                    payments.any { it.mode == "DELIVERY_PENDING" } -> "DELIVERY_PENDING"

                    hasPaid && hasCredit -> "PARTIAL"

                    hasCredit -> "CREDIT"

                    else -> "PAID"
                }


// 👇 THEN use it
                val duePaise = creditPaiseInput

                val dueAmount = BigDecimal.valueOf(creditPaiseInput, 2)
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .toDouble()

                // ===========================
                // PHONE VALIDATION
                // ===========================

                if (paymentStatus in listOf("CREDIT", "PARTIAL") && inputPhone.isBlank()) {
                    sendEvent("Phone required for credit sale")
                    return@launch
                }

                // ===========================
                // ENSURE CUSTOMER EXISTS (IF PHONE ENTERED)
                // ===========================

                var resolvedCustomerId: String?= null

                if (inputPhone.isNotBlank()) {

                    resolvedCustomerId = inputPhone

                    val existingCustomer = customerDao.getCustomerByPhone(inputPhone)

                    if (existingCustomer == null) {

                        val customer = PosCustomerEntity(
                            id = inputPhone,
                            ownerId = outlet.ownerId,
                            outletId = outlet.outletId,
                            name = inputName,
                            phone = inputPhone,
                            addressLine1 = null,
                            addressLine2 = null,
                            city = null,
                            state = null,
                            zipcode = null,
                            landmark = null,
                            creditLimit = 0.0,
                            currentDue = 0.0,   // 🔥 important
                            createdAt = now,
                            updatedAt = null
                        )

                        customerDao.insert(customer)
                    }
                }
                // ===========================
                // CUSTOMER CREDIT HANDLING
                // ===========================

                if (paymentStatus == "CREDIT" || paymentStatus == "PARTIAL") {

                    val cleanPhone = inputPhone.trim()
                    resolvedCustomerId = cleanPhone

                    val creditToAdd = duePaise / 100.0

                    Log.d("LEDGER_DEBUG", "Running DB block")

                    val existingCustomer = customerDao.getCustomerByPhone(cleanPhone)

                    if (existingCustomer == null) {
                        Log.e("CREDIT", "❌ Customer NOT FOUND for phone = $cleanPhone")
                        return@launch
                    }


                    // ✅ FIX: use ID (NOT phone)
                    customerDao.increaseDue(existingCustomer.id, creditToAdd)

                    val updatedCustomer = customerDao.getCustomerById(existingCustomer.id)

                    val lastBalance = ledgerDao.getLastBalance(existingCustomer.id) ?: 0.0
                    val newBalance = lastBalance + creditToAdd

                    val ledgerEntry = PosCustomerLedgerEntity(
                        id = UUID.randomUUID().toString(),
                        ownerId = outlet.ownerId,
                        outletId = outlet.outletId,
                        customerId = existingCustomer.id,   // ✅ FIX
                        orderId = orderId,
                        paymentId = null,
                        type = "ORDER",
                        debitAmount = creditToAdd,
                        creditAmount = 0.0,
                        balanceAfter = newBalance,
                        note = "Credit sale Order ",
                        createdAt = now,
                        deviceId = "POS"
                    )

                    ledgerDao.insert(ledgerEntry)

                }

                val paymentMode =
                    if (payments.size > 1) "MIXED"
                    else payments.firstOrNull()?.mode ?: "CREDIT"

                val finalizedById = PosSessionManager.getUserId(app) ?: ""
                val finalizedByName = PosSessionManager.getFullName(app) ?: ""
                val stewardName = kotItems
                    .mapNotNull { it.createdByName }
                    .firstOrNull { it.isNotBlank() } ?: "NONAME"

                val stewardId = kotItems
                    .mapNotNull { it.createdById }
                    .firstOrNull { it.isNotBlank() } ?: ""
                // ===========================
                // ORDER MASTER
                // ===========================
                //  val srNo =  orderSequenceRepository.getOrCreateOrderNo(tableId)


                val mapping = orderSequenceRepository.getOrCreateOrderNo(
                    mapKey = orderId,
                    deviceCode = "P1",
                    financialYear = getFinancialYearCode()
                )

                val srNo = mapping.srno

                val orderMaster = PosOrderMasterEntity(
                    id = orderId,
                    srno = srNo,
                    orderType = orderType,
                    saleType = "COMPLIMENTARY",
                    reason = reason,
                    tableNo = tableName,
                    customerName = inputName,
                    customerPhone = inputPhone,
                    customerId = resolvedCustomerId,

                    // keeping delivery address untouched
                    dAddressLine1 = deliveryAddress?.line1,
                    dAddressLine2 = deliveryAddress?.line2,
                    dCity = deliveryAddress?.city,
                    dState = deliveryAddress?.state,
                    dZipcode = deliveryAddress?.zipcode,
                    dLandmark = deliveryAddress?.landmark,

                    itemTotal = MoneyUtils.fromPaise(itemSubtotalPaise),

                    //taxTotal = MoneyUtils.fromPaise(taxTotalPaise),

                    // deliveryTax = ( _deliveryFee.value * _deliveryTaxPercent.value / 100.0 ),
                    deliveryFee = 0.0,
                    deliveryTax = 0.0,

                    itemTax = 0.0,
                    taxTotal = 0.0,
                    discountTotal = MoneyUtils.fromPaise(safeDiscountPaise),
                    //  grandTotal = grandTotal,
                    grandTotal = 0.0,

                    paymentMode = "FREE",
                    paymentStatus = "FREE",

                    paidAmount = 0.0,
                    dueAmount = 0.0,

                    orderStatus = "COMPLETED",

                    deviceId = "POS",
                    deviceName = "POS",
                    appVersion = "1.0",

                    createdById =stewardId,
                    createdByName=stewardName,
                    finalizedById= finalizedById,
                    finalizedByName= finalizedByName,
                    orderDate=orderDate,
                    businessDate = businessDate,
                    createdAt = now,
                    updatedAt = now,

                    syncStatus = "SYNCED",
                    lastSyncedAt = null,
                    notes = note
                )

                val orderItems = kotItems
                    .groupBy {
                        listOf(
                            it.productId,
                            it.basePrice,
                            it.taxRate,
                            it.note,
                            it.modifiersJson
                        )
                    }
                    .map {
                            (_, group) ->

                        val first = group.first()
                        val quantity = group.sumOf { it.quantity }

                        val modifierPricePerItem =
                            ModifierJsonHelper.fromJson(first.modifiersJson)
                                .flatMap { it.items }
                                .sumOf { it.price }
                        //   val itemGrossAmount =
                        //       ((first.basePrice + modifierPricePerItem) * quantity).round(2)

                        //     val basePlusModifier = first.basePrice + modifierPricePerItem

                        val basePlusModifier = first.basePrice + modifierPricePerItem

                        val itemGrossAmount = (basePlusModifier * quantity).round(2)


                        val taxPerItemPlusModifier =
                            if (first.taxType == "exclusive")
                                (basePlusModifier * (first.taxRate / 100))
                            else 0.0

                        val finalPricePerItemPlusModifier =
                            (basePlusModifier + taxPerItemPlusModifier).round(2)

                        val finalPriceTotalItemPlusModifier =
                            (finalPricePerItemPlusModifier * quantity).round(2)

                        val taxTotalItemPlusModifier =
                            (taxPerItemPlusModifier * quantity).round(2)

                        val modifierTotal =
                            (modifierPricePerItem * quantity).round(2)
                        Log.d("BASE_PRICE", "${first.basePrice}")

                        PosOrderItemEntity(
                            id = UUID.randomUUID().toString(),
                            // 🔹 SNAPSHOT CATEGORY NAME (enterprise safe)
                            categoryName = first.categoryName,
                            orderMasterId = orderId,
                            createdById = first.createdById,
                            createdByName = first.createdByName,
                            productId = first.productId,
                            name = first.name,
                            productMode = first.productMode,
                            currentStock = first.currentStock,
                            categoryId = first.categoryId,
                            parentId = first.parentId,
                            isVariant = first.isVariant,
                            basePrice = first.basePrice,
                            modifierPrice = modifierTotal,
                            quantity = quantity,
                            itemSubtotal = itemGrossAmount,
                            // 🔹 Currency snapshot (important for audit)
                            currency = _currencySymbol.value,
                            // 🔹 Payment snapshot (do NOT rely on join later)
                            status = "FREE",//first.status,
                            taxRate = first.taxRate,
                            taxType = first.taxType,
                            taxAmountPerItem = taxPerItemPlusModifier,
                            taxTotal = 0.0,
                            note = first.note,
                            modifiersJson = first.modifiersJson,
                            finalPricePerItem = 0.0,//finalPricePerItemPlusModifier,
                            finalTotal = 0.0,//finalPriceTotalItemPlusModifier,
                            createdAt = now
                        )
                    }


                fiscalService = getFiscalService("IN", fiskalyRepository)
                fiscalContext = withContext(Dispatchers.IO) {
                    fiscalService.start()
                }
                withContext(Dispatchers.IO) {
                    orderMasterDao.insert(orderMaster)
                    orderProductDao.insertAll(orderItems)


                    // UPDATE KOT STATUS PAID
//                     kotRepository.markHistoryPaid(
//                         tableNo = tableId,
//                         orderId = orderId
//                     )

                    kotRepository.markHistoryComplimentary(
                        tableNo = tableId,
                        orderId = orderId,
                        reason = reason
                    )

                    //MANAGE TABLE AFTER ORDER COMPLITE
                    repository.finalizeTableAfterPayment(
                        tableNo = tableId,
                        orderType = orderType
                    )

                    if (orderType == "TAKEAWAY" || orderType == "DELIVERY") {
                        val nextVirtualTable =
                            virtualTableRepository.markCompleted(
                                tableId = tableId,
                                tableName = tableName,
                                orderType = orderType
                            )

                        if (nextVirtualTable != null) {

                            Log.d(
                                "POS_ORDER",
                                "NEXT TABLE = ${nextVirtualTable.tableName}, id=${nextVirtualTable.id}"
                            )

                            posSessionViewModel.setNextVirtualTable(
                                tableId = nextVirtualTable.id,
                                tableName = nextVirtualTable.tableName,
                                orderType = nextVirtualTable.orderType
                            )


                        }
                    }


                    // ✅ 2. PRINT IMMEDIATELY
                    //  printOrder(orderMaster, orderItems)

                    // 🔥 FIRESTORE CLEAR
                    try {



                        // 1️⃣ DELETE LOCAL
                        kotRepository.deleteKotByTable(tableId)

                        //                        val remaining =
                        //                            kotItemDao.getItemsByTable(tableId).size
                        //
                        //                        Log.d(
                        //                            "TABLE_CLOSE",
                        //                            "After delete table=$tableId remaining=$remaining"
                        //        )

                        // 2️⃣ UPDATE FIRESTORE TO EMPTY
                        tableKotSyncService.clearTableSnapshot(tableId, source = "PAYMENT_CLEAR")

                        // 3️⃣ BACKUP QUEUE
                        SyncManagerProvider.get().addClearTable(tableId)
                        orderSequenceRepository.clearOrder(orderId)



                        Log.d("TABLE_SYNC", "✅ Table cleared after payment")
                    } catch (e: Exception) {
                        Log.e("TABLE_SYNC", "❌ Failed to clear table", e)
                    }
                }
                //FISKLAY CODE
                withContext(Dispatchers.IO) {
                    fiscalService.finish(
                        fiscalContext,
                        payments,
                        kotItems
                    )
                }

                sendEvent("Complimentary order saved")

                resetBillUi()
            } catch (e: Exception) {

                //                if (::fiscalService.isInitialized && ::fiscalContext.isInitialized) {
                //                    withContext(Dispatchers.IO) {
                //                        fiscalService.cancel(fiscalContext)
                //                    }
                //                }

                // if (!isFinished) {
                fiscalService.cancel(fiscalContext)
                // }

                Log.e("PAY_ERROR", "Payment failed", e)
                sendEvent("Payment failed")
            } finally {
                _isProcessing.value = false
                if (paymentMutex.isLocked) {
                    paymentMutex.unlock()
                }
            }

        }
    }




    fun cancelBill(
        payments: List<PaymentInput>,
        name: String,
        phone: String,
        cancellationReason: String,
    )
    {




        viewModelScope.launch {

            if (!paymentMutex.tryLock()) {
                sendEvent("Payment already in progress")
                return@launch
            }
            _isProcessing.value = true

            val outlet = outletDao.getOutlet()
            if (outlet == null) {

                _toastEvent.emit("Outlet not configured")
                _isProcessing.value = false
                return@launch
            }


            lateinit var fiscalService: FiscalService
            lateinit var fiscalContext: FiscalContext

            try {
                val inputPhone = phone.trim()
                val inputName = name.trim().ifBlank { "Customer" }
                val kotItems = kotItemDao.getDoneItemsForTableOnce(tableId)
                val orderDetailItems =  kotItemDao.getDoneAndDeletedItemsForOrderDetail(tableId)
                if (orderDetailItems.isEmpty()) {
                    sendEvent("No items to cancel")
                    return@launch
                }

                val now = System.currentTimeMillis()
                val orderDate = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(Date(now))
                val currentBusinessDay =
                    businessDayRepository.getCurrentBusinessDay()

                val businessDate = currentBusinessDay.businessDate
                val orderId = pendingOrderId ?: UUID.randomUUID().toString()
                pendingOrderId = orderId

                val mapping = orderSequenceRepository.getOrCreateOrderNo(
                    mapKey = orderId,
                    deviceCode = "P1",
                    financialYear = getFinancialYearCode()
                )

                val srNo = mapping.srno
                //================================
                // BILL CALCULATION
                //==================================
                val calculation = BillCalculator.calculate(
                    items = kotItems,
                    taxMode = outlet.taxMode,
                    discountFlat = _discountFlat.value,
                    discountPercent = _discountPercent.value,
                    deliveryFee = _deliveryFee.value,
                    deliveryTaxPercent = _deliveryTaxPercent.value
                )

                val itemSubtotalPaise = calculation.itemSubtotalPaise

                val validationKotItems = kotItemDao
                    .getDoneItemsForTableOnce(tableId)


                val validationItemSubtotalPaise = validationKotItems.sumOf { item ->

                    val modifierPrice =
                        ModifierJsonHelper.fromJson(item.modifiersJson)
                            .flatMap { group -> group.items }
                            .sumOf { modifier -> modifier.price }

                    val basePlusModifier =
                        item.basePrice + modifierPrice

                    MoneyUtils.toPaise(basePlusModifier) * item.quantity
                }

                val firstTotalQuantity = kotItems.sumOf { it.quantity }
                val validationTotalQuantity = validationKotItems.sumOf { it.quantity }

                if (
                    itemSubtotalPaise != validationItemSubtotalPaise ||
                    firstTotalQuantity != validationTotalQuantity
                ) {
                    withContext(Dispatchers.Main) {
                        sendEvent("Something went wrong. Please try again.")
                    }
                    return@launch
                }

                val safeDiscountPaise = calculation.discountPaise
                val exclusiveTaxPaise = calculation.exclusiveTaxPaise
                val inclusiveTaxPaise = calculation.inclusiveTaxPaise
// Total GST on items (used for saving/reporting)
                val itemTaxPaise = exclusiveTaxPaise + inclusiveTaxPaise
                val deliveryFeePaise = calculation.deliveryFeePaise
                val deliveryTaxPaise = calculation.deliveryTaxPaise
                val totalTaxPaise = calculation.totalTaxPaise
                val taxableAmountPaise = calculation.taxableAmountPaise
                val roundOffPaise = calculation.roundOffPaise
                val grandTotalPaise = calculation.grandTotalPaise

                // ===========================
                // PAYMENT CALCULATION
                // ===========================
                // ===========================
// PAYMENT CALCULATION
// ===========================


// 👇 FIRST define paymentStatus









// ===========================
// ENSURE CUSTOMER EXISTS (IF PHONE ENTERED)
// ===========================

                var resolvedCustomerId: String? = null

                if (inputPhone.isNotBlank()) {

                    resolvedCustomerId = inputPhone

                    val existingCustomer = customerDao.getCustomerByPhone(inputPhone)

                    if (existingCustomer == null) {

                        val customer = PosCustomerEntity(
                            id = inputPhone,
                            ownerId = outlet.ownerId,
                            outletId = outlet.outletId,
                            name = inputName,
                            phone = inputPhone,
                            addressLine1 = null,
                            addressLine2 = null,
                            city = null,
                            state = null,
                            zipcode = null,
                            landmark = null,
                            creditLimit = 0.0,
                            currentDue = 0.0,   // 🔥 important
                            createdAt = now,
                            updatedAt = null
                        )

                        customerDao.insert(customer)
                    }
                }
                // ===========================
                // CUSTOMER CREDIT HANDLING
                // ===========================



                val paymentMode = "NON"
//                    if (payments.size > 1) "MIXED"
//                    else payments.firstOrNull()?.mode ?: "CREDIT"

                val finalizedById = PosSessionManager.getUserId(app) ?: ""
                val finalizedByName = PosSessionManager.getFullName(app) ?: ""

                val stewardName = kotItems
                    .mapNotNull { it.createdByName }
                    .firstOrNull { it.isNotBlank() } ?: "NONAME"

                val stewardId = kotItems
                    .mapNotNull { it.createdById }
                    .firstOrNull { it.isNotBlank() } ?: ""
                // ===========================
                // ORDER MASTER
                // ===========================
                Log.d(
                    "ORDER_DETAIL",
                    "INSERT OREDER in billviewmodel orderType=${orderType}, TableName=${tableName} ,TableId=${tableId}"
                )

                val orderMaster = PosOrderMasterEntity(
                    id = orderId,
                    srno = srNo,
                    orderType = orderType,
                    tableNo = tableName,
                    customerName = inputName,
                    customerPhone = inputPhone,
                    customerId = resolvedCustomerId,
                    dAddressLine1 = deliveryAddress?.line1,
                    dAddressLine2 = deliveryAddress?.line2,
                    dCity = deliveryAddress?.city,
                    dState = deliveryAddress?.state,
                    dZipcode = deliveryAddress?.zipcode,
                    dLandmark = deliveryAddress?.landmark,
                    itemTotal = MoneyUtils.fromPaise(itemSubtotalPaise),
                    deliveryFee = 0.0,//_deliveryFee.value,
                    deliveryTax = 0.0,//MoneyUtils.fromPaise(deliveryTaxPaise),
                    itemTax = 0.0,//MoneyUtils.fromPaise(itemTaxPaise),
                    taxTotal = 0.0,//MoneyUtils.fromPaise(totalTaxPaise),
                    discountTotal = 0.0,// MoneyUtils.fromPaise(safeDiscountPaise),
                    grandTotal = 0.0,//MoneyUtils.fromPaise(grandTotalPaise),
                    paymentMode = "CANCEL",
                    paymentStatus = "CANCELLED",
                    paidAmount = 0.0,
                    dueAmount = 0.0,
                    orderStatus = "CANCELLED",
                    deviceId = "POS",
                    deviceName = "POS",
                    appVersion = "1.0",
                    createdById =stewardId,
                    createdByName=stewardName,
                    finalizedById=finalizedById,
                    finalizedByName=finalizedByName,
                    orderDate=orderDate,
                    businessDate = businessDate,
                    createdAt = now,
                    updatedAt = now,
                    syncStatus =  "PENDING",
                    lastSyncedAt = null,
                    reason = cancellationReason,
                    notes = "Bill Cancelled by $stewardName"
                )


                val orderItems = orderDetailItems
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
                        //   val itemGrossAmount =
                        //       ((first.basePrice + modifierPricePerItem) * quantity).round(2)
                        //     val basePlusModifier = first.basePrice + modifierPricePerItem
                        val basePlusModifier = first.basePrice + modifierPricePerItem
                        val itemGrossAmount = (basePlusModifier * quantity).round(2)
                        val taxPerItemPlusModifier =
                            if (first.taxType == "exclusive")
                                (basePlusModifier * (first.taxRate / 100))
                            else 0.0
                        val finalPricePerItemPlusModifier =
                            (basePlusModifier + taxPerItemPlusModifier).round(2)

                        val finalPriceTotalItemPlusModifier =
                            (finalPricePerItemPlusModifier * quantity).round(2)

                        val taxTotalItemPlusModifier =
                            (taxPerItemPlusModifier * quantity).round(2)

                        val modifierTotal =
                            (modifierPricePerItem * quantity).round(2)
                        Log.d("BASE_PRICE", "${first.basePrice}")

                        PosOrderItemEntity(
                            id = UUID.randomUUID().toString(),
                            // 🔹 SNAPSHOT CATEGORY NAME (enterprise safe)
                            categoryName = first.categoryName,
                            orderMasterId = orderId,
                            productId = first.productId,
                            name = first.name,
                            productMode = first.productMode,
                            currentStock = first.currentStock,
                            categoryId = first.categoryId,
                            createdById =first.createdById,
                            createdByName=first.createdByName,
                            parentId = first.parentId,
                            isVariant = first.isVariant,
                            basePrice = first.basePrice,
                            modifierPrice = modifierTotal,
                            quantity = quantity,
                            itemSubtotal = 0.0,//itemGrossAmount ,
                            // 🔹 Currency snapshot (important for audit)
                            currency = _currencySymbol.value,
                            // 🔹 Payment snapshot (do NOT rely on join later)
                            status = "DELETED",
                            taxRate = first.taxRate,
                            taxType = first.taxType,
                            taxAmountPerItem = taxPerItemPlusModifier,
                            taxTotal = taxTotalItemPlusModifier,
                            note = "invoice canceled",//first.note,
                            modifiersJson = first.modifiersJson,
                            finalPricePerItem = finalPricePerItemPlusModifier,
                            finalTotal = finalPriceTotalItemPlusModifier,
                            createdAt = now
                        )
                    }

                // =====================================================
// ORDER ITEM SUBTOTAL VALIDATION
// =====================================================

                val kotSubtotalPaise = kotItems.sumOf { item ->
                    MoneyUtils.toPaise(item.basePrice) * item.quantity
                }

                val orderItemsSubtotalPaise = orderItems.sumOf { item ->
                    MoneyUtils.toPaise(item.itemSubtotal)
                }


                // val (txId, clientId) = fiskalyRepository.startTransaction()
                //fiscalService = getFiscalService(outlet.countryName!!, fiskalyRepository)
                fiscalService = getFiscalService("IN", fiskalyRepository)

                withContext(Dispatchers.IO) {

                    db.withTransaction {

                        // ==========================================
                        // 1. READ CURRENT DONE KOT ITEMS
                        // ==========================================

                        val currentKotItems = kotItemDao
                            .getDoneItemsForTableOnce(tableId)


                        if (currentKotItems.isEmpty()) {
                            throw IllegalStateException("No items to bill")
                        }

                        // ==========================================
                        // 2. VALIDATE CURRENT KOT DATA
                        // ==========================================

                        val currentItemSubtotalPaise = 0.0;
//                            currentKotItems.sumOf { item ->
//
//                                val modifierPrice =
//                                    ModifierJsonHelper.fromJson(item.modifiersJson)
//                                        .flatMap { group -> group.items }
//                                        .sumOf { modifier -> modifier.price }
//
//                                val basePlusModifier =
//                                    item.basePrice + modifierPrice
//
//                                MoneyUtils.toPaise(basePlusModifier) * item.quantity
//                            }

                        val currentTotalQuantity =
                            currentKotItems.sumOf {
                                it.quantity
                            }

//                        if (
//                            currentItemSubtotalPaise != itemSubtotalPaise ||
//                            currentTotalQuantity != firstTotalQuantity
//                        ) {
//                            throw IllegalStateException("Bill data changed")
//                        }

                        // ==========================================
                        // 3. SAVE ORDER MASTER
                        // ==========================================

                        orderMasterDao.insert(orderMaster)

                        // ==========================================
                        // 4. VERIFY SAVED ORDER MASTER
                        // ==========================================

                        val savedOrder =
                            orderMasterDao.getByIdSync(orderId)

                        if (
                            savedOrder == null ||
                            MoneyUtils.toPaise(savedOrder.itemTotal) !=
                            itemSubtotalPaise
                        ) {
                            throw IllegalStateException("Order validation failed")
                        }

                        // ==========================================
                        // 5. SAVE ORDER ITEMS
                        // ==========================================

                        orderProductDao.insertAll(orderItems)

                        // ==========================================
                        // 6. SAVE PAYMENTS
                        // ==========================================

//                        if (payments.isNotEmpty() && totalPaidPaise > 0) {
//
//                            val paymentEntities = payments.map {
//                                PosOrderPaymentEntity(
//                                    id = UUID.randomUUID().toString(),
//                                    orderId = orderId,
//                                    ownerId = outlet.ownerId,
//                                    outletId = outlet.outletId,
//                                    amount = MoneyUtils.fromPaise(it.amount),
//                                    mode = it.mode,
//                                    provider = null,
//                                    method = null,
//                                    status = "SUCCESS",
//                                    deviceId = "POS",
//                                    createdAt = now,
//                                    businessDate = businessDate,
//                                    syncStatus = "PENDING"
//                                )
//                            }
//
//                            paymentRepository.insertPayments(
//                                paymentEntities
//                            )
//                        }

                        // ==========================================
                        // 7. UPDATE KOT HISTORY
                        // ==========================================

                        kotRepository.markHistoryPaid(
                            tableNo = tableId,
                            orderId = orderId
                        )

                        // ==========================================
                        // 8. FINALIZE TABLE
                        // ==========================================

                        repository.finalizeTableAfterPayment(
                            tableNo = tableId,
                            orderType = orderType
                        )

                        // ==========================================
                        // 9. DELETE LOCAL KOT
                        // ==========================================

                        kotRepository.deleteKotByTable(
                            tableId
                        )

                        // ==========================================
                        // 10. CLEAR ORDER SEQUENCE
                        // ==========================================

                        orderSequenceRepository.clearOrder(
                            orderId
                        )
                    } // END TRANSACTION

                    // ==============================================
                    // ROOM TRANSACTION SUCCESSFULLY COMMITTED
                    // ==============================================

                    if (
                        orderType == "TAKEAWAY" ||
                        orderType == "DELIVERY"
                    ) {

                        val nextVirtualTable =
                            virtualTableRepository.markCompleted(
                                tableId = tableId,
                                tableName = tableName,
                                orderType = orderType
                            )

                        if (nextVirtualTable != null) {

                            posSessionViewModel.setNextVirtualTable(
                                tableId = nextVirtualTable.id,
                                tableName = nextVirtualTable.tableName,
                                orderType = nextVirtualTable.orderType
                            )
                        }
                    }

                    // ==============================================
                    // FIRESTORE
                    // ==============================================

                    tableKotSyncService.clearTableSnapshot(
                        tableId,
                        source = "PAYMENT_CLEAR"
                    )

                    SyncManagerProvider.get()
                        .addClearTable(tableId)
                }


                //FISKLAY CODE


                sendEvent("Bill cancelled")
                pendingOrderId = null
                resetBillUi()
            } catch (e: Exception) {

//                if (::fiscalService.isInitialized && ::fiscalContext.isInitialized) {
//                    withContext(Dispatchers.IO) {
//                        fiscalService.cancel(fiscalContext)
//                    }
//                }

                // if (!isFinished) {
                fiscalService.cancel(fiscalContext)
                // }

                Log.e("CANCEL_ERROR", "Cancel failed", e)
                sendEvent("Cancel failed")
            }finally {
                _isProcessing.value = false
                if (paymentMutex.isLocked) {
                    paymentMutex.unlock()
                }
            }

        }
    }
    fun Double.round(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return kotlin.math.round(this * factor) / factor
    }
    fun deleteItem(
                   itemId: String,
                   reason: String
                   ) {
        viewModelScope.launch {
            try {
                //Mark Deleted
                kotRepository.markHistoryDeleted(
                    itemId = itemId,
                    reason = reason
                )
                // 1️⃣ Delete from KOT
                // kotItemDao.deleteItemById(itemId)

                val originalItem = kotItemDao.getItemById(itemId)
                val deletionNote = buildString {
                    append(reason)
                    append(" | Original Price: ")
                    append(originalItem?.basePrice ?: 0.0)
                }
                kotItemDao.markItemDeleted(
                    itemId = itemId,
                    reason = deletionNote
                )

                // 2️⃣ Update real table bill counters
                kotRepository.syncBillCount(tableId)

                // 3️⃣ Sync to correct table (DINE_IN / TAKEAWAY / DELIVERY)
                tableSyncManager.syncCart(tableId, orderType)
                tableSyncManager.syncBill(tableId, orderType)

                //  NEW: FIRESTORE TABLE SNAPSHOT SYNC (IMPORTANT)
                try {
                    //**********************************************************
                    //  UPDATE WAITER VIEW STATUS
                    //**********************************************************
//                        tableKotSyncService.syncTableSnapshot(
//                            tableId = tableId,
//                            source = "POS"
//                        )

                    //**********************************************************
                    // INSTEAD WE WILL USE QUEUE TO UPDATE WAITER VIEW STATUS
                    //**********************************************************
                    SyncManagerProvider.get().addTableUpdate(tableId)

                } catch (e: Exception) {
                    Log.e("TABLE_SYNC", "Failed to trigger snapshot sync", e)
                }

            } catch (e: Exception) {
                Log.e("DELETE", "Failed to delete item", e)
            }
        }
    }



    // --------------------------------------------------------
    // Set Delivery Address
    // --------------------------------------------------------
    fun setDeliveryAddress(address: DeliveryAddressUiState) {
        _deliveryAddress.value = address
    }

    // --------------------------------------------------------
    // Printing (Unified print pipeline)
    // --------------------------------------------------------
    private suspend fun printOrder(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>,
        kotNumberText: String="",
        stewardName: String= "",
        referenceId: String,
        earnedPoints: Int = 0,

    ) = withContext(Dispatchers.IO) {



        val outlet = outletDao.getOutlet()
        if (outlet == null) {
            withContext(Dispatchers.Main) {
                sendEvent("Please configure outlet first")
            }
            return@withContext
        }
        val outletInfo = OutletMapper.fromEntity(outlet)

        val printOrder = PrintOrderBuilder.build(
            master = order,
            items = items,
            outlet = outletInfo
        )


        val Pritnter_role = "BILLING"
        val printMode = prefs.getReceiptPrintMode(
            PrinterRole.valueOf(Pritnter_role)
        )

        when (printMode) {

            ReceiptPrintMode.TEXT -> {

                printerManager.enqueueBill(
                    printOrder,
                    order.paymentMode,
                    outletInfo,
                    kotNumberText = kotNumberText,
                    stewardName = stewardName,
                    referenceId,
                    earnedPoints,
                )
            }

            ReceiptPrintMode.IMAGE -> {

                printerManager.enqueueBillImage(
                    order = printOrder,
                    paymentMode = order.paymentMode,
                    outletInfo = outletInfo,
                    kotNumberText = kotNumberText,
                    stewardName = stewardName,
                    referenceId = referenceId,
                    earnedPoints = earnedPoints,
                )
            }
        }

       // printerManager.enqueueBill(printOrder,order.paymentMode, outletInfo,referenceId,)
//         printerManager.enqueueBillImage(
//            order = printOrder,
//            paymentMode = order.paymentMode,
//            outletInfo = outletInfo,
//            kotNumberText,
//            stewardName,
//             referenceId,
//        )

          }

    fun getDoneItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {
        return kotItemDao.getDoneItemsForTable(orderRef)
    }


    // file: BillViewModel.kt (inside the class)
    fun updateItemQuantity(
        itemId: String,
        newQty: Int,
        reason: String = ""
    ) {
        viewModelScope.launch {
            val qty = newQty.coerceAtLeast(0)

            val targetUi = _uiState.value.items
                .find { it.id == itemId }
                ?: return@launch

            val allItems = kotItemDao.getDoneItemsForTableOnce(tableId)

            val groupedItems = allItems.filter {
                it.productId == targetUi.productId &&
                        it.basePrice == targetUi.basePrice &&
                        it.taxRate == targetUi.taxRate &&
                        (it.note ?: "") == targetUi.note &&
                        (it.modifiersJson ?: "") == targetUi.modifiersJson &&
                        it.status == "DONE"
            }

            if (groupedItems.isEmpty()) return@launch

            val currentQty = groupedItems.sumOf { it.quantity }

            if (qty == currentQty) return@launch

            // Increasing quantity: keep existing rows and add only the difference.
            if (qty > currentQty) {
                val template = groupedItems.first()

                kotItemDao.insert(
                    template.copy(
                        id = UUID.randomUUID().toString(),
                        quantity = qty - currentQty
                    )
                )
            } else {
                // Decreasing quantity: a reason is required.
                if (reason.isBlank()) {
                    Log.d("EDIT_DEBUG", "Cancellation reason required")
                    return@launch
                }

                var remainingToCancel = currentQty - qty

                // Cancel portions from the newest KOT rows first.
                for (item in groupedItems.sortedByDescending { it.createdAt }) {
                    if (remainingToCancel <= 0) break

                    if (item.quantity <= remainingToCancel) {
                        // Entire row cancelled: preserve it as DELETED.
                        kotItemDao.markItemDeleted(
                            itemId = item.id,
                            reason = reason.trim()
                        )

                        remainingToCancel -= item.quantity
                    } else {
                        // Partial row cancellation:
                        // reduce original row and create a DELETED record
                        // for the cancelled portion.
                        val cancelledQty = remainingToCancel

                        kotItemDao.updateQuantity(
                            id = item.id,
                            qty = item.quantity - cancelledQty
                        )

                        kotItemDao.insert(
                            item.copy(
                                id = UUID.randomUUID().toString(),
                                quantity = cancelledQty,
                                status = "DELETED",
                                note = reason.trim(),
                                finalPrice = 0.0,
                                basePrice = 0.0,
                                syncedToCloud = false
                            )
                        )

                        remainingToCancel = 0
                    }
                }

                if (remainingToCancel > 0) {
                    Log.e(
                        "EDIT_DEBUG",
                        "Could not cancel requested quantity: $remainingToCancel"
                    )
                    return@launch
                }
            }

            // Firestore table snapshot sync
            try {
                tableKotSyncService.syncTableSnapshot(
                    tableId = tableId,
                    source = "POS"
                )

                // 2️⃣ Update real table bill counters
                kotRepository.syncBillCount(tableId)
                // 3️⃣ Sync to correct table (DINE_IN / TAKEAWAY / DELIVERY)
                tableSyncManager.syncBill(tableId, orderType)


            } catch (e: Exception) {
                Log.e("TABLE_SYNC", "Failed to trigger snapshot sync", e)
            }
        }
    }


    fun moveItemQuantity(
        itemId: String,
        moveQty: Int,
        destinationTableId: String,
        destinationTableName: String
    ) {
        viewModelScope.launch {
            val qtyToMove = moveQty.coerceAtLeast(0)

            if (qtyToMove == 0) return@launch

            if (destinationTableId == tableId) {
                Log.w("MOVE_ITEM", "Source and destination are the same")
                return@launch
            }

            val targetUi = _uiState.value.items
                .find { it.id == itemId }
                ?: return@launch

            val allItems = kotItemDao.getDoneItemsForTableOnce(tableId)

            // Find the database rows represented by this bill item.
            val groupedItems = allItems.filter {
                it.productId == targetUi.productId &&
                        it.basePrice == targetUi.basePrice &&
                        it.taxRate == targetUi.taxRate &&
                        it.note == targetUi.note &&
                        it.modifiersJson == targetUi.modifiersJson &&
                        it.status == "DONE"
            }

            if (groupedItems.isEmpty()) return@launch

            val currentQty = groupedItems.sumOf { it.quantity }

            if (qtyToMove > currentQty) {
                Log.w(
                    "MOVE_ITEM",
                    "Cannot move $qtyToMove units; only $currentQty available"
                )
                return@launch
            }

            var remainingToMove = qtyToMove

            // Move from the newest matching rows first.
            for (item in groupedItems.sortedByDescending { it.createdAt }) {
                if (remainingToMove <= 0) break

                if (item.quantity <= remainingToMove) {
                    // The complete row moves to the destination.
                    kotItemDao.moveItemToTable(
                        itemId = item.id,
                        newTableId = destinationTableId,
                        newTableName = destinationTableName
                    )

                    remainingToMove -= item.quantity
                } else {
                    // Split the row: leave the remainder on the source.
                    val movedItem = item.copy(
                        id = UUID.randomUUID().toString(),
                        tableNo = destinationTableId,
                        tableName = destinationTableName,
                        quantity = remainingToMove,
                        syncedToCloud = false,
                        syncedFromCloud = false
                    )

                    kotItemDao.insert(movedItem)

                    kotItemDao.updateQuantity(
                        id = item.id,
                        qty = item.quantity - remainingToMove
                    )

                    remainingToMove = 0
                }
            }

            if (remainingToMove > 0) {
                Log.e(
                    "MOVE_ITEM",
                    "Could not move requested quantity: $remainingToMove"
                )
                return@launch
            }

            // Refresh both tables in Firestore.
            try {
                tableKotSyncService.syncTableSnapshot(
                    tableId = tableId,
                    source = "POS"
                )

                tableKotSyncService.syncTableSnapshot(
                    tableId = destinationTableId,
                    source = "POS"
                )

                // 2️⃣ Update real table bill counters
                kotRepository.syncBillCount(tableId)
                kotRepository.syncBillCount(destinationTableId)
                // 3️⃣ Sync to correct table (DINE_IN / TAKEAWAY / DELIVERY)
                tableSyncManager.syncBill(tableId, orderType)
                tableSyncManager.syncBill(destinationTableId, orderType)


            } catch (e: Exception) {
                Log.e("MOVE_ITEM", "Table snapshot sync failed", e)
            }
        }
    }

    private var searchJob: Job? = null

    fun observeCustomerSuggestions(phone: String) {

        if (phone.length < 3) {
            _customerSuggestions.value = emptyList()
            searchJob?.cancel()
            return
        }

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            customerDao.searchCustomersByPhone(phone)
                .collectLatest { result ->
                    _customerSuggestions.value = result
                   // Log.d("SUGGEST", "Found: ${result.size}")
                }
        }
    }



    fun clearCustomerSuggestions() {
        _customerSuggestions.value = emptyList()
    }


    private fun getFinancialYearCode(): String {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        val startYear: Int
        val endYear: Int

        if (month >= 4) {
            startYear = year
            endYear = year + 1
        } else {
            startYear = year - 1
            endYear = year
        }

        return "${startYear % 100}${endYear % 100}"
    }

    private fun initializeInvoiceCounter() {

        if (invoiceCounterInitialized) return


        viewModelScope.launch {

            try {

                invoiceCounterRepository
                    .syncCounterFromFirestore()


                invoiceCounterInitialized = true


                Log.d(
                    "INVOICE_COUNTER",
                    "Invoice counter initialized successfully"
                )


            } catch (e: Exception) {


                Log.e(
                    "INVOICE_COUNTER",
                    "Invoice counter initialization failed",
                    e
                )

            }

        }
    }

}
data class BillCombine(
    val items: List<PosKotItemEntity>,
    val flat: Double,
    val percent: Double,
    val deliveryFee: Double,
    val deliveryTaxPercent: Double?
)