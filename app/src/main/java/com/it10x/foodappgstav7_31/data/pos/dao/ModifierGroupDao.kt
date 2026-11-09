package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.ModifierGroupEntity

@Dao
interface ModifierGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ModifierGroupEntity>)

    @Query("DELETE FROM modifier_groups")
    suspend fun clear()

    @Query("""
        SELECT * FROM modifier_groups
        WHERE status = 'published'
        ORDER BY sortOrder ASC
    """)
    suspend fun getAll(): List<ModifierGroupEntity>

    @Query("SELECT * FROM modifier_groups")
    suspend fun getAllGroups(): List<ModifierGroupEntity>
}