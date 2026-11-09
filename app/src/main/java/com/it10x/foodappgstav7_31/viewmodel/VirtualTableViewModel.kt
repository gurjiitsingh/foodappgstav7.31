package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.VirtualTableCounterEntity
import com.it10x.foodappgstav7_31.data.pos.entities.VirtualTableEntity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import androidx.room.withTransaction

class VirtualTableViewModel(
    app: Application
) : AndroidViewModel(app) {

    companion object {
        const val STATUS_NEW = "NEW"
        const val STATUS_RUNNING = "RUNNING"
        const val STATUS_COMPLETED = "COMPLETED"
    }
    private val db =
        AppDatabaseProvider.get(app)

    private val dao =
        db.virtualTableDao()
    private val counterDao =
        db.virtualTableCounterDao()

    private val cartDao =
        db.cartDao()

    private val kotDao =
        db.kotItemDao()


    // =========================================================
    // SELECTED ORDER TYPE
    // =========================================================

    private val selectedType =
        MutableStateFlow<String?>(null)


    // =========================================================
    // OBSERVE VIRTUAL TABLES
    // =========================================================

    val tables: StateFlow<List<VirtualTableEntity>> =
        selectedType
            .filterNotNull()
            .flatMapLatest { type ->

                dao.observeByType(type)
                    .onStart {
                        emit(emptyList())
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


    // =========================================================
    // SET ORDER TYPE
    // =========================================================

    fun setOrderType(type: String) {

        selectedType.value = type
    }


//    private fun getTodayDate(): String {
//        return SimpleDateFormat(
//            "yyyy-MM-dd",
//            Locale.getDefault()
//        ).format(System.currentTimeMillis())
//    }

//    private fun getTodayDate(): String {
//        val zone = TimeZone.getTimeZone(outletTimeZone)
//
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
//        dateFormat.timeZone = zone
//
//        return dateFormat.format(System.currentTimeMillis())
//    }

    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")

        return dateFormat.format(System.currentTimeMillis())
    }


    // =========================================================
// CREATE / REUSE TAKEAWAY / DELIVERY
// =========================================================

    suspend fun createNew(type: String): VirtualTableEntity {

        require(type == "TAKEAWAY" || type == "DELIVERY") {
            "Invalid virtual table type: $type"
        }

        val prefix = if (type == "TAKEAWAY") "TW" else "DL"

        return db.withTransaction {

            val today = getTodayDate()
            val startOfToday = getStartOfTodayMillis()

            // Get or initialize this order type's counter
            var counter = counterDao.get(type)

            if (counter == null || counter.resetDate != today) {

                counter = VirtualTableCounterEntity(
                    orderType = type,
                    lastNumber = 0,
                    status = "NEW",
                    resetDate = today
                )

                counterDao.upsert(counter)
            }

            // Find the latest table created today
            val currentTable =
                dao.getLatestByTypeForDay(type, startOfToday)

            // Reuse an unused NEW order
            if (currentTable?.status == "NEW") {
                return@withTransaction currentTable
            }

            // Allocate next number
            val nextNumber = counter.lastNumber + 1

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

            dao.insert(newTable)



// Verify insertion using the SAME DAO
            val savedTable = dao.getById(newTable.id)

            Log.d(
                "VIRTUAL_ORDER",
                """
    CREATE TABLE RESULT
    Inserted ID = ${newTable.id}
    Saved ID    = ${savedTable?.id}
    Saved Name  = ${savedTable?.tableName}
    Saved Type  = ${savedTable?.orderType}
    Saved Status = ${savedTable?.status}
    Total Records = ${dao.getAllTables().size}
    """.trimIndent()
            )

            newTable

            newTable
        }
    }
    // =========================================================
    // START OF TODAY
    // =========================================================

    private fun getStartOfToday(): Long {

        val cal =
            Calendar.getInstance()

        cal.set(
            Calendar.HOUR_OF_DAY,
            0
        )

        cal.set(
            Calendar.MINUTE,
            0
        )

        cal.set(
            Calendar.SECOND,
            0
        )

        cal.set(
            Calendar.MILLISECOND,
            0
        )

        return cal.timeInMillis
    }


    // =========================================================
    // DELETE OLD TABLES
    // =========================================================

    fun deleteOldTables(
        type: String
    ) {

//        viewModelScope.launch {
//
//            val startOfToday =
//                getStartOfToday()
//
//            dao.deleteOldTables(
//                type,
//                startOfToday
//            )
//        }
    }


    fun logAllTableStatuses() {

        viewModelScope.launch {

            val tables = dao.getAllTables()


        }
    }
    // =========================================================
    // DELETE
    // =========================================================

    fun delete(
        id: String
    ) {

        viewModelScope.launch {

            dao.deleteById(id)
        }
    }

    private fun getStartOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
