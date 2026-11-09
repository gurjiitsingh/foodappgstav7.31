package com.it10x.foodappgstav7_31

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.online.repository.OrdersRepository
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.viewmodel.OnlineOrdersViewModel
import com.it10x.foodappgstav7_31.viewmodel.RealtimeOrdersViewModel
import com.it10x.foodappgstav7_31.navigation.NavigationHost
import com.it10x.foodappgstav7_31.printer.AutoPrintManager
import com.it10x.foodappgstav7_31.service.OnlineOrderMonitoringService

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import com.it10x.foodappgstav7_31.firebase.ClientIdStore
import com.it10x.foodappgstav7_31.ui.settings.ClientSetupScreen
import com.it10x.foodappgstav7_31.ui.theme.FoodPosTheme
import com.it10x.foodappgstav7_31.ui.theme.PosThemeMode

import com.it10x.foodappgstav7_31.viewmodel.ThemeViewModel

import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.sync.GlobalOrderSyncManager
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.KotProcessor

import androidx.activity.viewModels
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong

import androidx.compose.material.icons.filled.TableBar

import com.it10x.foodappgstav7_31.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository

import com.it10x.foodappgstav7_31.ui.kitchen.KitchenViewModel
import com.it10x.foodappgstav7_31.ui.kitchen.KitchenViewModelFactory

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.ui.Alignment
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.it10x.foodappgstav7_31.core.FirstSyncManager
import com.it10x.foodappgstav7_31.core.PosRole
import com.it10x.foodappgstav7_31.core.PosRoleManager
import com.it10x.foodappgstav7_31.core.PosType
import com.it10x.foodappgstav7_31.core.rememberNetworkStatus
import com.it10x.foodappgstav7_31.data.pos.repository.PrinterRepository
import com.it10x.foodappgstav7_31.data.printer.PrinterUploadManager
import com.it10x.foodappgstav7_31.data.model.ClientFirebaseConfig
import com.it10x.foodappgstav7_31.data.online.sync.TableKotSyncService
import com.it10x.foodappgstav7_31.ui.settings.FirstAutoSyncScreen
import com.it10x.foodappgstav7_31.network.RetrofitInstance
import com.it10x.foodappgstav7_31.ui.components.TopBarNavButton
import com.it10x.foodappgstav7_31.ui.menu.fastfood.FastFoodMenu
import com.it10x.foodappgstav7_31.ui.menu.restaurant.RestaurantMainMenu
import com.it10x.foodappgstav7_31.ui.menu.restaurant.WaiterMenu
import com.it10x.foodappgstav7_31.ui.menu.retail.RetailMenu
import com.it10x.foodappgstav7_31.ui.theme.PosTheme
import kotlin.math.log

//LOGIN
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.it10x.foodappgstav7_31.auth.PosLoginViewModel
import com.it10x.foodappgstav7_31.ui.login.PosLoginScreen
import com.it10x.foodappgstav7_31.auth.PosSessionManager
import com.it10x.foodappgstav7_31.core.PosRenewalManager
import com.it10x.foodappgstav7_31.data.online.repository.PosRenewalSyncRepository
import com.it10x.foodappgstav7_31.viewmodel.ProductSyncViewModel

class MainActivity : ComponentActivity() {
    private lateinit var globalOrderSyncManager: GlobalOrderSyncManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // val role = PosRoleManager.getRole(this)
        val db = AppDatabaseProvider.get(this)

        //PRINTER SETTING UPLOAD
        // ================= PRINTER SYSTEM =================

// 1️⃣ Preferences
        val printerPreferences = PrinterPreferences(this)

// 2️⃣ Repository
        val printerRepository = PrinterRepository(
            db.printerDao()
        )

// 3️⃣ Upload manager
        val printerUploadManager by lazy {
            PrinterUploadManager(
                printerPreferences,
                printerRepository
            )
        }





