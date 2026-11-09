package com.it10x.foodappgstav7_31.navigation

import ThemeSettingsScreen
import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.it10x.foodappgstav7_31.core.PosRole
import com.it10x.foodappgstav7_31.core.PosRoleManager
import com.it10x.foodappgstav7_31.ui.setting.DeviceRoleSelectionScreen
import com.it10x.foodappgstav7_31.ui.waiter.WaiterPosScreen
import com.it10x.foodappgstav7_31.com.it10x.foodappgstav7_31.ui.tables.TableScreen
import com.it10x.foodappgstav7_31.com.ui.settings.PrinterRoleSelectionScreen
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.manager.TableSyncManager

import com.it10x.foodappgstav7_31.data.pos.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav7_31.data.pos.viewmodel.POSOrdersViewModelFactory
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.ui.categories.LocalCategoriesScreen
import com.it10x.foodappgstav7_31.ui.home.HomeScreen
import com.it10x.foodappgstav7_31.ui.orders.online.OnlineOrdersScreen
import com.it10x.foodappgstav7_31.ui.orders.local.LocalOrderDetailScreen
import com.it10x.foodappgstav7_31.ui.orders.local.LocalOrderDetailViewModel
import com.it10x.foodappgstav7_31.ui.orders.local.LocalOrderDetailViewModelFactory
import com.it10x.foodappgstav7_31.ui.orders.local.LocalOrdersScreen


import com.it10x.foodappgstav7_31.ui.pos.PosScreen
import com.it10x.foodappgstav7_31.ui.products.LocalProductsScreen
import com.it10x.foodappgstav7_31.ui.settings.*
import com.it10x.foodappgstav7_31.viewmodel.*
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository


import com.it10x.foodappgstav7_31.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_31.data.pos.repository.CategoryRepository
import com.it10x.foodappgstav7_31.data.pos.repository.CustomerLedgerRepository
import com.it10x.foodappgstav7_31.data.pos.repository.CustomerRepository
import com.it10x.foodappgstav7_31.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_31.data.pos.repository.VirtualTableRepository
import com.it10x.foodappgstav7_31.data.printer.PrinterUploadManager
import com.it10x.foodappgstav7_31.domain.usecase.TableReleaseUseCase
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel
import com.it10x.foodappgstav7_31.ui.cart.CartViewModelFactory
import com.it10x.foodappgstav7_31.ui.customer.CustomerLedgerScreen
import com.it10x.foodappgstav7_31.ui.customer.CustomerLedgerViewModel
import com.it10x.foodappgstav7_31.ui.customer.CustomerLedgerViewModelFactory
import com.it10x.foodappgstav7_31.ui.customer.CustomerListScreen
import com.it10x.foodappgstav7_31.ui.customer.CustomerViewModel
import com.it10x.foodappgstav7_31.ui.customer.CustomerViewModelFactory
import com.it10x.foodappgstav7_31.ui.delivery.DeliverySettlementScreen
import com.it10x.foodappgstav7_31.ui.delivery.DeliverySettlementViewModel
import com.it10x.foodappgstav7_31.ui.orders.history.HistoryOrdersScreen
import com.it10x.foodappgstav7_31.ui.orders.history.OrderItemsScreen

import com.it10x.foodappgstav7_31.ui.pos.PosSessionViewModel
import com.it10x.foodappgstav7_31.ui.pos.customer.CustomerAddressScreen
import com.it10x.foodappgstav7_31.ui.reports.CategoryProductReportScreen
import com.it10x.foodappgstav7_31.ui.reports.CategorySalesScreen
import com.it10x.foodappgstav7_31.ui.reports.ProductSalesScreen
import com.it10x.foodappgstav7_31.ui.reports.TotalSalesReportScreen

