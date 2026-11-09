package com.it10x.foodappgstav7_31.ui.kot.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import android.app.DatePickerDialog
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_31.data.pos.entities.KotHistorySummary

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class KotHistoryFilter {
    ALL,
    ACTIVE,
    PAID,
    PARTIAL,
    DELETED
}

enum class KotHistoryDateFilter {
    TODAY,
    YESTERDAY,
    PICK_DATE,
    ALL
}

@Composable
fun KotHistoryScreen(
    viewModel: KotHistoryViewModel,
    navController: NavController
) {

    val history by viewModel.historySummary.collectAsState()
    val filter by viewModel.filter.collectAsState()

    val context = LocalContext.current

    // Default Today
    val selectedDate = remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }


    val filteredHistory = history.filter { item ->


        // -------------------------
        // STATUS FILTER
        // -------------------------
        val statusMatch = when (filter) {

            KotHistoryFilter.ALL ->
                true

            KotHistoryFilter.ACTIVE ->
                item.status == "ACTIVE"

            KotHistoryFilter.PAID ->
                item.status == "PAID"

            KotHistoryFilter.PARTIAL ->
                item.status == "PARTIAL"

            KotHistoryFilter.DELETED ->
                item.status == "DELETED"
        }


        // -------------------------
        // DATE FILTER
        // -------------------------
        val itemCal = Calendar.getInstance().apply {
            timeInMillis = item.createdAt
        }

        val selectedCal = Calendar.getInstance().apply {
            timeInMillis = selectedDate.value
        }


        val dateMatch =
            itemCal.get(Calendar.YEAR) ==
                    selectedCal.get(Calendar.YEAR)
                    &&
                    itemCal.get(Calendar.DAY_OF_YEAR) ==
                    selectedCal.get(Calendar.DAY_OF_YEAR)



        statusMatch && dateMatch

    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {


        Text(
            text = "KOT History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )


        Spacer(
            modifier = Modifier.height(12.dp)
        )



        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),

                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


                KotHistoryFilterButton(
                    title = "ALL",
                    selected = filter == KotHistoryFilter.ALL
                ) {
                    viewModel.setFilter(KotHistoryFilter.ALL)
                }


                KotHistoryFilterButton(
                    title = "ACTIVE",
                    selected = filter == KotHistoryFilter.ACTIVE
                ) {
                    viewModel.setFilter(KotHistoryFilter.ACTIVE)
                }


                KotHistoryFilterButton(
                    title = "PAID",
                    selected = filter == KotHistoryFilter.PAID
                ) {
                    viewModel.setFilter(KotHistoryFilter.PAID)
                }


                KotHistoryFilterButton(
                    title = "PARTIAL",
                    selected = filter == KotHistoryFilter.PARTIAL
                ) {
                    viewModel.setFilter(KotHistoryFilter.PARTIAL)
                }


                KotHistoryFilterButton(
                    title = "DELETED",
                    selected = filter == KotHistoryFilter.DELETED
                ) {
                    viewModel.setFilter(KotHistoryFilter.DELETED)
                }



                Button(
                    onClick = {

                        val calendar = Calendar.getInstance()

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->

                                selectedDate.value =
                                    Calendar.getInstance().apply {

                                        set(
                                            year,
                                            month,
                                            day,
                                            0,
                                            0,
                                            0
                                        )

                                        set(
                                            Calendar.MILLISECOND,
                                            0
                                        )

                                    }.timeInMillis

                            },

                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)

                        ).show()

                    }
                ) {

                    Text(
                        SimpleDateFormat(
                            "dd MMM",
                            Locale.getDefault()
                        ).format(
                            Date(selectedDate.value)
                        )
                    )

                }

            }


            Spacer(
                modifier = Modifier.height(8.dp)
            )


//            Button(
//                onClick = {
//
//                    val calendar = Calendar.getInstance()
//
//                    DatePickerDialog(
//                        context,
//                        { _, year, month, day ->
//
//                            selectedDate.value =
//                                Calendar.getInstance().apply {
//
//                                    set(
//                                        year,
//                                        month,
//                                        day,
//                                        0,
//                                        0,
//                                        0
//                                    )
//
//                                    set(
//                                        Calendar.MILLISECOND,
//                                        0
//                                    )
//
//                                }.timeInMillis
//
//                        },
//
//                        calendar.get(Calendar.YEAR),
//                        calendar.get(Calendar.MONTH),
//                        calendar.get(Calendar.DAY_OF_MONTH)
//
//                    ).show()
//
//                }
//            ) {
//
//                Text(
//                    text = SimpleDateFormat(
//                        "dd MMM yyyy",
//                        Locale.getDefault()
//                    ).format(
//                        Date(selectedDate.value)
//                    )
//                )
//
//            }

        }



        Spacer(
            modifier = Modifier.height(12.dp)
        )



        if(filteredHistory.isEmpty()){


            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){

                Text("No KOT History")

            }


        }else{


            KotHistoryHeader()


            LazyColumn {


                items(
                    items = filteredHistory,
                    key = { it.batchId }
                ){ kot ->


                    KotHistoryRow(
                        summary = kot,
                        onClick = {

                            navController.navigate(
                                "kot_history_detail/${kot.batchId}"
                            )

                        }
                    )


                }


            }


        }


    }

}

