package com.it10x.foodappgstav7_31.data.pos.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore

import com.google.gson.GsonBuilder

import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity

import kotlinx.coroutines.flow.first

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// ============================================================
// JOINT EXPORT ROOT
// Master + Items
// ============================================================

data class PosOrderExport(
    val format: String = "POS_ORDER_EXPORT",
    val version: Int = 1,

    val exportedAt: Long,

    val startDate: String,
    val endDate: String,

    val orderCount: Int,
    val itemCount: Int,

    val orders: List<PosOrderExportOrder>
)


// ============================================================
// ONE ORDER
// ============================================================

data class PosOrderExportOrder(

    val master: PosOrderMasterEntity,

    val items: List<PosOrderItemEntity>
)


// ============================================================
// ORDERS ONLY EXPORT
// ============================================================

data class PosOrdersOnlyExport(
    val format: String = "POS_ORDERS_ONLY_EXPORT",
    val version: Int = 1,

    val exportedAt: Long,

    val startDate: String,
    val endDate: String,

    val orderCount: Int,

    val orders: List<PosOrderMasterEntity>
)


// ============================================================
// ITEMS ONLY EXPORT
// ============================================================

data class PosOrderItemsOnlyExport(
    val format: String = "POS_ORDER_ITEMS_ONLY_EXPORT",
    val version: Int = 1,

    val exportedAt: Long,

    val startDate: String,
    val endDate: String,

    val orderCount: Int,
    val itemCount: Int,

    /*
     * IMPORTANT:
     *
     * Items are selected from the master orders
     * inside the selected date range.
     *
     * We do NOT filter items by their own date.
     *
     * This guarantees that every exported item
     * belongs to one of the exported master orders.
     */
    val items: List<PosOrderItemEntity>
)


// ============================================================
// JSON EXPORTER
// ============================================================

object PosOrderJsonExporter {


    // ========================================================
    // 1. JOINT EXPORT
    //
    // Master + Items
    // ========================================================

