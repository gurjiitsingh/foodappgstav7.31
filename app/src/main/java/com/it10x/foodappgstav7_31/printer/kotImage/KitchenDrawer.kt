package com.it10x.foodappgstav7_31.printer.kotImage

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotItemEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KitchenDrawer(private val canvas: Canvas) {

    var y = 30f

    // ✅ AUTO WIDTH DETECTION (KEY CHANGE)
    private val receiptWidth = canvas.width.toFloat()

    // ✅ AUTO SCALE (32mm / 48mm handled automatically)
    private val scale = receiptWidth / 384f

    private val padding = 12f * scale
    private val lineHeight = 22f * scale

    private val paint = Paint().apply {
        color = Color.BLACK
        textSize = 18f * scale
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val boldPaint = Paint(paint).apply {
        textSize = 20f * scale
        typeface = Typeface.DEFAULT_BOLD
    }

    private val rightPaint = Paint(paint).apply {
        textAlign = Paint.Align.RIGHT
    }
    private val rightPaintBold = Paint(paint).apply {
        textAlign = Paint.Align.RIGHT
        textSize = 20f * scale
        typeface = Typeface.DEFAULT_BOLD
    }

    private val boxPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1f * scale
    }

    // =========================
    // TOP BOX
    // =========================
    fun drawTopBox(orderType: String? = null) {

        // ✅ Format order type (DINE_IN → DINE IN)
        val formattedOrderType = orderType
            ?.replace("_", " ")
            ?.uppercase()
            ?.trim()

        val boxHeight = 40f * scale

        // 🔲 Full width box
        canvas.drawRect(
            padding,
            y,
            canvas.width.toFloat() - padding,
            y + boxHeight,
            boxPaint
        )

        // ✅ Center Text Paint
        val centerPaint = Paint(boldPaint).apply {
            textAlign = Paint.Align.CENTER
            textSize = 30f * scale
            isFakeBoldText = true
        }

        // ✅ Draw Text (FIXED)
        if (!formattedOrderType.isNullOrBlank()) {
            canvas.drawText(
                formattedOrderType,
                canvas.width / 2f,
                y + (boxHeight / 2f) + (10f * scale),
                centerPaint
            )
        }

        y += boxHeight + (2f * scale)
    }

    // =========================
    // HEADER
    // =========================
    fun drawHeader(
        kotNumber: String,
        orderType: String,
        tableNo: String,
        tableName: String,
        items: List<PosKotItemEntity>
    ){

        val labelBoxWidth = 60f * scale   // K.No. label box
        val valueBoxWidth = 95f * scale   // KOT number box
        val rowHeight = 40f * scale

        val startX = padding
        val endX = canvas.width.toFloat() - padding

        // 🔲 K.NO LABEL BOX
        canvas.drawRect(
            startX,
            y,
            startX + labelBoxWidth,
            y + rowHeight,
            boxPaint
        )

        canvas.drawText(
            "K.No.",
            startX + 10f,
            y + 32f * scale,
            boldPaint
        )

        // 🔲 KOT NUMBER BOX (separate)
        canvas.drawRect(
            startX + labelBoxWidth,
            y,
            startX + labelBoxWidth + valueBoxWidth,
            y + rowHeight,
            boxPaint
        )

        canvas.drawText(
            kotNumber,
            startX + labelBoxWidth + 10f,
            y + 32f * scale,
            boldPaint
        )

        // 🔲 DATE BOX (remaining space)
        canvas.drawRect(
            startX + labelBoxWidth + valueBoxWidth,
            y,
            endX,
            y + rowHeight,
            boxPaint
        )

        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

        canvas.drawText(
            "Date :",
            startX + labelBoxWidth + valueBoxWidth + 10f,
            y + 32f * scale,
            boldPaint
        )
        canvas.drawText(
            "$time",
            startX + labelBoxWidth + valueBoxWidth + 80f,
            y + 32f * scale,
            paint
        )

        y += rowHeight + (2f * scale)

        // 🔲 TABLE NO BOX (FULL WIDTH)
        val boxHeight = 50f * scale

        canvas.drawRect(
            padding,
            y,
            canvas.width.toFloat() - padding,
            y + boxHeight,
            boxPaint
        )



        val cleanTableNo = tableNo.replace("_", " ")

        val referenceLabel = if (orderType == "DINE_IN") {
            "Table No."
        } else {
            "Ref No."
        }

        canvas.drawText(
            "$referenceLabel : $tableName",
            padding + 10f,
            y + 32f * scale,
            boldPaint
        )

        y += boxHeight + (2f * scale)


// 🔲 STEWARD BOX (FULL WIDTH)
        canvas.drawRect(
            padding,
            y,
            canvas.width.toFloat() - padding,
            y + boxHeight,
            boxPaint
        )

        val stewardName = items.firstOrNull()?.createdByName ?: ""

        canvas.drawText(
            "Steward : ${if (stewardName.isNotBlank()) stewardName else "NONAME"}",
            padding + 10f,
            y + 32f * scale,
            boldPaint
        )

        y += boxHeight + (10f * scale)



        y += lineHeight
    }

    // =========================
    // ITEMS
    // =========================
    fun drawItems1(items: List<PosKotItemEntity>) {



        // 🔲 TABLE HEADER
        val leftMargin = padding + 20f
        val rightMargin = padding + 20f

        canvas.drawText("Item Name", leftMargin, y, boldPaint)
        canvas.drawText(
            "Qty",
            canvas.width - rightMargin,
            y,
            rightPaintBold
        )

        y += 20f * scale

        val compactLineHeight = 26f * scale

        val nameX = padding

        if (items.isEmpty()) {
            canvas.drawText("No items", nameX, y, paint)
            y += compactLineHeight
            return
        }

        val rowHeight = 40f * scale
        val dividerX = canvas.width - (100f * scale)





        val lineHeight = 22f * scale
        val textPadding = 10f * scale
        val maxNameWidth = dividerX - padding - 20f * scale

        val modifierPaint = Paint(paint).apply {
            textSize = 20f * scale   // smaller than item
        }

        items.forEach { item ->

            val name = item.name?.trim().takeUnless { it.isNullOrEmpty() } ?: "Item"

            val qty = item.quantity.toString().toDoubleOrNull() ?: 0.0
            val qtyText = if (qty % 1.0 == 0.0) {
                qty.toInt().toString()
            } else {
                String.format(Locale.US, "%.2f", qty)
            }

            // 🔹 SPLIT ITEM NAME
            val words = name.split(" ")
            val lines = mutableListOf<String>()
            var currentLine = ""

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"

                if (boldPaint.measureText(testLine) > maxNameWidth) {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                    }
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine)

            // 🔹 GET MODIFIERS
            val modifiers = if (item.modifiersJson.isNotBlank()) {
                try {
                    item.modifiersJson
                        .removePrefix("[")
                        .removeSuffix("]")
                        .split(",")
                        .map { it.trim().replace("\"", "") }
                        .filter { it.isNotBlank() }
                } catch (e: Exception) {
                    listOf(item.modifiersJson)
                }
            } else {
                emptyList()
            }
            Log.d("KOT_DEBUG", "Modifiers JSON: ${item.modifiersJson}")
            // 🔹 CALCULATE HEIGHT
            // Count note lines
            val noteLinesCount = if (item.note.isNotBlank()) {
                val words = ("NOTE: ${item.note}").split(" ")
                val tempLines = mutableListOf<String>()
                var current = ""

                for (word in words) {
                    val test = if (current.isEmpty()) word else "$current $word"
                    if (modifierPaint.measureText(test) > maxNameWidth) {
                        if (current.isNotEmpty()) tempLines.add(current)
                        current = word
                    } else {
                        current = test
                    }
                }
                if (current.isNotEmpty()) tempLines.add(current)
                tempLines.size
            } else 0

            val rowHeight = ((lines.size + modifiers.size + noteLinesCount) * lineHeight) + 12f * scale

            // 🔲 BOX
            canvas.drawRect(
                padding,
                y,
                canvas.width.toFloat() - padding,
                y + rowHeight,
                boxPaint
            )

            // 🔲 DIVIDER
            canvas.drawLine(
                dividerX,
                y,
                dividerX,
                y + rowHeight,
                boxPaint
            )

            // 🔹 DRAW ITEM NAME
            var textY = y + lineHeight

            lines.forEachIndexed { index, line ->
                canvas.drawText(
                    line,
                    padding + textPadding,
                    textY,
                    boldPaint
                )

                // QTY ONLY FIRST LINE
                if (index == 0) {
                    canvas.drawText(
                        qtyText,
                        canvas.width - padding - textPadding,
                        textY,
                        rightPaintBold
                    )
                }

                textY += lineHeight
            }

            // 🔹 DRAW MODIFIERS (INDENTED)
            modifiers.forEach { mod ->

                val modText = when (mod) {
                    is String -> mod
                    else -> mod.toString() // or mod.name
                }

                canvas.drawText(
                    "• $modText",
                    padding + 25f * scale,   // 👈 indent
                    textY,
                    modifierPaint
                )

                textY += lineHeight
            }
// 🔹 DRAW NOTE (INDENTED)
            if (item.note.isNotBlank()) {

                val noteText = "NOTE: ${item.note}"

                // Wrap note text (same logic as name)
                val noteWords = noteText.split(" ")
                val noteLines = mutableListOf<String>()
                var currentLine = ""

                for (word in noteWords) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"

                    if (modifierPaint.measureText(testLine) > maxNameWidth) {
                        if (currentLine.isNotEmpty()) {
                            noteLines.add(currentLine)
                        }
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) noteLines.add(currentLine)

                // Draw note lines
                noteLines.forEach { line ->
                    canvas.drawText(
                        line,
                        padding + 25f * scale,   // 👈 same indent as modifiers
                        textY,
                        modifierPaint
                    )
                    textY += lineHeight
                }
            }
            y += rowHeight
        }

        y += 6f * scale
    }
    fun drawItems(items: List<PosKotItemEntity>) {

        // 🔲 TABLE HEADER
        val leftMargin = padding + 20f
        val rightMargin = padding + 20f

        canvas.drawText("Item Name", leftMargin, y, boldPaint)

        canvas.drawText(
            "Qty",
            canvas.width - rightMargin,
            y,
            rightPaintBold
        )

        y += 20f * scale

        val compactLineHeight = 26f * scale

        if (items.isEmpty()) {
            canvas.drawText("No items", padding, y, paint)
            y += compactLineHeight
            return
        }

        // =====================================================
        // LAYOUT
        // =====================================================

        val dividerX = canvas.width - (100f * scale)

        val lineHeight = 22f * scale
        val textPadding = 10f * scale

        val maxNameWidth =
            dividerX - padding - (20f * scale)

        // Modifier is smaller than item name
        val modifierPaint = Paint(paint).apply {
            textSize = 20f * scale
            typeface = Typeface.DEFAULT
        }

        items.forEach { item ->

            // =================================================
            // ITEM NAME
            // =================================================

            val name =
                item.name
                    ?.trim()
                    .takeUnless { it.isNullOrEmpty() }
                    ?: "Item"

            // =================================================
            // QUANTITY
            // =================================================

            val qty =
                item.quantity
                    .toString()
                    .toDoubleOrNull()
                    ?: 0.0

            val qtyText =
                if (qty % 1.0 == 0.0) {
                    qty.toInt().toString()
                } else {
                    String.format(Locale.US, "%.2f", qty)
                }

            // =================================================
            // WRAP ITEM NAME
            // =================================================

            val words = name.split(" ")

            val lines = mutableListOf<String>()

            var currentLine = ""

            for (word in words) {

                val testLine =
                    if (currentLine.isEmpty()) {
                        word
                    } else {
                        "$currentLine $word"
                    }

                if (boldPaint.measureText(testLine) > maxNameWidth) {

                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                    }

                    currentLine = word

                } else {
                    currentLine = testLine
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }

            // =================================================
            // 🔥 GET MODIFIERS
            // USE SAME PARSER AS TEXT KOT
            // =================================================

            val modifierLines = mutableListOf<String>()

            if (item.modifiersJson.isNotBlank()) {

                try {

                    val modifierGroups =
                        ModifierJsonHelper.fromJson(
                            item.modifiersJson
                        )

                    modifierGroups.forEach { group ->

                        // -------------------------------------
                        // Modifier group
                        // -------------------------------------

//                        if (group.groupName.isNotBlank()) {
//
//                            modifierLines.add(
//                                "   ${group.groupName}:"
//                            )
//                        }

                        // -------------------------------------
                        // Modifier items
                        // -------------------------------------

                        group.items.forEach { modifierItem ->

                            if (modifierItem.name.isNotBlank()) {

                                modifierLines.add("+ ${modifierItem.name}"
                                )
                            }
                        }
                    }

                } catch (e: Exception) {

                    Log.e(
                        "KOT_DEBUG",
                        "Failed to parse modifiersJson: ${item.modifiersJson}",
                        e
                    )

                    // Do NOT print JSON.
                    // If parsing fails, simply don't print
                    // the raw JSON on the KOT.
                }
            }

            Log.d(
                "KOT_DEBUG",
                "Item=${item.name}, modifierLines=$modifierLines"
            )

            // =================================================
            // NOTE LINES
            // =================================================

            val noteLines = mutableListOf<String>()

            if (item.note.isNotBlank()) {

                val noteText =
                    "      NOTE: ${item.note}"

                val noteWords =
                    noteText.split(" ")

                var currentNoteLine = ""

                for (word in noteWords) {

                    val testLine =
                        if (currentNoteLine.isEmpty()) {
                            word
                        } else {
                            "$currentNoteLine $word"
                        }

                    if (
                        modifierPaint.measureText(testLine)
                        > maxNameWidth
                    ) {

                        if (currentNoteLine.isNotEmpty()) {
                            noteLines.add(currentNoteLine)
                        }

                        currentNoteLine = word

                    } else {
                        currentNoteLine = testLine
                    }
                }

                if (currentNoteLine.isNotEmpty()) {
                    noteLines.add(currentNoteLine)
                }
            }

            // =================================================
            // TOTAL ROW HEIGHT
            // =================================================

            val totalLines =
                lines.size +
                        modifierLines.size +
                        noteLines.size

            val rowHeight =
                (totalLines * lineHeight) +
                        (12f * scale)

            // =================================================
            // 🔲 ITEM BOX
            // =================================================

            canvas.drawRect(
                padding,
                y,
                canvas.width.toFloat() - padding,
                y + rowHeight,
                boxPaint
            )

            // =================================================
            // 🔲 QTY DIVIDER
            // =================================================

            canvas.drawLine(
                dividerX,
                y,
                dividerX,
                y + rowHeight,
                boxPaint
            )

            // =================================================
            // DRAW ITEM
            // =================================================

            var textY =
                y + lineHeight

            lines.forEachIndexed { index, line ->

                canvas.drawText(
                    line,
                    padding + textPadding,
                    textY,
                    boldPaint
                )

                // ---------------------------------------------
                // Qty only on first item-name line
                // ---------------------------------------------

                if (index == 0) {

                    canvas.drawText(
                        qtyText,
                        canvas.width - padding - textPadding,
                        textY,
                        rightPaintBold
                    )
                }

                textY += lineHeight
            }

            // =================================================
            // 🔥 DRAW MODIFIERS
            // =================================================

            modifierLines.forEach { modifierLine ->

                canvas.drawText(
                    modifierLine,
                    padding + 10f * scale,
                    textY,
                    modifierPaint
                )

                textY += lineHeight
            }

            // =================================================
            // 🔹 DRAW NOTE
            // =================================================

            noteLines.forEach { noteLine ->

                canvas.drawText(
                    noteLine,
                    padding + 10f * scale,
                    textY,
                    modifierPaint
                )

                textY += lineHeight
            }

            // =================================================
            // NEXT ITEM
            // =================================================

            y += rowHeight
        }

        y += 6f * scale
    }
    // =========================
    // FOOTER
    // =========================
    fun drawFooter() {

        y += 20f * scale

        val centerX = receiptWidth / 2

        val centerPaint = Paint(paint).apply {
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("---- END ----", centerX, y, centerPaint)

        y += 1f * scale
    }
}


