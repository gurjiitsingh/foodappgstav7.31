package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.PosRenewalEntity

@Dao
interface PosRenewalDao {

    @Query("""
        SELECT *
        FROM pos_renewal
        WHERE id = 1
        LIMIT 1
    """)
    suspend fun getRenewal(): PosRenewalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(
        entity: PosRenewalEntity
    )

    @Query("DELETE FROM pos_renewal")
    suspend fun clear()
}