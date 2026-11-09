package com.it10x.foodappgstav7_31.fiskaly

import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.fiscal.FiscalContext
import com.it10x.foodappgstav7_31.fiscal.FiscalService
import com.it10x.foodappgstav7_31.fiskaly.FiskalyRepository
import com.it10x.foodappgstav7_31.network.model.PaymentAmount
import com.it10x.foodappgstav7_31.network.model.VatAmount
import com.it10x.foodappgstav7_31.ui.payment.PaymentInput
import com.it10x.foodappgstav7_31.utils.MoneyUtils

class GermanyFiscalService(
    private val fiskalyRepository: FiskalyRepository
) : FiscalService {

    override suspend fun start(): FiscalContext {
        val (txId, clientId) = fiskalyRepository.startTransaction()
        return FiscalContext(txId, clientId)
    }

    override suspend fun finish(
        context: FiscalContext,
        payments: List<PaymentInput>,
        items: List<PosKotItemEntity>
    ) {

        // ✅ VAT breakdown
        // ✅ VAT breakdown (STRICT SAFE)
        val vatList = items
            .groupBy { it.taxRate }
            .map { (rate, list) ->

                val (vatType, germanRate) = mapGermanVat(rate)

                val totalPaise = list.sumOf {

                    val basePaise = MoneyUtils.toPaise(it.basePrice)

                    val taxPaise = ((basePaise * germanRate) / 100.0).toLong()

                    (basePaise + taxPaise) * it.quantity
                }

                val amountDouble = MoneyUtils.fromPaise(totalPaise)

                Log.d("FISCAL_DEBUG", "VAT $rate → $vatType ($germanRate%) = $amountDouble")

                VatAmount(
                    vat_rate = vatType,
                    amount = String.format("%.2f", amountDouble)
                )
            }

        // ✅ Payment breakdown
        val paymentList = payments.map {
            PaymentAmount(
                payment_type =  mapPayment(it.mode),
                amount = String.format(
                    "%.2f",
                    MoneyUtils.fromPaise(it.amount)
                )
            )
        }

        // 🔥 ADD HERE (RIGHT BEFORE API CALL)
        try {

            require(context.txId != null) { "txId missing" }
            require(context.clientId != null) { "clientId missing" }
            require(vatList.isNotEmpty()) { "VAT empty" }
            require(paymentList.isNotEmpty()) { "Payment empty" }

        } catch (e: Exception) {
            Log.e("FISCAL_VALIDATE", "❌ Validation failed: ${e.message}")
            throw e
        }

        fiskalyRepository.finishTransaction(
            txId = context.txId,
            clientId = context.clientId!!,
            vatList = vatList,
            paymentList = paymentList
        )
    }

    override suspend fun cancel(context: FiscalContext) {
        fiskalyRepository.cancelTransaction(
            context.txId!!,
            context.clientId!!
        )
    }



    private fun mapPayment(mode: String): String {
        return when (mode.uppercase()) {
            "CASH" -> "CASH"
            "CARD" -> "EC"
            else -> "OTHER"
        }
    }

    private fun mapGermanVat(rate: Double): Pair<String, Double> {
        return when {
            rate >= 18.0 -> "NORMAL" to 19.0   // map 18–20 → 19%
            rate >= 6.0 -> "REDUCED" to 7.0    // map 6–10 → 7%
            else -> "REDUCED" to 7.0
        }
    }
}