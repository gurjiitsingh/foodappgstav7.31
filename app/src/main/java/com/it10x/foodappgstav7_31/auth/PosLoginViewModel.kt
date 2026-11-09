package com.it10x.foodappgstav7_31.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.entities.PosUserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PosLoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao =
        AppDatabaseProvider
            .get(application)
            .posUserDao()

    private val _users =
        MutableStateFlow<List<PosUserEntity>>(emptyList())
    val users: StateFlow<List<PosUserEntity>> =
        _users.asStateFlow()

    private val _isLoading =
        MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _error =
        MutableStateFlow<String?>(null)
    val error: StateFlow<String?> =
        _error.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeUsers().collect { list ->
                _users.value = list
                _isLoading.value = false
            }
        }
    }

    fun showError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }
}