fun parseModifiers(modifiersJson: String): List<String> {
    return try {
        val jsonArray = JSONArray(modifiersJson)
        val list = mutableListOf<String>()

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.get(i)

            when (item) {
                is String -> list.add(item)
                is JSONObject -> list.add(item.optString("name"))
            }
        }

        list
    } catch (e: Exception) {
        emptyList()
    }
}

//class KitchenDrawer(
//    private val canvas: Canvas,
//    private val receiptWidth: Float = 384f   // ✅ 384 = 48mm, use 256f for 32mm
//)
//{
//
//    var y = 30f
//
//    // ✅ Dynamic scaling (VERY IMPORTANT)
//    private val scale = receiptWidth / 384f
//
//    private val padding = 12f * scale
//    private val lineHeight = 22f * scale
//
//    private val paint = Paint().apply {
//        color = Color.BLACK
//        textSize = 18f * scale
//        isAntiAlias = true
//        textAlign = Paint.Align.LEFT
//    }
//
//    private val boldPaint = Paint(paint).apply {
//        textSize = 20f * scale
//        typeface = Typeface.DEFAULT_BOLD
//    }
//
//    private val rightPaint = Paint(paint).apply {
//        textAlign = Paint.Align.RIGHT
//    }
//
//    private val boxPaint = Paint().apply {
//        color = Color.BLACK
//        style = Paint.Style.STROKE
//        strokeWidth = 1f * scale
//    }
//
//    // =========================
//    // HEADER
//    // =========================
//    fun drawHeader(kotNumber: String, orderType: String) {
//
//        val boxHeight = 60f * scale
//
//        canvas.drawRect(
//            padding,
//            y,
//            receiptWidth - padding,
//            y + boxHeight,
//            boxPaint
//        )
//
//        boldPaint.textSize = 22f * scale
//        paint.textSize = 18f * scale
//
//        canvas.drawText("KOT:", padding + 8f, y + (25f * scale), boldPaint)
//        canvas.drawText(kotNumber, padding + (80f * scale), y + (25f * scale), paint)
//
//        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
//        canvas.drawText("Date: $time", padding + 8f, y + (45f * scale), paint)
//
//        y += boxHeight + (10f * scale)
//
//        boldPaint.textSize = 20f * scale
//
//        canvas.drawText("Table: $orderType", padding, y, boldPaint)
//        y += lineHeight
//
//        canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
//        y += 10f * scale
//
//        canvas.drawText("Item", padding, y, boldPaint)
//        canvas.drawText("Qty", receiptWidth - padding, y, rightPaint)
//
//        y += 10f * scale
//        canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
//        y += lineHeight
//    }
//
//    // =========================
//    // ITEMS
//    // =========================
//    fun drawItems(items: List<PosKotItemEntity>) {
//
//        val compactLineHeight = 26f * scale
//
//        val nameX = padding
//        val qtyX = receiptWidth - padding
//        val maxNameWidth = receiptWidth - (80f * scale)
//
//        if (items.isEmpty()) {
//            canvas.drawText("No items", nameX, y, paint)
//            y += compactLineHeight
//            return
//        }
//
//        items.forEach { item ->
//
//            val name = item.name?.trim().takeUnless { it.isNullOrEmpty() } ?: "Item"
//            val qty = item.quantity ?: 0.0
//            val note = item.note
//
//            val words = name.split(" ")
//            var line = ""
//
//            // 🔹 TEXT WRAP
//            for (word in words) {
//
//                val testLine = if (line.isEmpty()) word else "$line $word"
//
//                if (paint.measureText(testLine) > maxNameWidth) {
//
//                    if (line.isNotEmpty()) {
//                        canvas.drawText(line, nameX, y, paint)
//                        y += compactLineHeight
//                    }
//
//                    line = word
//                } else {
//                    line = testLine
//                }
//            }
//
//            // 🔹 LAST LINE + QTY
//            if (line.isNotEmpty()) {
//
//                canvas.drawText(line, nameX, y, paint)
//
//                val qtyText = try {
//                    String.format(Locale.US, "%.0f", qty)
//                } catch (e: Exception) {
//                    "0"
//                }
//
//                canvas.drawText(qtyText, qtyX, y, rightPaint)
//
//                y += compactLineHeight
//            }
//
//            // 🔹 NOTE
//            if (!note.isNullOrEmpty()) {
//                val notePaint = Paint(paint).apply {
//                    textSize = 16f * scale
//                }
//
//                canvas.drawText("• $note", nameX + (12f * scale), y, notePaint)
//                y += compactLineHeight
//            }
//
//            // 🔹 DIVIDER
//            canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
//            y += 6f * scale
//        }
//
//        y += 6f * scale
//    }
//
//    // =========================
//    // FOOTER
//    // =========================
//    fun drawFooter() {
//
//        y += 30f * scale
//
//        val centerX = receiptWidth / 2
//
//        val centerPaint = Paint(paint).apply {
//            textAlign = Paint.Align.CENTER
//        }
//
//        canvas.drawText("---- END ----", centerX, y, centerPaint)
//
//        y += 40f * scale
//    }
//}