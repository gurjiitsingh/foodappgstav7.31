
package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import androidx.room.withTransaction
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.dao.CartDao
import com.it10x.foodappgstav7_31.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_31.data.pos.dao.VirtualTableCounterDao
import com.it10x.foodappgstav7_31.data.pos.dao.VirtualTableDao
import com.it10x.foodappgstav7_31.data.pos.entities.VirtualTableCounterEntity
import com.it10x.foodappgstav7_31.data.pos.entities.VirtualTableEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class VirtualTableRepository(
    private val db: AppDatabase,
    private val virtualDao: VirtualTableDao,
    private val cartDao: CartDao,
    private val kotDao: KotItemDao,
    private val counterDao: VirtualTableCounterDao
) {

    // =========================================================
    // LOCAL BUSINESS DATE
    // =========================================================

    private fun getTodayDate(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.timeZone = java.util.TimeZone.getDefault()
        return format.format(System.currentTimeMillis())
    }

    private fun getStartOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // =========================================================
    // CREATE / GET CURRENT TAKEAWAY OR DELIVERY TABLE
    // =========================================================

    suspend fun createNew(type: String): VirtualTableEntity {

        val prefix = when (type) {
            "TAKEAWAY" -> "TW"
            "DELIVERY" -> "DL"
            else -> throw IllegalArgumentException(
                "Invalid virtual table type: $type"
            )
        }

        return db.withTransaction {

            val currentTable = virtualDao.getLatestByTypeForDay(
                type = type,
                startOfDay = getStartOfTodayMillis()
            )

            // Reuse today's unused table.
            if (currentTable?.status == "NEW") {
                return@withTransaction currentTable
            }

            // Otherwise allocate the next number.
            createNextInsideTransaction(type, prefix)
        }
    }

    // =========================================================
    // CREATE NEXT TABLE
    // Must be called inside a Room transaction.
    // =========================================================

    private suspend fun createNextInsideTransaction(
        type: String,
        prefix: String
    ): VirtualTableEntity {

        val today = getTodayDate()

        val existingCounter = counterDao.get(type)

        // Reset this type only when its saved business date changes.
        val counter = if (
            existingCounter == null ||
            existingCounter.resetDate != today
        ) {
            VirtualTableCounterEntity(
                orderType = type,
                lastNumber = 0,
                status = "NEW",
                resetDate = today
            )
        } else {
            existingCounter
        }

        val nextNumber = counter.lastNumber + 1

        // Save the counter without losing its reset date.
        counterDao.upsert(
            counter.copy(lastNumber = nextNumber)
        )

        val now = System.currentTimeMillis()

        val newTable = VirtualTableEntity(
            id = "${prefix}_${UUID.randomUUID()}",
            tableName = "$prefix$nextNumber",
            orderType = type,
            status = "NEW",
            createdAt = now,
            updatedAt = now
        )

        virtualDao.insert(newTable)

        return newTable
    }

    // =========================================================
    // MARK COMPLETED + CREATE NEXT TABLE
    // =========================================================

    suspend fun markCompleted(
        tableId: String,
        tableName: String,
        orderType: String
    ): VirtualTableEntity? {


     //   logAllVirtualTables()
        if (orderType != "TAKEAWAY" && orderType != "DELIVERY") {
            return null
        }



//        val beforeById = virtualDao.getById(tableId)
//        val beforeByName = virtualDao.getByName(tableName, orderType)

//        logVirtualTable("BEFORE BY ID", beforeById)
//        logVirtualTable("BEFORE BY NAME", beforeByName)

        Log.d(
            "VIRTUAL_ORDER",
            "INPUT: tableId=$tableId, tableName=$tableName, orderType=$orderType"
        )

        val prefix = if (orderType == "TAKEAWAY") "TW" else "DL"

        return db.withTransaction {

            // STATUS BEFORE
            val before = virtualDao.getById(tableId)

            Log.d(
                "VIRTUAL_ORDER",
                "BEFORE: id=$tableId, name=$tableName, status=${before?.status}"
            )

            // MARK COMPLETED
            val updatedRows = virtualDao.markCompleted(
                tableName = tableName,
                orderType = orderType,
                time = System.currentTimeMillis()
            )

            // STATUS AFTER
            val after = virtualDao.getById(tableId)

            Log.d(
                "VIRTUAL_ORDER",
                "AFTER: id=$tableId, name=$tableName, status=${after?.status}, updatedRows=$updatedRows"
            )

            if (updatedRows == 0) {
                Log.d("VIRTUAL_ORDER", "COMPLETION FAILED - NEXT ORDER NOT CREATED")
                return@withTransaction null
            }

            // CREATE NEXT ORDER
            val next = createNextInsideTransaction(orderType, prefix)

            Log.d(
                "VIRTUAL_ORDER",
                "NEXT CREATED: ${next.tableName}, status=${next.status}"
            )

            next
        }
    }


    suspend fun logAllVirtualTables() {
        val tables = virtualDao.getAllTables()

        Log.d("VIRTUAL_ORDER", "========== ALL VIRTUAL TABLES ==========")
        Log.d("VIRTUAL_ORDER", "TOTAL RECORDS = ${tables.size}")

        tables.forEachIndexed { index, table ->
            Log.d("VIRTUAL_ORDER", """
            ---------- RECORD ${index + 1} ----------
            id            = ${table.id}
            tableName     = ${table.tableName}
            orderType     = ${table.orderType}
            status        = ${table.status}
            waiterName    = ${table.waiterName}
            waiterId      = ${table.waiterId}
            activeOrderId = ${table.activeOrderId}
            guestsCount   = ${table.guestsCount}
            area          = ${table.area}
            sortOrder     = ${table.sortOrder}
            cartCount     = ${table.cartCount}
            kitchenCount  = ${table.kitchenCount}
            billCount     = ${table.billCount}
            billAmount    = ${table.billAmount}
            updatedAt     = ${table.updatedAt}
            createdAt     = ${table.createdAt}
            notes         = ${table.notes}
            synced        = ${table.synced}
        """.trimIndent())
        }

        Log.d("VIRTUAL_ORDER", "========== END OF TABLE LIST ==========")
    }
    private fun logVirtualTable(tag: String, table: VirtualTableEntity?) {
        if (table == null) {
            Log.d("VIRTUAL_ORDER", "$tag: TABLE NOT FOUND")
            return
        }

        Log.d("VIRTUAL_ORDER", """
        ================= $tag =================
        id              = ${table.id}
        tableName       = ${table.tableName}
        orderType       = ${table.orderType}
        status          = ${table.status}
        waiterName      = ${table.waiterName}
        waiterId        = ${table.waiterId}
        activeOrderId   = ${table.activeOrderId}
        guestsCount     = ${table.guestsCount}
        area            = ${table.area}
        sortOrder       = ${table.sortOrder}
        cartCount       = ${table.cartCount}
        kitchenCount    = ${table.kitchenCount}
        billCount       = ${table.billCount}
        billAmount      = ${table.billAmount}
        updatedAt       = ${table.updatedAt}
        createdAt       = ${table.createdAt}
        notes           = ${table.notes}
        synced          = ${table.synced}
        =========================================
    """.trimIndent())
    }
    // =========================================================
    // MARK TW/DL TABLE AS RUNNING
    // =========================================================

    suspend fun markRunningIfNew(
        tableId: String,
        orderType: String
    ) {

        if (
            orderType != "TAKEAWAY" &&
            orderType != "DELIVERY"
        ) {
            return
        }

        virtualDao.markRunningIfNew(
            tableId = tableId,
            time = System.currentTimeMillis()
        )
    }

    // =========================================================
    // SYNC CART COUNT
    // =========================================================

    suspend fun syncCartCount(tableId: String) {

        val count = cartDao.getCartCountForTable(tableId) ?: 0

        virtualDao.setCartCount(
            tableId = tableId,
            count = count,
            time = System.currentTimeMillis()
        )
    }

    // =========================================================
    // SYNC BILL DATA
    // =========================================================

    suspend fun syncBillData(tableId: String) {

        val billCount = kotDao.getBillQtyCount(tableId) ?: 0
        val billAmount = kotDao.sumDoneAmount(tableId) ?: 0.0

        virtualDao.setBillData(
            tableId = tableId,
            count = billCount,
            amount = billAmount,
            time = System.currentTimeMillis()
        )
    }

    // =========================================================
    // SYNC KITCHEN COUNT
    // =========================================================

    suspend fun syncKitchenCount(tableId: String) {

        val count = kotDao.getKitchenCountForTable(tableId) ?: 0

        virtualDao.setKitchenCount(
            tableId = tableId,
            count = count,
            time = System.currentTimeMillis()
        )
    }

    // =========================================================
    // REMOVE IF EMPTY
    // =========================================================

    suspend fun removeIfEmpty(tableId: String) {

        val table = virtualDao.getById(tableId) ?: return

        if (
            table.cartCount == 0 &&
            table.billCount == 0
        ) {
            virtualDao.deleteById(tableId)
        }
    }
}
