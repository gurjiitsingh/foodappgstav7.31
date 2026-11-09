package com.it10x.foodappgstav7_31.data.online.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.it10x.foodappgstav7_31.printer.utils.QrUtils
import com.it10x.foodappgstav7_31.utils.tax.TaxMode

class OutletSyncRepository(
    private val db: AppDatabase,
    private val context: android.content.Context
) {

    private val firestore = FirebaseFirestore.getInstance()

    // --------------------------------------------------
    // ⭐ DOWNLOAD & SAVE SINGLE OUTLET
    // --------------------------------------------------
    suspend fun syncOutlet() = withContext(Dispatchers.IO) {

        val snapshot = firestore
            .collection("outlets")
            .limit(1)     // ✅ SINGLE OUTLET DESIGN
            .get()
            .await()

        if (snapshot.isEmpty) {
            Log.w("SYNC_OUTLET", "No outlet found in Firestore")
            db.outletDao().deleteOutlet()
            return@withContext
        }

        val doc = snapshot.documents.first()
        val data = doc.data ?: return@withContext

        val outlet = OutletEntity(

            // 🔑 Firestore doc id
            outletId = doc.id,

            outletName = data["outletName"] as? String ?: "",
            ownerId = data["ownerId"] as? String ?: "",
            // ---------- ADDRESS ----------
            addressLine1 = data["addressLine1"] as? String ?: "",
            addressLine2 = data["addressLine2"] as? String,
            addressLine3 = data["addressLine3"] as? String,   // ⭐ NEW
            city = data["city"] as? String ?: "",
            state = data["state"] as? String,
            zipcode = data["zipcode"] as? String,
            countryName = data["countryName"] as? String,

            // ---------- TAX ----------
            taxType = data["taxType"] as? String,
            taxMode = data["taxMode"] as? String ?: TaxMode.PER_ITEM,
            gstVatNumber = data["gstVatNumber"] as? String,
            fssaiNumber = data["fssaiNumber"] as? String ?: "",

            // ---------- CONTACT ----------
            phone = data["phone"] as? String ?: "",
            phone2 = data["phone2"] as? String,
            email = data["email"] as? String,
            web = data["web"] as? String,                    // ⭐ NEW
            logoUrl = data["logoUrl"] as? String,
            // ---------- POS / PRINTER ----------
            printerWidth = when ((data["printerWidth"] as? Number)?.toInt()) {
                58 -> 58
                else -> 80
            },
            printerIPBill = data["printerIPBill"] as? String ?: "",
            printerIPKitchen = data["printerIPKitchen"] as? String ?: "",
            printerName = data["printerName"] as? String,
            footerNote = data["footerNote"] as? String,

            // ---------- QR ----------
            qrEnabled = data["qrEnabled"] as? Boolean ?: false,
            qrText = data["qrText"] as? String,
            qrTitle = data["qrTitle"] as? String,

            // ----------  UPI (NEW) ----------
            upiId = data["upiId"] as? String,
            upiName = data["upiName"] as? String,
            upiTitle = data["upiTitle"] as? String,

            // ---------- COUNTRY ----------
            countryCode = data["countryCode"] as? String ?: "IN",

            currencyCode = data["currencyCode"] as? String ?: "INR",

            localeTag = data["localeTag"] as? String ?: "en-IN",

            // ---------- STATUS ----------
            isActive = data["isActive"] as? Boolean ?: true,
            // ---------- NEW: DEFAULT CURRENCY ----------

            posType = data["posType"] as? String ?: "RESTAU",
            renewDate = data["renewDate"] as? String,
            showCategorySidebar =
            data["showCategorySidebar"] as? Boolean ?: true,

            startupScreen =
                data["startupScreen"] as? String ?: "tables",
        )

        Log.d(
            "SYNC_OUTLET",
            """
Outlet Synced:
Name       = ${outlet.outletName}
Addr1      = ${outlet.addressLine1}
Addr2      = ${outlet.addressLine2 ?: "-"}
Addr3      = ${outlet.addressLine3 ?: "-"}
City       = ${outlet.city}
Phone1     = ${outlet.phone}
Phone2     = ${outlet.phone2 ?: "-"}
Email      = ${outlet.email ?: "-"}
Web        = ${outlet.web ?: "-"}
Logo       = ${outlet.logoUrl ?: "No Logo"}
GST/VAT    = ${outlet.gstVatNumber ?: "N/A"}
Printer    = ${outlet.printerWidth}mm
""".trimIndent()
        )

        // ✅ Single-row table → replace
        db.outletDao().deleteOutlet()
        db.outletDao().saveOutlet(outlet)


        // --------------------------------------------
// ✅ DOWNLOAD LOGO AFTER SAVING OUTLET
// --------------------------------------------
        outlet.logoUrl?.let { url ->
            try {
                Log.d("LOGO_DEBUG", "Downloading from: $url")
                val logoFile = java.io.File(context.filesDir, "logo.png")

                val bitmap = downloadBitmapFromUrl(url)
//                bitmap?.let {
//                    java.io.FileOutputStream(logoFile).use { out ->
//                        it.compress(
//                            android.graphics.Bitmap.CompressFormat.PNG,
//                            100,
//                            out
//                        )
//                    }
//                    Log.d("LOGO_DEBUG", "Saved at: ${logoFile.absolutePath}")
//                    Log.d("LOGO_DEBUG", "File exists: ${logoFile.exists()}")
//                    Log.d("LOGO_DEBUG", "File size: ${logoFile.length()}")
//                    Log.d("SYNC_OUTLET", "Logo downloaded/updated")
//                }

                bitmap?.let { original ->

                    // ✅ Resize ONCE before saving
                    val resized = com.it10x.foodappgstav7_31.printer.bluetooth.BluetoothPrinter
                        .resizeBitmap(original, 300)

                    java.io.FileOutputStream(logoFile).use { out ->
                        resized.compress(
                            android.graphics.Bitmap.CompressFormat.PNG,
                            100,
                            out
                        )
                    }

                    Log.d(
                        "LOGO_DEBUG",
                        "Saved resized logo: ${resized.width}x${resized.height}"
                    )
                }

            } catch (e: Exception) {
                Log.e("SYNC_OUTLET", "Logo download failed", e)
            }
        }

        Log.d("QR_DEBUG","me ${outlet.upiId}  ${outlet.qrEnabled}")
        // --------------------------------------------
// ✅ GENERATE & SAVE QR PNG
// --------------------------------------------


        if (
            outlet.qrEnabled &&
            !outlet.upiId.isNullOrBlank()
        ) {

            try {

                Log.d("QR_DEBUG", "Generating QR")

                val qrBitmap = QrUtils.generateQr(
                    outlet.upiId,
                    256
                )

                val qrFile = java.io.File(
                    context.filesDir,
                    "qr.png"
                )

                java.io.FileOutputStream(qrFile).use { out ->

                    qrBitmap.compress(
                        android.graphics.Bitmap.CompressFormat.PNG,
                        100,
                        out
                    )
                }

                Log.d(
                    "QR_DEBUG",
                    "QR saved at: ${qrFile.absolutePath}"
                )

            } catch (e: Exception) {

                Log.e(
                    "QR_DEBUG",
                    "QR generation failed",
                    e
                )
            }

        } else {

            // ✅ DELETE OLD QR IF QR DISABLED

            val qrFile = java.io.File(
                context.filesDir,
                "qr.png"
            )

            if (qrFile.exists()) {

                qrFile.delete()

                Log.d(
                    "QR_DEBUG",
                    "Old QR deleted"
                )
            }
        }

    }

    private fun downloadBitmapFromUrl(url: String): Bitmap? {
        return try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.connect()

            val input = connection.inputStream
            val bytes = input.readBytes()

            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            if (bitmap == null) {
                Log.e("LOGO_DEBUG", "Bitmap decode FAILED ❌")
            } else {
                Log.d("LOGO_DEBUG", "Bitmap decode SUCCESS ✅")
            }

            bitmap

        } catch (e: Exception) {
            Log.e("LOGO_DEBUG", "Download error ❌", e)
            null
        }
    }

}