@Composable
private fun KotHistoryHeader() {


    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    if (isPhone) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF64748B))
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            HeaderCell("KOT", .12f)
            HeaderCell("Status", .20f)
            HeaderCell("Items", .10f)
            HeaderCell("Qty", .10f)
            HeaderCell("Table", .18f)


        }
    }

    if (!isPhone) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF64748B))
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            HeaderCell("KOT", .12f)
            HeaderCell("Status", .20f)
            HeaderCell("Items", .10f)
            HeaderCell("Qty", .10f)
            HeaderCell("Table", .18f)
            HeaderCell("By", .15f)
            HeaderCell("Type", .15f)
            HeaderCell("Source", .11f)
            HeaderCell("Date", .20f)
        }
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
        color = Color.White
    )
}

@Composable
private fun KotHistoryRow(
    summary: KotHistorySummary,
    onClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isPhone = configuration.screenWidthDp < 600
    val statusColor = when (summary.status) {
        "ACTIVE" -> Color(0xFF2E7D32)
        "PAID" -> Color(0xFF1565C0)
        "PARTIAL" -> Color(0xFFF57C00)
        "DELETED" -> Color.Red
        else -> Color.Gray
    }

    val statusText = when {
        summary.status == "PARTIAL" ->
            "PARTIAL (${summary.deletedCount})"

        summary.status == "PAID" && summary.deletedCount > 0 ->
            "PAID (${summary.deletedCount})"

        summary.status == "ACTIVE" && summary.deletedCount > 0 ->
            "ACTIVE (${summary.deletedCount})"

        else ->
            summary.status
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {


        if (!isPhone) {
            // TABLET: existing single row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.kotNumber,
                    modifier = Modifier.weight(.12f),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = statusText,
                    color = statusColor,
                    modifier = Modifier.weight(.20f)
                )

                Text(
                    text = summary.totalLines.toString(),
                    modifier = Modifier.weight(.10f)
                )

                Text(
                    text = summary.totalQty.toString(),
                    modifier = Modifier.weight(.10f)
                )

                Text(
                    text = summary.tableName,
                    modifier = Modifier.weight(.18f)
                )

                Text(
                    text = summary.createdByName,
                    modifier = Modifier.weight(.15f)
                )

                Text(
                    text = summary.orderType,
                    modifier = Modifier.weight(.15f)
                )

                Text(
                    text = summary.source,
                    modifier = Modifier.weight(.11f)
                )

                Text(
                    text = formatDate(summary.createdAt),
                    modifier = Modifier.weight(.20f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            // PHONE: split into two rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: KOT number, status, lines, quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = summary.kotNumber,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = statusText,
                        color = statusColor,
                        modifier = Modifier.weight(1.5f)
                    )

                    Text(
                        text = "Lines: ${summary.totalLines}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Qty: ${summary.totalQty}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Row 2: Table, creator, order type, source, date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = summary.tableName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = summary.orderType,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text(
                            text = summary.createdByName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = summary.source,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = formatDate(summary.createdAt),
                        modifier = Modifier.weight(1.3f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Divider()
    }


}


@Composable
private fun KotHistoryDateFilterRow(
    selected: KotHistoryDateFilter,
    onSelected: (KotHistoryDateFilter) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        KotHistoryDateFilter.values().forEach { filter ->

            FilterChip(
                selected = selected == filter,
                onClick = {
                    onSelected(filter)
                },
                label = {
                    Text(
                        when (filter) {
                            KotHistoryDateFilter.TODAY -> "Today"
                            KotHistoryDateFilter.YESTERDAY -> "Yesterday"
                            KotHistoryDateFilter.PICK_DATE -> "Pick Date"
                            KotHistoryDateFilter.ALL -> "All"
                        }
                    )
                }
            )

        }

    }

}


@Composable
private fun KotHistoryFilterRow(
    selected: KotHistoryFilter,
    onSelected: (KotHistoryFilter) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        KotHistoryFilter.values().forEach { filter ->

            FilterChip(
                selected = selected == filter,
                onClick = {
                    onSelected(filter)
                },
                label = {
                    Text(filter.name)
                }
            )

        }

    }

}
private fun formatDate(time: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy  hh:mm a",
        Locale.getDefault()
    ).format(Date(time))
}

@Composable
private fun KotHistoryFilterButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(title)
        }
    )

}