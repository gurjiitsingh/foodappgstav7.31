package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.dao.KotBatchDao
import com.it10x.foodappgstav7_31.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_31.data.pos.dao.TableDao
import com.it10x.foodappgstav7_31.data.pos.entities.KotHistorySummary
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotHistoryEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.flow.Flow

class KotRepository(
    private val batchDao: KotBatchDao,
    private val kotItemDao: KotItemDao,
    private val tableDao: TableDao
) {

    // =====================================================
    // KOT
    // =====================================================

    suspend fun insertItemsInBill(
        tableNo: String,
        items: List<PosKotItemEntity>,
        role: String
    ) {
        kotItemDao.insertAll(items)
    }

    suspend fun deleteKotByTable(tableId: String) {
        kotItemDao.deleteByTableId(tableId)
        syncBillCounters(tableId)
    }

    suspend fun markDoneAll(tableNo: String) {
        kotItemDao.markAllDone(tableNo)
    }

    suspend fun markPrinted(tableNo: String) {
        kotItemDao.markAllPrinted(tableNo)
    }

    private suspend fun syncBillCounters(tableNo: String) {

        val billQty = kotItemDao.getBillQtyCount(tableNo) ?: 0
        val billAmount = kotItemDao.sumDoneAmount(tableNo) ?: 0.0

        tableDao.updateBill(
            tableNo,
            billQty,
            billAmount
        )
    }

    private suspend fun syncKitchenCount(tableNo: String) {

        val count = kotItemDao.countBillDone(tableNo) ?: 0

        tableDao.setKitchenCount(
            tableNo,
            count
        )
    }

    suspend fun syncBillCount(tableNo: String) {
        syncBillCounters(tableNo)
    }

    suspend fun syncKinchenCount(tableNo: String) {
        syncKitchenCount(tableNo)
    }


    suspend fun transferSelectedItems(
        itemIds: List<String>,
        newTable: String,
        newTableName: String
    ) {
        if (itemIds.isEmpty()) return

        kotItemDao.transferSelectedItems(
            itemIds = itemIds,
            newTable = newTable,
            newTableName = newTableName
        )
    }

//    fun transferSelectedItems(
//        oldTableId: String,
//        newTableId: String,
//        newTableName: String,
//        itemIds: List<String>
//    ) {
//        viewModelScope.launch {
//            if (itemIds.isEmpty()) return@launch
//
//            try {
//                kotRepository.transferSelectedItems(
//                    itemIds = itemIds,
//                    newTable = newTableId,
//                    newTableName = newTableName
//                )
//
//                // Refresh source table
//                cartRepository.syncCartCount(oldTableId)
//                kotRepository.syncKinchenCount(oldTableId)
//                kotRepository.syncBillCount(oldTableId)
//
//                // Refresh destination table
//                cartRepository.syncCartCount(newTableId)
//                kotRepository.syncKinchenCount(newTableId)
//                kotRepository.syncBillCount(newTableId)
//
//                Log.d(
//                    "TABLE_CHANGE",
//                    "Moved selected items from $oldTableId -> $newTableId"
//                )
//
//            } catch (e: Exception) {
//                Log.e(
//                    "TABLE_CHANGE",
//                    "Failed to move selected items",
//                    e
//                )
//            }
//        }
//    }
    suspend fun getItemsForTable(
        tableNo: String
    ): List<PosKotItemEntity> {
        return kotItemDao.getAllItems(tableNo)
    }

    suspend fun transferTable(
        oldTableId: String,
        newTableId: String
    ) {

        if (oldTableId == newTableId) return

        try {

            kotItemDao.transferTable(
                oldTableId,
                newTableId
            )

            syncKinchenCount(oldTableId)
            syncBillCount(oldTableId)

            syncKinchenCount(newTableId)
            syncBillCount(newTableId)

            Log.d(
                "TABLE_TRANSFER",
                "Moved KOT from $oldTableId -> $newTableId"
            )

        } catch (e: Exception) {

            Log.e(
                "TABLE_TRANSFER",
                "Transfer failed",
                e
            )

        }
    }

    // =====================================================
    // KOT HISTORY
    // =====================================================

    suspend fun saveHistory(
        kotNumber: String,
        batch: PosKotBatchEntity,
        items: List<PosKotItemEntity>,
        source: String,
        createdByName: String,
    ) {

        val historyItems = items.map { item ->

            PosKotHistoryEntity(

                id = item.id,

                kotNumber = kotNumber,

                batchId = batch.id,

                sessionId = batch.sessionId ?: "",

                orderId = null,

                tableNo = batch.tableNo ?: "",

                 tableName = batch.tableName?: "",
                createdByName = createdByName,

                orderType = batch.orderType,

                productId = item.productId,

                name = item.name,

                quantity = item.quantity,

                modifierTotal = item.modifierTotal,

                note = item.note,

                modifiersJson = item.modifiersJson,

                status = "ACTIVE",

                deleted = false,
                deletedBy = null,
                deletedReason = null,
                deletedAt = null,

                paidAt = null,

                source = source,

                deviceId = batch.deviceId ?: "",

                deviceName = batch.deviceName,

                createdAt = item.createdAt
            )
        }

        batchDao.insertHistory(historyItems)
        val fiveDaysAgo =
            System.currentTimeMillis() -
                    (5L * 24 * 60 * 60 * 1000)

        batchDao.deleteHistoryOlderThan(fiveDaysAgo)
    }

    /**
     * Latest KOTs
     */
    fun getKotHistory(): Flow<List<PosKotHistoryEntity>> {
        return batchDao.getHistory()
    }

    /**
     * All history rows
     */
    fun getHistoryAll(): Flow<List<PosKotHistoryEntity>> {
        return batchDao.getAllHistory()
    }

    /**
     * One row per KOT
     */
    fun getHistorySummary(): Flow<List<KotHistorySummary>> {
        return batchDao.getHistorySummary()
    }

    /**
     * All items of one KOT
     */
    fun getHistoryByKotNumber(
        kotNumber: String
    ): Flow<List<PosKotHistoryEntity>> {
        return batchDao.getHistoryByKotNumber(kotNumber)
    }

    suspend fun markHistoryDeleted(
        itemId: String,
        reason: String
    ) {
        batchDao.markItemDeleted(
            itemId = itemId,
            reason = reason,
            deletedAt = System.currentTimeMillis()
        )
    }



    suspend fun markHistoryComplimentary(
        tableNo: String,
        orderId: String,
        reason: String
    ) {

        batchDao.markTableComplimentary(
            tableNo = tableNo,
            orderId = orderId,
            complimentaryAt = System.currentTimeMillis()
        )
    }

    suspend fun markHistoryPaid(
        tableNo: String,
        orderId: String
    ) {

        val rows = batchDao.markTablePaid(
            tableNo = tableNo,
            orderId = orderId,
            paidAt = System.currentTimeMillis()
        )

        Log.d(
            "KOT_HISTORY",
            "Marked PAID rows=$rows table=$tableNo order=$orderId"
        )
    }

    // =====================================================
    // KOT NUMBER
    // =====================================================

    suspend fun generateNextKotNumber(): String {

        val last = kotItemDao.getLastKotNumber()

        if (last == null) {
            return "K1"
        }

        val next = last
            .removePrefix("K")
            .toIntOrNull()
            ?.plus(1)
            ?: 1

        return "K$next"
    }
}