    suspend fun exportOrders(
        context: Context,
        db: AppDatabase,
        startTime: Long,
        endTime: Long
    ): Result<String> {

        return try {

            // ------------------------------------------------
            // GET MASTER ORDERS
            // ------------------------------------------------

            val orders =
                db.orderMasterDao()
                    .getOrdersBetween(
                        start = startTime,
                        end = endTime
                    )
                    .first()


            // ------------------------------------------------
            // GET ITEMS FOR EACH MASTER
            // ------------------------------------------------

            val exportOrders =
                mutableListOf<PosOrderExportOrder>()

            var totalItems = 0


            for (order in orders) {

                val items =
                    db.orderProductDao()
                        .getByOrderIdSync(order.id)

                totalItems += items.size


                exportOrders.add(
                    PosOrderExportOrder(
                        master = order,
                        items = items
                    )
                )
            }


            // ------------------------------------------------
            // DATE FORMAT
            // ------------------------------------------------

            val dateFormatter =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )


            // ------------------------------------------------
            // CREATE EXPORT
            // ------------------------------------------------

            val export =
                PosOrderExport(

                    exportedAt =
                        System.currentTimeMillis(),

                    startDate =
                        dateFormatter.format(
                            Date(startTime)
                        ),

                    endDate =
                        dateFormatter.format(
                            Date(endTime)
                        ),

                    orderCount =
                        orders.size,

                    itemCount =
                        totalItems,

                    orders =
                        exportOrders
                )


            // ------------------------------------------------
            // JSON
            // ------------------------------------------------

            val gson =
                GsonBuilder()
                    .setPrettyPrinting()
                    .create()

            val json =
                gson.toJson(export)


            // ------------------------------------------------
            // SAVE FILE
            // ------------------------------------------------

            val fileName =
                createFileName(
                    prefix = "POS_ORDERS"
                )


            saveJsonToDownloads(
                context = context,
                fileName = fileName,
                json = json
            )


            // ------------------------------------------------
            // SUCCESS
            // ------------------------------------------------

            Result.success(

                "Export completed.\n\n" +
                        "Orders: ${orders.size}\n" +
                        "Items: $totalItems\n\n" +
                        "File:\n$fileName\n\n" +
                        "Location:\nDownloads/POS/"
            )


        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ========================================================
    // 2. ORDERS ONLY
    //
    // Master records only
    // ========================================================

    suspend fun exportOrdersOnly(
        context: Context,
        db: AppDatabase,
        startTime: Long,
        endTime: Long
    ): Result<String> {

        return try {

            // ------------------------------------------------
            // GET MASTER ORDERS
            // ------------------------------------------------

            val orders =
                db.orderMasterDao()
                    .getOrdersBetween(
                        start = startTime,
                        end = endTime
                    )
                    .first()


            // ------------------------------------------------
            // DATE FORMAT
            // ------------------------------------------------

            val dateFormatter =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )


            // ------------------------------------------------
            // CREATE EXPORT OBJECT
            // ------------------------------------------------

            val export =
                PosOrdersOnlyExport(

                    exportedAt =
                        System.currentTimeMillis(),

                    startDate =
                        dateFormatter.format(
                            Date(startTime)
                        ),

                    endDate =
                        dateFormatter.format(
                            Date(endTime)
                        ),

                    orderCount =
                        orders.size,

                    orders =
                        orders
                )


            // ------------------------------------------------
            // JSON
            // ------------------------------------------------

            val gson =
                GsonBuilder()
                    .setPrettyPrinting()
                    .create()

            val json =
                gson.toJson(export)


            // ------------------------------------------------
            // FILE NAME
            // ------------------------------------------------

            val fileName =
                createFileName(
                    prefix = "POS_ORDERS_ONLY"
                )


            // ------------------------------------------------
            // SAVE
            // ------------------------------------------------

            saveJsonToDownloads(
                context = context,
                fileName = fileName,
                json = json
            )


            // ------------------------------------------------
            // SUCCESS
            // ------------------------------------------------

            Result.success(

                "Orders export completed.\n\n" +
                        "Orders: ${orders.size}\n\n" +
                        "File:\n$fileName\n\n" +
                        "Location:\nDownloads/POS/"
            )


        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ========================================================
    // 3. ITEMS ONLY
    //
    // Items belonging to selected master orders
    // ========================================================

    suspend fun exportOrderItemsOnly(
        context: Context,
        db: AppDatabase,
        startTime: Long,
        endTime: Long
    ): Result<String> {

        return try {

            // ------------------------------------------------
            // STEP 1
            //
            // First find MASTER ORDERS in selected date range.
            // ------------------------------------------------

            val orders =
                db.orderMasterDao()
                    .getOrdersBetween(
                        start = startTime,
                        end = endTime
                    )
                    .first()


            // ------------------------------------------------
            // STEP 2
            //
            // Get items belonging to those master orders.
            //
            // IMPORTANT:
            // We deliberately do NOT search items by item date.
            //
            // The master order determines which items belong
            // to this export.
            // ------------------------------------------------

            val allItems =
                mutableListOf<PosOrderItemEntity>()


            for (order in orders) {

                val items =
                    db.orderProductDao()
                        .getByOrderIdSync(order.id)

                allItems.addAll(items)
            }


            // ------------------------------------------------
            // DATE FORMAT
            // ------------------------------------------------

            val dateFormatter =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                )


            // ------------------------------------------------
            // CREATE EXPORT
            // ------------------------------------------------

            val export =
                PosOrderItemsOnlyExport(

                    exportedAt =
                        System.currentTimeMillis(),

                    startDate =
                        dateFormatter.format(
                            Date(startTime)
                        ),

                    endDate =
                        dateFormatter.format(
                            Date(endTime)
                        ),

                    orderCount =
                        orders.size,

                    itemCount =
                        allItems.size,

                    items =
                        allItems
                )


            // ------------------------------------------------
            // JSON
            // ------------------------------------------------

            val gson =
                GsonBuilder()
                    .setPrettyPrinting()
                    .create()

            val json =
                gson.toJson(export)


            // ------------------------------------------------
            // FILE NAME
            // ------------------------------------------------

            val fileName =
                createFileName(
                    prefix = "POS_ORDER_ITEMS"
                )


            // ------------------------------------------------
            // SAVE
            // ------------------------------------------------

            saveJsonToDownloads(
                context = context,
                fileName = fileName,
                json = json
            )


            // ------------------------------------------------
            // SUCCESS
            // ------------------------------------------------

            Result.success(

                "Order items export completed.\n\n" +
                        "Master Orders: ${orders.size}\n" +
                        "Items: ${allItems.size}\n\n" +
                        "File:\n$fileName\n\n" +
                        "Location:\nDownloads/POS/"
            )


        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // ========================================================
    // CREATE FILE NAME
    // ========================================================

    private fun createFileName(
        prefix: String
    ): String {

        val fileDate =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(
                Date()
            )

        return "${prefix}_$fileDate.json"
    }


    // ========================================================
    // SAVE JSON TO DOWNLOADS/POS
    // ========================================================

    private fun saveJsonToDownloads(
        context: Context,
        fileName: String,
        json: String
    ) {

        // ----------------------------------------------------
        // Android 10+
        // ----------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val resolver =
                context.contentResolver


            val values =
                ContentValues().apply {

                    put(
                        MediaStore.Downloads.DISPLAY_NAME,
                        fileName
                    )

                    put(
                        MediaStore.Downloads.MIME_TYPE,
                        "application/json"
                    )

                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "Download/POS"
                    )

                    put(
                        MediaStore.Downloads.IS_PENDING,
                        1
                    )
                }


            val uri =
                resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                )
                    ?: throw Exception(
                        "Unable to create JSON file"
                    )


            try {

                resolver
                    .openOutputStream(uri)
                    ?.use { output ->

                        output.write(
                            json.toByteArray(
                                Charsets.UTF_8
                            )
                        )

                        output.flush()
                    }
                    ?: throw Exception(
                        "Unable to open output stream"
                    )


                // ------------------------------------------------
                // Mark file as completed
                // ------------------------------------------------

                val completedValues =
                    ContentValues().apply {

                        put(
                            MediaStore.Downloads.IS_PENDING,
                            0
                        )
                    }


                resolver.update(
                    uri,
                    completedValues,
                    null,
                    null
                )


            } catch (e: Exception) {

                // ------------------------------------------------
                // Delete incomplete file
                // ------------------------------------------------

                resolver.delete(
                    uri,
                    null,
                    null
                )

                throw e
            }


        } else {

            throw Exception(
                "Android 10 or newer is required"
            )
        }
    }
}