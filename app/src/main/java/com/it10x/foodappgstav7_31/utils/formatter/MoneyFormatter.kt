package com.it10x.foodappgstav7_31.utils.formatter

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormatter {

    fun format(
        amount: Double,
        currencyCode: String,
        localeTag: String
    ): String {

        return try {

            val parts = localeTag.split("-")

            val locale = if (parts.size >= 2) {
                Locale(parts[0], parts[1])
            } else {
                Locale.forLanguageTag(localeTag)
            }

            val format = NumberFormat.getCurrencyInstance(locale)

            format.currency = Currency.getInstance(currencyCode)

            format.format(amount)

        } catch (e: Exception) {

            // Safe fallback
            "$currencyCode %.2f".format(amount)
        }
    }
}