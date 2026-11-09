package com.it10x.foodappgstav7_31.data.pos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_31.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.ReceiptPrintMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.print.OutletMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// 🔹 NEW (for atomic order no + API 24 safe date)
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class POSOrdersViewModel(
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel()
{


    private val prefs by lazy {
        PrinterPreferences(printerManager.appContext())
    }
    val loading: StateFlow<Boolean> get() = _loading
    private val _loading = MutableStateFlow(false)

    val pageIndex = MutableStateFlow(0)
    private val limit = 10
    private val srNoCounter = AtomicInteger(1)


    private val _orders = MutableStateFlow<List<PosOrderMasterEntity>>(emptyList())
    val orders: StateFlow<List<PosOrderMasterEntity>> = _orders

    suspend fun getOrderItems(
        orderId: String
    ): List<PosOrderItemEntity> {

        return repository.getOrderItems(orderId)
    }
    fun searchOrdersByDate(dateMillis: Long) {

        val startOfDay = dateMillis
        val endOfDay = startOfDay + 86400000

        viewModelScope.launch {

            repository.getOrdersByDate(startOfDay, endOfDay)
                .collect { list ->
                    _orders.value = list
                }
        }
    }

    // 🔹 NEW: API-24 safe business date (yyyyMMdd)
    private fun businessDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date())
    }
    // -------------------------
    // PAGINATION
    // -------------------------
    fun loadFirstPage() = loadOrders(0)
    fun loadNextPage() = loadOrders(pageIndex.value + 1)
    fun loadPrevPage() {
        val prev = if (pageIndex.value > 0) pageIndex.value - 1 else 0
        loadOrders(prev)
    }

    private fun loadOrders(page: Int) {
        viewModelScope.launch {
            _loading.value = true
            pageIndex.value = page
            val offset = page * limit
            val pagedOrders = repository.getPagedOrders(limit, offset)
//            pagedOrders.forEach {
//                Log.d("ORDER_SRNO", "Loaded order id=${it.id} srno=${it.srno}")
//            }

            _orders.value = pagedOrders.sortedByDescending { it.createdAt }
            _loading.value = false
        }
    }




    // -------------------------
    // PRINT ORDERS (AUTO + MANUAL + BUTTON)
    // -------------------------
    private fun printOrderStandard(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>,
        role: String,

    ) {
        Log.d("PRINT_SOURCE", "🟢 POSOrdersViewModel.printOrderStandard CALLED")

        viewModelScope.launch {

          //  Log.d("OUTLET_PRINT", "📨 Building PrintOrder…")



            // ---------------- OUTLET FROM ROOM ----------------
            val db = AppDatabaseProvider.get(printerManager.appContext())
        //    Log.d("OUTLET_DB_PRINT", "DB Path Print = ${db.openHelper.readableDatabase.path}")

        //    Log.d("OUTLET_PRINT", "🔍 Fetching outlet from Room…")

            val outlet = withContext(Dispatchers.IO) {
                db.outletDao().getOutlet()
            }


            if (outlet == null) {
                Log.e("OUTLET_PRINT", "❌ Outlet is NULL — using default title")
            } else {
              //  Log.d("OUTLET_PRINT", "✅ Outlet Loaded")
             //   Log.d("OUTLET_PRINT", "name=${outlet.outletName}")
             //   Log.d("OUTLET_PRINT", "city=${outlet.city}")
             //   Log.d("OUTLET_PRINT", "phone=${outlet.phone}")
            }

          // ---------------- BILLING PRINT ----------------

            val outletInfo = OutletMapper.fromEntity(outlet)

            val printOrder = PrintOrderBuilder.build(
                master = order,
                items = items,
                outlet = outletInfo
            )

            val stewardName =
                order.finalizedByName
                    ?.takeIf { it.isNotBlank() }
                    ?: order.createdByName.orEmpty()

//            val kotNumberText =
//                if (order.srno > 0)
//                    order.srno.toString()
//                else
//                    ""
            val kotNumberText =
                if (order.srno.isNotBlank())
                    order.srno
                else
                    ""

            val referenceId = order.id

            val printerRole = PrinterRole.BILLING

            val printMode = prefs.getReceiptPrintMode(printerRole)

            when (printMode) {

                ReceiptPrintMode.TEXT -> {

                    printerManager.enqueueBill(
                        order = printOrder,
                        paymentMode = order.paymentMode,
                        outletInfo = outletInfo,
                        kotNumberText = kotNumberText,
                        stewardName = stewardName,
                        referenceId = referenceId,
                        earnedPoints = 0,
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
                        earnedPoints = 0,
                    )
                }
            }

   // printerManager.printTextNew(PrinterRole.BILLING, printOrder)

            // SMALL DELAY
            kotlinx.coroutines.delay(150)

        }
    }


    // -------------------------
    // ORDER DETAILS
    // -------------------------
    fun getOrderProducts(orderId: String): StateFlow<List<PosOrderItemEntity>> {
        val flow = MutableStateFlow<List<PosOrderItemEntity>>(emptyList())
        viewModelScope.launch {
            flow.value = repository.getOrderItems(orderId)
        }
        return flow
    }

    // -------------------------
    // MANUAL PRINT OLD ORDER
    // -------------------------
    fun printOrder(orderId: String,role: String) {
        viewModelScope.launch {
            _loading.value = true
            try {

              //  Log.d("POS_PRINT", "Print requested for orderId=$orderId")

                val order = repository.getOrderById(orderId)
                if (order == null) {
                    Log.e("POS_PRINT", "Order NOT FOUND for orderId=$orderId")
                    return@launch
                }

                val items = repository.getOrderItems(orderId)
                if (items.isEmpty()) {
                    Log.d(
                        "ORDER_SRNO",
                        "Printing orderId=$orderId srno=${order.srno} items=${items.size}"
                    )
                    return@launch
                }

//                Log.d(
//                    "ORDER_SRNO",
//                    "Printing orderId=$orderId srno=${order.srno} items=${items.size}"
//                )

    printOrderStandard(order, items, role)


            } catch (e: Exception) {
                Log.e("POS_PRINT", "Exception while printing order", e)
            } finally {
                _loading.value = false
            }
        }



    }


}
