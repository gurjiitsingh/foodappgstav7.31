package com.it10x.foodappgstav7_31.printer

import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.print.OutletInfo
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Double
import kotlin.String

object PrintOrderBuilder {

    // -------------------------
    // BUILD PRINT ORDER
    // -------------------------
    fun build(
        master: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>,
        outlet: OutletInfo
    ): PrintOrder {

        val printItems = items.map { item ->
            PrintItem(
                name = item.name,
                quantity = item.quantity,
                price = item.basePrice,
                taxRate = item.taxRate,
                taxType = item.taxType,
                subtotal = item.itemSubtotal,
                note = item.note ?: "",
                modifiersJson = item.modifiersJson ?: ""
            )
        }

        return PrintOrder(

            // ---------- CORE ----------
            orderNo = master.srno,
            dateTime = master.createdAt.formatMillis(),

            // ---------- ORDER TYPE ----------
            orderType = master.orderType,
            tableNo = master.tableNo,
            paymentMode = master.paymentMode,


            // ---------- DELIVERY ----------
            customerName = master.customerName?:"Walk-in",
            customerPhone = master.customerPhone,
            dAddressLine1 = master.dAddressLine1,
            dAddressLine2 = master.dAddressLine2,
            dCity = master.dCity,
            dLandmark = master.dLandmark,
            dState = null,
            dZipcode = null,


            // ---------- ITEMS ----------
            items = printItems,

            // ---------- TOTALS ----------
            itemTotal = master.itemTotal,

            itemTax = master.itemTax,
            deliveryTax = master.deliveryTax,
            tax = master.taxTotal,

            deliveryFee = master.deliveryFee,
            discount = master.discountTotal,
            loyaltyDiscountPaise = master.loyaltyDiscountPaise,
            taxableAmount = master.taxableAmount,
            roundOff = master.roundOff,
// Temporary until we derive it from outlet/item
            gstRate = items.firstOrNull()?.taxRate ?: 0.0,
            taxMode = outlet.taxMode,

            grandTotal = master.grandTotal
        )
    }



    // -------------------------
    // DATE FORMAT
    // -------------------------
    private fun Long.formatMillis(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(this))
    }
}
