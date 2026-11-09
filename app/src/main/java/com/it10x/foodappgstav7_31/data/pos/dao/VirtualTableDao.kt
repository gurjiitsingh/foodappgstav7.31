package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_31.data.pos.entities.VirtualTableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VirtualTableDao {



    @Query("""
    SELECT *
    FROM virtual_tables
    WHERE orderType = :type
      AND createdAt >= :startOfDay
    ORDER BY createdAt DESC
    LIMIT 1
""")
    suspend fun getLatestByTypeForDay(
        type: String,
        startOfDay: Long
    ): VirtualTableEntity?

//    @Query("""
//    DELETE FROM virtual_tables
//""")
//    suspend fun deleteAllVirtualTables()

    @Query("""
    SELECT *
    FROM virtual_tables
    ORDER BY orderType ASC, createdAt ASC
""")
    suspend fun getAllTables(): List<VirtualTableEntity>


    @Query("""
    DELETE FROM virtual_tables
    WHERE tableName = :tableName
      AND orderType = :orderType
""")
    suspend fun deleteCompletedTable(
        tableName: String,
        orderType: String
    ): Int

    @Query("""
    UPDATE virtual_tables
    SET status = 'COMPLETED',
        updatedAt = :time
    WHERE tableName = :tableName
      AND orderType = :orderType
      AND status = 'RUNNING'
""")
    suspend fun markCompleted(
        tableName: String,
        orderType: String,
        time: Long
    ): Int

    @Query("""
    UPDATE virtual_tables
    SET status = 'RUNNING',
        updatedAt = :time
    WHERE id = :tableId
      AND orderType IN ('TAKEAWAY', 'DELIVERY')
      AND status = 'NEW'
""")
    suspend fun markRunningIfNew(
        tableId: String,
        time: Long
    )

    @Query("""
    SELECT *
    FROM virtual_tables
    WHERE tableName = :tableName
      AND orderType = :orderType
    LIMIT 1
""")
    suspend fun getByName(
        tableName: String,
        orderType: String
    ): VirtualTableEntity?

    @Query("""
        SELECT * 
        FROM virtual_tables 
        WHERE orderType = :type 
        ORDER BY createdAt ASC
    """)
    fun observeByType(type: String): Flow<List<VirtualTableEntity>>


    @Query("""
        SELECT * 
        FROM virtual_tables 
        WHERE orderType = :type 
        ORDER BY createdAt ASC
    """)
    suspend fun getByType(type: String): List<VirtualTableEntity>


    // =========================================================
    // GET CURRENT / LATEST VIRTUAL TABLE
    // =========================================================

    @Query("""
        SELECT * 
        FROM virtual_tables
        WHERE orderType = :type
        ORDER BY createdAt DESC
        LIMIT 1
    """)
    suspend fun getLatestByType(type: String): VirtualTableEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(table: VirtualTableEntity)


    @Update
    suspend fun update(table: VirtualTableEntity)


    // =========================================================
    // STATUS
    // =========================================================

    @Query("""
        UPDATE virtual_tables
        SET status = :status,
            updatedAt = :time
        WHERE id = :tableId
    """)
    suspend fun updateStatus(
        tableId: String,
        status: String,
        time: Long
    )


    @Query("DELETE FROM virtual_tables WHERE id = :id")
    suspend fun deleteById(id: String)


    @Query("""
        SELECT * 
        FROM virtual_tables 
        WHERE id = :tableId 
        LIMIT 1
    """)
    suspend fun getById(tableId: String): VirtualTableEntity?


    // =========================================================
    // CART
    // =========================================================

    @Query("""
        UPDATE virtual_tables 
        SET cartCount = :count,
            updatedAt = :time 
        WHERE id = :tableId
    """)
    suspend fun setCartCount(
        tableId: String,
        count: Int,
        time: Long
    )


    // =========================================================
    // BILL
    // =========================================================

    @Query("""
        UPDATE virtual_tables 
        SET billCount = :count,
            billAmount = :amount,
            updatedAt = :time 
        WHERE id = :tableId
    """)
    suspend fun setBillData(
        tableId: String,
        count: Int,
        amount: Double,
        time: Long
    )


    @Query("""
        UPDATE virtual_tables 
        SET billCount = :count,
            billAmount = :amount
        WHERE id = :tableId
    """)
    suspend fun clearBillData(
        tableId: String,
        count: Int,
        amount: Double
    )


    // =========================================================
    // KITCHEN
    // =========================================================

    @Query("""
        UPDATE virtual_tables 
        SET kitchenCount = :count,
            updatedAt = :time 
        WHERE id = :tableId
    """)
    suspend fun setKitchenCount(
        tableId: String,
        count: Int,
        time: Long
    )


    // =========================================================
    // BILL QUANTITY
    // =========================================================

    @Query("""
        SELECT SUM(quantity) 
        FROM pos_kot_items 
        WHERE tableNo = :tableNo
    """)
    suspend fun getBillQtyCount(
        tableNo: String
    ): Int?


    // =========================================================
    // DELETE OLD TABLES
    // =========================================================

    @Query("""
        DELETE FROM virtual_tables 
        WHERE orderType = :type 
        AND createdAt < :startOfToday
    """)
    suspend fun deleteOldTables(
        type: String,
        startOfToday: Long
    )
}