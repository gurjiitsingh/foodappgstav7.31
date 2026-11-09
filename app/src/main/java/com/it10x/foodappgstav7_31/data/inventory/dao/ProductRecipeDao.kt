package com.it10x.foodappgstav7_31.data.inventory.dao



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entity.ProductRecipeEntity

@Dao
interface ProductRecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        recipes: List<ProductRecipeEntity>
    )

    @Query("SELECT * FROM product_recipes")
    suspend fun getAll(): List<ProductRecipeEntity>

    @Query(
        "SELECT * FROM product_recipes WHERE productId = :productId"
    )
    suspend fun getByProductId(
        productId: String
    ): List<ProductRecipeEntity>

    @Query("DELETE FROM product_recipes")
    suspend fun clear()
}