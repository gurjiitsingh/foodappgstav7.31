package com.it10x.foodappgstav7_31.data.print


import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity

/**
 * Utility mapper to safely build OutletInfo from database entity.
 */
object OutletMapper {

    fun fromEntity(outlet: OutletEntity?): OutletInfo {
        return outlet?.let {
            OutletInfo(
                outletId = it.outletId,

                outletName = it.outletName.takeIf { it.isNotBlank() } ?: "FOOD APP",
                posType = it.posType ?: "RESTAU",

                addressLine1 = it.addressLine1.takeIf { it.isNotBlank() } ?: "",
                addressLine2 = it.addressLine2?.takeIf { it.isNotBlank() },
                addressLine3 = it.addressLine3?.takeIf { it.isNotBlank() },

                city = it.city.takeIf { it.isNotBlank() },
                state = it.state?.takeIf { it.isNotBlank() },
                zipcode = it.zipcode?.takeIf { it.isNotBlank() },
                countryName = it.countryName?.takeIf { it.isNotBlank() },

                taxType = it.taxType?.takeIf { it.isNotBlank() },
                taxMode = it.taxMode,
                gstVatNumber = it.gstVatNumber?.takeIf { it.isNotBlank() },
                fssaiNumber = it.fssaiNumber?.takeIf { it.isNotBlank() },

                phone = it.phone.takeIf { it.isNotBlank() },
                phone2 = it.phone2?.takeIf { it.isNotBlank() },

                email = it.email?.takeIf { it.isNotBlank() },
                web = it.web?.takeIf { it.isNotBlank() },

                printerWidth = it.printerWidth,
                printerName = it.printerName?.takeIf { it.isNotBlank() },

                footerNote = it.footerNote?.takeIf { it.isNotBlank() },

                // ✅ QR
                qrEnabled = it.qrEnabled,
                qrText = it.qrText?.takeIf { qr -> qr.isNotBlank() },
                qrTitle = it.qrTitle?.takeIf { qr -> qr.isNotBlank() },


                // ✅ UPI (GROUPED CLEANLY)
                upiId = it.upiId?.takeIf { it.isNotBlank() },
                upiName = it.upiName?.takeIf { it.isNotBlank() },
                upiTitle = it.upiTitle?.takeIf { it.isNotBlank() },

                isActive = it.isActive,

                countryCode = it.countryCode
                    ?.takeIf { code -> code.isNotBlank() } ?: "IN",

                localeTag = it.localeTag
                    ?.takeIf { locale -> locale.isNotBlank() } ?: "en-IN",

                currencyCode = it.currencyCode
                    ?.takeIf { code -> code.isNotBlank() } ?: "INR",

                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        } ?: OutletInfo(outletName = "FOOD APP")
    }
}