package com.it10x.foodappgstav7_31.utils.bill


import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
//import com.it10x.foodappgstav7_31.utils.ModifierJsonHelper
import com.it10x.foodappgstav7_31.utils.MoneyUtils
import com.it10x.foodappgstav7_31.utils.tax.TaxMode
import kotlin.math.roundToLong


object BillCalculator {

    fun calculate(
        items: List<PosKotItemEntity>,
        taxMode: String,
        discountFlat: Double,
        discountPercent: Double,
        deliveryFee: Double,
        deliveryTaxPercent: Double,
        loyaltyDiscount: Double = 0.0
    ): BillCalculationResult {

        // =========================
        // ITEM SUBTOTAL
        // =========================
        val itemSubtotalPaise = items.sumOf {

            val modifierPrice =
                ModifierJsonHelper.fromJson(it.modifiersJson)
                    .flatMap { g -> g.items }
                    .sumOf { m -> m.price }

                        Log.d(
                "POS_MODIFIER",
                "ITMENAME=${it.name}, taxCat=${it.taxCategory} taxType1=${it.taxRate1} ${it.taxType1} "
            )

            //  val base = it.basePrice
            val base = it.basePrice + modifierPrice
            MoneyUtils.toPaise(base) * it.quantity

        }

        // =========================
        // RAW TAX
        // =========================
        var exclusiveTaxPaise = 0L
        var inclusiveTaxPaise = 0L

        items.forEach { item ->

            val modifierPrice =
                ModifierJsonHelper.fromJson(item.modifiersJson)
                    .flatMap { g -> g.items }
                    .sumOf { m -> m.price }


val basePrice_modi = item.basePrice + modifierPrice
            val basePaise = MoneyUtils.toPaise(basePrice_modi)

            val taxType =
                effectiveTaxType(
                    taxMode,
                    item.taxType
                )

            val taxPerItem = when (taxType.lowercase()) {

                "exclusive" -> {
                    (basePaise * item.taxRate / 100.0)
                        .roundToLong()
                }

                "inclusive" -> {
                    (basePaise * item.taxRate / (100.0 + item.taxRate))
                        .roundToLong()
                }

                else -> 0L
            }

            if (taxType.equals("exclusive", true)) {
                exclusiveTaxPaise += taxPerItem * item.quantity
            } else if (taxType.equals("inclusive", true)) {
                inclusiveTaxPaise += taxPerItem * item.quantity
            }
        }

        // =========================
// NORMAL DISCOUNT
// =========================
        val flatPaise = MoneyUtils.toPaise(discountFlat)

        val discountEligibleSubtotalPaise = items.sumOf { item ->
            val modifierPrice =
                ModifierJsonHelper.fromJson(item.modifiersJson)
                    .flatMap { it.items }
                    .sumOf { it.price }

            val basePrice_modi = item.basePrice + modifierPrice

            if (item.discountEligible) {
                MoneyUtils.toPaise(basePrice_modi) * item.quantity
            } else {
                0L
            }
        }

        val percentPaise =
            ((discountEligibleSubtotalPaise * discountPercent) / 100.0)
                .roundToLong()

        val discountPaise =
            if (flatPaise > 0) flatPaise else percentPaise

        val safeDiscountPaise =
            discountPaise.coerceAtMost(itemSubtotalPaise)

// =========================
// LOYALTY DISCOUNT
// =========================
        val requestedLoyaltyDiscountPaise =
            MoneyUtils.toPaise(loyaltyDiscount)

        val remainingAfterNormalDiscount =
            (itemSubtotalPaise - safeDiscountPaise).coerceAtLeast(0L)

        val safeLoyaltyDiscountPaise =
            requestedLoyaltyDiscountPaise.coerceAtMost(
                remainingAfterNormalDiscount
            )

// Combined discount
        val totalItemDiscountPaise =
            safeDiscountPaise + safeLoyaltyDiscountPaise

// =========================
// RECALCULATE TAX AFTER DISCOUNTS
// =========================
        val discountRatio =
            if (itemSubtotalPaise == 0L) {
                0.0
            } else {
                totalItemDiscountPaise.toDouble() /
                        itemSubtotalPaise.toDouble()
            }

        exclusiveTaxPaise =
            (exclusiveTaxPaise * (1 - discountRatio))
                .roundToLong()

        inclusiveTaxPaise =
            (inclusiveTaxPaise * (1 - discountRatio))
                .roundToLong()

        // =========================
        // DISCOUNT
        // =========================
//        val flatPaise =
//            MoneyUtils.toPaise(discountFlat)
//
//        // Calculate amount eligible for percentage discount
//        val discountEligibleSubtotalPaise =
//            items.sumOf { item ->
//
//                val modifierPrice =
//                    ModifierJsonHelper.fromJson(item.modifiersJson)
//                        .flatMap { g -> g.items }
//                        .sumOf { m -> m.price }
//
//
//                val basePrice_modi = item.basePrice + modifierPrice
//                if (item.discountEligible) {
//                    MoneyUtils.toPaise(basePrice_modi) * item.quantity
//                } else {
//                    0L
//                }
//            }
//
//        val percentPaise =
//            ((discountEligibleSubtotalPaise * discountPercent) / 100.0)
//                .roundToLong()
//
//        val discountPaise =
//            if (flatPaise > 0)
//                flatPaise
//            else
//                percentPaise
//
//        val safeDiscountPaise =
//            discountPaise.coerceAtMost(itemSubtotalPaise)
//
//        val discountRatio =
//            if (itemSubtotalPaise == 0L)
//                0.0
//            else
//                safeDiscountPaise.toDouble() /
//                        itemSubtotalPaise.toDouble()
//
//        exclusiveTaxPaise =
//            (exclusiveTaxPaise * (1 - discountRatio))
//                .roundToLong()
//
//        inclusiveTaxPaise =
//            (inclusiveTaxPaise * (1 - discountRatio))
//                .roundToLong()

        // =========================
        // DELIVERY
        // =========================
        val deliveryFeePaise =
            MoneyUtils.toPaise(deliveryFee)

        val deliveryRate = resolveDeliveryTaxRate(
            items,
            taxMode,
            deliveryTaxPercent
        )
        val deliveryTaxPaise =
            (deliveryFeePaise * deliveryRate / 100.0)
                .roundToLong()
        val totalTaxPaise =
            exclusiveTaxPaise +
                    inclusiveTaxPaise +
                    deliveryTaxPaise

        // =========================
// TAXABLE AMOUNT
// =========================
// Taxable amount after discount,
// before adding GST.
        // =========================
// TAXABLE AMOUNT
// =========================
        val taxableAmountPaise =
            itemSubtotalPaise - totalItemDiscountPaise



// =========================
// GRAND TOTAL
// =========================
        val hasExclusiveTax = items.any {
            effectiveTaxType(taxMode, it.taxType).equals("exclusive", ignoreCase = true)
        }

//        val calculatedGrandTotalPaise =
//            itemSubtotalPaise -
//                    safeDiscountPaise +
//                    exclusiveTaxPaise +
//                    deliveryFeePaise +
//                    deliveryTaxPaise


        // =========================
// GRAND TOTAL
// =========================
        val calculatedGrandTotalPaise =
            taxableAmountPaise +
                    exclusiveTaxPaise +
                    deliveryFeePaise +
                    deliveryTaxPaise

// =========================
// ROUND OFF
// =========================
// Round payable amount to nearest rupee.
        val roundedGrandTotalPaise =
            ((calculatedGrandTotalPaise + 50L) / 100L) * 100L

        val roundOffPaise =
            roundedGrandTotalPaise -
                    calculatedGrandTotalPaise

        val grandTotalPaise =
            calculatedGrandTotalPaise +
                    roundOffPaise

        return BillCalculationResult(
            itemSubtotalPaise = itemSubtotalPaise,

            exclusiveTaxPaise = exclusiveTaxPaise,
            inclusiveTaxPaise = inclusiveTaxPaise,
            totalTaxPaise = totalTaxPaise,

            discountPaise = safeDiscountPaise,

            deliveryFeePaise = deliveryFeePaise,
            deliveryTaxPaise = deliveryTaxPaise,

            taxableAmountPaise = taxableAmountPaise,
            loyaltyDiscountPaise = safeLoyaltyDiscountPaise,
            roundOffPaise = roundOffPaise,

            grandTotalPaise = grandTotalPaise
        )
    }

    private fun resolveDeliveryTaxRate(
        items: List<PosKotItemEntity>,
        taxMode: String,
        deliveryTaxPercent: Double
    ): Double {

        // 1. Explicit delivery GST configured
        if (deliveryTaxPercent > 0) {
            return deliveryTaxPercent
        }

        // 2. Otherwise use item/order GST
        val firstItem = items.firstOrNull() ?: return 0.0

        return firstItem.taxRate
    }


}

private fun effectiveTaxType(
    outletTaxMode: String,
    itemTaxType: String
): String {

    return when (outletTaxMode) {

        TaxMode.FORCE_INCLUSIVE -> "inclusive"

        TaxMode.FORCE_EXCLUSIVE -> "exclusive"

        else -> itemTaxType
    }
}