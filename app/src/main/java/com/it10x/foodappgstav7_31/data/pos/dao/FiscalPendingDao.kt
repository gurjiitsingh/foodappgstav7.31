package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.PosFiscalPendingEntity

@Dao
interface FiscalPendingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PosFiscalPendingEntity)

    @Query("SELECT * FROM pos_fiscal_pending")
    suspend fun getAll(): List<PosFiscalPendingEntity>

    @Query("DELETE FROM pos_fiscal_pending WHERE id = :id")
    suspend fun delete(id: String)
}