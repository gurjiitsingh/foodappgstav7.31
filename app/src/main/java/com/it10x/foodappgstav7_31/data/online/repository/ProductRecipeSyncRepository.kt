package com.it10x.foodappgstav7_31.data.online.repository



import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entity.ProductRecipeEntity
import kotlinx.coroutines.tasks.await



class ProductRecipeSyncRepository(
    private val db: AppDatabase
) {

    private val firestore =
        FirebaseFirestore.getInstance()

    suspend fun syncProductRecipes() {

        val snapshot =
            firestore.collection("productRecipes")
                .get()
                .await()

        val recipes = snapshot.documents.map { doc ->

            ProductRecipeEntity(
                id = doc.id,
                productId =
                    doc.getString("productId") ?: "",
                productName =
                    doc.getString("productName") ?: "",
                inventoryItemId =
                    doc.getString("inventoryItemId") ?: "",
                inventoryItemName =
                    doc.getString("inventoryItemName") ?: "",
                quantity =
                    doc.getDouble("quantity") ?: 0.0,
                wastagePercent =
                    doc.getDouble("wastagePercent") ?: 0.0,
                unit =
                    doc.getString("unit") ?: "",
                createdAt =
                    doc.getTimestamp("createdAt")
                        ?.toDate()
                        ?.time ?: 0L
            )
        }

        db.productRecipeDao().clear()

        db.productRecipeDao()
            .insertAll(recipes)
    }
}