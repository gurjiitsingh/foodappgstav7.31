package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import androidx.room.withTransaction
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.OrderCounterEntity
import com.it10x.foodappgstav7_31.data.pos.entities.OrderSerialMapEntity

class OrderSequenceRepository(
    private val db: AppDatabase
) {

    private val counterDao = db.orderCounterDao()
    private val mapDao = db.orderSerialMapDao()

    /**
     * Returns the existing serial number for this order.
     * If the order has never been assigned a serial,
     * creates a new one atomically.
     */


    suspend fun getOrCreateOrderNo(
        mapKey: String,
        deviceCode: String,
        financialYear: String
    ): OrderSerialMapEntity {


        return db.withTransaction {

            // Already assigned?
            mapDao.get(mapKey)?.let {
                return@withTransaction it
            }

            // Read current counter
            val dbCounter = counterDao.getCounter()

            Log.d(
                "SERIAL_DEBUG",
                "DB COUNTER = ${dbCounter?.invoiceSerialNo}"
            )

            val counter = dbCounter ?: OrderCounterEntity()

            // Generate next number
            val nextSerial = counter.invoiceSerialNo + 1

            // Save updated counter
            counterDao.save(
                counter.copy(
                    invoiceSerialNo = nextSerial,
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Create printable serial
            val srno = "$deviceCode-$financialYear-$nextSerial"

            val mapping = OrderSerialMapEntity(
                mapKey = mapKey,
                orderId = null,
                orderSerialNo = nextSerial,
                srno = srno,
                createdAt = System.currentTimeMillis()
            )

            Log.d(
                "SERIAL_DEBUG",
                """
    mapKey=$mapKey
    oldCounter=${counter.invoiceSerialNo}
    next=$nextSerial
    srno=$srno
    """.trimIndent()
            )

            // Save mapping
            mapDao.insert(mapping)

            mapping
        }
    }
    /**
     * Removes the mapping after the bill
     * is fully completed (optional).
     */
    suspend fun clearOrder(mapKey: String) {

        mapDao.delete(mapKey)

        Log.d(
            "ORDER_COUNTER",
            "Cleared mapping for order=$mapKey"
        )
    }


    suspend fun moveTable(
        oldTableKey: String,
        newTableKey: String
    ) {

        if (oldTableKey == newTableKey) return

        db.withTransaction {

            // New table already has a bill
            if (mapDao.get(newTableKey) != null) {
                return@withTransaction
            }

            // Old table has no bill
            if (mapDao.get(oldTableKey) == null) {
                return@withTransaction
            }

            mapDao.updateTableKey(
                oldTableKey = oldTableKey,
                newTableKey = newTableKey
            )
        }
    }




}



/**
 * Returns current counter value only.
 * Does NOT increment.
 */
//suspend fun currentOrderNo(): Long {
//
//    return counterDao
//        .getCounter()
//        ?.invoiceSerialNo
//        ?: 0L
//}

/**
 * Returns serial if already assigned.
 * Otherwise null.
 */
//suspend fun getOrderNo(
//    orderId: String
//): Long? {
//
//    return mapDao.get(orderId)?.orderSerialNo
//}