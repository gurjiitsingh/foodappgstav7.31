package com.it10x.foodappgstav7_31.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController

import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.export.PosOrderJsonExporter

import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDataScreen(
    navController: NavController
) {

    // =========================================================
    // CONTEXT / DATABASE
    // =========================================================

    val context = LocalContext.current

    val db = remember {
        AppDatabaseProvider.get(context)
    }

    val scope = rememberCoroutineScope()


    // =========================================================
    // DATE STATE
    // =========================================================

    var fromDate by remember {
        mutableStateOf<Long?>(null)
    }

    var toDate by remember {
        mutableStateOf<Long?>(null)
    }

    var showFromDatePicker by remember {
        mutableStateOf(false)
    }

    var showToDatePicker by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // EXPORT STATE
    // =========================================================

    var exportingCombined by remember {
        mutableStateOf(false)
    }

    var exportingOrders by remember {
        mutableStateOf(false)
    }

    var exportingItems by remember {
        mutableStateOf(false)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }


    // =========================================================
    // DATE FORMATTER
    // =========================================================

    val dateFormatter = remember {

        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        )
    }


    // =========================================================
    // ANY EXPORT RUNNING
    // =========================================================

    val exporting =
        exportingCombined ||
                exportingOrders ||
                exportingItems


    // =========================================================
    // GET DAY START
    // =========================================================

    fun startOfDay(dateMillis: Long): Long {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = dateMillis

        calendar.set(
            Calendar.HOUR_OF_DAY,
            0
        )

        calendar.set(
            Calendar.MINUTE,
            0
        )

        calendar.set(
            Calendar.SECOND,
            0
        )

        calendar.set(
            Calendar.MILLISECOND,
            0
        )

        return calendar.timeInMillis
    }


    // =========================================================
    // GET DAY END
    // =========================================================

    fun endOfDay(dateMillis: Long): Long {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = dateMillis

        calendar.set(
            Calendar.HOUR_OF_DAY,
            0
        )

        calendar.set(
            Calendar.MINUTE,
            0
        )

        calendar.set(
            Calendar.SECOND,
            0
        )

        calendar.set(
            Calendar.MILLISECOND,
            0
        )

        calendar.add(
            Calendar.DAY_OF_MONTH,
            1
        )

        return calendar.timeInMillis - 1
    }


    // =========================================================
    // VALID DATE RANGE
    // =========================================================

    fun getDateRange(): Pair<Long, Long>? {

        val from = fromDate
            ?: return null

        val to = toDate
            ?: return null

        val startTime =
            startOfDay(from)

        val endTime =
            endOfDay(to)

        if (startTime > endTime) {
            return null
        }

        return Pair(
            startTime,
            endTime
        )
    }


    // =========================================================
    // UI
    // =========================================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        // =====================================================
        // TITLE
        // =====================================================

        Text(
            text = "Export Data",
            style = MaterialTheme.typography.headlineMedium
        )


        // =====================================================
        // DATE RANGE
        // =====================================================

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
        ) {

            Column(

                modifier =
                    Modifier.padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Export Date Range",
                    style =
                        MaterialTheme.typography.titleLarge
                )


                // -------------------------------------------------
                // FROM DATE
                // -------------------------------------------------

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "From:",
                        modifier =
                            Modifier.weight(0.25f),
                        style =
                            MaterialTheme.typography.bodyLarge
                    )

                    OutlinedButton(

                        onClick = {
                            showFromDatePicker = true
                        },

                        modifier =
                            Modifier.weight(0.75f)
                    ) {

                        Text(

                            fromDate?.let {

                                dateFormatter.format(
                                    Date(it)
                                )

                            } ?: "Select From Date"
                        )
                    }
                }


                // -------------------------------------------------
                // TO DATE
                // -------------------------------------------------

                Row(

                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        text = "To:",
                        modifier =
                            Modifier.weight(0.25f),
                        style =
                            MaterialTheme.typography.bodyLarge
                    )

                    OutlinedButton(

                        onClick = {
                            showToDatePicker = true
                        },

                        modifier =
                            Modifier.weight(0.75f)
                    ) {

                        Text(

                            toDate?.let {

                                dateFormatter.format(
                                    Date(it)
                                )

                            } ?: "Select To Date"
                        )
                    }
                }


                // -------------------------------------------------
                // CLEAR
                // -------------------------------------------------

                OutlinedButton(

                    onClick = {

                        fromDate = null
                        toDate = null
                        message = null

                    },

                    enabled = !exporting,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text("Clear Dates")
                }


                // -------------------------------------------------
                // RANGE ERROR
                // -------------------------------------------------

                if (
                    fromDate != null &&
                    toDate != null &&
                    startOfDay(fromDate!!) > endOfDay(toDate!!)
                ) {

                    Text(
                        text =
                            "To Date cannot be before From Date.",
                        color =
                            MaterialTheme.colorScheme.error,
                        style =
                            MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }


        // =====================================================
        // COMBINED EXPORT
        // =====================================================

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
        ) {

            Column(

                modifier =
                    Modifier.padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Combined Export",
                    style =
                        MaterialTheme.typography.titleLarge
                )


                Text(
                    text =
                        "Exports master orders together with their " +
                                "order items in one JSON file.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )


                Button(

                    enabled =
                        getDateRange() != null &&
                                !exporting,

                    onClick = {

                        val range =
                            getDateRange()
                                ?: return@Button

                        val startTime =
                            range.first

                        val endTime =
                            range.second


                        scope.launch {

                            exportingCombined = true
                            message = null


                            try {

                                val result =
                                    PosOrderJsonExporter
                                        .exportOrders(

                                            context = context,

                                            db = db,

                                            startTime =
                                                startTime,

                                            endTime =
                                                endTime
                                        )


                                message =
                                    result.fold(

                                        onSuccess = {
                                            "Combined export successful:\n$it"
                                        },

                                        onFailure = {

                                            "Combined export failed:\n" +
                                                    (
                                                            it.message
                                                                ?: "Unknown error"
                                                            )
                                        }
                                    )

                            } catch (e: Exception) {

                                message =
                                    "Combined export failed:\n" +
                                            (
                                                    e.message
                                                        ?: "Unknown error"
                                                    )

                            } finally {

                                exportingCombined = false
                            }
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(

                        if (exportingCombined)
                            "Exporting..."
                        else
                            "Download Combined JSON"
                    )
                }
            }
        }


        // =====================================================
        // MASTER ORDERS
        // =====================================================

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
        ) {

            Column(

                modifier =
                    Modifier.padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Master Orders",
                    style =
                        MaterialTheme.typography.titleLarge
                )


                Text(
                    text =
                        "Exports only pos_order_master records " +
                                "for the selected date range.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )


                Button(

                    enabled =
                        getDateRange() != null &&
                                !exporting,

                    onClick = {

                        val range =
                            getDateRange()
                                ?: return@Button

                        val startTime =
                            range.first

                        val endTime =
                            range.second


                        scope.launch {

                            exportingOrders = true
                            message = null


                            try {

                                val result =
                                    PosOrderJsonExporter
                                        .exportOrdersOnly(

                                            context = context,

                                            db = db,

                                            startTime =
                                                startTime,

                                            endTime =
                                                endTime
                                        )


                                message =
                                    result.fold(

                                        onSuccess = {

                                            "Master orders exported successfully:\n$it"
                                        },

                                        onFailure = {

                                            "Order export failed:\n" +
                                                    (
                                                            it.message
                                                                ?: "Unknown error"
                                                            )
                                        }
                                    )

                            } catch (e: Exception) {

                                message =
                                    "Order export failed:\n" +
                                            (
                                                    e.message
                                                        ?: "Unknown error"
                                                    )

                            } finally {

                                exportingOrders = false
                            }
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(

                        if (exportingOrders)
                            "Exporting Orders..."
                        else
                            "Download Master Orders JSON"
                    )
                }
            }
        }


        // =====================================================
        // ORDER ITEMS
        // =====================================================

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
        ) {

            Column(

                modifier =
                    Modifier.padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Order Items",
                    style =
                        MaterialTheme.typography.titleLarge
                )


                Text(
                    text =
                        "Exports items belonging to the master orders " +
                                "selected by the date range. Items are " +
                                "linked using orderMasterId.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )


                Button(

                    enabled =
                        getDateRange() != null &&
                                !exporting,

                    onClick = {

                        val range =
                            getDateRange()
                                ?: return@Button

                        val startTime =
                            range.first

                        val endTime =
                            range.second


                        scope.launch {

                            exportingItems = true
                            message = null


                            try {

                                val result =
                                    PosOrderJsonExporter
                                        .exportOrderItemsOnly(

                                            context = context,

                                            db = db,

                                            startTime =
                                                startTime,

                                            endTime =
                                                endTime
                                        )


                                message =
                                    result.fold(

                                        onSuccess = {

                                            "Order items exported successfully:\n$it"
                                        },

                                        onFailure = {

                                            "Order item export failed:\n" +
                                                    (
                                                            it.message
                                                                ?: "Unknown error"
                                                            )
                                        }
                                    )

                            } catch (e: Exception) {

                                message =
                                    "Order item export failed:\n" +
                                            (
                                                    e.message
                                                        ?: "Unknown error"
                                                    )

                            } finally {

                                exportingItems = false
                            }
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(

                        if (exportingItems)
                            "Exporting Items..."
                        else
                            "Download Order Items JSON"
                    )
                }
            }
        }


        // =====================================================
        // RESULT
        // =====================================================

        message?.let { resultMessage ->

            Card(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    text = resultMessage,
                    modifier =
                        Modifier.padding(16.dp),
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }
        }


        // =====================================================
        // OTHER DATA
        // =====================================================

        Card(

            modifier =
                Modifier.fillMaxWidth(),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
        ) {

            Column(

                modifier =
                    Modifier.padding(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = "Other Data",
                    style =
                        MaterialTheme.typography.titleMedium
                )


                Text(
                    text =
                        "Customer, product and complete POS " +
                                "backup exports can be added here later.",
                    style =
                        MaterialTheme.typography.bodyMedium
                )
            }
        }


        // =====================================================
        // BACK
        // =====================================================

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        OutlinedButton(

            onClick = {
                navController.popBackStack()
            },

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text("Back")
        }
    }


    // =========================================================
    // FROM DATE PICKER
    // =========================================================

    if (showFromDatePicker) {

        val datePickerState =
            rememberDatePickerState(

                initialSelectedDateMillis =
                    fromDate
                        ?: System.currentTimeMillis()
            )


        DatePickerDialog(

            onDismissRequest = {
                showFromDatePicker = false
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let {

                                fromDate = it

                                // If To Date is empty,
                                // automatically use From Date.
                                if (toDate == null) {
                                    toDate = it
                                }
                            }

                        showFromDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        showFromDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }


    // =========================================================
    // TO DATE PICKER
    // =========================================================

    if (showToDatePicker) {

        val datePickerState =
            rememberDatePickerState(

                initialSelectedDateMillis =
                    toDate
                        ?: fromDate
                        ?: System.currentTimeMillis()
            )


        DatePickerDialog(

            onDismissRequest = {
                showToDatePicker = false
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let {

                                toDate = it
                            }

                        showToDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        showToDatePicker = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}