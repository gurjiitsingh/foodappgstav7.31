package com.it10x.foodappgstav7_31.ui.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_31.data.pos.dao.SalesMasterDao
import com.it10x.foodappgstav7_31.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_31.data.pos.dao.PosOrderPaymentDao

class SalesViewModelFactory(
    private val salesMasterDao: SalesMasterDao,
    private val orderProductDao: OrderProductDao,
    private val paymentDao: PosOrderPaymentDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            return SalesViewModel(
                salesMasterDao = salesMasterDao,
                orderProductDao = orderProductDao,
                paymentDao = paymentDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}