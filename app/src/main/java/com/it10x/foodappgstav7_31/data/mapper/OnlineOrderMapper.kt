package com.it10x.foodappgstav7_31.data.mapper

import android.util.Log
import com.it10x.foodappgstav7_31.data.online.models.OrderProductData
import com.it10x.foodappgstav7_31.data.online.models.toDoubleSafe
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.data.pos.models.CartModifier
import com.it10x.foodappgstav7_31.data.pos.models.CartModifierItem


object OnlineOrderMapper {

    fun toKotItems(
        orderProducts: List<OrderProductData>,
        kotBatchId: String = "ONLINE",
        tableNo: String? = null
    ): List<PosKotItemEntity> {

        val now = System.currentTimeMillis()

        Log.d(
            "ONLINE_KOT",
            "Mapping ${orderProducts.size} online products to KOT items"
        )

        return orderProducts.mapIndexed { index, item ->

            // =====================================================
            // FIRESTORE MODIFIERS → CART MODIFIERS
            // =====================================================

            val modifiers = item.modifiers
                .groupBy { it.groupId }
                .map { (groupId, modifierItems) ->

                    CartModifier(
                        groupId = groupId,
                        groupName = "",
                        items = modifierItems.map { modifier ->

                            CartModifierItem(
                                itemId = modifier.id,
                                name = modifier.name,
                                price = modifier.price.toDoubleSafe()

                            )
                        }
                    )
                }

            // =====================================================
            // CONVERT TO JSON
            // =====================================================

            val modifiersJson = ModifierJsonHelper.toJson(modifiers)

//            Log.e(
//                "ONLINE_KOT",
//                """
//                ====================================================
//                ONLINE KOT ITEM [$index]
//                name       = ${item.name}
//                quantity   = ${item.quantity}
//                modifiers  = ${item.modifiers.size}
//                json       = $modifiersJson
//                ====================================================
//                """.trimIndent()
//            )

            PosKotItemEntity(
                id = item.id.ifBlank {
                    "ONLINE-${System.nanoTime()}"
                },

                categoryName = item.categoryName,
                productMode = "",
                currentStock = 0.0,

                sessionId = "",
                kotBatchId = kotBatchId,

                tableNo = tableNo ?: "ONLINE",
                tableName = "ONLINE",

                productId = item.productId.ifBlank {
                    item.id
                },

                name = item.name,

                categoryId = item.productCat.ifBlank {
                    "ONLINE"
                },

                parentId = null,
                isVariant = false,

                basePrice = item.priceDouble(),
                finalPrice = 0.0,
                modifierTotal = 0.0,

                quantity = item.quantity,

                taxRate = (item.taxRate as? Number)?.toDouble() ?: 0.0,
                taxType = "exclusive",

                taxCategory = "FOOD_GST",
                taxRate1 =   0.0,
                taxType1 = "exclusive",

                // ⭐ THIS WAS MISSING
                modifiersJson = modifiersJson,
                note = item.note ?: "",
                kotNumber = "",
                status = "DONE",
                kitchenPrinted = false,

                createdAt = now
            )
        }
    }
}