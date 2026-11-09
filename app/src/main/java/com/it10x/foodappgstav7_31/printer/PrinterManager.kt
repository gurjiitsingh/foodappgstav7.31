package com.it10x.foodappgstav7_31.printer
import com.it10x.foodappgstav7_31.printer.common.PrintDocument
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.it10x.foodappgstav7_31.data.PrinterConfig
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.PrinterType
import com.it10x.foodappgstav7_31.data.print.OutletInfo
import com.it10x.foodappgstav7_31.printer.bluetooth.BluetoothPrinter
import com.it10x.foodappgstav7_31.printer.lan.LanPrinter
import com.it10x.foodappgstav7_31.printer.usb.USBPrinter
import com.it10x.foodappgstav7_31.data.print.OutletMapper
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_31.ui.sales.SalesUiState
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.it10x.foodappgstav7_31.printer.PrintJob
import kotlinx.coroutines.runBlocking
import com.it10x.foodappgstav7_31.data.printqueue.PrintQueueDao
import com.it10x.foodappgstav7_31.printer.common.PaymentQr
import com.it10x.foodappgstav7_31.printer.kotImage.KitchenBitmapGenerator
import com.it10x.foodappgstav7_31.printer.queue.PrintQueueManager
import com.it10x.foodappgstav7_31.printer.utils.QrUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PrinterManager private constructor(
    private val context: Context
) {

    companion object {
        @Volatile
        private var INSTANCE: PrinterManager? = null

        fun getInstance(context: Context): PrinterManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrinterManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }




    private val scope = CoroutineScope(Dispatchers.IO)

    private val queueManager: PrintQueueManager by lazy {

        val db = AppDatabaseProvider.get(context)

        PrintQueueManager.getInstance(
            dao = db.printQueueDao(),
            printerManager = this
        )
    }

    private val prefs by lazy { PrinterPreferences(context) }
    fun appContext(): Context = context.applicationContext

//IMAGE PRINT QUE
//fun enqueueImagePrint(  // THIS IS CHANGED TO SUSPEND


    suspend fun enqueueImagePrint(
        referenceId: String,
        earnedPoints: Int,
        role: PrinterRole,
        bitmap: Bitmap,
        paymentMode: String? = null,
        grandTotal: Double? = null
    ) {
        try {

            // --------------------------------------------------
            // PRINT QUEUE DIRECTORY
            // --------------------------------------------------

            val printDir = File(
                context.filesDir,
                "print_queue"
            )

            if (!printDir.exists()) {
                printDir.mkdirs()
            }

            // --------------------------------------------------
            // SAVE IMAGE
            // --------------------------------------------------

            val imageFile = File(
                printDir,
                "${role.name.lowercase()}_${System.currentTimeMillis()}.png"
            )

            imageFile.outputStream().use { stream ->

                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    stream
                )
            }

            Log.d(
                "IMAGE_PRINT",
                "Image saved: ${imageFile.absolutePath}"
            )

            // --------------------------------------------------
            // COMMON PRINT QUEUE
            // --------------------------------------------------

            queueManager.enqueueImage(
                referenceId = referenceId,
                earnedPoints = earnedPoints,
                role = role,
                imagePath = imageFile.absolutePath,
                paymentMode = paymentMode,
                grandTotal = grandTotal
            )

        } catch (e: Exception) {

            Log.e(
                "IMAGE_PRINT",
                "Failed to save or enqueue image",
                e
            )
        }
    }

    suspend fun enqueueBillImage(
        order: PrintOrder,
        paymentMode: String,
        outletInfo: OutletInfo,
        kotNumberText: String,
        stewardName: String,
        referenceId: String,
        earnedPoints: Int,
    ) {
//        Log.d("IMAGE_ORDER", "----------------------------")
//        Log.d("IMAGE_ORDER", "itemTotal   = ${order.itemTotal}")
//        Log.d("IMAGE_ORDER", "discount    = ${order.discount}")
//        Log.d("IMAGE_ORDER", "itemTax     = ${order.itemTax}")
//        Log.d("IMAGE_ORDER", "deliveryFee = ${order.deliveryFee}")
//        Log.d("IMAGE_ORDER", "deliveryTax = ${order.deliveryTax}")
//        Log.d("IMAGE_ORDER", "taxTotal    = ${order.tax}")
//        Log.d("IMAGE_ORDER", "grandTotal  = ${order.grandTotal}")
//
//        Log.d("IMAGE_ORDER", "taxType     = ${outletInfo.taxType}")
//        Log.d("IMAGE_ORDER", "taxMode     = ${outletInfo.taxMode}")

        // --------------------------------------------------
        // LOAD SAVED LOGO
        // --------------------------------------------------

        val logoBitmap = loadSavedLogo()

        // --------------------------------------------------
        // IMAGE PRINTER SIZE
        //
        // 80mm -> 48 characters -> billing48IMAGE()
        // 58mm -> 32 characters -> billing32IMAGE()
        // --------------------------------------------------

        val size = prefs.getPrinterSize(
            PrinterRole.BILLING
        ) ?: "80mm"

        Log.d(
            "IMAGE_PRINT",
            "Billing image format = $size"
        )

        // --------------------------------------------------
        // CREATE BILL IMAGE
        // --------------------------------------------------

        val bitmap = when (size) {

            // 80mm / 48 characters
            "80mm" -> {
                ReceiptFormatter.billing48IMAGE(
                    context = context,
                    order = order,
                    outletInfo = outletInfo,
                    logo = logoBitmap,
                    kotNumberText = kotNumberText,
                    stewardName = stewardName
                )
            }

            // 58mm / 32 characters
            "58mm" -> {
                ReceiptFormatter.billing32IMAGE(
                    context = context,
                    order = order,
                    outletInfo = outletInfo,
                    logo = logoBitmap,
                    kotNumberText = kotNumberText,
                    stewardName = stewardName
                )
            }

            // Unknown setting -> 80mm
            else -> {
                Log.w(
                    "IMAGE_PRINT",
                    "Unknown printer size=$size, using 80mm"
                )

                ReceiptFormatter.billing48IMAGE(
                    context = context,
                    order = order,
                    outletInfo = outletInfo,
                    logo = logoBitmap,
                    kotNumberText = kotNumberText,
                    stewardName = stewardName
                )
            }
        }

        // --------------------------------------------------
        // COMMON IMAGE PRINT QUEUE
        // --------------------------------------------------

        enqueueImagePrint(
            role = PrinterRole.BILLING,
            bitmap = bitmap,
            referenceId = referenceId,
            earnedPoints = earnedPoints,
            paymentMode = paymentMode,
            grandTotal = order.grandTotal
        )
    }

    //TEXT PRINT QUE
    fun enqueueBill(
        order: PrintOrder,
        paymentMode: String,
        outletInfo: OutletInfo,
        kotNumberText: String,
        stewardName: String,
        referenceId: String,
        earnedPoints: Int = 0,
        ) {
        Log.d(
            "TEXT_PRINT",
            "earnedPoints value = $earnedPoints"
        )
        // --------------------------------------------------
        // TEXT PRINTER SIZE
        //
        // 80mm -> 48 characters -> billing48()
        // 58mm -> 32 characters -> billing32()
        // --------------------------------------------------

        val size = prefs.getPrinterSize(
            PrinterRole.BILLING
        ) ?: "80mm"

        Log.d(
            "TEXT_PRINT",
            "Billing text format = $size"
        )

        // --------------------------------------------------
        // CREATE BILL TEXT
        // --------------------------------------------------

        val receiptText = when (size) {

            // 80mm / 48 characters
            "80mm" -> {
                ReceiptFormatter.billing48(
                    order = order,
                    kotNumberText = kotNumberText,
                    stewardName = stewardName,
                    outletInfo = outletInfo,
                    earnedPoints = earnedPoints,
                )
            }

            // 58mm / 32 characters
            "58mm" -> {
                ReceiptFormatter.billing32(
                    order = order,
//                    kotNumberText = kotNumberText,
//                    stewardName = stewardName,
                    outletInfo = outletInfo
                )
            }

            // Unknown setting -> 80mm
            else -> {
                Log.w(
                    "TEXT_PRINT",
                    "Unknown printer size=$size, using 80mm"
                )

                ReceiptFormatter.billing48(
                    order = order,
                    outletInfo = outletInfo,
                    earnedPoints = earnedPoints,
                )
            }
        }
//        val qrData =
//            PaymentQr.createPaymentQrData(
//                order = order,
//                outletInfo = outletInfo
//            )


        val qrData =
            if (outletInfo.qrEnabled) {
                PaymentQr.createPaymentQrData(
                    order = order,
                    outletInfo = outletInfo
                )
            } else {
                null
            }

        val document = PrintDocument(
            text = receiptText,
            logo = null,
            qrData = qrData
        )
        // --------------------------------------------------
        // COMMON TEXT PRINT QUEUE
        // --------------------------------------------------


        enqueuePrint(
            role = PrinterRole.BILLING,
            text = receiptText,
            document = document,
            referenceId = referenceId,
            earnedPoints = earnedPoints,
            paymentMode = paymentMode,
            grandTotal = order.grandTotal
        )
    }
    fun enqueuePrint(
        role: PrinterRole,
        text: String,
        document: PrintDocument,
        referenceId: String,
        earnedPoints: Int,
        paymentMode: String? = null,
        grandTotal: Double? = null
    ) {
       // Log.e("PRINT_DEBUG", "🔥 enqueuePrint CALLED role=$role")
        Log.d(
            "TEXT_PRINT",
            text
        )


        scope.launch {
            queueManager.enqueue(role, document,  text, paymentMode, grandTotal,referenceId, earnedPoints)
        }
    }





    fun enqueueKitchen(
        sessionKey: String,
        tableName:String,
        orderType: String,
        items: List<PosKotItemEntity>,
        kotNumber: String,
        referenceId: String,
        earnedPoints: Int,
    ) {

        val text = ReceiptFormatter.posKitchen(
            sessionKey = sessionKey,
            tableName = tableName,
            orderType = orderType,
            items = items,
            kotNumber = kotNumber,

            )

        //enqueuePrint(PrinterRole.KITCHEN, text,referenceId)
        enqueuePrint(
            role = PrinterRole.KITCHEN,
            text = text,
            document = PrintDocument(text = text),
            referenceId = referenceId,
            earnedPoints = earnedPoints,
        )
    }


    fun printBitmap(
        role: PrinterRole,
        imagePath: String,
        onResult: (Boolean) -> Unit = {}
    )
    {
        Log.d(
            "IMAGE_TEST",
            "PrinterManager.printBitmap()"
        )
        val config = prefs.getPrinterConfig(role)

        Log.d("IMAGE_TEST", "Config = $config")

        if (config == null) {
            Log.d("IMAGE_TEST", "Config is NULL")
            Log.e("PRINT_BITMAP", "No printer configured for role=$role")
            onResult(false)
            return
        }

        //--------------------------------------
        // Load Bitmap
        //--------------------------------------

        val bitmap = BitmapFactory.decodeFile(imagePath)

        if (bitmap == null) {
            Log.e("IMAGE_TEST", "Unable to decode bitmap: $imagePath")
            onResult(false)
            return
        }

        //--------------------------------------
        // Print
        //--------------------------------------


//        LanPrinter.printBitmap(
//            ip = config.ip,
//            port = config.port,
//            bitmap = bitmap,
//            onResult = onResult
//        )


        when (config.type) {


            PrinterType.LAN -> {

                Log.d(
                    "IMAGE_TEST",
                    "Entering LAN bitmap print"
                )

                if (config.ip.isBlank()) {

                    Log.e(
                        "PRINT_BITMAP",
                        "LAN IP missing"
                    )

                    onResult(false)
                    return
                }


                LanPrinter.printBitmap(
                    ip = config.ip,
                    port = config.port,
                    bitmap = bitmap,
                    onResult = onResult
                )
            }



            PrinterType.BLUETOOTH -> {

                BluetoothPrinter.printBitmap(
                    mac = config.bluetoothAddress,
                    bitmap = bitmap,
                    onResult = onResult
                )
            }


            PrinterType.USB -> {

                val usbManager =
                    context.getSystemService(Context.USB_SERVICE)
                            as android.hardware.usb.UsbManager


                val saved = prefs.getUSBPrinter(role)


                if (saved == null) {
                    Log.e("USB", "No saved USB printer")
                    onResult(false)
                    return
                }


                val (vendorId, productId) = saved


                val device = usbManager.deviceList.values.find {

                    it.vendorId == vendorId &&
                            it.productId == productId

                }


                if (device == null) {

                    Log.e("USB", "Device not found")

                    onResult(false)
                    return
                }


                if (!usbManager.hasPermission(device)) {

                    Log.e("USB", "No permission")

                    onResult(false)
                    return
                }


                try {

                    USBPrinter.printBitmap(
                        context = context,
                        device = device,
                        bitmap = bitmap,
                        onResult = onResult
                    )


                } catch (e: Exception) {

                    Log.e(
                        "USB",
                        "USB Bitmap print failed",
                        e
                    )

                    onResult(false)
                }
            }


            PrinterType.WIFI -> {
                onResult(false)
            }
        }




    }
    // --------------------------------
    // SELECT FORMAT DIRECT PRINT
    // --------------------------------




    fun printTextNew(
        role: PrinterRole,
        order: PrintOrder,
        kotNumberText:String,
        stewardName:String,

        onResult: (Boolean) -> Unit = {}
    )
    {
        //  Log.e("PRINT_NEW", "Printing for role=$role")

        // Get printer configuration and preferences
        val config = prefs.getPrinterConfig(role)


        if (config == null) {
            Log.e("PPRINTTEST", "No printer configured for role=$role")
            onResult(false)
            return
        }

        // ✅ Select format based on page size
        val size = prefs.getPrinterSize(role) ?: "80mm"

        // ✅ Auto-load outlet info if not provided

        val info = getOutletInfoOrNull()
        if (info == null) {
            onResult(false)
            return
        }




        // ✅ Select format based on printer page size
        val receiptText = when (size) {
            "80mm" -> ReceiptFormatter.billing48(order, kotNumberText= kotNumberText,
                stewardName, info, 0 )
            else -> ReceiptFormatter.billing32(order, info)
        }


        Log.e(
            "PRINTTEST",
            "\n================= BILL NEWTEXT =================\n$receiptText\n=================================================="
        )

        // ✅ Printing logic (kept same as before)
        when (config.type) {
            PrinterType.BLUETOOTH -> {
                if (config.bluetoothAddress.isBlank()) {
                    Log.e("PRINT_NEW", "Bluetooth address missing")
                    onResult(false)
                    return
                }
                BluetoothPrinter.printText(
                    config.bluetoothAddress,
                    receiptText,
                    onResult
                )
            }

            PrinterType.LAN -> {
                if (config.ip.isBlank()) {
                    Log.e("PRINT_NEW", "LAN IP missing")
                    onResult(false)
                    return
                }
                LanPrinter.printText(
                    config.ip,
                    config.port,
                    receiptText,
                    onResult
                )
            }

            PrinterType.USB -> {

                val usbManager = context.getSystemService(Context.USB_SERVICE) as android.hardware.usb.UsbManager

                val saved = prefs.getUSBPrinter(role)

                if (saved == null) {
                    Log.e("PRINT_NEW", "No saved USB printer")
                    onResult(false)
                    return
                }

                val (vendorId, productId) = saved

                val device = usbManager.deviceList.values.find {
                    it.vendorId == vendorId && it.productId == productId
                }

                if (device == null) {
                    Log.e("PRINT_NEW", "USB device not found")
                    onResult(false)
                    return
                }

                if (!usbManager.hasPermission(device)) {
                    Log.e("PRINT_NEW", "USB permission denied")
                    onResult(false)
                    return
                }

                // ✅ CORRECT CALL (with device)
                USBPrinter.printText(
                    context,
                    device,
                    receiptText,
                    onResult
                )
            }

            PrinterType.WIFI -> {
                Log.e("PRINT_NEW", "WiFi printing not supported yet")
                onResult(false)
            }
        }
    }


    // --------------------------------
    // REAL PRINT (USED BY BUTTON + AUTO)
    // --------------------------------
    fun printText(
        role: PrinterRole,
        text: String,
        paymentMode: String? = null,
        grandTotal: Double? = null,
        qrData: String? = null,
        onResult: (Boolean) -> Unit = {}
    )
    {

        val config = prefs.getPrinterConfig(role)

        if (config == null) {
            Log.e(
                "PRINTTEST",
                "No printer configured for role=$role"
            )
            onResult(false)
            return
        }

        when (config.type) {

            // =========================================
            // BLUETOOTH
            // =========================================

            PrinterType.BLUETOOTH -> {

                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }

                BluetoothPrinter.printText(
                    mac = config.bluetoothAddress,
                    text = text,
                    qrData = qrData,
                    onResult = onResult
                )
            }


            // =========================================
            // LAN
            // =========================================

            PrinterType.LAN -> {

                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }

                LanPrinter.printText(
                    ip = config.ip,
                    port = config.port,
                    text = text,
                    qrData = qrData,
                    onResult = onResult
                )
            }


            // =========================================
            // USB
            // =========================================

            PrinterType.USB -> {

                val usbManager =
                    context.getSystemService(
                        Context.USB_SERVICE
                    ) as android.hardware.usb.UsbManager

                val saved =
                    prefs.getUSBPrinter(role)

                if (saved == null) {
                    Log.e(
                        "USB",
                        "No saved USB printer"
                    )
                    onResult(false)
                    return
                }

                val (vendorId, productId) = saved

                val device =
                    usbManager.deviceList.values.find {

                        it.vendorId == vendorId &&
                                it.productId == productId
                    }

                if (device == null) {
                    Log.e(
                        "USB",
                        "Device not found"
                    )
                    onResult(false)
                    return
                }

                if (!usbManager.hasPermission(device)) {
                    Log.e(
                        "USB",
                        "No permission"
                    )
                    onResult(false)
                    return
                }

                USBPrinter.printText(
                    context = context,
                    device = device,
                    text = text,
                    qrData = qrData,
                    onResult = onResult
                )
            }


            // =========================================
            // WIFI
            // =========================================

            PrinterType.WIFI -> {
                onResult(false)
            }
        }
    }


    fun printText_without_qr(
        role: PrinterRole,
        text: String,
        paymentMode: String? = null,
        grandTotal: Double? = null,
        onResult: (Boolean) -> Unit = {}
    ) {

        val config = prefs.getPrinterConfig(role)

        if (config == null) {
            Log.e(
                "PRINTTEST",
                "No printer configured for role=$role"
            )
            onResult(false)
            return
        }

        when (config.type) {

            // =========================================
            // BLUETOOTH
            // =========================================

            PrinterType.BLUETOOTH -> {

                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }

                BluetoothPrinter.printText(
                    mac = config.bluetoothAddress,
                    text = text,
                    onResult = onResult
                )
            }


            // =========================================
            // LAN
            // =========================================

            PrinterType.LAN -> {

                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }

                LanPrinter.printText(
                    ip = config.ip,
                    port = config.port,
                    text = text,
                    onResult = onResult
                )
            }


            // =========================================
            // USB
            // =========================================

            PrinterType.USB -> {

                val usbManager =
                    context.getSystemService(
                        Context.USB_SERVICE
                    ) as android.hardware.usb.UsbManager

                val saved =
                    prefs.getUSBPrinter(role)

                if (saved == null) {
                    Log.e(
                        "USB",
                        "No saved USB printer"
                    )
                    onResult(false)
                    return
                }

                val (vendorId, productId) = saved

                val device =
                    usbManager.deviceList.values.find {

                        it.vendorId == vendorId &&
                                it.productId == productId
                    }

                if (device == null) {
                    Log.e(
                        "USB",
                        "Device not found"
                    )
                    onResult(false)
                    return
                }

                if (!usbManager.hasPermission(device)) {
                    Log.e(
                        "USB",
                        "No permission"
                    )
                    onResult(false)
                    return
                }

                USBPrinter.printText(
                    context = context,
                    device = device,
                    text = text,
                    onResult = onResult
                )
            }


            // =========================================
            // WIFI
            // =========================================

            PrinterType.WIFI -> {
                onResult(false)
            }
        }
    }

    // --------------------------------
    // NEW PRINT JOB STRATAGY
    // --------------------------------


    fun print(job: PrintJob, onResult: (Boolean) -> Unit = {}) {
        when (job) {



            is PrintJob.SalesReport -> {

                val size = prefs.getPrinterSize(PrinterRole.BILLING) ?: "80mm"
                val width = if (size == "80mm") 48 else 32


                val info = getOutletInfoOrNull()
                if (info == null) {
                    onResult(false)
                    return
                }

                val text = ReceiptFormatter.salesFullReport(
                    state = job.state,
                    info = info,
                    width = width,
                    printMillis = job.printMillis
                )

                printText(PrinterRole.BILLING, text)
            }


            is PrintJob.CategoryWiseSalesReport -> {

                val size = prefs.getPrinterSize(PrinterRole.BILLING) ?: "80mm"
                val width = if (size == "80mm") 48 else 32

                val info = getOutletInfoOrNull()
                if (info == null) {
                    onResult(false)
                    return
                }

                val text = ReceiptFormatter.categoryWiseSalesReport(
                    categorySales = job.categorySales,
                    info = info,
                    width = width,
                    fromMillis = job.fromMillis,
                    toMillis = job.toMillis,
                    printMillis = job.printMillis
                )

                printText(PrinterRole.BILLING, text)
            }

            is PrintJob.SingleCategoryDetail -> {
                val size = prefs.getPrinterSize(PrinterRole.BILLING) ?: "80mm"
                val width = if (size == "80mm") 48 else 32

                val info = getOutletInfoOrNull()
                if (info == null) {
                    onResult(false)
                    return
                }

                val text = ReceiptFormatter.salesBySingleCategory(
                    category = job.category,
                    items = job.items,
                    outletInfo = info,
                    width = width,
                    fromMillis = job.fromMillis,
                    toMillis = job.toMillis,
                    printMillis = job.printMillis
                )

                printText(PrinterRole.BILLING, text)
            }

            // ✅ NEW: Category Summary


            is PrintJob.TotalSalesReport -> {

                val size = prefs.getPrinterSize(PrinterRole.BILLING) ?: "80mm"
                val width = if (size == "80mm") 48 else 32

                val info = getOutletInfoOrNull()
                if (info == null) {
                    onResult(false)
                    return
                }

                val text = ReceiptFormatter.totalSalesReport(
                    beforeDiscount = job.beforeDiscount,
                    discount = job.discount,
                    afterDiscount = job.afterDiscount,
                    tax = job.tax,
                    info = info,
                    width = width,
                    fromMillis = job.fromMillis,
                    toMillis = job.toMillis,
                    printMillis = job.printMillis
                )

                printText(PrinterRole.BILLING, text)
            }


            is PrintJob.CategorySummary -> {

                val size = prefs.getPrinterSize(PrinterRole.BILLING) ?: "80mm"
                val width = if (size == "80mm") 48 else 32

                val info = getOutletInfoOrNull()
                if (info == null) {
                    onResult(false)
                    return
                }

                val text = ReceiptFormatter.salesCategorySummary(
                    category = job.category,
                    totalQty = job.qty,
                    totalAmount = job.amount,
                    info = info,
                    width = width,
                    fromMillis = job.fromMillis,
                    toMillis = job.toMillis,
                    printMillis = job.printMillis
                )

                printText(PrinterRole.BILLING, text)
            }

            is PrintJob.ProductSummary -> {
                val size = prefs.getPrinterSize(PrinterRole.BILLING) ?: "80mm"
                val width = if (size == "80mm") 48 else 32

                val info = getOutletInfoOrNull()
                if (info == null) {
                    onResult(false)
                    return
                }

                val text = ReceiptFormatter.salesProductSummary(
                    product = job.product,
                    qty = job.qty,
                    amount = job.amount,
                    info = info,
                    width = width,
                    fromMillis = job.fromMillis,
                    toMillis = job.toMillis,
                    printMillis = job.printMillis
                )

                printText(PrinterRole.BILLING, text)
            }

            // ✅ ADD THIS BLOCK
            is PrintJob.CategoryProductReport -> {

                val size = prefs.getPrinterSize(PrinterRole.BILLING) ?: "80mm"
                val width = if (size == "80mm") 48 else 32

                val info = getOutletInfoOrNull()
                if (info == null) {
                    onResult(false)
                    return
                }

                val text = ReceiptFormatter.salesCategoryProductList(
                    category = job.category,
                    items = job.items,
                    outletInfo = info,
                    width = width,
                    fromMillis = job.fromMillis,
                    toMillis = job.toMillis,
                    printMillis = job.printMillis
                )

                printText(PrinterRole.BILLING, text)
            }
        }
    }





    // --------------------------------
    // TEST PRINT (already OK)
    // --------------------------------
    fun printTest(
        config: PrinterConfig,
        onResult: (Boolean) -> Unit
    ) {
        val roleLabel = config.role.name

        when (config.type) {

            PrinterType.BLUETOOTH -> {
                //    Log.d("PRINT_BT", "Test BT address='${config.bluetoothAddress}'")
                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }
                BluetoothPrinter.printTest(
                    config.bluetoothAddress,
                    roleLabel,
                    onResult
                )
            }

            PrinterType.LAN -> {
                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }
                LanPrinter.printTest(
                    config.ip,
                    config.port,
                    roleLabel,
                    onResult
                )
            }



            PrinterType.USB -> {
                val device = config.usbDevice ?: run {
                    onResult(false)
                    return
                }

                USBPrinter.printTest(
                    context = context,
                    device = device,
                    roleLabel = roleLabel,
                    onResult = onResult
                )
            }






            PrinterType.WIFI -> onResult(false)
        }
    }





    fun printTextKitchen(
        role: PrinterRole,
        tableName: String,
        sessionKey: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        kotNumber: String,
        onResult: (Boolean) -> Unit = {}
    ) {

        val config = prefs.getPrinterConfig(role)

        if (config == null) {
            Log.e("PRINTTEST", "No printer configured for role=$role")
            onResult(false)
            return
        }

        val text = ReceiptFormatter.posKitchen(
            sessionKey = sessionKey,
            tableName = tableName,
            orderType = orderType,
            items = items,
            kotNumber = kotNumber
        )

        Log.e(
            "PRINTTEST",
            "\n================= KITCHEN RECEIPT =================\n$text\n=================================================="
        )

        when (config.type) {

            PrinterType.BLUETOOTH -> {

                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }

                BluetoothPrinter.printText(
                    config.bluetoothAddress,
                    text,
                    onResult
                )
            }

            PrinterType.LAN -> {

                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }

                LanPrinter.printText(
                    config.ip,
                    config.port,
                    text,
                    onResult
                )
            }

            PrinterType.USB -> {

                val usbManager =
                    context.getSystemService(Context.USB_SERVICE) as android.hardware.usb.UsbManager

                val saved = prefs.getUSBPrinter(role)

                if (saved == null) {
                    Log.e("PRINT_NEW", "No saved USB printer")
                    onResult(false)
                    return
                }

                val (vendorId, productId) = saved

                val device = usbManager.deviceList.values.find {
                    it.vendorId == vendorId &&
                            it.productId == productId
                }

                if (device == null) {
                    Log.e("PRINT_NEW", "USB device not found")
                    onResult(false)
                    return
                }

                if (!usbManager.hasPermission(device)) {
                    Log.e("PRINT_NEW", "USB permission denied")
                    onResult(false)
                    return
                }

                USBPrinter.printText(
                    context,
                    device,
                    text,
                    onResult
                )
            }

            PrinterType.WIFI -> {
                onResult(false)
            }
        }
    }


    private fun loadSavedLogo(): Bitmap? {

        val logoFile = java.io.File(context.filesDir, "logo.png")

        return if (logoFile.exists()) {
            BitmapFactory.decodeFile(logoFile.absolutePath)
        } else null
    }


    private fun getOutletInfoOrNull(): OutletInfo? {
        val outletDao = AppDatabaseProvider.get(context).outletDao()
        val outletEntity = runBlocking { outletDao.getOutlet() }

        return if (outletEntity == null) {
            Log.e("PRINTER", "Outlet not configured")
            null
        } else {
            OutletMapper.fromEntity(outletEntity)
        }
    }


