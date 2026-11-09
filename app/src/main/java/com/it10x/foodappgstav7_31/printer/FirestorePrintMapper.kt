package com.it10x.foodappgstav7_31.printer

import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_31.data.online.models.OrderProductData
import com.it10x.foodappgstav7_31.data.online.models.formattedTime
import com.it10x.foodappgstav7_31.data.online.models.toDoubleSafe
import com.it10x.foodappgstav7_31.data.pos.models.CartModifier
import com.it10x.foodappgstav7_31.data.pos.models.CartModifierItem

object FirestorePrintMapper {

    fun map(
        order: OrderMasterData,
        items: List<OrderProductData>
    ): PrintOrder {

        val printItems = items.map { item ->

            // =====================================================
            // MODIFIERS
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

            val modifiersJson =
                ModifierJsonHelper.toJson(modifiers)

            PrintItem(
                name = item.name,
                quantity = item.quantity,
                price = item.finalPriceDouble(),
                subtotal = toDouble(item.itemSubtotal),

                // NOTE
                note = item.note ?: "",

                taxRate = toDouble(item.taxRate),
                taxType = item.taxType ?: "",

                // MODIFIERS
                modifiersJson = modifiersJson
            )
        }

        return PrintOrder(

            // ---------- CORE ----------
            orderNo = order.srno,
            customerName = order.customerName.ifBlank { "Walk-in" },
            dateTime = order.formattedTime(),

            // ---------- ORDER TYPE ----------
            orderType = order.orderType,
            tableNo = order.tableNo,
            paymentMode = order.paymentType,

            // ---------- DELIVERY SNAPSHOT ----------
            dAddressLine1 = order.dAddressLine1,
            dAddressLine2 = order.dAddressLine2,
            dCity = order.dCity,
            dState = order.dState,
            dZipcode = order.dZipcode,
            customerPhone = order.customerPhone,
            dLandmark = order.dLandmark,

            // ---------- ITEMS ----------
            items = printItems,

            // ---------- TOTALS ----------
            itemTotal = toDouble(order.itemTotal),
            deliveryFee = toDouble(order.deliveryFee),
            discount = toDouble(order.discountTotal),
            tax = toDouble(order.taxTotal),
            grandTotal = toDouble(order.grandTotal)
        )
    }

    private fun toDouble(value: Any?): Double =
        when (value) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
}