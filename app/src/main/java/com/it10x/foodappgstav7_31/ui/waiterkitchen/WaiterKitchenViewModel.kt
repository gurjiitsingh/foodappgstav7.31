package com.it10x.foodappgstav7_31.ui.waiterkitchen

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.compose.foundation.lazy.items

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_31.data.pos.usecase.KotToBillUseCase
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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



import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.models.waiter.WaiterOrder
import com.it10x.foodappgstav7_31.data.online.models.waiter.WaiterOrderItem

import com.it10x.foodappgstav7_31.data.pos.repository.WaiterKitchenRepository

import kotlinx.coroutines.tasks.await
class WaiterKitchenViewModel(
    app: Application,
    private val tableId: String,
    private val tableName: String,
    private val sessionId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val waiterKitchenRepository: WaiterKitchenRepository,
    private val cartViewModel: CartViewModel,
) : AndroidViewModel(app) {

    private var kotPrintJob: Job? = null
    private val pendingKotItems = mutableListOf<PosKotItemEntity>( )
    private var pendingBatchId: String? = null
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading
    private val kotItemDao =
        AppDatabaseProvider.get(app).kotItemDao()

    private val _sendSuccess = MutableStateFlow(false)
    val sendSuccess: StateFlow<Boolean> = _sendSuccess

    private val kotToBillUseCase =
        KotToBillUseCase(kotItemDao)

    val kotItems: StateFlow<List<PosKotItemEntity>> =
        kotItemDao.getAllKotItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private var isProcessing = false

    private val kotRepository = KotRepository(
        AppDatabaseProvider.get(app).kotBatchDao(),
        AppDatabaseProvider.get(app).kotItemDao(),
        AppDatabaseProvider.get(app).tableDao()
    )

    private val cartRepository = CartRepository(
        AppDatabaseProvider.get(app).cartDao(),
        AppDatabaseProvider.get(app).tableDao()
    )

    private val printerManager =
        PrinterManager.getInstance(app.applicationContext)


    fun waiterCartTo_FireStore_Bill(
        cartList: List<PosCartEntity>,
        tableNo: String,
        deviceId: String,
        deviceName: String?,
        role: String
    ) {
        Log.d("KOT_DEBUG","WaiterKitchenViewModel is sending Waiter kot--------------")
        // ✅ Prevent double click immediately
        if (isProcessing) return
        isProcessing = true

        viewModelScope.launch {

            val dao = AppDatabaseProvider.get(getApplication()).cartDao()

            val latestCart = try {
                dao.getCartItemsByTableId(tableNo).first()
            } catch (e: Exception) {
                Log.e(
                    "WaiterKitchenVM",
                    "❌ Failed to load cart from DB: ${e.message}",
                    e
                )
                emptyList()
            }

            if (latestCart.isEmpty()) {
                Log.w(
                    "WaiterKitchenVM",
                    "⚠️ No items found in DB for table=$tableNo"
                )

                // ✅ unlock
                isProcessing = false
                return@launch
            }

            _loading.value = true

            try {

                latestCart.forEach {
                    Log.d(
                        "WAITER_SEND",
                        """
        product=${it.name}
        createdById=${it.createdById}
        createdByName=${it.createdByName}
        """.trimIndent()
                    )
                }

                val success = withContext(Dispatchers.IO) {
                    waiterKitchenRepository.sendOrderToFireStore(
                        cartList = latestCart,
                        tableNo = tableNo,
                        tableName = tableName,
                        sessionId = sessionId,

                        orderType = orderType,
                        deviceId = deviceId,
                        deviceName = deviceName
                    )
                }

                if (!success) {
                    Log.e(
                        "WaiterKitchenVM",
                        "❌ Firestore upload failed"
                    )
                    return@launch
                }

                repository.clearCart(orderType, tableNo)
                cartRepository.syncCartCount(tableNo)

                Log.d(
                    "WaiterKitchenVM",
                    "✅ Firestore + Bill saved successfully"
                )

                _sendSuccess.value = true

            } catch (e: Exception) {

                Log.e(
                    "WaiterKitchenVM",
                    "❌ Error in waiterCartToBill",
                    e
                )

            } finally {

                _loading.value = false

                // ✅ always unlock
                isProcessing = false
            }
        }
    }




    fun posFireStoreToWaiterBill(
        cartList: List<PosCartEntity>,
        tableNo: String,
        deviceId: String,
        deviceName: String?,
        role : String
    ) {
        if (isProcessing) return   // 🔥 Prevent duplicate presses

        viewModelScope.launch {
            // Always get fresh items from DB
            val dao = AppDatabaseProvider.get(getApplication()).cartDao()
            val latestCart = try {
                dao.getCartItemsByTableId(tableNo).first()
            } catch (e: Exception) {
                Log.e("WaiterKitchenVM", "❌ Failed to load cart from DB: ${e.message}", e)
                emptyList()
            }

            if (latestCart.isEmpty()) {
                Log.w("WaiterKitchenVM", "⚠️ No items found in DB for table=$tableNo")
                return@launch
            }


            if (isProcessing) return@launch
            isProcessing = true
            _loading.value = true

            try {
                val billSaved = saveCartItemToBillView(
                    orderType = orderType,
                    sessionId = sessionId,
                    tableNo = tableNo,
                    cartItems = cartList,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = "appVersion",
                    role = role,
                )

                if (!billSaved) {
                    Log.e("WaiterKitchenVM", "❌ Bill save failed")
                    return@launch
                }

               // repository.clearCart(orderType, tableNo)
                cartRepository.syncCartCount(tableNo)

                Log.d("WaiterKitchenVM", "✅ Firestore + Bill saved successfully")

                // ✅ MOVE HERE
                _sendSuccess.value = true

            } catch (e: Exception) {
                Log.e("WaiterKitchenVM", "❌ Error in waiterCartToBill", e)
            } finally {

                // ⭐ VERY IMPORTANT
                _loading.value = false
                isProcessing = false
            }
        }





    }




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

            val batch = PosKotBatchEntity(
                id = batchId,
                kotNumber = "",
                sessionId = sessionId,
                tableNo = tableNo,
                tableName = tableName,
                orderType = orderType,
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion,
                createdAt = now,
                sentBy = "WAITRER",
                syncStatus = "DONE",
                lastSyncedAt = null
            )

            kotBatchDao.insert(batch)

            Log.d("SYNC", "--- WaiterKitchenViewmodel----1")

//            cartItems.forEach {
//                Log.d(
//                    "KOT_DEBUG",
//                    "Item: ${it.name} qty=${it.quantity}"
//                )
//            }

            val items = cartItems.map { cart ->
          //    Log.d("KOT_DEBUG", "Saving item: ${cart.name} qty=${cart.quantity}")
                PosKotItemEntity(
                    id = UUID.randomUUID().toString(),
                    kotNumber = "",
                    sessionId = sessionId,
                    kotBatchId = batchId,
                    tableNo = tableNo,
                    tableName = tableName,
                    productId = cart.productId,
                    name = cart.name,
                    productMode = cart.productMode ,
                    currentStock = cart.currentStock,
                    categoryId = cart.categoryId,
                    categoryName = cart.categoryName,
                    createdById = cart.createdById,
                    createdByName = cart.createdByName,

                    parentId = cart.parentId,
                    isVariant = cart.isVariant,
                    basePrice = cart.basePrice,
                    finalPrice = cart.finalPrice,
                    modifierTotal = cart.modifierTotal,
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



    fun getPendingItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {


        return if (orderType == "DINE_IN") {
            kotItemDao.getPendingItemsForTable(orderRef)
        } else {
            kotItemDao.getPendingItemsForTable(orderType)
          //  kotItemDao.getPendingItemsForSession(orderRef)
        }
    }
     // ✅ POS signal: kitchen completed for table
    fun resetSendSuccess() {
        _sendSuccess.value = false
    }

}





