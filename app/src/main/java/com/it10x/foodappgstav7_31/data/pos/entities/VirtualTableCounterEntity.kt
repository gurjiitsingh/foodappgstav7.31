package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity

@Entity(
    tableName = "virtual_table_counters",
    primaryKeys = ["orderType"]
)
data class VirtualTableCounterEntity(


    val orderType: String,

    val lastNumber: Int = 0,

    val status: String = "NEW",

    val resetDate: String = ""
)