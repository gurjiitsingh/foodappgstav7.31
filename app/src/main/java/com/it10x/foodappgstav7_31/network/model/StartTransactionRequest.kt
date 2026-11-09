package com.it10x.foodappgstav7_31.network.model

import com.google.gson.annotations.SerializedName


data class StartTransactionRequest(
    val state: String = "ACTIVE",
    val client_id: String
)

//data class StartTransactionRequest(
//
//    @SerializedName("state")
//    val state: String = "ACTIVE",
//
//    @SerializedName("client_id")
//    val clientId: String
//)

//data class StartTransactionRequest(
//
//    @SerializedName("state")
//    val state: String = "ACTIVE",
//
//    @SerializedName("client_id")
//    val clientId: String,
//
//    @SerializedName("tx_revision")
//    val txRevision: Int,
//
//    @SerializedName("process_data")
//    val processData: String
//)