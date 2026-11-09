package com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modifier_groups")
data class ModifierGroupEntity(

    @PrimaryKey
    val id: String,              // Firestore doc id

    val name: String,

    val minSelection: Int,
    val maxSelection: Int,

    val sortOrder: Int = 0,

    val status: String          // published / draft
)