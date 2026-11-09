package com.it10x.foodappgstav7_31.data.mapper

import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderEntity
import com.it10x.foodappgstav7_31.data.online.models.OrderMasterData

fun OnlineReceivedOrderEntity.toOrderMasterData(): OrderMasterData {
    return OrderMasterData(
        id = id,
        srno = srno,
        source = source,
        orderNumber = orderNumber,

        customerId = customerId,
        customerName = customerName.orEmpty(),
        email = email.orEmpty(),
        addressId = addressId.orEmpty(),
        customerPhone = customerPhone,

        dAddressLine1 = dAddressLine1,
        dAddressLine2 = dAddressLine2,
        dCity = dCity,
        dState = dState,
        dZipcode = dZipcode,
        dLandmark = dLandmark,

        orderType = orderType,
        tableNo = tableNo,

        itemTotal = itemTotal,
        subTotal = subTotal,
        discountTotal = discountTotal,
        taxTotal = taxTotal,
        deliveryFee = deliveryFee,
        grandTotal = grandTotal,

        paymentType = paymentType.ifBlank {
            paymentMethod ?: paymentMode.orEmpty()
        },
        paymentStatus = paymentStatus,
        orderStatus = orderStatus ?: status,

        outletId = outletId,
        outletName = outletName,
        productsCount = productsCount,
        notes = notes,

        createdAt = null,
        createdAtMillis = createdAtMillis,
        orderDate = orderDate,
        orderMonth = orderMonth,
        orderYear = orderYear,

        printed = printed,
        acknowledged = acknowledged,
        syncStatus = syncStatus,

        couponFlat = couponFlat,
        pickUpDiscount = pickUpDiscount,
        couponPercent = couponPercent
    )
}