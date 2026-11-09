package com.it10x.foodappgstav7_31.utils




object ModifierPriceCalculator {

    fun calculateTotal(modifiersJson: String): Double {
        val list = ModifierJsonHelper.fromJson(modifiersJson)

        return list.sumOf { group ->
            group.items.sumOf { it.price }
        }
    }

    fun flattenSummary(modifiersJson: String): String {
        val list = ModifierJsonHelper.fromJson(modifiersJson)

        return list.flatMap { group ->
            group.items.map {
                "${it.name} +${it.price}"
            }
        }.joinToString(", ")
    }
}