package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.it10x.foodappgstav7_31.data.pos.entities.VirtualTableCounterEntity

@Dao
interface VirtualTableCounterDao {

    @Query("""
        SELECT *
        FROM virtual_table_counters
        WHERE orderType = :orderType
        LIMIT 1
    """)
    suspend fun get(orderType: String): VirtualTableCounterEntity?

    @Upsert
    suspend fun upsert(counter: VirtualTableCounterEntity)

}