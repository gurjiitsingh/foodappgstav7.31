package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.OrderSerialMapEntity

@Dao
interface OrderSerialMapDao {

    @Query("""
        SELECT * FROM order_serial_map
        WHERE mapKey = :mapKey
        LIMIT 1
    """)
    suspend fun get(mapKey: String): OrderSerialMapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OrderSerialMapEntity)

    @Delete
    suspend fun delete(entity: OrderSerialMapEntity)

    @Query("DELETE FROM order_serial_map WHERE mapKey = :mapKey")
    suspend fun delete(mapKey: String)

    // ✅ Move bill from one table to another
    @Query("""
        UPDATE order_serial_map
        SET mapKey = :newTableKey
        WHERE mapKey = :oldTableKey
    """)
    suspend fun updateTableKey(
        oldTableKey: String,
        newTableKey: String
    )
}