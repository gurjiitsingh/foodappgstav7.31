package com.it10x.foodappgstav7_31.ui.kitchen



import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_31.data.PrinterPreferences
import com.it10x.foodappgstav7_31.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_31.ui.cart.CartViewModel

class KitchenViewModelFactory(
    private val app: Application,
    private val tableId: String,
    private val tableName: String,
    private val sessionId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(KitchenViewModel::class.java)) {
            val prefs = PrinterPreferences(app.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return KitchenViewModel(app, tableId, tableName, sessionId, orderType, repository ,  prefs = prefs,) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
