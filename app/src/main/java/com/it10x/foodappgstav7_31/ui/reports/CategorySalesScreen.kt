package com.it10x.foodappgstav7_31.ui.reports

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.printer.PrintJob
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.ui.components.CategoryPickerDialog
import com.it10x.foodappgstav7_31.utils.formatter.MoneyFormatter
import com.it10x.foodappgstav7_31.viewmodel.OnlineReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CategorySalesScreen(
    navController: NavController,
    viewModel: OnlineReportsViewModel,
    currencyCode: String,
    localeTag: String
) {

    val context = LocalContext.current

    val printer = remember {
        PrinterManager.getInstance(context)
    }

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

    var startDate by remember {
        mutableStateOf(startCalendar.timeInMillis)
    }

    var endDate by remember {
        mutableStateOf(endCalendar.timeInMillis)
    }

    val dateFormatter = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    var showCategoryDialog by remember {
        mutableStateOf(false)
    }

    var selectedCategoryId by remember {
        mutableStateOf<String?>(null)
    }

    var selectedCategoryName by remember {
        mutableStateOf("Select Category")
    }

    val categories by viewModel.categories.collectAsState()

    val qty by viewModel.qty.collectAsState()

    val totalSales by viewModel.totalSales.collectAsState()

    val loading by viewModel.loading.collectAsState()

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // CATEGORY
                Box {

                    OutlinedButton(
                        modifier = Modifier.wrapContentWidth(),
                        contentPadding = PaddingValues(
                            horizontal = 15.dp,
                            vertical = 2.dp
                        ),
                        onClick = {
                            showCategoryDialog = true
                        }
                    ) {
                        Text(
                            selectedCategoryName,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // START DATE
                OutlinedButton(
                    modifier = Modifier.wrapContentWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 15.dp,
                        vertical = 2.dp
                    ),
                    onClick = {

                        val calendar = Calendar.getInstance()

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->

                                val cal = Calendar.getInstance()

                                cal.set(y, m, d, 0, 0, 0)
                                cal.set(Calendar.MILLISECOND, 0)

                                startDate = cal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(
                        dateFormatter.format(Date(startDate)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // END DATE
                OutlinedButton(
                    modifier = Modifier.wrapContentWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 15.dp,
                        vertical = 2.dp
                    ),
                    onClick = {

                        val calendar = Calendar.getInstance()

                        DatePickerDialog(
                            context,
                            { _, y, m, d ->

                                val cal = Calendar.getInstance()

                                cal.set(y, m, d, 23, 59, 59)
                                cal.set(Calendar.MILLISECOND, 999)

                                endDate = cal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                ) {
                    Text(
                        dateFormatter.format(Date(endDate)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // LOAD
                Button(
                    modifier = Modifier.wrapContentWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 15.dp,
                        vertical = 2.dp
                    ),
                    onClick = {

                        if (selectedCategoryId == null) return@Button

                        if (startDate > endDate) return@Button

                        viewModel.loadCategoryReport(
                            categoryId = selectedCategoryId!!,
                            startMillis = startDate,
                            endMillis = endDate
                        )
                    }
                ) {
                    Text(
                        "Load",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            when {

                loading -> {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {

                    if (qty == 0 && totalSales == 0.0) {

                        Text(
                            "No data found for selected range",
                            color = Color.Gray,
                            modifier = Modifier.padding(8.dp)
                        )

                    } else {

                        CategoryHeader()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                CategoryRow(
                                    category = selectedCategoryName,
                                    qty = qty,
                                    sales = totalSales,
                                    currencyCode = currencyCode,
                                    localeTag = localeTag
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (!loading && !(qty == 0 && totalSales == 0.0)) {

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {

                    Button(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        onClick = {

                            printer.print(
                                PrintJob.CategorySummary(
                                    category = selectedCategoryName,
                                    qty = qty,
                                    amount = totalSales,
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

            if (showCategoryDialog) {

                CategoryPickerDialog(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,

                    onCategorySelected = { category ->

                        selectedCategoryId = category.id
                        selectedCategoryName = category.name
                    },

                    onDismiss = {
                        showCategoryDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    category: String,
    qty: Int,
    sales: Double,
    currencyCode: String,
    localeTag: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Text(
            category,
            modifier = Modifier.weight(0.50f)
        )

        Text(
            qty.toString(),
            modifier = Modifier.weight(0.20f)
        )

        Text(
            MoneyFormatter.format(
                amount = sales,
                currencyCode = currencyCode,
                localeTag = localeTag
            ),
            modifier = Modifier.weight(0.30f)
        )
    }

    Divider()
}

@Composable
fun CategoryHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(8.dp)
    ) {

        HeaderCell("Category", 0.50f)
        HeaderCell("Qty", 0.20f)
        HeaderCell("Sales", 0.30f)
    }
}

@Composable
private fun RowScope.HeaderCell(
    text: String,
    weight: Float
) {

    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}