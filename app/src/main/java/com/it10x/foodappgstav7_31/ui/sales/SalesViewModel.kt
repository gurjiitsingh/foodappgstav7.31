// File: SalesViewModel.kt
package com.it10x.foodappgstav7_31.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_31.data.pos.dao.SalesMasterDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import com.it10x.foodappgstav7_31.data.pos.dao.PosOrderPaymentDao

class SalesViewModel(
    private val salesMasterDao: SalesMasterDao,
    private val orderProductDao: OrderProductDao,
    private val paymentDao: PosOrderPaymentDao
) : ViewModel() {

    companion object {
        private const val TAG = "SALESDEBUG"
    }

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _fromDate = MutableStateFlow(startOfToday())
    private val _toDate = MutableStateFlow(endOfToday())

    init {
        refreshSales()
    }

    fun setDateRange(from: Long, to: Long) {
        _fromDate.value = from
        _toDate.value = to
        refreshSales()
    }

    fun refreshSales() {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(isLoading = true)

            val from = _fromDate.value
            val to = _toDate.value

            // ✅ ALL ORDERS (including CREDIT)
            val sales = salesMasterDao.getAllOrdersBetween(from, to)

            // ---------------- TOTALS ----------------
            val total = sales.sumOf { it.grandTotal }
            val taxTotal = sales.sumOf { it.taxTotal }
            val discountTotal = sales.sumOf { it.discountTotal }

            val totalBeforeDiscount = sales.sumOf {
                it.grandTotal + it.discountTotal
            }

            // ---------------- CREDIT / RECEIVED ----------------
            val creditTotal = sales
                .filter { it.paymentMode.equals("CREDIT", true) }
                .sumOf { it.grandTotal }

            val receivedTotal = sales
                .filter { !it.paymentMode.equals("CREDIT", true) }
                .sumOf { it.grandTotal }

            // ---------------- PAYMENT BREAKUP ----------------
            // ---------------- PAYMENT BREAKUP ----------------
// Read individual successful payments, not pos_order_master.paymentMode.
            val paymentBreakup = paymentDao
                .getPaymentBreakup(from, to)
                .associate { row ->
                    row.paymentMode to row.total
                }

            // ---------------- CATEGORY SALES ----------------
            val categoryResults =
                orderProductDao.getCategorySalesBetween(from, to)

            val categorySales = categoryResults.associate {
                it.categoryName to Pair(it.totalQty, it.total)
            }

            // ---------------- ITEM SALES ----------------
            val itemResults =
                orderProductDao.getItemSalesBetween(from, to)

            val itemSales = itemResults
                .groupBy { it.categoryName }
                .mapValues { entry ->
                    entry.value.associate {
                        it.itemName to Pair(it.totalQty, it.total)
                    }
                }

            // ---------------- GROUPED TOTALS ----------------
            val beveragesTotal = categorySales
                .filterKeys { it.equals("Beverages", true) }
                .values.sumOf { it.second }

            val wineTotal = categorySales
                .filterKeys { it.equals("Wine", true) }
                .values.sumOf { it.second }

            val foodTotal = categorySales
                .filterKeys {
                    !it.equals("Beverages", true) &&
                            !it.equals("Wine", true)
                }
                .values.sumOf { it.second }

            // ---------------- FINAL UI STATE ----------------
            _uiState.value = SalesUiState(
                isLoading = false,
                orders = sales,
                totalSales = total,
                totalBeforeDiscount = totalBeforeDiscount,
                taxTotal = taxTotal,
                discountTotal = discountTotal,
                paymentBreakup = paymentBreakup,
                categorySales = categorySales,
                itemSales = itemSales,
                foodTotal = foodTotal,
                beveragesTotal = beveragesTotal,
                wineTotal = wineTotal,
                creditTotal = creditTotal,       // ✅ NEW
                receivedTotal = receivedTotal,   // ✅ NEW
                from = from,
                to = to
            )
        }
    }

    // ---------------- HELPER FUNCTIONS ----------------

    fun startOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun endOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}