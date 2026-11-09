package com.it10x.foodappgstav7_31.ui.bill

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.online.repository.CashierOrderSyncRepository
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.manager.TableSyncManager
import com.it10x.foodappgstav7_31.data.pos.repository.*
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.fiskaly.FiskalyRepository
import com.it10x.foodappgstav7_31.network.fiskaly.FiskalyClient
import com.it10x.foodappgstav7_31.ui.pos.PosSessionViewModel

class BillViewModelFactory(
    private val application: Application,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String,
    private val posSessionViewModel: PosSessionViewModel
) : ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {

            val db = AppDatabaseProvider.get(application)

            // -----------------------------
            // REPOSITORIES
            // -----------------------------

            val orderSequenceRepository = OrderSequenceRepository(db)

            val printerManager =
                PrinterManager.getInstance(application.applicationContext)
            val prefs = PrinterPreferences(application.applicationContext)


            val kotRepository = KotRepository(
                db.kotBatchDao(),
                db.kotItemDao(),
                db.tableDao()
            )

            // ✅ ADD THIS (Missing Earlier)
            val cartRepository = CartRepository(
                db.cartDao(),
                db.tableDao()
            )

            // ✅ ADD THIS (Missing Earlier)
            val virtualTableRepository = VirtualTableRepository(
                db = db,
                db.virtualTableDao(),
                db.cartDao(),
                db.kotItemDao(),
                counterDao = db.virtualTableCounterDao()
            )

            // ✅ NOW THIS WORKS
            val tableSyncManager = TableSyncManager(
                tableRepo = kotRepository,
                cartRepo = cartRepository,
                virtualRepo = virtualTableRepository
            )

            val ordersRepository = POSOrdersRepository(
                db = db,
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                cartDao = db.cartDao(),
                tableDao = db.tableDao(),
                virtualTableDao = db.virtualTableDao()
            )

            val paymentRepository = POSPaymentRepository(
                paymentDao = db.posOrderPaymentDao()
            )

            // -----------------------------
// FISKALY
// -----------------------------
            val fiskalyRepository = FiskalyRepository(
                context = application.applicationContext,
                api = FiskalyClient.api
            )

            // -----------------------------
            // FIRESTORE
            // -----------------------------

            val firestore = FirebaseFirestore.getInstance()

            val invoiceCounterRepository = InvoiceCounterRepository(
                firestore = firestore,
                orderCounterDao = db.orderCounterDao()
            )

            val cashierOrderSyncRepository = CashierOrderSyncRepository(
                firestore = firestore,
                kotItemDao = db.kotItemDao()
            )

            val businessDayRepository = BusinessDayRepository(
                db.businessDayDao()
            )

            @Suppress("UNCHECKED_CAST")
            return BillViewModel(
                app = application,
                db = db,
                kotItemDao = db.kotItemDao(),
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                orderSequenceRepository = orderSequenceRepository,
                invoiceCounterRepository = invoiceCounterRepository,
                outletDao = db.outletDao(),
                tableId = tableId,
                tableName = tableName,
                orderType = orderType,
                repository = ordersRepository,
                printerManager = printerManager,
                prefs = prefs,
                outletRepository = OutletRepository(db.outletDao()),
                paymentRepository = paymentRepository,
                customerDao = db.posCustomerDao(),
                ledgerDao = db.posCustomerLedgerDao(),
                kotRepository = kotRepository,
                cashierOrderSyncRepository = cashierOrderSyncRepository,
                tableSyncManager = tableSyncManager,
                businessDayRepository = businessDayRepository,
                fiskalyRepository = fiskalyRepository,
                virtualTableRepository = virtualTableRepository,
                posSessionViewModel = posSessionViewModel
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}