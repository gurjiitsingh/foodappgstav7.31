package com.it10x.foodappgstav7_31.ui.dayclosing

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.pos.repository.BusinessDayRepository
import com.it10x.foodappgstav7_31.data.pos.repository.DayClosingRepository

class DayClosingViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val db = AppDatabaseProvider.get(application)

        val businessDayRepository =
            BusinessDayRepository(
                db.businessDayDao()
            )

        val dayClosingRepository =
            DayClosingRepository(
                db = db,
                dayClosingDao = db.dayClosingDao(),
                businessDayDao = db.businessDayDao(),
                orderMasterDao = db.orderMasterDao(),
                paymentDao = db.posOrderPaymentDao()
            )

        return DayClosingViewModel(
            application,
            businessDayRepository,
            dayClosingRepository
        ) as T
    }
}