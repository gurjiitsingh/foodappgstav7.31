package com.it10x.foodappgstav7_31.ui.orders.local

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import java.text.SimpleDateFormat
import java.util.*
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter

@Composable
fun LocalOrderDetailScreen(
    viewModel: LocalOrderDetailViewModel,
    currencyCode: String,
    localeTag: String,
    onBack: () -> Unit
) {
    val order by viewModel.orderInfo.collectAsState()
    val products by viewModel.products.collectAsState()

    val subtotal by viewModel.subtotal.collectAsState()
    val tax by viewModel.taxTotal.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    val discount by viewModel.discount.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    val totalPaid by viewModel.totalPaid.collectAsState()
    val due by viewModel.dueAmount.collectAsState()
    val status by viewModel.paymentStatus.collectAsState()

    val deliveryFee by viewModel.deliveryFee.collectAsState()
    val deliveryTax by viewModel.deliveryTax.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ================= HEADER =================
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Order Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ================= ORDER INFO =================
        order?.let { o ->
            item {
                OrderInfoCard(o)
            }
        }

        // ================= ITEMS TITLE =================
        item {
            Text("Items", style = MaterialTheme.typography.titleMedium)
            Divider()
        }

        // ================= PRODUCTS =================
        items(products, key = { it.id }) { item ->
            OrderProductRow(
                item = item,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
            Divider(color = Color(0xFFE0E0E0))
        }

        // ================= TOTALS =================
        item {
            OrderTotals(
                subtotal = subtotal,
                tax = tax,
                deliveryFee = deliveryFee,
                deliveryTax = deliveryTax,
                discount = discount,
                grandTotal = grandTotal,
                totalPaid = totalPaid,
                due = due,
                status = status,
                currencyCode = currencyCode,
                localeTag = localeTag,
                onEditClick = { showEditDialog = true }
            )
        }
    }


    // ================= EDIT GRAND TOTAL DIALOG =================
    if (showEditDialog && order != null) {
        EditGrandTotalDialog(
            currentTotal = order!!.grandTotal,
            currencyCode = currencyCode,
            localeTag = localeTag,
            onDismiss = { showEditDialog = false },
            onConfirm = { newTotal ->
                showEditDialog = false
                viewModel.updateGrandTotal(newTotal)
            }
        )
    }
}

