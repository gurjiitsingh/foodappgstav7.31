package com.it10x.foodappgstav7_31.data.pos.repository

import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.dao.BusinessDayDao
import com.it10x.foodappgstav7_31.data.pos.dao.DayClosingDao
import com.it10x.foodappgstav7_31.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_31.data.pos.dao.PosOrderPaymentDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosDayClosingEntity

class DayClosingRepository(

    private val db: AppDatabase,

    private val dayClosingDao: DayClosingDao,

    private val businessDayDao: BusinessDayDao,

    private val orderMasterDao: OrderMasterDao,

    private val paymentDao: PosOrderPaymentDao
) {

    suspend fun save(entity: PosDayClosingEntity) {
        dayClosingDao.insert(entity)
    }

    suspend fun history(): List<PosDayClosingEntity> {
        return dayClosingDao.getAll()
    }

    suspend fun alreadyClosed(
        businessDate: String
    ): Boolean {

        return dayClosingDao.getByBusinessDate(
            businessDate
        ) != null
    }

    suspend fun getSummary(
        businessDate: String
    ): DayClosingSummary {
     //   clearDayClosingHistory()
        val totalSales =
            orderMasterDao.getTotalSales(businessDate)

        val totalOrders =
            orderMasterDao.getOrderCount(businessDate)

        val totalDiscount =
            orderMasterDao.getTotalDiscount(businessDate)

        val totalTax =
            orderMasterDao.getTotalTax(businessDate)

        val complimentarySales =
            orderMasterDao.getComplimentarySales(businessDate)

        val cashSales =
            paymentDao.getPaymentTotal(
                mode = "CASH",
                businessDate = businessDate
            )

        val cardSales =
            paymentDao.getPaymentTotal(
                mode = "CARD",
                businessDate = businessDate
            )

        val upiSales =
            paymentDao.getPaymentTotal(
                mode = "UPI",
                businessDate = businessDate
            )

        val walletSales =
            paymentDao.getPaymentTotal(
                mode = "WALLET",
                businessDate = businessDate
            )

        val creditSales =
            orderMasterDao.getCreditSales(businessDate)

        return DayClosingSummary(
            totalOrders = totalOrders,
            totalSales = totalSales,
            totalDiscount = totalDiscount,
            totalTax = totalTax,
            complimentarySales = complimentarySales,
            cashSales = cashSales,
            cardSales = cardSales,
            upiSales = upiSales,
            walletSales = walletSales,
            creditSales = creditSales
        )


    }
//FOR TESTING
    suspend fun clearDayClosingHistory() {
        dayClosingDao.deleteAll()
    }
}

data class DayClosingSummary(

    val totalOrders: Int,

    val totalSales: Double,

    val totalDiscount: Double,

    val totalTax: Double,

    val complimentarySales: Double,

    val cashSales: Double,

    val cardSales: Double,

    val upiSales: Double,

    val walletSales: Double,

    val creditSales: Double
)