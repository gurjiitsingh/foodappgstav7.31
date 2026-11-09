package com.it10x.foodappgstav7_31.ui.sales

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.printer.PrintJob
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    onBack: () -> Unit,
    onHistoryReport: () -> Unit,
    currencyCode: String,
    localeTag: String
) {

    val context = LocalContext.current
    val printer = remember { PrinterManager.getInstance(context) }

    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // ---------------- FIXED DATE ROW ----------------


        if (isPhone) {
            // PHONE: two rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Back + date filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    CompactSalesButton(
                        text = "Today",
                        onClick = {
                            viewModel.setDateRange(
                                viewModel.startOfToday(),
                                viewModel.endOfToday()
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CompactSalesButton(
                        text = "This Month",
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.set(Calendar.DAY_OF_MONTH, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)

                            viewModel.setDateRange(
                                calendar.timeInMillis,
                                viewModel.endOfToday()
                            )
                        },
                        modifier = Modifier.weight(1.3f)
                    )

                    CompactSalesButton(
                        text = "Custom",
                        onClick = {
                            val calendar = Calendar.getInstance()

                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val fromCal = Calendar.getInstance()
                                    fromCal.set(y, m, d, 0, 0, 0)
                                    fromCal.set(Calendar.MILLISECOND, 0)

                                    DatePickerDialog(
                                        context,
                                        { _, y2, m2, d2 ->
                                            val toCal = Calendar.getInstance()
                                            toCal.set(y2, m2, d2, 23, 59, 59)
                                            toCal.set(Calendar.MILLISECOND, 999)

                                            viewModel.setDateRange(
                                                fromCal.timeInMillis,
                                                toCal.timeInMillis
                                            )
                                        },
                                        y,
                                        m,
                                        d
                                    ).show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: History Report
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    CompactSalesButton(
                        text = "History Report",
                        onClick = onHistoryReport
                    )
                }
            }
        } else {
            // TABLET: keep the existing single-row layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                CompactSalesButton(
                    text = "Today",
                    onClick = {
                        viewModel.setDateRange(
                            viewModel.startOfToday(),
                            viewModel.endOfToday()
                        )
                    }
                )

                CompactSalesButton(
                    text = "This Month",
                    onClick = {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        viewModel.setDateRange(
                            calendar.timeInMillis,
                            viewModel.endOfToday()
                        )
                    }
                )

                CompactSalesButton(
                    text = "Custom",
                    onClick = {
                        val calendar = Calendar.getInstance()

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val fromCal = Calendar.getInstance()
                                fromCal.set(y, m, d, 0, 0, 0)
                                fromCal.set(Calendar.MILLISECOND, 0)

                                DatePickerDialog(
                                    context,
                                    { _, y2, m2, d2 ->
                                        val toCal = Calendar.getInstance()
                                        toCal.set(y2, m2, d2, 23, 59, 59)
                                        toCal.set(Calendar.MILLISECOND, 999)

                                        viewModel.setDateRange(
                                            fromCal.timeInMillis,
                                            toCal.timeInMillis
                                        )
                                    },
                                    y,
                                    m,
                                    d
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                CompactSalesButton(
                    text = "History Report",
                    onClick = onHistoryReport
                )
            }
        }

        Divider()


        Divider()

        // ---------------- SCROLLABLE CONTENT ----------------

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {

            if (uiState.isLoading) {

                item {

                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

            } else {

                // ---------------- SUMMARY ----------------

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {

                        Column(
                            Modifier.padding(12.dp)
                        ) {

                            Text(
                                "Summary",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(8.dp))

                            SummaryRow(
                                label = "Total Before Discount",
                                value = uiState.totalBeforeDiscount,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )

                            SummaryRow(
                                label = "Total Discount",
                                value = uiState.discountTotal,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )

                            SummaryRow(
                                label = "Total Sales (All Orders)",
                                value = uiState.totalSales,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )

                            SummaryRow(
                                label = "Tax",
                                value = uiState.taxTotal,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )

                            Spacer(Modifier.height(8.dp))

                            SummaryRow(
                                label = "Received Amount",
                                value = uiState.receivedTotal,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )

                            SummaryRow(
                                label = "Credit Pending",
                                value = uiState.creditTotal,
                                currencyCode = currencyCode,
                                localeTag = localeTag
                            )

                            Spacer(Modifier.height(8.dp))

                            Divider()

                            Spacer(Modifier.height(8.dp))

                            uiState.paymentBreakup.forEach { (type, amount) ->

                                SummaryRow(
                                    label = type,
                                    value = amount,
                                    currencyCode = currencyCode,
                                    localeTag = localeTag
                                )
                            }
                        }
                    }
                }

                // ---------------- GROUPED SALES ----------------

                item {

                    Column {

                        Divider()

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Grouped Sales",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(Modifier.height(6.dp))

                        SummaryRow(
                            label = "Food (Except Beverages & Wine)",
                            value = uiState.foodTotal,
                            currencyCode = currencyCode,
                            localeTag = localeTag
                        )

                        SummaryRow(
                            label = "Beverages",
                            value = uiState.beveragesTotal,
                            currencyCode = currencyCode,
                            localeTag = localeTag
                        )

                        SummaryRow(
                            label = "Wine",
                            value = uiState.wineTotal,
                            currencyCode = currencyCode,
                            localeTag = localeTag
                        )
                    }
                }

                // ---------------- PRINT BUTTON ----------------

                item {

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {

                        Button(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .fillMaxWidth(),
                            onClick = {
                                printer.print(
                                    PrintJob.SalesReport(uiState)
                                )
                            }
                        ) {
                            Text("Print Report")
                        }
                    }
                }

                // ---------------- CATEGORY BREAKDOWN ----------------

                item {

                    Column {

                        Divider()

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Category Breakdown",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(Modifier.height(6.dp))
                    }
                }

                item {

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {

                        Button(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .fillMaxWidth(),
                            onClick = {

                                printer.print(
                                    PrintJob.CategoryWiseSalesReport(
                                        categorySales = uiState.categorySales,
                                        fromMillis = uiState.from,
                                        toMillis = uiState.to
                                    )
                                )
                            }
                        ) {
                            Text("Print All Category Sales")
                        }
                    }
                }

                items(uiState.categorySales.toList()) { (category, data) ->

                    val qty = data.first
                    val amount = data.second

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // LEFT

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(category)

                                Text("Qty: $qty")

                                Text(
                                    MoneyFormatter.format(
                                        amount = amount,
                                        currencyCode = currencyCode,
                                        localeTag = localeTag
                                    )
                                )
                            }

                            // RIGHT

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {

                                Button(
                                    onClick = {

                                        val items =
                                            uiState.itemSales[category]
                                                ?: emptyMap()

                                        printer.print(
                                            PrintJob.SingleCategoryDetail(
                                                category = category,
                                                items = items,
                                                fromMillis = uiState.from,
                                                toMillis = uiState.to
                                            )
                                        )
                                    }
                                ) {
                                    Text("Print Detail")
                                }

                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: Double,
    currencyCode: String,
    localeTag: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(label)

        Text(
            MoneyFormatter.format(
                amount = value,
                currencyCode = currencyCode,
                localeTag = localeTag
            )
        )
    }
}


@Composable
private fun CompactSalesButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        contentPadding = PaddingValues(
            horizontal = 8.dp,
            vertical = 4.dp
        )
    ) {
        Text(
            text = text,
            maxLines = 1,
            softWrap = false
        )
    }
}