package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.model.ModifierGroupWithItems

class ModifierRepository(private val db: AppDatabase) {


    suspend fun getModifiersForProduct1(
        productId: String
    ): List<ModifierGroupWithItems>
    {

        val mappings =
            db.productModifierDao().getByProduct(productId)

        Log.d(
            "MOD_DEBUG",
            "=============================================="
        )

        Log.d(
            "MOD_DEBUG",
            "getModifiersForProduct()"
        )

        Log.d(
            "MOD_DEBUG",
            "productId = $productId"
        )

        Log.d(
            "MOD_DEBUG",
            "mappings.size = ${mappings.size}"
        )

        mappings.forEachIndexed { index, map ->
            Log.d(
                "MOD_DEBUG",
                "MAPPING[$index] id=${map.id}, productId=${map.productId}, groupId=${map.groupId}, sortOrder=${map.sortOrder}"
            )
        }

        val result = mutableListOf<ModifierGroupWithItems>()

        for (map in mappings) {

            val group = db.modifierGroupDao()
                .getAll()
                .find { it.id == map.groupId }

            if (group == null) {
                Log.d(
                    "MOD_DEBUG",
                    "GROUP NOT FOUND: groupId=${map.groupId}"
                )
                continue
            }

            val items =
                db.modifierItemDao()
                    .getByGroup(group.id)

            Log.d(
                "MOD_DEBUG",
                "GROUP FOUND: id=${group.id}, name=${group.name}, items=${items.size}"
            )

            result.add(
                ModifierGroupWithItems(
                    group = group,
                    items = items
                )
            )
        }

        Log.d(
            "MOD_DEBUG",
            "FINAL modifierGroups.size = ${result.size}"
        )

        Log.d(
            "MOD_DEBUG",
            "=============================================="
        )

        return result
    }
    suspend fun getModifiersForProduct(productId: String): List<ModifierGroupWithItems> {

        val mappings = db.productModifierDao().getByProduct(productId)

        val result = mutableListOf<ModifierGroupWithItems>()

        for (map in mappings) {

            val group = db.modifierGroupDao()
                .getAll()
                .find { it.id == map.groupId } ?: continue

            val items = db.modifierItemDao()
                .getByGroup(group.id)

            result.add(
                ModifierGroupWithItems(
                    group = group,
                    items = items
                )
            )
        }

        return result
    }
}