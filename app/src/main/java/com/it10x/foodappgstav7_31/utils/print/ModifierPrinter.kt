package com.it10x.foodappgstav7_31.utils.print

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.it10x.foodappgstav7_31.data.pos.models.CartModifier

object ModifierPrinter {

    private val gson = Gson()

    fun format(modifiersJson: String): List<String> {

        if (modifiersJson.isBlank()) return emptyList()

        val type = object : TypeToken<List<CartModifier>>() {}.type

        val list: List<CartModifier> = try {
            gson.fromJson(modifiersJson, type)
        } catch (e: Exception) {
            return emptyList()
        }

        val lines = mutableListOf<String>()

        list.forEach { group ->

            lines.add("  ${group.groupName}")

            group.items.forEach { item ->
                val priceText = if (item.price > 0)
                    " (+₹${item.price})"
                else ""

                lines.add("    - ${item.name}$priceText")
            }
        }

        return lines
    }
}