        val processedDao = db.processedCloudOrderDao()
        val kotRepository = KotRepository(
            batchDao = db.kotBatchDao(),
            kotItemDao = db.kotItemDao(),
            tableDao = db.tableDao()
        )
        val printerManager = PrinterManager.getInstance(this)
        val kotProcessor = KotProcessor(
            kotItemDao = db.kotItemDao(),
            kotRepository = kotRepository,
            printerManager = printerManager
        )
        // 1️⃣ Get database instance


        val repository = POSOrdersRepository(
            db = db,
            orderMasterDao = db.orderMasterDao(),
            orderProductDao = db.orderProductDao(),
            cartDao = db.cartDao(),
            tableDao = db.tableDao(),
            virtualTableDao = db.virtualTableDao()
        )

        val tableId = "Bar_B_4"
        val tableName = "Bar B 4"
        val sessionId = "DINE_IN-Bar_B_4-1771777223369"
        val orderType = "DINE_IN"

        val kitchenFactory = KitchenViewModelFactory(
            app = application,
            tableId = tableId,
            tableName = tableName,
            sessionId = sessionId,
            orderType = orderType,
            repository = repository
        )


        val kitchenVM: KitchenViewModel by viewModels { kitchenFactory }






        setContent {

            val themeVM: ThemeViewModel = viewModel()
            val themeModeString by themeVM.themeMode.collectAsState()
            val themeMode = PosThemeMode.valueOf(themeModeString)
            val context = LocalContext.current

            val outletDao = db.outletDao()
            var posType by remember { mutableStateOf(PosType.RESTAU) }

            LaunchedEffect(Unit) {
                val outlet = outletDao.getOutlet()

                posType = try {
                    PosType.valueOf(outlet?.posType ?: "RESTAU")
                } catch (e: Exception) {
                    PosType.RESTAU
                }
            }



//            var role by remember { mutableStateOf(PosRoleManager.getRole(context)) }
            val role = remember {

                when (PosSessionManager.getRole(context)?.uppercase()) {

                    "ADMIN" -> PosRole.MAIN

                    else -> PosRole.WAITER
                }
            }
            FoodPosTheme(
                mode = themeMode
            ) {
//                LaunchedEffect(role) {
//                    if (role == null) {
//                        PosRoleManager.saveRole(context, PosRole.MAIN)
//                        role = PosRole.MAIN
//                    }
//                }
           // val clientId = remember { ClientIdStore.get(context) }
                var clientId by remember { mutableStateOf(ClientIdStore.get(context)) }
                if (clientId == null) {

                    ClientSetupScreen(
                        onActivated = {
                            clientId = ClientIdStore.get(context)
                        }
                    )

                    return@FoodPosTheme
                }



                var config by remember { mutableStateOf<ClientFirebaseConfig?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var error by remember { mutableStateOf<String?>(null) }

                // 🔥 LOAD LOCAL CONFIG (OFFLINE SUPPORT)
                val localConfig = ClientIdStore.getConfig(context)

                if (localConfig != null && config == null) {
                    config = ClientFirebaseConfig(
                        apiKey = localConfig.apiKey,
                        applicationId = localConfig.applicationId,
                        projectId = localConfig.projectId
                    )
                    isLoading = false
                }

                LaunchedEffect(clientId) {

                    // 🔥 Skip API if config already saved
                    if (ClientIdStore.getConfig(context) != null) {
                        isLoading = false
                        return@LaunchedEffect
                    }

                    try {
                        val response = RetrofitInstance.api.getClientConfig(clientId!!)

                        if (response.success) {
                            config = response.data

                            // 🔥 SAVE FOR FUTURE (ONE TIME ONLY)
                            ClientIdStore.saveConfig(
                                context,
                                response.data.apiKey,
                                response.data.applicationId,
                                response.data.projectId
                            )
                        } else {
                            error = "Invalid response"
                        }

                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isLoading = false
                    }
                }

                if (isLoading) {
                    Text("Loading client config...")
                    return@FoodPosTheme
                }

//                if (error != null) {
//                    Text("Error: $error")
//                    return@FoodPosTheme
//                }

                if (error != null) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Error: $error"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {

                                // Clear all saved data
                                ClientIdStore.clear(context)

                                // Reset states
                                clientId = null
                                config = null
                                error = null
                                isLoading = false
                            }
                        ) {
                            Text("Enter Client Key Again")
                        }
                    }

