package com.it10x.foodappgstav7_31.ui.kot.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotHistoryEntity
import com.it10x.foodappgstav7_31.data.pos.repository.KotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class KotHistoryViewModel(
    private val repository: KotRepository
) : ViewModel() {


    private val _filter =
        MutableStateFlow(KotHistoryFilter.ALL)

    val filter = _filter.asStateFlow()

    fun setFilter(filter: KotHistoryFilter) {
        _filter.value = filter
    }
    /**
     * One row per KOT
     */
    val historySummary =
        repository.getHistorySummary()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Full history (optional)
     * Keep this if another screen still uses it.
     */
    val history =
        repository.getHistoryAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Detail of a single KOT (all items)
     */
    fun getKotDetails(
        batchId: String
    ) = repository.getHistoryByKotNumber(batchId)

    //BY DATE
    private val _dateFilter =
        MutableStateFlow(KotHistoryDateFilter.TODAY)

    val dateFilter: StateFlow<KotHistoryDateFilter> =
        _dateFilter

    private val _selectedDate =
        MutableStateFlow<Long?>(null)

    val selectedDate: StateFlow<Long?> =
        _selectedDate

    fun setDateFilter(filter: KotHistoryDateFilter) {
        _dateFilter.value = filter
    }

    fun setSelectedDate(time: Long) {
        _selectedDate.value = time
    }
}