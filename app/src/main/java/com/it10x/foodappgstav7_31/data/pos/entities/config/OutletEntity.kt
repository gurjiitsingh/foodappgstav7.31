package com.it10x.foodappgstav7_31.data.pos.entities.config

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.it10x.foodappgstav7_31.utils.tax.TaxMode

@Entity(tableName = "outlet_config")
data class OutletEntity(

    @PrimaryKey
    val outletId: String,

    val outletName: String,
    val ownerId: String, // ✅ ADD THIS (REQUIRED FOR SYNC)
    // ---------- ADDRESS ----------
    val addressLine1: String,
    val addressLine2: String?,
    val addressLine3: String?,     // ⭐ NEW
    val city: String,
    val state: String?,
    val zipcode: String?,
    val countryName: String?,


    // ---------- COUNTRY ---------
    val countryCode: String? = null,
    val currencyCode: String = "INR",
    val localeTag: String? = null,
    // ---------- TAX ----------

    val taxType: String?,
    val taxMode: String = TaxMode.PER_ITEM,
    val gstVatNumber: String?,
    val fssaiNumber: String?,
    // ---------- CONTACT ----------
    val phone: String,
    val phone2: String?,           // ⭐ already there
    val email: String?,            // ⭐ already there
    val web: String?,              // ⭐ NEW
    val logoUrl: String?,


    // ---------- PRINTER ----------
    val printerWidth: Int,
    val printerName: String?,
    val footerNote: String?,
    val printerIPKitchen: String?,
    val printerIPBill: String?,
    // QR
    val qrEnabled: Boolean = false,

    val qrText: String? = null,
    val qrTitle: String? = null,

    //------------ UPI -------
    val upiId: String?,
    val upiName: String?,
    val upiTitle: String? = null,

   // ---------- POS UI SETTINGS ----------
    val posType: String = "RESTAU",
    val renewDate: String? = null,
    val showCategorySidebar: Boolean = true,

    // START SCREEN:
    // "tables" or "pos"
//    val startupScreen: String = "tables",
    val startupScreen: String = "pos",

    // ---------- STATUS ----------
    val isActive: Boolean,

    // ---------- META ----------
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
