package com.it10x.foodappgstav7_31.data.online.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.ModifierGroupEntity
import com.it10x.foodappgstav7_31.data.pos.entities.ModifierItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.ProductModifierEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ModifierSyncRepository(
    private val db: AppDatabase
) {

    private val firestore = FirebaseFirestore.getInstance()

    // --------------------------------------------------
    // ⭐ SYNC MODIFIER GROUPS
    // --------------------------------------------------
    suspend fun syncModifierGroups() = withContext(Dispatchers.IO) {

        val snapshot = firestore
            .collection("modifierGroups")
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            ModifierGroupEntity(
                id = doc.id,
                name = data["name"] as? String ?: "",
                minSelection = (data["minSelection"] as? Number)?.toInt() ?: 0,
                maxSelection = (data["maxSelection"] as? Number)?.toInt() ?: 0,
                sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,
                status = data["status"] as? String ?: "draft"
            )
        }

        db.modifierGroupDao().clear()
        db.modifierGroupDao().insertAll(list)

        Log.d("SYNC_MOD_GROUP", "Inserted ${list.size} modifier groups")
    }

    // --------------------------------------------------
    // ⭐ SYNC MODIFIER ITEMS
    // --------------------------------------------------
    suspend fun syncModifierItems() = withContext(Dispatchers.IO) {

        val snapshot = firestore
            .collection("modifierItems")
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            ModifierItemEntity(
                id = doc.id,
                name = data["name"] as? String ?: "",
                groupId = data["groupId"] as? String ?: "",
                price = anyToDouble(data["price"]),
                isDefault = data["isDefault"] as? Boolean ?: false,
                sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,
                status = data["status"] as? String ?: "draft"
            )
        }

        db.modifierItemDao().clear()
        db.modifierItemDao().insertAll(list)

        Log.d("SYNC_MOD_ITEM", "Inserted ${list.size} modifier items")
    }

    // --------------------------------------------------
    // ⭐ SYNC PRODUCT ↔ MODIFIER GROUP MAPPING
    // --------------------------------------------------
    suspend fun syncProductModifiers() = withContext(Dispatchers.IO) {

        val snapshot = firestore
            .collection("productModifiers")
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            ProductModifierEntity(
                id = doc.id,
                productId = data["productId"] as? String ?: "",
                groupId = data["groupId"] as? String ?: "",
                sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0
            )
        }

        db.productModifierDao().clear()
        db.productModifierDao().insertAll(list)

        Log.d("SYNC_PRODUCT_MOD", "Inserted ${list.size} mappings")
    }

    // --------------------------------------------------
    // ⭐ HELPER
    // --------------------------------------------------
    private fun anyToDouble(v: Any?): Double =
        when (v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
}