package com.it10x.foodappgstav7_31.ui.pos

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class PosSessionViewModel : ViewModel() {

    private val _tableId =
        MutableStateFlow<String?>(null)

    val tableId =
        _tableId.asStateFlow()


    private val _tableName =
        MutableStateFlow<String?>(null)

    val tableName =
        _tableName.asStateFlow()


    private val _orderType =
        MutableStateFlow("DINE_IN")

    val orderType =
        _orderType.asStateFlow()


    // =========================================================
    // SET ORDER TYPE
    // =========================================================

    fun setOrderType(orderType: String) {
        _orderType.value = orderType

        Log.d(
            "POS_ORDER",
            "Order type changed to: $orderType"
        )
    }


    private val _sessionId =
        MutableStateFlow("POS_DEFAULT")

    val sessionId =
        _sessionId.asStateFlow()


    // =========================================================
    // SET CURRENT TABLE / SESSION
    // =========================================================

    fun setTable(
        tableId: String,
        tableName: String,
        orderType: String = "DINE_IN"
    ) {

        _tableId.value = tableId
        _tableName.value = tableName
        _orderType.value = orderType

        _sessionId.value =
            if (tableId.isNotBlank()) {
                "POS-$tableId"
            } else {
                "POS_DEFAULT"
            }
    }


    // =========================================================
    // SET CURRENT VIRTUAL TABLE / SESSION
    // =========================================================

    fun setNextVirtualTable(
        tableId: String,
        tableName: String,
        orderType: String
    ) {

        Log.d(
            "POS_ORDER",
            "PosSessionViewModel in = " +
                    "$tableName, id=$tableId, type=$orderType"
        )

        _tableId.value = tableId
        _tableName.value = tableName
        _orderType.value = orderType

        _sessionId.value =
            if (tableId.isNotBlank()) {
                "POS-$tableId"
            } else {
                "POS_DEFAULT"
            }
    }


    // =========================================================
    // CLEAR TABLE
    // =========================================================

    fun clearTable() {

        _tableId.value = null
        _tableName.value = null
        _orderType.value = "DINE_IN"
        _sessionId.value = "POS_DEFAULT"
    }
}


//class PosSessionViewModel : ViewModel() {
//
//    private val _tableId =
//        MutableStateFlow<String?>(null)
//
//    val tableId =
//        _tableId.asStateFlow()
//
//
//    private val _tableName =
//        MutableStateFlow<String?>(null)
//
//    val tableName =
//        _tableName.asStateFlow()
//
//
//    private val _orderType =
//        MutableStateFlow("DINE_IN")
//
//    val orderType =
//        _orderType.asStateFlow()
//
//
//    private val _sessionId =
//        MutableStateFlow("POS_DEFAULT")
//
//    val sessionId =
//        _sessionId.asStateFlow()
//
//
//    // =========================================================
//    // SET CURRENT TABLE / SESSION
//    // =========================================================
//
//    fun setTable(
//        tableId: String,
//        tableName: String,
//        orderType: String = "DINE_IN"
//    ) {
//
//        _tableId.value = tableId
//        _tableName.value = tableName
//        _orderType.value = orderType
//
//        _sessionId.value =
//            if (tableId.isNotBlank()) {
//                "POS-$tableId"
//            } else {
//                "POS_DEFAULT"
//            }
//    }
//
//
//    // =========================================================
//    // SET CURRENT VIRTUAL TABLE / SESSION
//    // =========================================================
//
//    fun setNextVirtualTable(
//        tableId: String,
//        tableName: String,
//        orderType: String
//    ) {
//
//        Log.d(
//            "POS_ORDER",
//            "PosSessionViewModel in = " +
//                    "$tableName, id=$tableId, type=$orderType"
//        )
//
//        _tableId.value = tableId
//        _tableName.value = tableName
//        _orderType.value = orderType
//
//        _sessionId.value =
//            if (tableId.isNotBlank()) {
//                "POS-$tableId"
//            } else {
//                "POS_DEFAULT"
//            }
//    }
//
//
//    // =========================================================
//    // CLEAR TABLE
//    // =========================================================
//
//    fun clearTable() {
//
//        _tableId.value = null
//        _tableName.value = null
//        _orderType.value = "DINE_IN"
//        _sessionId.value = "POS_DEFAULT"
//    }
//}