                    return@FoodPosTheme
                }

                if (config == null) {
                    Text("Config not found")
                    return@FoodPosTheme
                }

//                FirebaseApp.getApps(context).forEach {
//                    FirebaseApp.getInstance(it.name).delete()
//                }

                val options = FirebaseOptions.Builder()
                    .setApiKey(config!!.apiKey)
                    .setApplicationId(config!!.applicationId)
                    .setProjectId(config!!.projectId)
                    .build()

               // FirebaseApp.initializeApp(context, options)
                if (FirebaseApp.getApps(context).isEmpty()) {

                    FirebaseApp.initializeApp(
                        context,
                        options
                    )
                }


                val firestore = remember { FirebaseFirestore.getInstance() }
                var firstSyncDone by remember {
                    mutableStateOf(FirstSyncManager.isFirstSyncDone(context))
                }


// =====================================================
// POS RENEWAL CHECK
// =====================================================

                val posRenewalManager = remember {
                    PosRenewalManager(
                        PosRenewalSyncRepository(
                            db = db,
                            firestore = firestore
                        )
                    )
                }

                var renewalChecked by remember {
                    mutableStateOf(false)
                }

                var renewalValid by remember {
                    mutableStateOf(false)
                }


                LaunchedEffect(posRenewalManager) {

                    renewalValid =
                        posRenewalManager.checkAtStartup()

                    renewalChecked = true
                }


// =====================================================
// CHECKING RENEWAL
// =====================================================

                if (!renewalChecked) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "Checking ..."
                        )
                    }

                    return@FoodPosTheme
                }


// =====================================================
// RENEWAL EXPIRED
// =====================================================

                if (!renewalValid) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "POS EXPIRED"
                        )
                    }

                    return@FoodPosTheme
                }


// =====================================================
// RENEWAL VALID
//
// DO NOT RETURN HERE.
//
// The POS UI below this code will now open.
// =====================================================

// RENEW CODE END


                if (!firstSyncDone) {

                    FirstAutoSyncScreen(
                        onFinished = {
                            firstSyncDone = true
                        }
                    )

                    return@FoodPosTheme
                }

                val tableSyncService = remember {
                    TableKotSyncService(
                        firestore = firestore,
                        kotItemDao = db.kotItemDao()
                    )
                }

                LaunchedEffect(Unit) {
                    tableSyncService.cleanupStaleFirestoreTables()
                }


                // ✅ Initialize Firebase dynamically now

                // Inside setContent { ... } in MainActivity

                val globalSyncManager = remember {
                    GlobalOrderSyncManager(
                        firestore = firestore,
                        processedDao = processedDao,
                        kitchenViewModel = kitchenVM,
                      //  waiterkitchenViewModel,
                        role = role ?: PosRole.MAIN,
                    )
                }

// Store globally in activity for later cleanup
                globalOrderSyncManager = globalSyncManager

// Start the appropriate listener based on role
                LaunchedEffect(globalSyncManager, role) {
                    // Always stop any previous listener first
                    globalSyncManager.stopListening()


                    Log.d("KOT_DEBUG", "LaunchedEffect executed. role=$role")

                    globalSyncManager.stopListening()

                    Log.d("KOT_DEBUG", "Calling startListening()")

                    // Start listener automatically according to role
                    globalSyncManager.startListening()
                }

            // ✅ START SERVICE ONLY NOW
                if (role == PosRole.MAIN) {
                    LaunchedEffect(Unit) {
                        val serviceIntent = Intent(context, OnlineOrderMonitoringService::class.java)
                        context.startForegroundService(serviceIntent)
                    }
                }
