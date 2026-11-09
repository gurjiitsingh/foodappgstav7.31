package com.it10x.foodappgstav7_31.ui.reports

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.ui.components.CategoryPickerDialog
import com.it10x.foodappgstav7_31.data.pos.entities.CategoryEntity
import com.it10x.foodappgstav7_31.printer.PrintJob
import com.it10x.foodappgstav7_31.printer.PrinterManager
import com.it10x.foodappgstav7_31.viewmodel.OnlineReportsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProductSalesScreen(
    navController: NavController,
    viewModel: OnlineReportsViewModel,
    productsViewModel: com.it10x.foodappgstav7_31.data.pos.viewmodel.ProductsLocalViewModel
) {

    val context = LocalContext.current

    // ---------------- DATE ----------------
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

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ---------------- CATEGORY ----------------
    val categories by viewModel.categories.collectAsState()
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryName by remember { mutableStateOf("Select Category") }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // ---------------- PRODUCT ----------------
    val products by productsViewModel.products.collectAsState()
    var selectedProductId by remember { mutableStateOf<String?>(null) }
    var selectedProductName by remember { mutableStateOf("Select Product") }

    var searchQuery by remember { mutableStateOf("") }

    // ---------------- RESULT ----------------
    val qty by viewModel.qty.collectAsState()
    val totalSales by viewModel.totalSales.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // ---------------- UI ----------------
    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // ================= TOP BAR =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // BACK
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // CATEGORY
                OutlinedButton(
                    onClick = { showCategoryDialog = true }
                ) {
                    Text(selectedCategoryName, fontWeight = FontWeight.Bold)
                }

                // PRODUCT DROPDOWN
                ProductDropdown(
                    products = products,
                    selectedProductName = selectedProductName,
                    onProductSelected = {
                        Log.d("PRODUCT_TRACE", "Selected product: $it")
                        selectedProductId = it.id
                        selectedProductName = it.name
                    }
                )

                // START DATE
                OutlinedButton(onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val c = Calendar.getInstance()
                            c.set(y, m, d, 0, 0, 0)
                            c.set(Calendar.MILLISECOND, 0)
                            startDate = c.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(dateFormatter.format(Date(startDate)))
                }

                // END DATE
                OutlinedButton(onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            val c = Calendar.getInstance()
                            c.set(y, m, d, 23, 59, 59)
                            c.set(Calendar.MILLISECOND, 999)
                            endDate = c.timeInMillis
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(dateFormatter.format(Date(endDate)))
                }

                // LOAD
                Button(onClick = {

                    Log.d("UI_CLICK", "Button clicked")

                    Log.d("UI_CLICK", "categoryId: $selectedCategoryId")
                    Log.d("UI_CLICK", "productId: $selectedProductId")

                    if (selectedCategoryId == null) {
                        Log.d("UI_CLICK", "Category NULL")
                        return@Button
                    }

                    if (selectedProductId == null) {
                        Log.d("UI_CLICK", "Product NULL")
                        return@Button
                    }

                    viewModel.loadProductReport(
                        productId = selectedProductId!!,
                        productName = selectedProductName,
                        startMillis = startDate,
                        endMillis = endDate
                    )

                }) {
                    Text("Load")
                }
            }

            Spacer(Modifier.height(20.dp))

            // ================= RESULT =================
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
                            "No data found",
                            color = Color.Gray
                        )
                    } else {

                        ProductHeader()

                        ProductRow(
                            product = selectedProductName,
                            qty = qty,
                            sales = totalSales
                        )
                    }
                }
            }





            Spacer(Modifier.height(20.dp))

            if (!loading && !(qty == 0 && totalSales == 0.0)) {

                val printer = remember { PrinterManager.getInstance(context) }

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
                                PrintJob.ProductSummary(
                                    product = selectedProductName,
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


            // ================= CATEGORY DIALOG =================
            if (showCategoryDialog) {
                CategoryPickerDialog(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = {
                        selectedCategoryId = it.id
                        selectedCategoryName = it.name

                        // 🔥 VERY IMPORTANT: filter products
                        productsViewModel.setCategory(it.id)

                        // reset product
                        selectedProductId = null
                        selectedProductName = "Select Product"
                    },
                    onDismiss = { showCategoryDialog = false }
                )
            }
        }
    }
}

@Composable
fun ProductDropdown(
    products: List<com.it10x.foodappgstav7_31.data.pos.entities.ProductEntity>,
    selectedProductName: String,
    onProductSelected: (com.it10x.foodappgstav7_31.data.pos.entities.ProductEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedProductName)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.take(50).forEach { product -> // 🔥 limit for performance
                DropdownMenuItem(
                    text = { Text(product.name) },
                    onClick = {
                        onProductSelected(product)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProductRow(
    product: String,
    qty: Int,
    sales: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(product, modifier = Modifier.weight(0.5f))
        Text(qty.toString(), modifier = Modifier.weight(0.2f))
        Text("₹${"%.2f".format(sales)}", modifier = Modifier.weight(0.3f))
    }
}

@Composable
fun ProductHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFEFEF))
            .padding(8.dp)
    ) {
        HeaderCell("Product", 0.5f)
        HeaderCell("Qty", 0.2f)
        HeaderCell("Sales", 0.3f)
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(
        text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}