@Composable
fun OrderProductRow(item: PosOrderItemEntity, currencyCode: String, localeTag: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
//                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                if (item.status == "DELETED") {
                    Text(
                        text = "DELETED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        fontWeight = FontWeight.Bold
                    )

                    if (item.note.isNotBlank()) {
                        Text(
                            text = "Reason: ${item.note}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))
                Spacer(Modifier.height(2.dp))

                val finalPriceAndModifier = item.finalPricePerItem;
               // Text("${item.quantity} × ₹${"%.2f".format(item.basePrice)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text(
                    MoneyFormatter.format(
                        amount = item.basePrice,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                if (item.createdByName.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "Created by: ${item.createdByName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1976D2)
                    )
                }

                Spacer(Modifier.height(2.dp))
                val modifiers = ModifierJsonHelper.fromJson(item.modifiersJson)

                modifiers.forEach { group ->
                    group.items.forEach { mod ->
                        Text(
                            text = "  + ${mod.name} (+${
                                MoneyFormatter.format(
                                    amount = mod.price,
                                    currencyCode = currencyCode,
                                    localeTag = localeTag
                                )
                            })",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF616161)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text("GST ${item.taxRate}% (${item.taxType})", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    "${item.quantity} × ${
                        MoneyFormatter.format(
                            amount = finalPriceAndModifier,
                            currencyCode = currencyCode,
                            localeTag = localeTag
                        )
                    }"
                )
                if (item.isVariant && !item.parentId.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text("Variant item", style = MaterialTheme.typography.labelSmall, color = Color(0xFF616161))
                }


            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    MoneyFormatter.format(
                        amount = item.finalTotal,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${MoneyFormatter.format(
                        amount = item.finalPricePerItem,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )} / item * ${item.quantity}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}



@Composable
fun EditGrandTotalDialog(
    currentTotal: Double,
    currencyCode: String,
    localeTag: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var totalText by remember { mutableStateOf(currentTotal.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit Grand Total", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = totalText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) totalText = it },
                    singleLine = true,
                    label = {   Text(
                        "New Total ($currencyCode)"
                    ) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val newTotal = totalText.toDoubleOrNull() ?: currentTotal
                        onConfirm(newTotal)
                    }) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat {
    return remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
}


@Composable
fun OrderTotals(
    subtotal: Double,
    tax: Double,
    deliveryFee: Double,
    deliveryTax: Double,
    discount: Double,
    grandTotal: Double,
    totalPaid: Double,        // <-- new
    due: Double,              // <-- new
    status: String,           // <-- new
    currencyCode: String,
    localeTag: String,
    onEditClick: () -> Unit = {}
) {

    Column {

        TotalRow(
            label = "Subtotal",
            value = subtotal,
            currencyCode = currencyCode,
            localeTag = localeTag
        )

        TotalRow(
            label = "Tax",
            value = tax,
            currencyCode = currencyCode,
            localeTag = localeTag
        )

        // ✅ Delivery Fee
        if (deliveryFee > 0) {

            TotalRow(
                label = "Delivery Fee",
                value = deliveryFee,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }

        // ✅ Delivery Tax
        if (deliveryTax > 0) {

            TotalRow(
                label = "Delivery Tax",
                value = deliveryTax,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }

        // ✅ Discount
        if (discount > 0) {

            TotalRow(
                label = "Discount",
                value = -discount,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        }

        // ✅ Payment Status
        Text(
            "Payment Status: $status",
            fontWeight = FontWeight.Medium,
            color = when (status) {

                "PAID" -> Color(0xFF2E7D32)

                "PARTIAL" -> Color(0xFFFFA000)

                "CREDIT" -> Color(0xFFD32F2F)

                else -> Color.DarkGray
            },
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Divider(
            Modifier.padding(vertical = 4.dp)
        )

        // ✅ Grand Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Grand Total",
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = MoneyFormatter.format(
                        amount = grandTotal,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    ),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(22.dp)
                ) {

                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Total",
                        tint = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}


@Composable
fun TotalRow(
    label: String,
    value: Double,
    currencyCode: String,
    localeTag: String,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(
            MoneyFormatter.format(
                amount = value,
                currencyCode = currencyCode,
                localeTag = localeTag
            ),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}


@Composable
fun OrderInfoCard(o: PosOrderMasterEntity) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ORDER INFO
            Column {
                Text(
                    rememberDateFormatter().format(Date(o.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Order #${o.srno} • ${o.orderType} ${o.tableNo ?: ""}",
                    color = Color.White
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Payment: ${o.paymentMode}",
                    color = Color.White
                )

                if (o.saleType == "COMPLIMENTARY") {
                    Text(
                        "Order Type: COMPLIMENTARY",
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "Status: ${o.orderStatus}",
                    color = Color.White
                )
                if (o.orderStatus == "CANCELLED" && !o.reason.isNullOrBlank()) {
                    Text(
                        "Cancel Reason: ${o.reason}",
                        color = Color.White
                    )
                }

                Text(
                    "Sync Status: ${o.syncStatus}",
                    color = Color.White
                )
            }

            // DELIVERY INFO (only if delivery)
            if (o.orderType == "DELIVERY") {

                Divider(color = Color.Gray)

                Column {
                    Text("Delivery Address", fontWeight = FontWeight.Bold, color = Color.White)

                    Text(o.customerName ?: "Walk-in", color = Color.White)

                    o.customerPhone?.let {
                        Text(it, color = Color.LightGray)
                    }

                    listOfNotNull(
                        o.dAddressLine1,
                        o.dAddressLine2,
                        o.dCity,
                        o.dState,
                        o.dZipcode
                    ).forEach {
                        Text(it, color = Color.LightGray)
                    }
                }
            }
        }
    }
}