import com.it10x.foodappgstav7_31.ui.sales.SalesScreen
import com.it10x.foodappgstav7_31.ui.sales.SalesViewModel
import com.it10x.foodappgstav7_31.ui.sales.SalesViewModelFactory
import com.it10x.foodappgstav7_31.ui.tables.WaiterTableViewScreen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import com.it10x.foodappgstav7_31.core.PosType
import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity
import com.it10x.foodappgstav7_31.ui.dayclosing.DayClosingScreen
import com.it10x.foodappgstav7_31.ui.dayclosing.DayClosingViewModel
import com.it10x.foodappgstav7_31.ui.dayclosing.DayClosingViewModelFactory
import com.it10x.foodappgstav7_31.ui.kot.history.KotHistoryDetailScreen
import com.it10x.foodappgstav7_31.ui.kot.history.KotHistoryScreen
import com.it10x.foodappgstav7_31.ui.pos.FastFoodPosScreen
import com.it10x.foodappgstav7_31.ui.pos.RetailPosScreen
 import com.it10x.foodappgstav7_31.ui.kot.history.KotHistoryViewModel
import com.it10x.foodappgstav7_31.ui.kot.history.KotHistoryViewModelFactory
import com.it10x.foodappgstav7_31.ui.pos.RunningOrdersScreen
import com.it10x.foodappgstav7_31.ui.tables.TableChangeScreen
import com.it10x.foodappgstav7_31.data.pos.repository.loyalty.LoyaltyRepository
import com.it10x.foodappgstav7_31.data.pos.viewmodel.loyalty.LoyaltyViewModel
import com.it10x.foodappgstav7_31.ui.loyalty.LoyaltyScreen

private const val LOYALTY_OWNER_ID = "DEFAULT_OWNER"
private const val LOYALTY_OUTLET_ID = "DEFAULT_OUTLET"

