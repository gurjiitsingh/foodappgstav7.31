package com.it10x.foodappgstav7_31.data.printer



import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.PrinterRole
import com.it10x.foodappgstav7_31.data.PrinterType
import com.it10x.foodappgstav7_31.data.pos.entities.PrinterEntity

object PrinterRestoreManager {

    fun restoreToPreferences(
        printers: List<PrinterEntity>,
        prefs: PrinterPreferences
    ) {

        printers.forEach { printer ->

            val role = PrinterRole.valueOf(printer.printerType)

            when (printer.connectionType) {

                "LAN" -> {
                    prefs.savePrinterType(role, PrinterType.LAN)
                    prefs.saveLanPrinter(
                        role,
                        printer.ipAddress ?: "",
                        printer.port ?: 9100
                    )
                }

                "BLUETOOTH" -> {
                    prefs.savePrinterType(role, PrinterType.BLUETOOTH)
                    prefs.saveBluetoothPrinter(
                        role,
                        printer.printerName,
                        printer.macAddress ?: ""
                    )
                }
                "USB" -> {
                    // ❌ Do NOT restore USB from DB
                    // USB must be selected manually from device list
                }

//                "USB" -> {
//                    prefs.savePrinterType(role, PrinterType.USB)
//                    prefs.saveUSBPrinter(
//                        role,
//                        device.vendorId,
//                        device.productId
//                    )
//                }
            }
        }
    }
}