//IMAGE PRINT OF KOT

  suspend  fun enqueueKitchenImage(
        sessionKey: String,
        tableName: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        referenceId: String,
        earnedPoints: Int,
        kotNumber: String,
    ) {

       // Log.d("IMAGE_TEST", "before formatter---------")

        try {
            val bitmap = KitchenBitmapGenerator.generate(
                context = context,
                sessionKey = sessionKey,
                tableName,
                orderType = orderType,
                items = items,
                kotNumber = kotNumber,
            )

         //   Log.d("IMAGE_TEST", "before enqueue---------")

            enqueueImagePrint(
                role = PrinterRole.KITCHEN,
                bitmap = bitmap,
                referenceId = referenceId,
                earnedPoints = earnedPoints,
                paymentMode = "",
                grandTotal = 0.0
            )

        } catch (e: Exception) {
            Log.e("IMAGE_TEST", "KOT generation failed", e)
        }
    }



    private fun getPrinterConfigOrFail(
        role: PrinterRole,
        onResult: (Boolean) -> Unit
    ): PrinterConfig? {

        val config = prefs.getPrinterConfig(role)

        if (config == null) {
            Log.e(
                "PRINT",
                "No printer configured for role=$role"
            )

            onResult(false)
            return null
        }

        return config
    }



}//END OF CLASS




