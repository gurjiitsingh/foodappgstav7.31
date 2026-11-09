package com.it10x.foodappgstav7_31.ui.cart

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_31.data.pos.entities.ProductEntity
import com.it10x.foodappgstav7_31.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_31.data.pos.repository.CategoryRepository
import com.it10x.foodappgstav7_31.domain.usecase.TableReleaseUseCase
import com.it10x.foodappgstav7_31.ui.pos.toTitleCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.it10x.foodappgstav7_31.data.pos.repository.VirtualTableRepository
import com.it10x.foodappgstav7_31.data.pos.manager.TableSyncManager
import com.it10x.foodappgstav7_31.fiskaly.FiskalyService
import com.it10x.foodappgstav7_31.fiskaly.FiskalyServiceFactory
import com.it10x.foodappgstav7_31.auth.PosSessionManager

sealed class CartUiEvent {
    object SessionRequired : CartUiEvent()
    object TableRequired : CartUiEvent()
}

class CartViewModel(
    private val app: Application,
    private val repository: CartRepository,
    private val categoryRepository: CategoryRepository,
    private val tableReleaseUseCase: TableReleaseUseCase,
    private val tableSyncManager: TableSyncManager,
    private val virtualTableRepository: VirtualTableRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentTableId =
        savedStateHandle.getStateFlow<String?>("tableId", null)
    private val currentTableName =
        savedStateHandle.getStateFlow<String?>("tableName", null)
    private val currentOrderType =
        savedStateHandle.getStateFlow("orderType", "DINE_IN")

    private val _uiEvent = MutableSharedFlow<CartUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    // ---------- SESSION ----------
    private val sessionId = savedStateHandle.getStateFlow<String?>("sessionId", null)

    private val createdById = savedStateHandle.getStateFlow<String?>("createdById", null)

    private val createdByName = savedStateHandle.getStateFlow<String?>("createdByName", null)

    val  sessionKey: StateFlow<String?> = sessionId
    // ---------- CART ----------
    val cart: StateFlow<List<PosCartEntity>> =
        combine(currentOrderType, currentTableId) { _, _ ->
            cartScopeKey()
        }
            .filterNotNull()
            .flatMapLatest { scopeKey ->
                repository.observeCart(scopeKey)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())



    // ---------- SETTERS ----------
    private fun canMutateCart(): Boolean {

//    Log.d(
//        "CART_DEBUG",
//        "canMutateCart (In CartViewModel:canMutateCart)  currentOrderType.value=${currentOrderType.value} currentTableId.value=${currentTableId.value} "
//    )


        return when (currentOrderType.value) {
            "DINE_IN" -> !currentTableId.value.isNullOrBlank()
            "TAKEAWAY" -> !currentTableId.value.isNullOrBlank()
            "DELIVERY" -> !currentTableId.value.isNullOrBlank()
            else -> true //NOT :-- TAKEAWAY / DELIVERY always allowed
        }
    }

    // ---------- MUTATIONS ----------
    fun addProductToCart(
        product: ProductEntity,
        price: Double,
        modifiersJson: String = ""
    ) {



        val modifierTotal = ModifierJsonHelper
            .fromJson(modifiersJson)
            .sumOf { group ->
                group.items.sumOf { it.price }
            }
       val finalPrice = price + modifierTotal




        viewModelScope.launch {

//            Log.d(
//                "POS_ORDER",
//                "in cart orderType=${currentOrderType.value}, createdByName=${createdByName.value}, tableId=${currentTableId.value} , TableName=${currentTableName.value}, sessionId=${sessionId.value}"
//            )
            if (sessionId.value.isNullOrBlank()) {
                _uiEvent.emit(CartUiEvent.SessionRequired)
                initSession(currentOrderType.value, currentTableId.value)
            }

            if (!canMutateCart()) {
                _uiEvent.emit(CartUiEvent.TableRequired)
                return@launch
            }

            val category = categoryRepository.getCategoryById(product.categoryId)

            val resolvedKitchenPrint =
                product.kitchenPrintReq
                    ?: category?.kitchenPrintReq
                    ?: true



            val cartItem = PosCartEntity(
                productId = product.id,
                name = toTitleCase(product.name),
                basePrice = price,
                discountEligible = product.discountEligible,
                finalPrice = finalPrice,
                modifierTotal = modifierTotal,
                productMode = product.productMode  ?: "raw_stock",
                currentStock = product.currentStock ?: 0.0,
                note = "",
                modifiersJson = modifiersJson,
                quantity = 1,
                taxRate = product.taxRate ?: 0.0,
                taxType = product.taxType ?: "inclusive",
                taxRate1 = product.taxRate1 ?: 0.0,
                taxType1 = product.taxType1 ?: "inclusive",
                taxCategory = product.taxCategory,
                parentId = null,
                isVariant = false,
                categoryId = product.categoryId,
                categoryName = product.productCat,
                kitchenPrintReq = resolvedKitchenPrint,
                createdById = createdById.value ?: "",
                createdByName = createdByName.value ?: "",
                sessionId = sessionId.value!!,
                tableId = currentTableId.value,
                tableName = currentTableName.value,
            )



            val type = currentOrderType.value

// =====================================================
// FIND ACTIVE TABLE BEFORE ADDING CART ITEM
// =====================================================

            var tableId = currentTableId.value
            var tableName = currentTableName.value



// =====================================================
// NOW CREATE CART ITEM USING ACTIVE TABLE
// =====================================================

            val finalCartItem = cartItem.copy(
                sessionId = sessionId.value ?: return@launch,
                tableId = tableId,
                tableName = tableName
            )

            repository.addToCart(
                finalCartItem,
                tableId ?: return@launch
            )

// =====================================================
// MARK TW/DL AS RUNNING
// =====================================================

            virtualTableRepository.markRunningIfNew(
                tableId = tableId ?: return@launch,
                orderType = type
            )

// =====================================================
// SYNC
// =====================================================

            tableSyncManager.syncCart(
                tableId ?: return@launch,
                type
            )

            tableSyncManager.syncBill(
                tableId ?: return@launch,
                type
            )
        }
    }

    fun increase(item: PosCartEntity) {

        if (!canMutateCart()) return

        viewModelScope.launch {
            repository.increaseById(item.id, item.tableId!!)
        }
    }



    fun decrease(productId: String, tableNo: String) {
        if (!canMutateCart()) return

        viewModelScope.launch {

            repository.decrease(productId, tableNo)

            val currentTable = currentTableId.value ?: return@launch
            val type = currentOrderType.value

            tableSyncManager.syncCart(currentTable, type)
            tableSyncManager.syncBill(currentTable, type)
        }
    }



    fun initSession(orderType: String, tableId: String? = null, tableName: String? = null) {



        val resolvedTableId = when (orderType) {
            "DINE_IN" -> tableId
            "TAKEAWAY" -> tableId
            "DELIVERY" -> tableId
            else -> null
        }


        if (resolvedTableId.isNullOrBlank()) {
            Log.e(
                "SESSION_DEBUG",
                "initSession FAILED: tableId is null for orderType=$orderType"
            )
            return
        }

        // Prevent duplicate session
        if (
            sessionId.value != null &&
            currentOrderType.value == orderType &&
            currentTableId.value == resolvedTableId
        ) {
            Log.d(
                "SESSION_DEBUG",
                "Session already active. sessionId=${sessionId.value}"
            )
            return
        }

        val sid = "$orderType-$resolvedTableId-${System.currentTimeMillis()}"

        savedStateHandle["orderType"] = orderType
        savedStateHandle["tableId"] = resolvedTableId
        savedStateHandle["sessionId"] = sid
        savedStateHandle["tableName"] = tableName

        savedStateHandle["createdById"] = PosSessionManager.getUserId(app)
        savedStateHandle["createdByName"] = PosSessionManager.getFullName(app)

        Log.d(
            "SESSION_DEBUG",
            """
        Session created
        sessionId = $sid
        orderType = ${savedStateHandle.get<String>("orderType")}
        tableId   = ${savedStateHandle.get<String>("tableId")}
         tableName   = ${savedStateHandle.get<String>("tableName")}
        createdBy = ${savedStateHandle.get<String>("createdByName")}
        """.trimIndent()
        )
    }

    fun getCreatedById(): String? =
        createdById.value

    fun getCreatedByName(): String? =
        createdByName.value

    private fun cartScopeKey(): String? {
        return when (currentOrderType.value) {
            "DINE_IN" -> currentTableId.value
            "TAKEAWAY" -> currentTableId.value
            "DELIVERY" -> currentTableId.value
            else -> null
        }
    }


    fun updateNote(item: PosCartEntity, note: String?) {
        viewModelScope.launch {
            repository.updateNote(item, note)
        }
    }

    fun togglePrint(item: PosCartEntity) {
        viewModelScope.launch {
            repository.updatePrintFlag(
                id = item.id,
                value = !item.kitchenPrintReq
            )
        }
    }


}
