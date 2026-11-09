package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.it10x.foodappgstav7_31.data.pos.entities.PosBusinessDayEntity

@Dao
interface BusinessDayDao {

    // ----------------------------------------------------
    // CURRENT BUSINESS DAY
    // ----------------------------------------------------

    @Query("SELECT * FROM pos_business_day WHERE id = 'CURRENT' LIMIT 1")
    suspend fun getCurrentBusinessDay(): PosBusinessDayEntity?

    // ----------------------------------------------------
    // CREATE / REPLACE
    // ----------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(businessDay: PosBusinessDayEntity)

    // ----------------------------------------------------
    // UPDATE
    // ----------------------------------------------------

    @Update
    suspend fun update(businessDay: PosBusinessDayEntity)

    // ----------------------------------------------------
    // DELETE (Optional - mainly for testing)
    // ----------------------------------------------------

    @Query("DELETE FROM pos_business_day")
    suspend fun clear()


    @Query("DELETE FROM pos_business_day")
    suspend fun deleteAll()
}