package com.it10x.foodappgstav7_31.ui.orders.local

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_31.data.pos.viewmodel.POSOrdersViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.platform.LocalContext

import com.it10x.foodappgstav7_31.utils.share.ReceiptPdfGenerator
import com.it10x.foodappgstav7_31.utils.share.ShareUtils
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import com.it10x.foodappgstav7_31.data.pos.entities.config.OutletEntity
import com.it10x.foodappgstav7_31.printer.billimage.ReceiptImageGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalOrdersScreen(
    viewModel: POSOrdersViewModel,
    navController: NavController,
    outlet: OutletEntity?
){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val orders by viewModel.orders.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    var selectedOrder by remember {
        mutableStateOf<PosOrderMasterEntity?>(null)
    }
    var selectedMimeType by remember {
        mutableStateOf<String?>(null)
    }

    var selectedUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val currencyCode =
        outlet?.currencyCode ?: "INR"

    val localeTag =
        outlet?.localeTag ?: "en-IN"
    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    LaunchedEffect(Unit) {
        viewModel.loadFirstPage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        if (isPhone) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {



                // 🔹 Push next button to extreme right
                Spacer(modifier = Modifier.weight(1f))

                // 🔹 History Orders Button (same navigation as drawer)
                Button(
                    onClick = {
                        navController.navigate("history_orders")
                    }
                ) {
                    Text("History Orders")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {

                OutlinedButton(
                    onClick = { showDatePicker = true }
                ) {
                    Text(
                        selectedDate?.let { dateFormatter.format(Date(it)) }
                            ?: "Select Date"
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    enabled = selectedDate != null,
                    onClick = {
                        selectedDate?.let {
                            viewModel.searchOrdersByDate(it)
                        }
                    }
                ) {
                    Text("Search")
                }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        selectedDate = null
                        viewModel.loadFirstPage()
                    }
                ) {
                    Text("Reset")
                }


            }
        }
        if (!isPhone) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedButton(
                    onClick = { showDatePicker = true }
                ) {
                    Text(
                        selectedDate?.let { dateFormatter.format(Date(it)) }
                            ?: "Select Date"
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    enabled = selectedDate != null,
                    onClick = {
                        selectedDate?.let {
                            viewModel.searchOrdersByDate(it)
                        }
                    }
                ) {
                    Text("Search")
                }

                Spacer(Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        selectedDate = null
                        viewModel.loadFirstPage()
                    }
                ) {
                    Text("Reset")
                }

                // 🔹 Push next button to extreme right
                Spacer(modifier = Modifier.weight(1f))

                // 🔹 History Orders Button (same navigation as drawer)
                Button(
                    onClick = {
                        navController.navigate("history_orders")
                    }
                ) {
                    Text("History Orders")
                }
            }
        }

        when {
            loading && orders.isEmpty() ->
                Text("Loading orders...")

            orders.isEmpty() ->
                Text("No local orders found")

            else -> {
                LocalPosOrderTableHeader()

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(orders, key = { it.id }) { order ->
                        LocalPosOrderTableRow(
                            order = order,
                            currencyCode = currencyCode,
                            localeTag = localeTag,
                            onOrderClick = {
                                navController.navigate("local_order_detail/${order.id}")
                            },
                            onPrintBill = {
                                viewModel.printOrder(order.id, role = "bill")
                            },
                            onPrintKitchen = {
                                viewModel.printOrder(order.id, role = "bill")
                            },
                            onShareWhatsApp = {
                                selectedOrder = order
                            }
                        )
                    }


                }


               // ✅ PAGINATION FOOTER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(
                        onClick = { viewModel.loadPrevPage() },
                        enabled = !loading
                    ) {
                        Text("← Previous")
                    }

                    Text(
                        text = "Page ${viewModel.pageIndex.collectAsState().value + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Button(
                        onClick = { viewModel.loadNextPage() },
                        enabled = !loading
                    ) {
                        Text("Next →")
                    }
                }

            }
        }
    }


    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    selectedOrder?.let { order ->

        AlertDialog(
            onDismissRequest = {
                selectedOrder = null
            },

            title = {
                Text("Share Receipt")
            },

            text = {
                Text("Choose receipt format")
            },

            confirmButton = {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = {

                            coroutineScope.launch {

                                val orderItems =
                                    viewModel.getOrderItems(order.id)

                                val imageUri =
                                    ReceiptImageGenerator.generateReceiptImage(
                                        context = context,
                                        order = order,
                                        items = orderItems,
                                        outlet = outlet
                                    )

                                selectedUri = imageUri
                                selectedMimeType = "image/png"

                                selectedOrder = null
                            }
                        }
                    ) {
                        Text("Image")
                    }

                    Button(
                        onClick = {

                            coroutineScope.launch {

                                val orderItems =
                                    viewModel.getOrderItems(order.id)

                                val pdfUri =
                                    ReceiptPdfGenerator.generatePdf(
                                        context = context,
                                        order = order,
                                        items = orderItems,
                                        outlet = outlet
                                    )

                                selectedUri = pdfUri
                                selectedMimeType = "application/pdf"

                                selectedOrder = null
                            }
                        }
                    ) {
                        Text("PDF")
                    }
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        selectedOrder = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }


    selectedUri?.let { uri ->

        AlertDialog(
            onDismissRequest = {
                selectedUri = null
            },

            title = {
                Text("Share Via")
            },

            text = {
                Text("Choose app")
            },

            confirmButton = {

                Column {

                    Button(
                        onClick = {

                            ShareUtils.shareToWhatsApp(
                                context = context,
                                uri = uri,
                                mimeType = selectedMimeType!!
                            )

                            selectedUri = null
                        }
                    ) {
                        Text("WhatsApp")
                    }

                    Spacer(Modifier.height(8.dp))



                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {

                            ShareUtils.shareFile(
                                context = context,
                                uri = uri,
                                mimeType = selectedMimeType!!
                            )

                            selectedUri = null
                        }
                    ) {
                        Text("Other Apps")
                    }
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        selectedUri = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }


}


@Composable
fun LocalPosOrderTableHeader() {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    if (isPhone) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF64748B))
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            HeaderCell("Order#", 0.18f)
            HeaderCell("Order Type", 0.14f)
            HeaderCell("Table / Ref No", 0.17f)
            HeaderCell("Finalized By", 0.17f)
          //  HeaderCell("Amount", 0.14f)

        }
    }

    if (!isPhone) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF64748B))
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            HeaderCell("Order#", 0.12f)
            HeaderCell("Order Type", 0.14f)
            HeaderCell("Table / Ref No", 0.18f)
            HeaderCell("Finalized By", 0.18f)
            HeaderCell("Amount", 0.14f)
            HeaderCell("Time", 0.12f)
            HeaderCell("Actions", 0.12f)
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelSmall
    )
}