// ------------------------------------
// CORE SINGLETON OBJECTS (ONCE)
// ------------------------------------

            val ordersRepository = remember { OrdersRepository() }

            val ordersViewModel: OnlineOrdersViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return OnlineOrdersViewModel(printerManager) as T
                    }
                }
            )

            val autoPrintManager = remember {
                AutoPrintManager(
                    printerManager = printerManager,
                    ordersRepository = ordersRepository
                )
            }

            // ------------------------------------
            // REALTIME ORDERS VIEWMODEL (FACTORY)
            // ------------------------------------
            val realtimeOrdersVM: RealtimeOrdersViewModel =
                viewModel(factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return RealtimeOrdersViewModel(
                            application = application,
                            autoPrintManager = autoPrintManager
                        ) as T
                    }
                })



            // ------------------------------------
            // UI STATE
            // ------------------------------------
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var loggedIn by remember {
                    mutableStateOf(PosSessionManager.isLoggedIn(context))
                }

// ------------------------------------
// LOGIN + SYNC VIEWMODELS
// ------------------------------------
                val loginViewModel: PosLoginViewModel = viewModel()
                val syncViewModel: ProductSyncViewModel = viewModel()

                val users by loginViewModel.users.collectAsState()
                val loginLoading by loginViewModel.isLoading.collectAsState()
                val loginError by loginViewModel.error.collectAsState()