@Composable
fun NavigationHost(
    navController: NavHostController,
    printerManager: PrinterManager,
    printerPreferences: PrinterPreferences,
    printerUploadManager: PrinterUploadManager,
    realtimeOrdersViewModel: RealtimeOrdersViewModel,
    paddingValues: PaddingValues = PaddingValues(),
    onSavePrinterSettings: () -> Unit = {}
) {

    val context = LocalContext.current
    val db = AppDatabaseProvider.get(context)
    val outletDao = remember {
        db.outletDao()
    }

    var outlet by remember {
        mutableStateOf<OutletEntity?>(null)
    }

    var outletLoaded by remember {
        mutableStateOf(false)
    }

    var currencyCode by remember {
        mutableStateOf("INR")
    }

    var localeTag by remember {
        mutableStateOf("en-IN")
    }

    LaunchedEffect(Unit) {
        val loadedOutlet = outletDao.getOutlet()

        outlet = loadedOutlet

        currencyCode = loadedOutlet?.currencyCode ?: "INR"
        localeTag = loadedOutlet?.localeTag ?: "en-IN"

        outletLoaded = true
    }

    val startupScreen = outlet?.startupScreen ?: "pos"
    // -----------------------------
    // SHARED VIEWMODELS
    // -----------------------------

    val printerSettingsViewModel: PrinterSettingsViewModel = viewModel(
        factory = PrinterSettingsViewModelFactory(
            prefs = printerPreferences,
            printerManager = printerManager
        )
    )

// -----------------------------
// SHARED REPOSITORIES
// -----------------------------

    val cartRepository = remember {
        CartRepository(
            db.cartDao(),
            db.tableDao()
        )
    }

    val kotRepository = remember {
        KotRepository(
            db.kotBatchDao(),
            db.kotItemDao(),
            db.tableDao()
        )
    }

    val virtualTableRepository = remember {
        VirtualTableRepository(
            db = db,
            virtualDao = db.virtualTableDao(),
            cartDao = db.cartDao(),
            kotDao = db.kotItemDao(),
            counterDao = db.virtualTableCounterDao()
        )
    }

    val tableSyncManager = remember {
        TableSyncManager(
            tableRepo = kotRepository,
            cartRepo = cartRepository,   // ✅ Now it exists
            virtualRepo = virtualTableRepository
        )
    }

    val categoryRepository = remember {
        CategoryRepository(db.categoryDao())
    }

    val tableReleaseUseCase = remember {
        TableReleaseUseCase(
            cartRepository = cartRepository,
            tableDao = db.tableDao()
        )
    }
    val application = context.applicationContext as Application
    val cartViewModelFactory = remember {
        CartViewModelFactory(
            app = application,
            repository = cartRepository,
            categoryRepository = categoryRepository,
            tableReleaseUseCase = tableReleaseUseCase,
            tableSyncManager = tableSyncManager,
            virtualTableRepository = virtualTableRepository
        )
    }





    val ordersViewModel: OnlineOrdersViewModel = viewModel(
        factory = OnlineOrdersViewModelFactory(printerManager)
    )
    val posOrdersViewModel: POSOrdersViewModel = viewModel(
        factory = POSOrdersViewModelFactory(
            db = db,
            printerManager = printerManager
        )
    )
    val posSessionViewModel: PosSessionViewModel = viewModel()
    val posTableViewModel: PosTableViewModel = viewModel()




    // -----------------------------
    // NAV HOST
    // -----------------------------

    val role = PosRoleManager.getRole(context)


    if (!outletLoaded) {
        return
    }



   // val posType = outlet?.posType ?: "RESTAU"
    val posType = try {
        PosType.valueOf(outlet?.posType ?: "RESTAU")
    } catch (e: Exception) {
        PosType.RESTAU
    }

    val startDestination = when (posType) {

        PosType.RETAIL -> "retail_pos"

        PosType.FAST_FOOD -> "fastfood_pos"

        PosType.RESTAU -> {
            when (role) {

                null -> "device_role_selection"

                PosRole.WAITER -> "posWaiter"

                PosRole.MAIN -> {
                    if (startupScreen == "pos") "pos" else "tables"
                }
            }
        }
    }

    val runningOrdersDestination = when (posType) {

        PosType.RETAIL -> "retail_pos"

        PosType.FAST_FOOD -> "fastfood_pos"

        PosType.RESTAU -> {
            when (role) {
                PosRole.MAIN -> "pos"
                PosRole.WAITER -> "posWaiter"
                null -> "device_role_selection"
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(paddingValues)
    ) {



        // ---------------- LOCAL PRODUCTS ----------------
        composable("local_products") {
            LocalProductsScreen()
        }

        // ---------------- SYNC ----------------
        composable("sync_data") {
            SyncScreen(
                navController = navController,
                onBack = { navController.popBackStack() }
            )
        }

        // ---------------- EXPORT DATA ----------------
        composable("export_data") {
            ExportDataScreen(
                navController = navController
            )
        }

        // ---------------- LOCAL ORDERS ----------------
        composable("local_orders") {

            val localOrdersViewModel: POSOrdersViewModel = viewModel(
                factory = POSOrdersViewModelFactory(
                    db = db,
                    printerManager = printerManager
                )
            )

            LocalOrdersScreen(
                viewModel = localOrdersViewModel,
                navController = navController,
                outlet = outlet
            )
        }
//        composable("running_orders") {
//
//            val virtualTableViewModel: VirtualTableViewModel = viewModel()
//
//            RunningOrdersScreen(
//                viewModel = virtualTableViewModel,
//
//                onTableSelected = { table ->
//
//                    // ---------------------------------------------------------
//                    // SET THE SELECTED VIRTUAL TABLE IN THE SHARED POS SESSION
//                    // ---------------------------------------------------------
//
//                    posSessionViewModel.setNextVirtualTable(
//                        tableId = table.id,
//                        tableName = table.tableName,
//                        orderType = table.orderType
//                    )
//
//                    // ---------------------------------------------------------
//                    // OPEN POS FOR THIS EXACT VIRTUAL TABLE
//                    // ---------------------------------------------------------
//
//                    navController.navigate(
//                        "pos?tableId=${table.id}" +
//                                "&tableName=${table.tableName}" +
//                                "&orderType=${table.orderType}"
//                    )
//                },
//
//                onClose = {
//                    navController.popBackStack()
//                }
//            )
//        }

        composable("running_orders") {

            val virtualTableViewModel: VirtualTableViewModel = viewModel()

            RunningOrdersScreen(
                viewModel = virtualTableViewModel,

                onTableSelected = { table ->

                    // Set the selected virtual table in the shared POS session
                    posSessionViewModel.setNextVirtualTable(
                        tableId = table.id,
                        tableName = table.tableName,
                        orderType = table.orderType
                    )

                    // Open the correct POS screen
                    navController.navigate(runningOrdersDestination)
                },

                onClose = {
                    navController.popBackStack()
                }
            )
        }

        // ---------------- TABLE CHANGE ----------------
        composable("table_change") {

            TableChangeScreen(
                navController = navController,
                posTableViewModel = posTableViewModel,
                onClose = {
                    navController.popBackStack()
                }
            )
        }

        // ---------------- LOCAL ORDER DETAIL ----------------
        composable(
            route = "local_order_detail/{orderId}",
            arguments = listOf(
                navArgument("orderId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments!!
                .getString("orderId")!!

            val ordersRepository = remember {

                POSOrdersRepository(
                    db = db,
                    orderMasterDao = db.orderMasterDao(),
                    orderProductDao = db.orderProductDao(),
                    cartDao = db.cartDao(),
                    tableDao = db.tableDao(),
                    virtualTableDao = db.virtualTableDao()
                )
            }

            val detailViewModel: LocalOrderDetailViewModel = viewModel(
                factory = LocalOrderDetailViewModelFactory(
                    orderId = orderId,
                    repository = ordersRepository,
                    printerManager = printerManager
                )
            )




            LocalOrderDetailScreen(
                viewModel = detailViewModel,
                onBack = {
                    navController.popBackStack()
                },
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }


        // ---------------- CATEGORIES ----------------
        composable("local_categories") {
            LocalCategoriesScreen()
        }

        // ---------------- HOME ----------------
        composable("home") {
            HomeScreen(navController = navController)
        }

        // ---------------- ONLINE ORDERS ----------------
        composable("orders") {
            OnlineOrdersScreen(

                printerManager = printerManager,
                ordersViewModel = ordersViewModel,
                realtimeOrdersViewModel = realtimeOrdersViewModel,
                outlet = outlet
            )
        }

        composable("products") { Text("Products Screen") }
        composable("categories") { Text("Categories Screen") }



        composable(
            route = "kot_history_detail/{batchId}"
        ) { backStackEntry ->

            val batchId =
                backStackEntry.arguments?.getString("batchId") ?: ""

            val historyViewModel: KotHistoryViewModel = viewModel(
                factory = KotHistoryViewModelFactory(
                    repository = kotRepository
                )
            )

            KotHistoryDetailScreen(
                batchId = batchId,
                viewModel = historyViewModel,
                navController = navController
            )
        }

        composable("kot_history") {

            val historyViewModel: KotHistoryViewModel = viewModel(
                factory = KotHistoryViewModelFactory(
                    repository = kotRepository
                )
            )

            KotHistoryScreen(
                viewModel = historyViewModel,
                navController = navController
            )
        }

        // ---------------- POS ----------------


        composable("pos") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            PosScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }

        composable("posWaiter") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            WaiterPosScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }

        composable("retail_pos") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            RetailPosScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }

        composable("fastfood_pos") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            FastFoodPosScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }

        composable("history_orders") {
            HistoryOrdersScreen(
                printerManager = printerManager,
                ordersViewModel = ordersViewModel,
                realtimeOrdersViewModel = realtimeOrdersViewModel,
                navController = navController
            )
        }

        composable(
            "history_order_items/{orderId}"
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""

            OrderItemsScreen(
                orderId = orderId,
                navController = navController
            )
        }




        // ---------------- TABLES ----------------



        composable("tables") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            TableScreen(
                navController = navController,
                onOpenSettings = {
                    navController.navigate("printer_role_selection")
                },
                ordersViewModel = posOrdersViewModel,
                posSessionViewModel = posSessionViewModel,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel
            )
        }

// ---------------- DAY CLOSING ----------------
        composable("day_closing") {

            val viewModel: DayClosingViewModel = viewModel(
                factory = DayClosingViewModelFactory(
                    application = application
                )
            )

            DayClosingScreen(
                viewModel = viewModel,
                currencyCode = currencyCode,
                localeTag = localeTag,
                onCloseDay = {
                    viewModel.closeBusinessDay()
                }
            )
        }
// ---------------- SALES ----------------

        composable("sales") {
            val context = LocalContext.current
            val db = AppDatabaseProvider.get(context)

            val salesViewModel: SalesViewModel = viewModel(
                factory = SalesViewModelFactory(
                    salesMasterDao = db.salesMasterDao(),
                    orderProductDao = db.orderProductDao(),
                    paymentDao = db.posOrderPaymentDao()
                )
            )

            SalesScreen(
                viewModel = salesViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onHistoryReport = {
                    navController.navigate("total_sales")
                },
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }

        composable("category_sales") {

            val context = LocalContext.current

            val reportsViewModel: OnlineReportsViewModel = viewModel(
                factory = OnlineReportsViewModelFactory(
                    context.applicationContext as Application
                )
            )

            CategorySalesScreen(
                navController = navController,
                viewModel = reportsViewModel,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }

        composable("products_sales") {

            val context = LocalContext.current

            // ✅ Reports ViewModel
            val reportsViewModel: OnlineReportsViewModel = viewModel(
                factory = OnlineReportsViewModelFactory(
                    context.applicationContext as Application
                )
            )

            // ✅ Database
            val db = com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider.get(context)

            // ✅ Products ViewModel (IMPORTANT)
            val productsViewModel: com.it10x.foodappgstav7_31.data.pos.viewmodel.ProductsLocalViewModel =
                viewModel(
                    factory = com.it10x.foodappgstav7_31.data.pos.viewmodel.ProductsLocalViewModelFactory(
                        db.productDao()
                    )
                )

            // ✅ Screen
            ProductSalesScreen(
                navController = navController,
                viewModel = reportsViewModel,
                productsViewModel = productsViewModel
            )
        }



        composable("category_products_sales") {

            val context = LocalContext.current

            // ✅ Reports ViewModel
            val reportsViewModel: OnlineReportsViewModel = viewModel(
                factory = OnlineReportsViewModelFactory(
                    context.applicationContext as Application
                )
            )

            // ✅ Screen (FIXED)
            CategoryProductReportScreen(
                navController = navController,
                viewModel = reportsViewModel
            )
        }

        composable("total_sales") {

            val context = LocalContext.current

            val reportsViewModel: OnlineReportsViewModel = viewModel(
                factory = OnlineReportsViewModelFactory(
                    context.applicationContext as Application
                )
            )

            TotalSalesReportScreen(
                viewModel = reportsViewModel,
                onBack = { navController.popBackStack() },
                onHistoryCategoryReport =  { navController.navigate("category_sales") },
                onHistoryProductReport =  { navController.navigate("products_sales") },
                onHistoryCategoryProductReport =  { navController.navigate("category_products_sales") },
                currencyCode = currencyCode,
                localeTag = localeTag
            )

        }



        // ---------------- PRINTER SETTINGS ----------------
        composable("printer_role_selection") {
            PrinterRoleSelectionScreen(
                prefs = printerPreferences,
                onBillingClick = { navController.navigate("printer_settings/BILLING") },
                onKitchenClick = { navController.navigate("printer_settings/KITCHEN") }
            )
        }

        composable(
            "printer_settings/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->

            val role = PrinterRole.valueOf(
                backStackEntry.arguments!!.getString("role")!!
            )

            val scope = rememberCoroutineScope()

            PrinterSettingsScreen(
                viewModel = printerSettingsViewModel,
                prefs = printerPreferences,
                role = role,
                onSave = {
                    onSavePrinterSettings()

                    scope.launch {
                        printerUploadManager.uploadPrinter(role)
                    }

                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                onBluetoothSelected = {
                    navController.navigate("bluetooth_devices/${role.name}")
                },
                onUSBSelected = {
                    navController.navigate("usb_devices/${role.name}")
                },
                onLanSelected = {
                    navController.navigate("lan_printer_settings/${role.name}")
                }
            )
        }

        composable("lan_printer_settings/{role}") { backStackEntry ->
            val role = PrinterRole.valueOf(
                backStackEntry.arguments!!.getString("role")!!
            )
            LanPrinterSettingsScreen(
                viewModel = printerSettingsViewModel,
                role = role,
                onBack = { navController.popBackStack() }
            )
        }

        composable("bluetooth_devices/{role}") {
            val role = PrinterRole.valueOf(
                it.arguments!!.getString("role")!!
            )
            BluetoothDeviceScreen(
                role = role,
                settingsViewModel = printerSettingsViewModel
            )
        }

        composable("usb_devices/{role}") {
            USBPrinterScreen(
                role = PrinterRole.valueOf(
                    it.arguments!!.getString("role")!!
                )
            )
        }

        composable("settings") {
            SettingsScreen(
                navController = navController
            )
        }

        composable("outlet_information") {
            OutletInformationScreen(
                navController = navController
            )
        }

        composable("advanced_settings") {
            AdvancedSettingsScreen()
        }




        composable("theme_settings") {
            ThemeSettingsScreen()
        }

        composable("device_role_selection") {
            DeviceRoleSelectionScreen(
                onRoleSelected = {
                    navController.navigate("pos") {
                        popUpTo("device_role_selection") { inclusive = true }
                    }
                }
            )
        }


        composable("customers") {

            val context = LocalContext.current
            val application = context.applicationContext as Application
            val db = AppDatabaseProvider.get(application)

            val viewModel: CustomerViewModel = viewModel(
                factory = CustomerViewModelFactory(
                    CustomerRepository(db.posCustomerDao())
                )
            )

            CustomerListScreen(
                viewModel = viewModel,
                onCustomerClick = { customerId ->
                    navController.navigate("customer_ledger/$customerId")
                },
                onLoyaltyClick = { customerId ->
                    navController.navigate("loyalty/$customerId")
                }
            )
        }


        composable(
            route = "customer_ledger/{customerId}",
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val customerId = backStackEntry.arguments?.getString("customerId")!!

            val context = LocalContext.current
            val application = context.applicationContext as Application
            val db = AppDatabaseProvider.get(application)

            val repository = CustomerLedgerRepository(db)

            val viewModel: CustomerLedgerViewModel = viewModel(
                factory = CustomerLedgerViewModelFactory(repository, customerId)
            )

            CustomerLedgerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }


        composable("loyalty/{customerId}") { backStackEntry ->

            val customerId = backStackEntry.arguments
                ?.getString("customerId")

            if (customerId != null) {

                val loyaltyRepository = remember(db) {
                    LoyaltyRepository(db)
                }

                val loyaltyViewModel: LoyaltyViewModel = viewModel(
                    factory = LoyaltyViewModel.Factory(loyaltyRepository)
                )

                // Replace these with the actual fields from OutletEntity
                // and your logged-in owner/session data.
                LoyaltyScreen(
                    customerId = customerId,
                    ownerId = LOYALTY_OWNER_ID,
                    outletId = LOYALTY_OUTLET_ID,
                    viewModel = loyaltyViewModel
                )
            }
        }



        composable("delivery_settlement") {
            DeliverySettlementScreen(
                viewModel = DeliverySettlementViewModel(db),
                onBack = { navController.popBackStack() }
            )
        }


        composable("Address") { backStackEntry ->

            val context = LocalContext.current
            val db = AppDatabaseProvider.get(context)

            val phone = backStackEntry.arguments?.getString("phone") ?: ""

            val repository = CustomerRepository(
                db.posCustomerDao()
            )

            CustomerAddressScreen(
                phone = phone,
                repository = repository,
                ownerId = "OWNER_ID",     // replace with your real ownerId
                outletId = "OUTLET_ID",   // replace with your real outletId
                onSaved = {
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }


        composable("waiter_tables_view") {

            val cartViewModel: CartViewModel = viewModel(
                factory = cartViewModelFactory
            )

            val posTableViewModel: PosTableViewModel = viewModel()

            WaiterTableViewScreen(
                navController = navController,
                cartViewModel = cartViewModel,
                posTableViewModel = posTableViewModel, // ✅ REQUIRED
                posSessionViewModel = posSessionViewModel
            )
        }


    }
}
