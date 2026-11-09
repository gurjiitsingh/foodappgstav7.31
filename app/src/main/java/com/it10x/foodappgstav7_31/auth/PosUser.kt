package com.it10x.foodappgstav7_31.auth

data class PosUser(

    val id: String = "",

    val fullName: String = "",

    val mobile: String = "",

    val employeeId: String = "",

    val role: String = "",

    val allowPosLogin: Boolean = true,

    val active: Boolean = true
)