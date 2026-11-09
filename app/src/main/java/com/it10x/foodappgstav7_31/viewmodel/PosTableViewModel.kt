package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.online.sync.TableKotSyncService
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.TableEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.it10x.foodappgstav7_31.data.pos.repository.TableRepository
import com.it10x.foodappgstav7_31.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_31.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_31.data.pos.repository.OrderSequenceRepository

object TableStatus {

    const val OCCUPIED = "OCCUPIED"
    const val AVAILABLE = "AVAILABLE"
    const val ORDERING = "ORDERING"
    const val KITCHEN = "KITCHEN"
    const val KITCHEN_PRINTED = "KITCHEN_PRINTED"
    const val BILL = "BILL"
    const val BILL_REQUESTED = "BILL_REQUESTED"
}

class PosTableViewModel(app: Application) : AndroidViewModel(app) {

    // ✅ FIRST — StateFlow
    private val _tables = MutableStateFlow<List<TableUiState>>(emptyList())
    val tables: StateFlow<List<TableUiState>> = _tables

    private val _tableItems =
        MutableStateFlow<List<PosKotItemEntity>>(emptyList())

    val tableItems: StateFlow<List<PosKotItemEntity>> = _tableItems

    // ✅ THEN dao
    private val database = AppDatabaseProvider.get(app)

    private val dao = database.tableDao()
    private val orderDao = database.orderMasterDao()
    private val repository = TableRepository(dao)

    private val firestore = FirebaseFirestore.getInstance()
    private val cartRepository = CartRepository(
        dao = database.cartDao(),
        tableDao = dao
    )

    private val kotRepository = KotRepository(
        batchDao = database.kotBatchDao(),
        kotItemDao = database.kotItemDao(),
        tableDao = dao
    )

    private val tableKotSyncService = TableKotSyncService(
        firestore,
        kotItemDao = database.kotItemDao(),
    )

    private val orderSequenceRepository =
        OrderSequenceRepository(database)

    init {
        observeTables()
        refreshAllTableCounts()
    }


    data class TableUiState(
        val table: TableEntity,
        val color: TableColor,
        val isBilled: Boolean = false
    ) {
        val cartCount get() = table.cartCount
        val kitchenPendingCount get() = table.kitchenCount
        val billDoneCount get() = table.billCount
        val billAmount get() = table.billAmount
        val runningAmount get() = table.billAmount
    }

    enum class TableColor {
        GRAY,
        BLUE,
        GREEN,
        RED
    }



    private fun observeTables() {
        viewModelScope.launch {
            dao.observeAllTables().collect { tableList ->

                val uiList = tableList.map { table ->

                    val isBilled = table.billCount > 0 || table.kitchenCount > 0

                    val color = when {
                        table.billCount > 0 -> TableColor.RED
                        table.kitchenCount > 0 -> TableColor.GREEN
                        table.cartCount > 0 -> TableColor.BLUE
                        else -> TableColor.GRAY
                    }

                    TableUiState(
                        table = table,
                        color = color,
                        isBilled = isBilled
                    )
                }

                _tables.emit(uiList)
            }
        }
    }

    private fun refreshAllTableCounts() {
        viewModelScope.launch {

            val allTables = dao.getAll()

            allTables.forEach { table ->
                val tableId = table.id

                cartRepository.syncCartCount(tableId)
                kotRepository.syncKinchenCount(tableId)
                kotRepository.syncBillCount(tableId)
            }
        }
    }


    fun loadTables() {
        viewModelScope.launch {
            try {
                val tableList = dao.getAll()

                // 🔹 Add this to print all tables and their area values
//                tableList.forEach { table ->
//                    Log.d("TABLE_DEBUG", "Table ${table.id} (${table.tableName}) → area=${table.area}")
//                }

                val uiList = tableList.map { table ->

                    val isBilled = table.billCount > 0 || table.kitchenCount > 0

                    val color = when {
                        table.billCount > 0 -> TableColor.RED
                        table.kitchenCount > 0 -> TableColor.GREEN
                        table.cartCount > 0 -> TableColor.BLUE
                        else -> TableColor.GRAY
                    }

                    TableUiState(
                        table = table,
                        color = color,
                        isBilled = isBilled
                    )
                }


                _tables.emit(uiList)

            } catch (e: Exception) {
                _tables.value = emptyList()
            }
        }
    }


    fun loadTableItems(tableNo: String) {

        viewModelScope.launch {

            try {

                val items =
                    kotRepository.getItemsForTable(tableNo)

                _tableItems.value = items

            } catch (e: Exception) {

                Log.e(
                    "TABLE_CHANGE",
                    "Failed to load items for table=$tableNo",
                    e
                )

                _tableItems.value = emptyList()
            }
        }
    }

    fun transferSelectedItems(
        oldTableId: String,
        newTableId: String,
        newTableName: String,
        itemIds: List<String>
    ) {
        viewModelScope.launch {

            if (itemIds.isEmpty()) {
                return@launch
            }

            kotRepository.transferSelectedItems(
                itemIds = itemIds,
                newTable = newTableId,
                newTableName = newTableName
            )

            // Refresh source table
            cartRepository.syncCartCount(oldTableId)
            kotRepository.syncKinchenCount(oldTableId)
            kotRepository.syncBillCount(oldTableId)

            // Refresh destination table
            cartRepository.syncCartCount(newTableId)
            kotRepository.syncKinchenCount(newTableId)
            kotRepository.syncBillCount(newTableId)
        }
    }

    fun clearTableItems() {
        _tableItems.value = emptyList()
    }

    fun transferTable(oldTableId: String, newTableId: String) {
        viewModelScope.launch {
            // move KOT items
            kotRepository.transferTable(oldTableId, newTableId)
            Log.d("TABLE_SYNC", "Triggered snapshot sync for table=$oldTableId")
            Log.d("TABLE_SYNC", "Triggered snapshot sync for table=$newTableId")


            // ✅ Move bill serial mapping
            orderSequenceRepository.moveTable(
                oldTableKey = oldTableId,
                newTableKey = newTableId
            )

            Log.d("TABLE_SYNC", "Moved serial mapping $oldTableId -> $newTableId")

            // refresh table counters
            cartRepository.syncCartCount(oldTableId)
            cartRepository.syncCartCount(newTableId)

            kotRepository.syncKinchenCount(oldTableId)
            kotRepository.syncKinchenCount(newTableId)

            kotRepository.syncBillCount(oldTableId)
            kotRepository.syncBillCount(newTableId)
            // 🚀 NEW: FIRESTORE TABLE SNAPSHOT SYNC (IMPORTANT)
//            try {
//                tableKotSyncService.syncTableSnapshot(
//                    tableId = oldTableId,
//                   )
//
//                tableKotSyncService.syncTableSnapshot(
//                    tableId = newTableId,
//                   )
//
//               Log.d("TABLE_SYNC", "Triggered snapshot sync for table=$newTableId")
//
//            } catch (e: Exception) {
//                Log.e("TABLE_SYNC", "Failed to trigger snapshot sync", e)
//            }


        }
    }


//    fun transferTable(oldTable: String, newTable: String) {
//        viewModelScope.launch {
//            kotItemDao.transferTable(oldTable, newTable)
//            repository.finalizeTableAfterTransfer(oldTable, newTable)
//            // sendEvent("Table moved to $newTable")
//        }
//    }



}
