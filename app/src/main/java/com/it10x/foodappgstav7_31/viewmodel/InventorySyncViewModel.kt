package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.repository.InventorySyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InventorySyncViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabaseProvider.get(app)
    private val repo = InventorySyncRepository(db)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    private val _lastSyncSuccess = MutableStateFlow<Boolean?>(null)
    val lastSyncSuccess: StateFlow<Boolean?> = _lastSyncSuccess

    fun syncInventoryNow() {

        viewModelScope.launch {

            _isSyncing.value = true
            _status.value = "Updating inventory to server..."

            try {
                repo.syncInventoryFromSales()

                _status.value = "Inventory updated successfully ✅"
                _lastSyncSuccess.value = true

            } catch (e: Exception) {

                _status.value = "Sync failed: ${e.message}"
                _lastSyncSuccess.value = false
            }

            _isSyncing.value = false
        }
    }
}