package com.it10x.foodappgstav7_31.network.model

import com.google.gson.annotations.SerializedName

data class TseResponse(

    @SerializedName("tss_id")
    val id: String?,   // make nullable (safe)

    val state: String?,

    val admin_puk: String?   // ✅ correct
)