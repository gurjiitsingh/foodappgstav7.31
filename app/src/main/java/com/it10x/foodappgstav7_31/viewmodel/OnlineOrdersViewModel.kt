package com.it10x.foodappgstav7_31.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.mapper.OnlineOrderMapper
import com.it10x.foodappgstav7_31.data.mapper.toOrderMasterData
import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderDao
import com.it10x.foodappgstav7_31.data.online.models.CategorySaleData
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.online.models.OrderProductData
import com.it10x.foodappgstav7_31.data.online.repository.OrdersRepository
import com.it10x.foodappgstav7_31.printer.FirestorePrintMapper
import com.it10x.foodappgstav7_31.printer.PrinterManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlinx.coroutines.launch
class OnlineOrdersViewModel(
    private val printerManager: PrinterManager
) : ViewModel() {

    private val repo = OrdersRepository()

    // =====================================================
    // EXISTING ORDERS STATE
    // Used by POS history and existing repository workflows
    // =====================================================

    val pageIndex = MutableStateFlow(0)

    private val _orders =
        MutableStateFlow<List<OrderMasterData>>(emptyList())

    val orders: StateFlow<List<OrderMasterData>> = _orders

    private val _orderItems =
        MutableStateFlow<Map<String, List<OrderProductData>>>(emptyMap())

    // =====================================================
    // ROOM ONLINE ORDERS
    // Source of truth for OnlineOrdersScreen
    // =====================================================

    private val _localOnlineOrders =
        MutableStateFlow<List<OrderMasterData>>(emptyList())

    val localOnlineOrders: StateFlow<List<OrderMasterData>> =
        _localOnlineOrders.asStateFlow()

    private var localOrdersObserverJob: Job? = null

    /**
     * Observe the separate Room online-order table.
     *
     * Room emits again automatically whenever its rows change.
     * This does not change POS history or Firestore listeners.
     */
    fun observeLocalOnlineOrders(dao: OnlineReceivedOrderDao) {
        if (localOrdersObserverJob?.isActive == true) {
            return
        }

        localOrdersObserverJob = viewModelScope.launch {
            try {
                dao.observeAllOrders().collect { roomOrders ->

                    _localOnlineOrders.value = roomOrders.map { entity ->
                        entity.toOrderMasterData()
                    }

                    Log.d(
                        "LOCAL_ONLINE_ORDERS",
                        "Room emitted ${roomOrders.size} online orders"
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "LOCAL_ONLINE_ORDERS",
                    "Failed to observe Room online orders",
                    e
                )
            }
        }
    }

    // =====================================================
    // CATEGORY SALES STATE
    // =====================================================

    private val _categorySales =
        MutableStateFlow<List<CategorySaleData>>(emptyList())

    val categorySales: StateFlow<List<CategorySaleData>> =
        _categorySales

    private val _loading = MutableStateFlow(false)

    val loading: StateFlow<Boolean> = _loading

    private val limit = 10

    // =====================================================
    // ORDER ITEMS
    // NOTE: Still uses the existing repository in this version.
    // =====================================================

    suspend fun getOrderItems(
        orderId: String
    ): List<OrderProductData> {
        return repo.getOrderProducts(orderId)
    }

    // =====================================================
    // PRINT ONLINE ORDER — BILL + KITCHEN
    // =====================================================

    fun printOrder(order: OrderMasterData) {
        viewModelScope.launch {
            try {
                val items = getOrderItems(order.id)

                Log.d(
                    "PRINT",
                    "Order No. ${order.srno}, items=${items.size}"
                )

                if (items.isEmpty()) {
                    Log.e(
                        "PRINT",
                        "No items found for order ${order.srno}"
                    )
                    return@launch
                }

                // BILL PRINT
                val printOrder = FirestorePrintMapper.map(order, items)

                printerManager.printTextNew(
                    PrinterRole.BILLING,
                    printOrder,
                    kotNumberText = "",
                    stewardName = ""
                )

                delay(2_000)

                // KITCHEN PRINT
                val kotItems = OnlineOrderMapper.toKotItems(items)

                printerManager.printTextKitchen(
                    PrinterRole.KITCHEN,
                    tableName = order.srno,
                    sessionKey = order.srno,
                    orderType = "Online order",
                    kotItems,
                    kotNumber = ""
                ) { success ->
                    Log.d(
                        "PRINT",
                        "Kitchen print success=$success"
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "PRINT",
                    "Failed to print online order ${order.id}",
                    e
                )
            }
        }
    }

    // =====================================================
    // PRINT FROM HISTORY
    // Existing POS/history repository behavior retained
    // =====================================================

    fun printOrderFromHistory(
        orderId: String,
        role: String
    ) {
        viewModelScope.launch {
            _loading.value = true

            try {
                Log.d(
                    "ONLINE_PRINT",
                    "Print requested for orderId=$orderId"
                )

                val order = _orders.value.find {
                    it.id == orderId
                }

                if (order == null) {
                    Log.e(
                        "ONLINE_PRINT",
                        "Order NOT FOUND for orderId=$orderId"
                    )
                    return@launch
                }

                val items = repo.getOrderProducts(orderId)

                if (items.isEmpty()) {
                    Log.e(
                        "ONLINE_PRINT",
                        "No items for orderId=$orderId"
                    )
                    return@launch
                }

                if (role == "bill") {
                    val printOrder = FirestorePrintMapper.map(
                        order,
                        items
                    )

                    printerManager.printTextNew(
                        PrinterRole.BILLING,
                        printOrder,
                        kotNumberText = "",
                        stewardName = ""
                    )
                }

                if (role == "kitchen") {
                    val kotItems = OnlineOrderMapper.toKotItems(items)

                    printerManager.printTextKitchen(
                        PrinterRole.KITCHEN,
                        tableName = order.srno,
                        sessionKey = order.srno,
                        orderType = "Online order",
                        kotItems,
                        kotNumber = ""
                    ) { success ->
                        Log.d(
                            "ONLINE_PRINT",
                            "Kitchen print success=$success"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    "ONLINE_PRINT",
                    "Print failed for orderId=$orderId",
                    e
                )
            } finally {
                _loading.value = false
            }
        }
    }

    // =====================================================
    // EXISTING PAGED REPOSITORY LOADING
    // Retained for other screens and workflows
    // =====================================================

    private fun loadOrders(page: Int) {
        viewModelScope.launch {
            try {
                _loading.value = true
                pageIndex.value = page

                val offset = page * limit

                val pagedOrders = repo.getPagedOrders(
                    limit = limit,
                    offset = offset
                )

                _orders.value = pagedOrders.sortedByDescending {
                    it.createdAtMillis
                }
            } catch (e: Exception) {
                Log.e("PAGED_ORDERS", "Load failed", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            _loading.value = true

            try {
                pageIndex.value = 0
                repo.resetPagination()

                _orders.value = repo.getFirstPage(limit.toLong())
                    .sortedByDescending { it.createdAtMillis }
            } catch (e: Exception) {
                Log.e("PAGED_ORDERS", "First page load failed", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            _loading.value = true

            try {
                val newOrders = repo.getNextPage(limit.toLong())

                if (newOrders.isNotEmpty()) {
                    pageIndex.value++
                    _orders.value = newOrders.sortedByDescending {
                        it.createdAtMillis
                    }
                }
            } catch (e: Exception) {
                Log.e("PAGED_ORDERS", "Next page load failed", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadPrevPage() {
        if (pageIndex.value == 0) return

        viewModelScope.launch {
            _loading.value = true

            try {
                val prevOrders = repo.getPrevPage(limit.toLong())

                if (prevOrders.isNotEmpty()) {
                    pageIndex.value--
                    _orders.value = prevOrders.sortedByDescending {
                        it.createdAtMillis
                    }
                }
            } catch (e: Exception) {
                Log.e("PAGED_ORDERS", "Previous page load failed", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private fun totalLine(
        label: String,
        value: Double
    ): String {
        if (value == 0.0) return ""

        val left = label.padEnd(14)
        val right = formatAmount(value).padStart(18)

        return left + right
    }

    private fun formatAmount(value: Double?): String =
        "%.2f".format(value ?: 0.0)

    private fun toDouble(value: Any?): Double =
        when (value) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

    private fun btSafe(
        text: String,
        max: Int
    ): String {
        return text
            .replace(Regex("[^A-Za-z0-9 ]"), "")
            .trim()
            .take(max)
    }

    // =====================================================
    // ONLINE ORDER DATE SEARCH
    // Existing repository workflow retained
    // =====================================================

    fun searchOrdersByDate(
        startMillis: Long,
        endMillis: Long
    ) {
        viewModelScope.launch {
            _loading.value = true

            try {
                val result = repo.searchOrdersByDate(
                    startMillis = startMillis,
                    endMillis = endMillis,
                    limit = 50
                )

                _orders.value = result.sortedByDescending {
                    it.createdAtMillis
                }
            } catch (e: Exception) {
                Log.e("ONLINE_ORDERS", "Date search failed", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun buildDayRange(
        selectedDate: Long
    ): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    // =====================================================
    // POS HISTORY
    // =====================================================

    fun loadPosHistoryFirstPage() {
        loadOrders(0)
    }

    fun loadPosHistoryNextPage() {
        loadOrders(pageIndex.value + 1)
    }

    fun loadPosHistoryPrevPage() {
        if (pageIndex.value == 0) return
        loadOrders(pageIndex.value - 1)
    }

    fun searchPOSOrdersByDate(
        startMillis: Long,
        endMillis: Long
    ) {
        viewModelScope.launch {
            _loading.value = true

            try {
                val result = repo.searchPOSOrdersByDate(
                    startMillis = startMillis,
                    endMillis = endMillis,
                    limit = 100
                )

                _orders.value = result.sortedByDescending {
                    it.createdAtMillis
                }
            } catch (e: Exception) {
                Log.e("POS_HISTORY", "Date search failed", e)
            } finally {
                _loading.value = false
            }
        }
    }

    // =====================================================
    // CATEGORY SALES REPORT
    // =====================================================

    fun loadCategorySales(
        startMillis: Long,
        endMillis: Long
    ) {
        viewModelScope.launch {
            _loading.value = true

            try {
                _categorySales.value = repo.getCategorySalesByDate(
                    startMillis = startMillis,
                    endMillis = endMillis
                )
            } catch (e: Exception) {
                Log.e("CATEGORY_SALES", "Load failed", e)
            } finally {
                _loading.value = false
            }
        }
    }


    fun updateLocalOrderStatus(
        dao: OnlineReceivedOrderDao,
        orderId: String,
        status: String
    ) {
        viewModelScope.launch {
            try {
                dao.updateOrderStatus(
                    orderId = orderId,
                    status = status
                )

                Log.d(
                    "LOCAL_ONLINE_ORDERS",
                    "Order $orderId updated locally to $status"
                )
            } catch (e: Exception) {
                Log.e(
                    "LOCAL_ONLINE_ORDERS",
                    "Failed to update local order status",
                    e
                )
            }
        }
    }
}