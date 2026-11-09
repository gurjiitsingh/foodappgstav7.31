package com.it10x.foodappgstav7_31.data.mapper

import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import java.util.*

/**
 * Mapper to convert POS Order Items → KOT Items
 * (Used when reprinting or resending items to kitchen)
 */
object PosOrderToKotMapper {

    fun toKotItems(
        orderItems: List<PosOrderItemEntity>,
        kotBatchId: String = "POS",
        tableNo: String? = null
    ): List<PosKotItemEntity> {
        val now = System.currentTimeMillis()

        Log.d("PRINT", "Mapping ${orderItems.size} order items to KOT items...")

        orderItems.forEachIndexed { index, item ->
            Log.d(
                "PRINT",
                "Item[$index]: name='${item.name}', qty=${item.quantity}, base=${item.basePrice}, tax=${item.taxRate}"
            )
        }

        return orderItems.map { item ->
            PosKotItemEntity(
                id = UUID.randomUUID().toString(),
                kotNumber = "",
                sessionId = "dummySessionId",
                tableName = "ONLINE",
                kotBatchId = kotBatchId,
                tableNo = tableNo ?: "POS",

                productId = item.productId,
                categoryName = item.categoryName,
                name = item.name,
                productMode = item.productMode,
                currentStock = item.currentStock,
                categoryId = item.categoryId,

                parentId = item.parentId,
                isVariant = item.isVariant,

                basePrice = item.basePrice,
                finalPrice = 0.0,
                modifierTotal = 0.0,
                quantity = item.quantity,

                taxCategory = "FOOD_GST",
                taxRate = item.taxRate,
                taxType = item.taxType,
                taxRate1 = 0.0,
                taxType1 = "exclusive",



                status = "DONE",
                kitchenPrinted = false,
                createdAt = now
            )
        }
    }
}
