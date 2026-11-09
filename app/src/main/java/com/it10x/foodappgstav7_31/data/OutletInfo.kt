package com.it10x.foodappgstav7_31.data.print
import com.it10x.foodappgstav7_31.utils.tax.TaxMode
data class OutletInfo(

    // BASIC
    val outletId: String? = null,
    val outletName: String,

    val posType: String = "RESTAU",
    // ADDRESS
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressLine3: String? = null,

    val city: String? = null,
    val state: String? = null,
    val zipcode: String? = null,
  

    // COUNTRY
    val countryCode: String? = null,

    val currencyCode: String = "INR",
    val localeTag: String = "en-IN",
    val countryName: String? = null,
    // TAX
    val taxType: String? = null,
    val gstVatNumber: String? = null,
    val fssaiNumber: String? = null,
    var taxMode: String = TaxMode.PER_ITEM,

    // CONTACT
    val phone: String? = null,
    val phone2: String? = null,
    val email: String? = null,
    val web: String? = null,

    // UPI
    val upiId: String? = null,
    val upiName: String? = null,
    val upiTitle: String? = null,

    // PRINTER / POS
    val printerWidth: Int? = 80,
    val printerName: String? = null,
    val footerNote: String? = null,

    // QR
    val qrEnabled: Boolean = false,
    val qrText: String? = null,
    val qrTitle: String? = null,

    // STATUS
    val isActive: Boolean = true,


    // METADATA
    val createdAt: Any? = null,
    val updatedAt: Any? = null
)