@Composable
fun LocalPosOrderTableRow(
    order: PosOrderMasterEntity,
    currencyCode: String,
    localeTag: String,
    onOrderClick: () -> Unit,
    onPrintBill: () -> Unit,
    onPrintKitchen: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    if (isPhone) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOrderClick() }
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {

            // =====================================================
            // ORDER NUMBER
            // =====================================================

            Text(
                text = if (order.saleType == "COMPLIMENTARY") {
                    "#${order.srno} COMP"
                } else {
                    "#${order.srno}"
                },
                modifier = Modifier.weight(0.20f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )


            // =====================================================
            // ORDER TYPE
            // =====================================================

            val displayOrderType = when (order.orderType.lowercase()) {

                "dine_in",
                "dine-in",
                "dinein" -> "DINE IN"

                "takeaway" -> "TAKEAWAY"

                "delivery" -> "DELIVERY"

                else -> order.orderType
            }

            Text(
                text = displayOrderType,
                modifier = Modifier.weight(0.16f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )


            // =====================================================
            // TABLE / REFERENCE NUMBER
            // =====================================================

            val tableOrRef = when (order.orderType.lowercase()) {

                "dine_in",
                "dine-in",
                "dinein" -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }

                "takeaway" -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }

                "delivery" -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }

                else -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }
            }

            Text(
                text = tableOrRef,
                modifier = Modifier.weight(0.17f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )


            // =====================================================
            // FINALIZED BY
            // =====================================================

            Text(
                text = order.finalizedByName
                    ?.takeIf { it.isNotBlank() }
                    ?.let { " $it" }
                    ?: "-",
                modifier = Modifier.weight(0.17f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )





        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOrderClick() }
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {


            // =====================================================
            // AMOUNT
            // =====================================================

            Text(
                text = MoneyFormatter.format(
                    amount = order.grandTotal,
                    currencyCode = currencyCode,
                    localeTag = localeTag
                ),
                modifier = Modifier.weight(0.14f),
                fontWeight = FontWeight.Medium
            )

            // =====================================================
            // TIME
            // =====================================================

            Text(
                text = formatLocalTime(order.createdAt),
                modifier = Modifier.weight(0.12f),
                style = MaterialTheme.typography.bodySmall
            )


            // =====================================================
            // ACTIONS
            // =====================================================

            Row(
                modifier = Modifier.weight(0.12f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                IconButton(
                    onClick = onPrintBill
                ) {
                    Icon(
                        imageVector = Icons.Filled.Print,
                        contentDescription = "Print Bill",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onShareWhatsApp
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Share WhatsApp",
                        tint = Color(0xFF25D366)
                    )
                }
            }
        }

        Divider()
    }

    if (!isPhone) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOrderClick() }
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {

            // =====================================================
            // ORDER NUMBER
            // =====================================================

            Text(
                text = if (order.saleType == "COMPLIMENTARY") {
                    "#${order.srno} COMP"
                } else {
                    "#${order.srno}"
                },
                modifier = Modifier.weight(0.12f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )


            // =====================================================
            // ORDER TYPE
            // =====================================================

            val displayOrderType = when (order.orderType.lowercase()) {

                "dine_in",
                "dine-in",
                "dinein" -> "DINE IN"

                "takeaway" -> "TAKEAWAY"

                "delivery" -> "DELIVERY"

                else -> order.orderType
            }

            Text(
                text = displayOrderType,
                modifier = Modifier.weight(0.14f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )


            // =====================================================
            // TABLE / REFERENCE NUMBER
            // =====================================================

            val tableOrRef = when (order.orderType.lowercase()) {

                "dine_in",
                "dine-in",
                "dinein" -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }

                "takeaway" -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }

                "delivery" -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }

                else -> {
                    order.tableNo
                        ?.takeIf { it.isNotBlank() }
                        ?: "-"
                }
            }

            Text(
                text = tableOrRef,
                modifier = Modifier.weight(0.18f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )


            // =====================================================
            // FINALIZED BY
            // =====================================================

            Text(
                text = order.finalizedByName
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "By $it" }
                    ?: "-",
                modifier = Modifier.weight(0.18f),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )


            // =====================================================
            // AMOUNT
            // =====================================================

            Text(
                text = MoneyFormatter.format(
                    amount = order.grandTotal,
                    currencyCode = currencyCode,
                    localeTag = localeTag
                ),
                modifier = Modifier.weight(0.14f),
                fontWeight = FontWeight.Medium
            )


            // =====================================================
            // TIME
            // =====================================================

            Text(
                text = formatLocalTime(order.createdAt),
                modifier = Modifier.weight(0.12f),
                style = MaterialTheme.typography.bodySmall
            )


            // =====================================================
            // ACTIONS
            // =====================================================

            Row(
                modifier = Modifier.weight(0.12f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                IconButton(
                    onClick = onPrintBill
                ) {
                    Icon(
                        imageVector = Icons.Filled.Print,
                        contentDescription = "Print Bill",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onShareWhatsApp
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Share WhatsApp",
                        tint = Color(0xFF25D366)
                    )
                }
            }
        }

        Divider()
    }
}



private fun formatLocalTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}


private fun getComplimentaryReason(order: PosOrderMasterEntity): String? {

    if (order.saleType != "COMPLIMENTARY") {
        return null
    }

    return order.notes
        ?.removePrefix("FREE ORDER - ")
        ?.split("|")
        ?.firstOrNull()
        ?.trim()
}