package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.InventorySyncEntity

@Dao
interface InventorySyncDao {

    // =====================================================
    // INSERT SYNC RECORDS
    // =====================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        items: List<InventorySyncEntity>
    )

    // =====================================================
    // GET ALL SYNCED ORDER ITEM IDS
    // (USED TO PREVENT DOUBLE INVENTORY DEDUCTION)
    // =====================================================
    @Query(
        """
        SELECT orderItemId 
        FROM inventory_sync
        """
    )
    suspend fun getSyncedIds(): List<String>

    // =====================================================
    // OPTIONAL: CLEAR SYNC HISTORY (FOR RESET / DEBUG)
    // =====================================================
    @Query("DELETE FROM inventory_sync")
    suspend fun clearAll()
}