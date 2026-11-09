package com.it10x.foodappgstav7_31.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_31.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_31.data.online.repository.PosOrderSyncRepository
import com.it10x.foodappgstav7_31.data.pos.repository.POSPaymentRepository

class OrderSyncViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderSyncViewModel::class.java)) {

            val db = AppDatabaseProvider.get(application)
            val paymentRepository = POSPaymentRepository(
                db.posOrderPaymentDao()
            )

            val repository = PosOrderSyncRepository(
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                outletDao = db.outletDao(),
                paymentRepository = paymentRepository
            )

            @Suppress("UNCHECKED_CAST")
            return OrderSyncViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
