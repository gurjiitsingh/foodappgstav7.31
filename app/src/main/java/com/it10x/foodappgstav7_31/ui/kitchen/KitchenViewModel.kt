package com.it10x.foodappgstav7_31.ui.kitchen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.ReceiptPrintMode
import com.it10x.foodappgstav7_31.data.online.sync.SyncManagerProvider

import com.it10x.foodappgstav7_31.data.online.sync.TableKotSyncService
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotBatchEntity

import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_31.data.pos.usecase.KotToBillUseCase
import com.it10x.foodappgstav7_31.printer.PrinterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import com.it10x.foodappgstav7_31.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_31.data.pos.repository.VirtualTableRepository
import com.it10x.foodappgstav7_31.data.pos.manager.TableSyncManager
import com.it10x.foodappgstav7_31.printer.ReceiptFormatter
import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.flow.asStateFlow

class KitchenViewModel(
    app: Application,
    private val tableId: String,
    private val tableName: String,
    private val sessionId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val prefs: PrinterPreferences,


    ) : AndroidViewModel(app) {

    //  var isFromFirestore = false
    private val firestore = FirebaseFirestore.getInstance()

    private val db = AppDatabaseProvider.get(app)
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading
    private val kotItemDao =
        AppDatabaseProvider.get(app).kotItemDao()


    private val kotToBillUseCase =
        KotToBillUseCase(kotItemDao)

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    val kotItems: StateFlow<List<PosKotItemEntity>> =
        kotItemDao.getAllKotItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )


    private val kotRepository = KotRepository(
        db.kotBatchDao(),
        db.kotItemDao(),
        db.tableDao()
    )

    private val cartRepository = CartRepository(
        db.cartDao(),
        db.tableDao()
    )

    private val virtualTableRepository = VirtualTableRepository(
        db = db,
        AppDatabaseProvider.get(app).virtualTableDao(),
        AppDatabaseProvider.get(app).cartDao(),
        AppDatabaseProvider.get(app).kotItemDao(),
        AppDatabaseProvider.get(app).virtualTableCounterDao()
    )


    val kotHistory =
        kotRepository
            .getKotHistory()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )



    private val tableSyncManager = TableSyncManager(
        tableRepo = kotRepository,
        cartRepo = cartRepository,
        virtualRepo = virtualTableRepository
    )

    val printerManager =
        PrinterManager.getInstance(getApplication<Application>().applicationContext)


    private val tableKotSyncService = TableKotSyncService(
        firestore,
        kotItemDao
    )


    fun getPendingItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {


        return if (orderType == "DINE_IN" || orderType == "TAKEAWAY" || orderType == "DELIVERY") {
            kotItemDao.getPendingItemsForTable(orderRef)
        } else {
            kotItemDao.getPendingItemsForTable(orderType)
            //  kotItemDao.getPendingItemsForSession(orderRef)
        }
    }



    // *************************************************
    // THIS FUNCTION RECEIVE ITEM FROM MAIN POS  ((((((((BUTTON)))))))
    // *************************************************

    fun cartToKotMainPOS(
        orderType: String,
        tableNo: String,
        tableName: String,
        sessionId: String,
        paymentType: String,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
    )
    {
        viewModelScope.launch {

            if (_isSending.value) return@launch

            _isSending.value = true
            _loading.value = true

            val tableId = tableNo

            val cartList = repository.getCartItemsByTableId(tableId).first()


            if (cartList.isEmpty()) {
                _isSending.value = false
                _loading.value = false
                return@launch
            }
            Log.d("KOT_DEBUG", "--- MAIN POS BUTTON----")
            try {
                val kotSaved = withContext(Dispatchers.IO) {
                    saveKotFromMainPOSandWairterFirestore(
                        orderType = orderType,
                        sessionId = sessionId,
                        tableNo = tableNo,
                        tableName = tableName,
                        cartItems = cartList,
                        deviceId = deviceId,
                        deviceName = deviceName,
                        appVersion = appVersion,
                        role = role,
                        source = "POS",
                    )
                }

                if (!kotSaved) {
                    return@launch
                }

            } finally {
                _isSending.value = false
                _loading.value = false
            }

            // ✅ background work AFTER UI state is reset
            launch(Dispatchers.IO) {
                try {
                    repository.clearCart(orderType, tableId)
                    tableSyncManager.syncCart(tableId, orderType)
                    tableSyncManager.syncBill(tableId, orderType)
                } catch (e: Exception) {
                    Log.e("SYNC", "Background sync failed", e)
                }
            }
        }
    }


    // ********************************************************
    // THIS FUNCTION RECEIVE ITEM FORM WAITER THROUGH FIRESTORE
    // ********************************************************

    suspend fun saveKotFromFirestoreWaiter(
        orderType: String,
        sessionId: String,
        tableNo: String,
        tableName: String,
        cartItems: List<PosCartEntity>,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
        source: String,
    )
    {


        //THIS FUNCTION RECEIVE DATA FROM WAITER POS 1.
        if (cartItems.isEmpty()) {
            Log.w("KOT_BRIDGE", "⚠️ createKotAndPrint called with empty cartItems")
            return
        }
        Log.d("KOT_DEBUG", "--- FIRESTORE DIRECT----")

        _loading.value = true

        try {
            val saved = saveKotFromMainPOSandWairterFirestore(
                orderType = orderType,
                sessionId = sessionId,
                tableNo = tableNo,
                tableName = tableName,
                cartItems = cartItems,
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion,
                role = role,
                source = source,
            )

            if (!saved) {
                Log.e("KOT_BRIDGE", "❌ Failed to create KOT + Print")
                return
            }

            kotRepository.syncBillCount(tableNo)



            Log.d(
                "TABLE_SYNC",
                "✅ POS table snapshot uploaded after waiter order"
            )
        } catch (e: Exception) {
            Log.e("KOT_BRIDGE", "❌ Exception in createKotAndPrint()", e)
        } finally {
            _loading.value = false
        }
    }





    // ****************************************
    // PRIVATE USED RECIVE DATA FROM FUNCITONS, (MAIN POS, WAITER DEVICE)
    // ****************************************

    private suspend fun saveKotFromMainPOSandWairterFirestore(
        orderType: String,
        sessionId: String,
        tableNo: String?,
        tableName: String,
        cartItems: List<PosCartEntity>,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
        source: String,
    ): Boolean = withContext(Dispatchers.IO) {
          Log.d("KOT_DEBUG", "tableName FROM FIRESORE: ${tableName}")
        //FROM MAIN POS AND
        //FROM FIRESTORE WAITER POS
        val kotNumber = kotRepository.generateNextKotNumber()
        val tableNo = tableNo ?: "";
        try {
            val db = AppDatabaseProvider.get(printerManager.appContext())
            val kotBatchDao = db.kotBatchDao()
            val kotItemDao = db.kotItemDao()

            val batchId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val batch = PosKotBatchEntity(
                id = batchId,
                kotNumber = kotNumber,
                sessionId = sessionId,
                tableNo = tableNo,
                tableName = tableName,
                orderType = orderType,
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion,
                createdAt = now,
                sentBy = null,
                syncStatus = "DONE",
                lastSyncedAt = null
            )

            kotBatchDao.insert(batch)

            val items = cartItems.map { cart ->
              //  Log.d("ORDER_TYPE_TRACE", "orderType=$orderType  tableNo=${tableNo}")

                val modifierTotal = ModifierJsonHelper
                    .fromJson(cart.modifiersJson)
                    .sumOf { group -> group.items.sumOf { it.price } }

                val finalPrice = cart.basePrice + modifierTotal

                PosKotItemEntity(
                    id = UUID.randomUUID().toString(),
                    kotNumber = kotNumber,
                    sessionId = sessionId,
                    kotBatchId = batchId,
                    tableNo = tableNo,
                    tableName = tableName,
                    productId = cart.productId,
                    name = cart.name,
                    productMode = cart.productMode,
                    currentStock = cart.currentStock,
                    categoryId = cart.categoryId,
                    categoryName = cart.categoryName,
                    createdById = cart.createdById,
                    createdByName = cart.createdByName,
                    parentId = cart.parentId,
                    isVariant = cart.isVariant,
                    basePrice = cart.basePrice,
                    discountEligible = cart.discountEligible,
                    finalPrice = finalPrice,
                    modifierTotal = modifierTotal,
                    quantity = cart.quantity,
                    taxCategory = cart.taxCategory,
                    taxRate = cart.taxRate,
                    taxType = cart.taxType,
                    taxRate1 = cart.taxRate1,
                    taxType1 = cart.taxType1,
                    note = cart.note,
                    modifiersJson = cart.modifiersJson,
                    kitchenPrintReq = cart.kitchenPrintReq,
                    kitchenPrinted = false,
                    status = "DONE",
                    createdAt = now
                )
            }
            val createdByName = cartItems.firstOrNull()?.createdByName ?: ""
            val createdById = cartItems.firstOrNull()?.createdById ?: ""
          //  Log.d("KOT_DEBUG", "---- MainKitchenViewmodel----source:${source}")
            kotRepository.insertItemsInBill(tableNo, items, role)
            kotRepository.saveHistory(
                kotNumber = kotNumber,
                batch = batch,
                items = items,
                source = source,
                createdByName = createdByName,

            )
            kotRepository.syncBillCount(tableId)
           val Pritnter_role = "KITCHEN"
            val printMode = prefs.getReceiptPrintMode(
                PrinterRole.valueOf(Pritnter_role)
            )



            withContext(Dispatchers.IO) {

                val printItems = lockAndFetchBatch(batchId)

                if (printItems.isNotEmpty()) {

                    when (printMode) {

                        ReceiptPrintMode.TEXT -> {

                            printerManager.enqueueKitchen(
                                sessionKey = tableNo,
                                tableName = tableName,
                                orderType = orderType,
                                kotNumber = kotNumber,
                                referenceId = batchId,
                                items = printItems,
                                earnedPoints = 0,
                            )
                        }


                        ReceiptPrintMode.IMAGE -> {

                            printerManager.enqueueKitchenImage(
                                sessionKey = tableNo,
                                tableName = tableName,
                                orderType = orderType,
                                kotNumber = kotNumber,
                                referenceId = batchId,
                                items = printItems,
                                earnedPoints = 0,
                            )
                        }
                    }
                }
            }



            // 🔥 UPDATE WAITER TABLE SNAPSHOT
            // 🔥 ADD THIS HERE
            //**********************************************************
            //  UPDATE WAITER VIEW STATUS
            //**********************************************************
//            withContext(Dispatchers.IO) {
//                tableKotSyncService.syncTableSnapshot(
//                    tableId = tableNo,
//                    source = "POS"
//                )
//            }

            //**********************************************************
            // INSTEAD WE WILL USE QUEUE TO UPDATE WAITER VIEW STATUS
            //**********************************************************
            SyncManagerProvider
                .get()
                .addTableUpdate(tableNo)

//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//
//                    val printItems = lockAndFetchBatch(batchId)
//
//                    printerManager.enqueueKitchenImage(
//                        sessionKey = tableNo,
//                        tableName = tableName,
//                        orderType = orderType,
//                        kotNumber = kotNumber,
//                        items = printItems
//                    )
//
////                                        printerManager.enqueueKitchen(
////                            sessionKey = tableNo,
////                            orderType = orderType,
////                            kotNumber = kotNumber,
////                            items = printItems
////                        )
//
////                    if (printItems.isNotEmpty()) {
////
////
////                        when (printMode) {
////
////                            ReceiptPrintMode.TEXT -> {
////
////                    printerManager.enqueueKitchen(
////                            sessionKey = tableNo,
////                            orderType = orderType,
////                            kotNumber = kotNumber,
////                            items = printItems
////                        )
////                            }
////
////                            ReceiptPrintMode.IMAGE -> {
////
////                                printerManager.enqueueKitchenImage(
////                                    sessionKey = tableNo,
////                                    tableName = tableName,
////                                    orderType = orderType,
////                                    kotNumber = kotNumber,
////                                    items = printItems
////                                )
////                            }
////                        }
////
////
////
////
////                    }
//
//                    tableKotSyncService.syncTableSnapshot(
//                        tableId = tableNo,
//                        source = source
//                    )
//
//                } catch (e: Exception) {
//                    Log.e("ASYNC_TASK", "❌ Background failed", e)
//                }
//            }




            true
        } catch (e: Exception) {
            Log.e("KOT", "❌ Failed to save KOT", e)
            false
        }
    }

    //*************************************************
    //THIS CODE IS TESTED FOR LONG TIME
    //*************************************************


    @Transaction
    suspend fun lockAndFetchBatch(batchId: String): List<PosKotItemEntity> {

        // 1. fetch FIRST
        val items = kotItemDao.getItemsByBatchId(batchId)

        if (items.isEmpty()) return emptyList()

        // 2. then mark as printed
        val updated = kotItemDao.markBatchKitchenPrintedBatch(batchId)

        if (BuildConfig.DEBUG) {
            Log.e("LOCK_BATCH", "batch=$batchId updatedRows=$updated")
        }

        return items
    }



    //*************************************************
    //THIS CODE IS NEW
    //***************************************************