// NEW
                val syncing by syncViewModel.syncing.collectAsState()

                if (!loggedIn) {

                    PosLoginScreen(

                        users = users,

                        isLoading = loginLoading,

                        syncing = syncing, // ✅ add this

                        error = loginError,

                        onLoginClick = { user, password ->

                            if (user.loginPin != password) {
                                loginViewModel.showError("Invalid PIN")
                                return@PosLoginScreen
                            }

                            PosSessionManager.login(
                                context = context,
                                userId = user.userId,
                                employeeId = user.employeeId,
                                fullName = user.fullName,
                                mobile = user.mobile,
                                role = user.role
                            )

                            loggedIn = true
                        },

                        onSyncUsersClick = {
                            syncViewModel.syncUsersOnly()
                        }

                    )

                    return@FoodPosTheme
                }
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(270.dp)
                        )
                        {

                            // ✅ Make drawer scrollable
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .padding(bottom = 16.dp) // optional spacing at bottom
                            ) {
                                Log.d("postype", "posType: $posType")
                                // ===== HEADER =====
                                Text(
                                    "Menu",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(16.dp)
                                )


                                when (posType) {

                                    PosType.RETAIL -> RetailMenu(
                                        navController = navController,
                                        drawerState = drawerState,
                                        scope = scope
                                    )

                                    PosType.FAST_FOOD -> FastFoodMenu(
                                        navController = navController,
                                        drawerState = drawerState,
                                        scope = scope,
                                        onLogout = {
                                            loggedIn = false
                                        }
                                    )

                                    PosType.RESTAU -> {
                                        when (role) {

                                            PosRole.MAIN -> RestaurantMainMenu(
                                                navController = navController,
                                                drawerState = drawerState,
                                                scope = scope,
                                                onLogout = {
                                                    loggedIn = false

                                                }
                                            )

                                            PosRole.WAITER -> WaiterMenu(
                                                navController, drawerState, scope, onLogout = {
                                                    loggedIn = false
                                                }
                                            )

                                            else -> RestaurantMainMenu(
                                                navController = navController,
                                                drawerState = drawerState,
                                                scope = scope,
                                                onLogout = {
                                                    loggedIn = false
                                                }
                                            )
                                        }
                                    }

                                    else -> RestaurantMainMenu(
                                        navController = navController,
                                        drawerState = drawerState,
                                        scope = scope,
                                        onLogout = {
                                            loggedIn = false
                                        }
                                    )
                                }


                            }
                        }
                    }
                ) {
                Scaffold(
                    topBar = {

                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        val isOnline by rememberNetworkStatus()

                        CenterAlignedTopAppBar(

                            title = {},

                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = PosTheme.topBar.background,
                                titleContentColor = PosTheme.topBar.content,
                                navigationIconContentColor = PosTheme.topBar.content,
                                actionIconContentColor = PosTheme.topBar.content
                            ),

                            navigationIcon = {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // Drawer button
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Menu"
                                        )
                                    }

                                    // Network indicator
                                    Text(
                                        text = if (isOnline) "🟢" else "🔴",
                                        modifier = Modifier.padding(start = 6.dp),
                                        color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },

                            actions = {

                                val commonShape = RoundedCornerShape(8.dp)
                                val commonHeight = 48.dp




                                if (role == PosRole.MAIN) {

                                    val isFastFood = posType == PosType.FAST_FOOD

                                    // TABLES
                                    if (!isFastFood) {
                                        TopBarNavButton(
                                            selected = currentRoute == "tables",
                                            icon = Icons.Default.TableBar,
                                            description = "",
                                            size = commonHeight,
                                            shape = commonShape,
                                            onClick = {
                                                navController.navigate("tables") {
                                                    launchSingleTop = true
                                                }
                                            }
                                        )

                                        Spacer(Modifier.width(6.dp))
                                    }

                                    // POS
                                    TopBarNavButton(
                                        selected =
                                            if (isFastFood)
                                                currentRoute == "fastfood"
                                            else
                                                currentRoute == "pos",
                                        icon = Icons.Default.PointOfSale,
                                        description = "",
                                        size = commonHeight,
                                        shape = commonShape,
                                        onClick = {
                                            navController.navigate(
                                                if (isFastFood) "fastfood_pos" else "pos"
                                            ) {
                                                launchSingleTop = true
                                            }
                                        }
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    // LOCAL ORDERS
                                    TopBarNavButton(
                                        selected = currentRoute == "local_orders",
                                        icon = Icons.Default.ReceiptLong,
                                        description = "",
                                        size = commonHeight,
                                        shape = commonShape,
                                        onClick = {
                                            navController.navigate("local_orders") {
                                                launchSingleTop = true
                                            }
                                        }
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    StopSoundButton(
                                        onOrdersClick = {
                                            navController.navigate("orders") {
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }

                                if (role == PosRole.WAITER) {

                                    TopBarNavButton(
                                        selected = currentRoute == "posWaiter",
                                        icon = Icons.Default.PointOfSale,
                                        description = "",
                                        size = commonHeight,
                                        shape = commonShape,
                                        onClick = {
                                            navController.navigate("posWaiter") { launchSingleTop = true }
                                        }
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    TopBarNavButton(
                                        selected = currentRoute == "local_orders",
                                        icon = Icons.Default.ReceiptLong,
                                        description = "",
                                        size = commonHeight,
                                        shape = commonShape,
                                        onClick = {
                                            navController.navigate("local_orders") { launchSingleTop = true }
                                        }
                                    )
                                }
                            }
                        )

                    }

                ) { paddingValues ->

                    NavigationHost(
                        navController = navController,
                        printerManager = printerManager,
                        printerPreferences = printerPreferences,
                        printerUploadManager = printerUploadManager,
                        realtimeOrdersViewModel = realtimeOrdersVM,
                        paddingValues = paddingValues,
                        onSavePrinterSettings = { }
                    )
                }

            }
        }}
    }



    override fun onDestroy() {
        super.onDestroy()
        // Stop listener when activity is destroyed
        globalOrderSyncManager.stopListening()
    }


    @Composable
    fun StopSoundButton(
        onOrdersClick: () -> Unit
    ) {
        val context = LocalContext.current
        val commonShape = RoundedCornerShape(8.dp)
        val commonHeight = 48.dp

        IconButton(
            onClick = {
                // 1. Stop ringtone
                val intent = Intent("STOP_RINGTONE").apply {
                    setPackage(context.packageName)
                }

                context.sendBroadcast(intent)

                // 2. Go to Online Orders
                onOrdersClick()
            },
            modifier = Modifier
                .size(commonHeight)
                .background(
                    MaterialTheme.colorScheme.error,
                    shape = commonShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.VolumeOff,
                contentDescription = "Stop Sound",
                tint = Color.White
            )
        }
    }



}

@Composable
fun SidebarSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)


            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge
        )
    }
    Spacer(modifier = Modifier.height(4.dp)) // small separation
}



