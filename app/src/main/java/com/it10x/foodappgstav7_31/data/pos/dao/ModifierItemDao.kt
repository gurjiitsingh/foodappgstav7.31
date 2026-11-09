package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.ModifierItemEntity

@Dao
interface ModifierItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ModifierItemEntity>)

    @Query("DELETE FROM modifier_items")
    suspend fun clear()

    @Query("""
        SELECT * FROM modifier_items
        WHERE groupId = :groupId
        AND status = 'published'
        ORDER BY sortOrder ASC
    """)
    suspend fun getByGroup(groupId: String): List<ModifierItemEntity>

    @Query("SELECT * FROM modifier_items")
    suspend fun getAllItems(): List<ModifierItemEntity>
}