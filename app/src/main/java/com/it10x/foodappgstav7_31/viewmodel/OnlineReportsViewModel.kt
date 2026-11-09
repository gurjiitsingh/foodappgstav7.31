package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.online.models.TotalSalesReportResult
import com.it10x.foodappgstav7_31.data.online.repository.ReportsRepository
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.dao.CategoryDao
import com.it10x.foodappgstav7_31.data.pos.entities.CategoryEntity
import com.it10x.foodappgstav7_31.ui.reports.model.ProductReportItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar


class OnlineReportsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _categoryProductReport =
        MutableStateFlow<List<ProductReportItem>>(emptyList())
    val categoryProductReport: StateFlow<List<ProductReportItem>> =
        _categoryProductReport

    private val repo = ReportsRepository()
    private val categoryDao =
        AppDatabaseProvider.get(application).categoryDao()

    // -------- CATEGORY LIST --------

    private val _categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = _categories

    // -------- REPORT DATA --------

    private val _qty = MutableStateFlow(0)
    val qty: StateFlow<Int> = _qty

    private val _totalSales = MutableStateFlow(0.0)
    val totalSales: StateFlow<Double> = _totalSales

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _totalBeforeDiscount = MutableStateFlow(0.0)
    val totalBeforeDiscount: StateFlow<Double> = _totalBeforeDiscount

    private val _totalDiscount = MutableStateFlow(0.0)
    val totalDiscount: StateFlow<Double> = _totalDiscount

    private val _totalTax = MutableStateFlow(0.0)
    val totalTax: StateFlow<Double> = _totalTax

    init {
        loadCategories()
    }

    private fun loadCategories() {

        viewModelScope.launch {

            _categories.value =
                categoryDao.getAllCategories()

        }
    }

    fun loadCategoryReportOld(
        category: String,
        startMillis: Long,
        endMillis: Long
    ) {

        viewModelScope.launch {

            _loading.value = true

            val result = repo.getCategorySalesByDate(
                categoryId = category,
                startMillis = startMillis,
                endMillis = endMillis
            )

            _qty.value = result?.totalQty ?: 0
            _totalSales.value = result?.totalSales ?: 0.0

            _loading.value = false
        }
    }
    fun loadCategoryReport(
        categoryId: String,
        startMillis: Long,
        endMillis: Long
    ) {

        viewModelScope.launch {

            if (_loading.value) return@launch

            _loading.value = true

            try {

                val result = repo.getCategorySalesByDate(
                    categoryId = categoryId,   // ✅ pass ID
                    startMillis = startMillis,
                    endMillis = endMillis
                )

                _qty.value = result?.totalQty ?: 0
                _totalSales.value = result?.totalSales ?: 0.0

            } catch (e: Exception) {

                e.printStackTrace()

                _qty.value = 0
                _totalSales.value = 0.0

            } finally {
                _loading.value = false
            }
        }
    }

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




    fun loadTotalSalesReport(startMillis: Long, endMillis: Long) {

        viewModelScope.launch {

            _loading.value = true

            val result = repo.getTotalSalesReport(startMillis, endMillis)

            _totalSales.value = result.totalSales
            _totalDiscount.value = result.totalDiscount
            _totalTax.value = result.totalTax   // ✅ ADD THIS
            _totalBeforeDiscount.value =
                result.totalSales + result.totalDiscount

            _loading.value = false
        }
    }



    fun loadProductReport(
        productId: String,
        productName: String,
        startMillis: Long,
        endMillis: Long
    ) {
        Log.d("PRODUCT_REPORT", "loadProductReport called with: $productId")
        viewModelScope.launch {

            if (_loading.value) return@launch // ✅ prevent double click

            _loading.value = true

            try {

                val result = repo.getProductSalesByDate(
                    productId = productId,
                    startMillis = startMillis,
                    endMillis = endMillis
                )

                _qty.value = result?.totalQty ?: 0
                _totalSales.value = result?.totalSales ?: 0.0

            } catch (e: Exception) {

                e.printStackTrace()

                _qty.value = 0
                _totalSales.value = 0.0

            } finally {
                _loading.value = false
            }
        }
    }

    fun loadCategoryProductReport(
        categoryId: String,
        startMillis: Long,
        endMillis: Long
    ) {
        viewModelScope.launch {

            if (_loading.value) return@launch

            _loading.value = true

            try {

                val result = repo.getCategoryProductReport(
                    categoryId,
                    startMillis,
                    endMillis
                )

                _categoryProductReport.value = result.map {
                    ProductReportItem(
                        productId = "", // optional
                        name = it.productName,
                        qty = it.totalQty,
                        total = it.totalSales
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _categoryProductReport.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }




}