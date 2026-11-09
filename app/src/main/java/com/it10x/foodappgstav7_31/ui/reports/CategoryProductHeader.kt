package com.it10x.foodappgstav7_31.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import com.it10x.foodappgstav7_31.ui.reports.model.ProductReportItem

@Composable
fun CategoryProductHeader() {
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
fun CategoryProductRow(item: ProductReportItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(item.name, modifier = Modifier.weight(0.5f))
        Text(item.qty.toString(), modifier = Modifier.weight(0.2f))
        Text("₹${"%.2f".format(item.total)}", modifier = Modifier.weight(0.3f))
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