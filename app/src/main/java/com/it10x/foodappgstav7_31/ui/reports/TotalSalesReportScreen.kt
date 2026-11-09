package com.it10x.foodappgstav7_31.ui.reports

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_31.printer.PrintJob
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import com.it10x.foodappgstav7_31.viewmodel.OnlineReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TotalSalesReportScreen(
    viewModel: OnlineReportsViewModel,
    onBack: () -> Unit,
    onHistoryCategoryReport: () -> Unit,
    onHistoryProductReport: () -> Unit,
    onHistoryCategoryProductReport: () -> Unit,
    currencyCode: String,
    localeTag: String
) {

    val context = LocalContext.current

    val totalSales by viewModel.totalSales.collectAsState()
    val totalDiscount by viewModel.totalDiscount.collectAsState()
    val totalBefore by viewModel.totalBeforeDiscount.collectAsState()
    val totalTax by viewModel.totalTax.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // ✅ DEFAULT TODAY RANGE
    val startCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCalendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    var startDate by remember { mutableStateOf(startCalendar.timeInMillis) }
    var endDate by remember { mutableStateOf(endCalendar.timeInMillis) }

    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    val printer = remember {
        PrinterManager.getInstance(context)
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Spacer(Modifier.height(12.dp))

        // 🔥 DATE ROW
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            // START DATE
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val selected = Calendar.getInstance()
                            selected.set(y, m, d, 0, 0, 0)
                            selected.set(Calendar.MILLISECOND, 0)

                            startDate = selected.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Text(dateFormatter.format(Date(startDate)))
            }

            // END DATE
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val selected = Calendar.getInstance()
                            selected.set(y, m, d, 23, 59, 59)
                            selected.set(Calendar.MILLISECOND, 999)

                            endDate = selected.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Text(dateFormatter.format(Date(endDate)))
            }

            // LOAD BUTTON
            Button(
                onClick = {
                    viewModel.loadTotalSalesReport(startDate, endDate)
                }
            ) {
                Text("Load")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    onHistoryCategoryReport()
                }
            ) {
                Text("Categorys Report")
            }

            Button(
                onClick = {
                    onHistoryProductReport()
                }
            ) {
                Text("Products Report")
            }

            Button(
                onClick = {
                    onHistoryCategoryProductReport()
                }
            ) {
                Text("Category Products Report")
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Total Sales Report",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(20.dp))

        // 🔥 RESULT
        when {
            loading -> {
                Text("Loading...")
            }

            else -> {

                SummaryRow(
                    label = "Before Discount",
                    value = totalBefore,
                    currencyCode = currencyCode,
                    localeTag = localeTag
                )

                SummaryRow(
                    label = "Discount",
                    value = totalDiscount,
                    currencyCode = currencyCode,
                    localeTag = localeTag
                )

                SummaryRow(
                    label = "After Discount",
                    value = totalSales,
                    currencyCode = currencyCode,
                    localeTag = localeTag
                )

                SummaryRow(
                    label = "Tax",
                    value = totalTax,
                    currencyCode = currencyCode,
                    localeTag = localeTag
                )
            }
        }

        if (!loading) {

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    printer.print(
                        PrintJob.TotalSalesReport(
                            beforeDiscount = totalBefore,
                            discount = totalDiscount,
                            afterDiscount = totalSales,
                            tax = totalTax,
                            fromMillis = startDate,
                            toMillis = endDate
                        )
                    )
                }
            ) {
                Text("Print")
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