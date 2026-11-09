package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.ProductModifierEntity

@Dao
interface ProductModifierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ProductModifierEntity>)

    @Query("DELETE FROM product_modifiers")
    suspend fun clear()

    @Query("""
    SELECT * FROM product_modifiers
    WHERE productId = :productId
""")
    suspend fun debugGetByProduct(productId: String): List<ProductModifierEntity>

    @Query("""
        SELECT * FROM product_modifiers
        WHERE productId = :productId
        ORDER BY sortOrder ASC
    """)
    suspend fun getByProduct(productId: String): List<ProductModifierEntity>


    @Query("""
        SELECT * FROM product_modifiers
        ORDER BY sortOrder ASC
    """)
    suspend fun getAllByProduct(): List<ProductModifierEntity>
    @Query("SELECT COUNT(*) FROM product_modifiers WHERE productId = :productId")
    suspend fun hasModifiers(productId: String): Int

}