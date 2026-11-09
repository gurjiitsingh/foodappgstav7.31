package com.it10x.foodappgstav7_31.core

import com.it10x.foodappgstav7_31.data.online.repository.PosRenewalSyncRepository

class PosRenewalManager(
    private val repository: PosRenewalSyncRepository
) {

    suspend fun checkAtStartup(): Boolean {
        return repository.checkRenewalAtStartup()
    }
}