//    @Transaction
//    suspend fun lockAndFetchBatch(batchId: String): List<PosKotItemEntity> {
//
//        val updated = kotItemDao.markBatchKitchenPrintedBatch(batchId)
//
//        if (updated == 0) {
//            Log.d("LOCK_BATCH", "Already printed batch=$batchId")
//            return emptyList()
//        }
//
//        return kotItemDao.getItemsByBatchId(batchId)
//    }

    //*******************************************
    //  GlobalOrderSyncManager LISTENER
    //*********************************************
    //****************************************************************************************
    // * 1 THIS FUNCTION USED TO  FORCE UPDATED WAITER TABLE BY MAIN POS IF NOT UPDATED BY SYNC*
    // ***************************************************************************************
    suspend fun replaceKotFromFirestoreWaiterListener(
        tableId: String,
        sessionId: String,
        items: List<Map<String, Any>>,
        source: String
    ) {


        try {

            if (source != "FIRESTORE") {
                Log.d("SYNC_VM", "⛔ Ignored non-firestore source")
                return
            }

            Log.e(
                "FIRESTORE_IMPORT",
                "TABLE=$tableId ITEMS=${items.size}"
            )

            Log.d(
                "SYNC_VM",
                "DELETE table=$tableId items=${items.size}"
            )

            kotRepository.deleteKotByTable(tableId)



            if (items.isEmpty()) {
                Log.d("SYNC_VM", "🪹 Table empty after delete: $tableId")
                return
            }

            val cartList = items.map { item ->
                PosCartEntity(
                    sessionId = sessionId,
                    tableId = tableId,
                    tableName = tableName,
                    productId = item["productId"]?.toString() ?: "",
                    name = item["name"]?.toString() ?: "",
                    productMode =
                        item["productMode"]?.toString()
                            ?: "raw_stock",
                    currentStock =
                        (item["currentStock"] as? Number)
                            ?.toDouble() ?: 0.0,
                    categoryId = "",
                    categoryName = item["category"]?.toString() ?: "",
                    parentId = null,
                    isVariant = false,
                    basePrice = (item["price"] as? Number)?.toDouble() ?: 0.0,
                    finalPrice = 0.0,
                    modifierTotal = 0.0,
                    quantity = (item["quantity"] as? Number)?.toInt() ?: 1,
                    taxRate = 0.0,
                    taxType = "exclusive",
                    taxCategory = item["taxCategory"]?.toString() ?: "",

                    taxRate1 =0.0,
                    taxType1 = "exclusive",

                    note = item["note"]?.toString() ?: "",
                    modifiersJson = "",
                    kitchenPrintReq = false,
                    createdAt = System.currentTimeMillis()
                )
            }
            //****************************************************************************************
            // * 2 THIS FUNCTION USED TO  FORCE UPDATED WAITER TABLE BY MAIN POS IF NOT UPDATED BY SYNC*
            // ***************************************************************************************
            saveCartItemToBillView(
                orderType = "DINE_IN",
                sessionId = sessionId,
                tableNo = tableId,
                cartItems = cartList,
                deviceId = "FIRESTORE_SYNC",
                deviceName = "FIRESTORE_SYNC",
                appVersion = "FIRESTORE_SYNC",
                role = "FIRESTORE_TABLE"
            )

        } catch (e: Exception) {
            Log.e("SYNC_VM", "❌ replaceKotFromFirestore failed", e)
        }
    }

    //****************************************************************************************
    // * 2 THIS FUNCTION USED TO  FORCE UPDATED WAITER TABLE BY MAIN POS IF NOT UPDATED BY SYNC*
    // ***************************************************************************************
    private suspend fun saveCartItemToBillView(
        orderType: String,
        sessionId: String,
        tableNo: String?,
        cartItems: List<PosCartEntity>,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
    ): Boolean = withContext(Dispatchers.IO) {

        val tableNo = tableNo?: "";
        try {
            val db = AppDatabaseProvider.get(printerManager.appContext())
            val kotBatchDao = db.kotBatchDao()
            val kotItemDao = db.kotItemDao()

            val batchId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            repository.markAllSent(tableNo)

            // NOW THIS IS COMMENTED BECAUSE IT IS DUPLICATE

//            val batch = PosKotBatchEntity(
//                id = batchId,
//                kotNumber = "DummykotNumber",
//                sessionId = sessionId,
//                tableNo = tableNo,
//                tableName = tableName,
//                orderType = orderType,
//                deviceId = deviceId,
//                deviceName = deviceName,
//                appVersion = appVersion,
//                createdAt = now,
//                sentBy = "WAITRER",
//                syncStatus = "DONE",
//                lastSyncedAt = null
//            )
//
//            kotBatchDao.insert(batch)

            Log.d("KOT_DEBUG", "Force updated Waiter tabele-------------------")



            val items = cartItems.map { cart ->
                //    Log.d("KOT_DEBUG", "Saving item: ${cart.name} qty=${cart.quantity}")
                PosKotItemEntity(
                    id = UUID.randomUUID().toString(),
                    kotNumber = "dummykotNumber",
                    sessionId = sessionId,
                    kotBatchId = batchId,
                    tableNo = tableNo,
                    tableName = tableName,
                    productId = cart.productId,
                    name = cart.name,
                    productMode = cart.productMode,
                    currentStock = cart.currentStock,
                    categoryId = cart.categoryId,
                    categoryName = cart.categoryName,
                    parentId = cart.parentId,
                    isVariant = cart.isVariant,
                    basePrice = cart.basePrice,
                    finalPrice = 0.0,
                    modifierTotal = 0.0,
                    quantity = cart.quantity,
                    taxCategory = cart.taxCategory,
                    taxRate = cart.taxRate,
                    taxType = cart.taxType,
                    taxRate1 = cart.taxRate1,
                    taxType1 = cart.taxType1,
                    note = cart.note,
                    modifiersJson = cart.modifiersJson,
                    kitchenPrinted = true,
                    status = "DONE",
                    createdAt = now
                )
            }

            kotRepository.insertItemsInBill(tableNo, items,role)
            kotRepository.markDoneAll(tableNo)
            kotRepository.syncKinchenCount(tableNo)
            kotRepository.syncBillCount(tableNo)

            true

        } catch (e: Exception) {
            Log.e("KOT", "❌ Failed to save KOT", e)
            false
        }
    }


}


//
//saveKotFromFirestoreWaiter() ✅
//
//Receives a brand new kitchen order.
//Should generate a new kotNumber.
//Should create a new batch.
//Should save history.
//Should print.
//
//replaceKotFromFirestoreWaiterListener() ❌ (currently)
//
//Receives a table state synchronisation.
//It should not generate a new kotNumber.
//It should not create a new batch.
//It should not save history.
//It should not print.
//
//I would refactor saveCartItemToBillView()
//into something like restoreTableSnapshot(),
//making it clear that its job is only to restore the local table state, not to
//create a new KOT. That separation will also eliminate the duplicate KOT numbers you're seeing.

//GlobalOrderSyncManager
//│
//▼
//startMainPosListener()
//│
//▼
//waiter_orders collection
//│
//▼
//saveKotFromFirestoreWaiter()
//│
//▼
//saveKotFromMainPOSandWairterFirestore()