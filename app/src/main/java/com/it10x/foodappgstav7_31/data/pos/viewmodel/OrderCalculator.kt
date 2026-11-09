package com.it10x.foodappgstav7_31.data.pos.viewmodel

import com.it10x.foodappgstav7_31.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_31.utils.tax.TaxMode

object OrderCalculator {

    fun subtotal(items: List<PosCartEntity>): Double {
        return items.sumOf { it.basePrice * it.quantity }
    }

    fun tax(items: List<PosCartEntity>): Double {
        return items.sumOf {
            val rate = it.taxRate ?: 0.0
            (it.basePrice * it.quantity) * rate / 100
        }
    }

    fun grandTotal(
        items: List<PosCartEntity>,
        taxMode: String
    ): Double {

        val subtotal = subtotal(items)
        val tax = tax(items)

        return when (taxMode) {

            TaxMode.FORCE_INCLUSIVE -> {
                // Customer sees price including tax
                subtotal
            }

            TaxMode.FORCE_EXCLUSIVE -> {
                // Tax added on top
                subtotal + tax
            }

            TaxMode.PER_ITEM -> {

                subtotal + items.sumOf { item ->

                    val isExclusive =
                        item.taxType.equals("exclusive", ignoreCase = true)

                    if (isExclusive) {
                        item.basePrice *
                                item.quantity *
                                (item.taxRate / 100.0)
                    } else {
                        0.0
                    }
                }
            }

            else -> subtotal + tax
        }
    }
}
