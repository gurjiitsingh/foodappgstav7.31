package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderMasterDao {




    @Query("SELECT * FROM pos_order_master WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): PosOrderMasterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PosOrderMasterEntity)

    @Query("SELECT * FROM pos_order_master WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: String): PosOrderMasterEntity?



    @Query("""
    SELECT IFNULL(SUM(grandTotal), 0)
    FROM pos_order_master
    WHERE tableNo = :tableId
      AND orderStatus IN ('NEW', 'OPEN')
""")
    suspend fun getRunningTotalForTable(tableId: String): Double



//    @Query("SELECT * FROM pos_order_master ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
//    fun getPagedOrders(limit: Int, offset: Int): List<PosOrderMasterEntity>

    @Query("""
    UPDATE pos_order_master
    SET orderStatus = 'PAID',
        paymentStatus = 'PAID',
        paymentMode = :paymentType,
        updatedAt = :time
    WHERE tableNo = :tableNo
      AND orderStatus IN ('NEW', 'OPEN')
""")
    suspend fun markOrdersPaid(
        tableNo: String,
        paymentType: String,
        time: Long
    )

    @Query("""
    SELECT * FROM pos_order_items
    WHERE orderMasterId IN (
        SELECT id FROM pos_order_master
        WHERE tableNo = :tableNo
          AND orderStatus IN ('NEW', 'OPEN')
    )
""")
    suspend fun getAllItemsForTable(tableNo: String): List<PosOrderItemEntity>


    @Query("""
SELECT * FROM pos_order_master
WHERE tableNo = :tableId
  AND orderStatus IN ('NEW', 'OPEN')
ORDER BY createdAt ASC
""")
    suspend fun getOpenOrdersForTable(tableId: String): List<PosOrderMasterEntity>

    @Query("""
UPDATE pos_order_master
SET orderStatus = 'PAID',
    paymentStatus = 'PAID',
    updatedAt = :time
WHERE tableNo = :tableId
  AND orderStatus IN ('NEW', 'OPEN')
""")
    suspend fun closeTableOrders(tableId: String, time: Long)

    @Query("""
SELECT * FROM pos_order_master 
ORDER BY createdAt DESC 
LIMIT :limit OFFSET :offset
""")
    suspend fun getPagedOrders(limit: Int, offset: Int): List<PosOrderMasterEntity>



    @Query("""
SELECT * FROM pos_order_master
WHERE syncStatus = 'PENDING'
""")
    suspend fun getPendingSyncOrders(): List<PosOrderMasterEntity>

    @Query("""
UPDATE pos_order_master
SET syncStatus = 'SYNCED',
    lastSyncedAt = :time
WHERE id IN (:ids)
""")
    suspend fun markOrdersSynced(ids: List<String>, time: Long)


    @Query("UPDATE pos_order_master SET grandTotal = :newTotal, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateGrandTotal(orderId: String, newTotal: Double, updatedAt: Long = System.currentTimeMillis())


    @Query("""
    SELECT * FROM pos_order_master
    WHERE paymentStatus = 'DELIVERY_PENDING'
    ORDER BY createdAt DESC
""")
    suspend fun getDeliveryPendingOrders(): List<PosOrderMasterEntity>


    @Query("""
UPDATE pos_order_master
SET paymentStatus = :status,
    paymentMode = :paymentMode,
    paidAmount = :paidAmount,
    dueAmount = :dueAmount,
    updatedAt = :time
WHERE id = :orderId
""")
    suspend fun updatePaymentStatus(
        orderId: String,
        status: String,
        paymentMode: String,
        paidAmount: Double,
        dueAmount: Double,
        time: Long
    )


//    @Query("""
//SELECT * FROM pos_order_master
//WHERE customerId = :customerId
//AND paymentStatus IN ('CREDIT','PARTIAL')
//ORDER BY createdAt ASC
//""")
//    suspend fun getPendingOrdersForCustomer(customerId: String): List<PosOrderMasterEntity>



    @Query("""
    SELECT * FROM pos_order_master
    WHERE customerId = :customerId
      AND paymentStatus IN ('CREDIT', 'PARTIAL')
    ORDER BY createdAt ASC
""")
    suspend fun getPendingOrdersForCustomer(
        customerId: String
    ): List<PosOrderMasterEntity>






    @Query("SELECT * FROM pos_order_master ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PosOrderMasterEntity>>

    @Query("SELECT * FROM pos_order_master WHERE createdAt BETWEEN :start AND :end ORDER BY createdAt DESC")
    fun getOrdersBetween(start: Long, end: Long): Flow<List<PosOrderMasterEntity>>



    //DAY CLOSE

    @Query("""
SELECT IFNULL(SUM(grandTotal),0)
FROM pos_order_master
WHERE businessDate=:businessDate
""")
    suspend fun getTotalSales(
        businessDate:String
    ):Double


    @Query("""
SELECT COUNT(*)
FROM pos_order_master
WHERE businessDate=:businessDate
""")
    suspend fun getOrderCount(
        businessDate:String
    ):Int

    @Query("""
SELECT IFNULL(SUM(discountTotal),0)
FROM pos_order_master
WHERE businessDate=:businessDate
""")
    suspend fun getTotalDiscount(
        businessDate:String
    ):Double


    @Query("""
SELECT IFNULL(SUM(taxTotal),0)
FROM pos_order_master
WHERE businessDate=:businessDate
""")
    suspend fun getTotalTax(
        businessDate:String
    ):Double

    @Query("""
SELECT IFNULL(SUM(grandTotal),0)
FROM pos_order_master
WHERE businessDate=:businessDate
AND saleType='COMPLIMENTARY'
""")
    suspend fun getComplimentarySales(
        businessDate:String
    ):Double

    @Query("""
SELECT IFNULL(SUM(dueAmount),0)
FROM pos_order_master
WHERE businessDate=:businessDate
""")
    suspend fun getCreditSales(
        businessDate:String
    ):Double


    @Query("""
    SELECT *
    FROM pos_order_master
    WHERE createdAt >= :startTime
      AND createdAt <= :endTime
    ORDER BY createdAt ASC
""")
    suspend fun getOrdersForExport(
        startTime: Long,
        endTime: Long
    ): List<PosOrderMasterEntity>


}


