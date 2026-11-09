package com.it10x.foodappgstav7_31.ui.dayclosing
import android.app.Application
import java.time.LocalDate
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_31.data.pos.entities.PosDayClosingEntity
import com.it10x.foodappgstav7_31.data.pos.repository.BusinessDayRepository
import com.it10x.foodappgstav7_31.data.pos.repository.DayClosingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.it10x.foodappgstav7_31.auth.PosSessionManager

class DayClosingViewModel(

    private val app: Application,
    private val businessDayRepository: BusinessDayRepository,
    private val dayClosingRepository: DayClosingRepository

) : ViewModel() {

    private var isClosing = false
    private val _uiState = MutableStateFlow(DayClosingUiState())

    val uiState: StateFlow<DayClosingUiState> =
        _uiState.asStateFlow()

    init {
        viewModelScope.launch {
//NEXT LINE ARE TO TEST ALWAY COMMENTE THESE
//            businessDayRepository.resetBusinessDayForTesting()
//            dayClosingRepository.clearDayClosingHistory()

            loadData()
        }
    }

    private fun loadData() {

        viewModelScope.launch {

            try {

                Log.d("DAY_CLOSE", "Loading Business Day")

                val businessDay =
                    businessDayRepository.getCurrentBusinessDay()


                Log.d("DAY_CLOSE", "Business Day = $businessDay")

                val summary =
                    dayClosingRepository.getSummary(
                        businessDay.businessDate
                    )

                // =======================================
// PREVENT CLOSE WITHOUT ORDERS
// =======================================

//                if (summary.totalOrders <= 0) {
//
//                    Log.d(
//                        "DAY_CLOSE",
//                        "Cannot close day. No orders found."
//                    )
//
//                    _uiState.value =
//                        _uiState.value.copy(
//                            errorMessage =
//                                "Cannot close day. No orders found."
//                        )
//
//                    return@launch
//                }

                _uiState.value = _uiState.value.copy(

                    businessDate = businessDay.businessDate,

                    openedBy = businessDay.openedByName,

                    openedAt = businessDay.openedAt,

                    openingCash = businessDay.openingCash,

                    actualCash = "",

                    cashHandedOver = "",

                    totalOrders = summary.totalOrders,

                    totalSales = summary.totalSales,

                    totalDiscount = summary.totalDiscount,

                    totalTax = summary.totalTax,

                    complimentarySales = summary.complimentarySales,

                    cashSales = summary.cashSales,

                    cardSales = summary.cardSales,

                    upiSales = summary.upiSales,

                    walletSales = summary.walletSales,

                    creditSales = summary.creditSales,

                    expectedCash =
                        businessDay.openingCash +
                                summary.cashSales

                )

            } catch (e: Exception) {

                Log.e("DAY_CLOSE", "Load failed", e)

            }
        }
    }

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    fun updateSelectedDate(date: LocalDate) {
        selectedDate = date
        loadDayData(date) // 🔥 fetch sales for selected date
    }

    fun closeBusinessDay() {

        if (isClosing) {
            return
        }


        isClosing = true


        viewModelScope.launch {

            try {


                _uiState.value =
                    _uiState.value.copy(
                        errorMessage = null
                    )


                Log.d(
                    "DAY_CLOSE",
                    "Starting Day Closing"
                )



                val businessDay =
                    businessDayRepository
                        .getCurrentBusinessDay()
                val closedById =
                    PosSessionManager.getUserId(app)

                val closedByName =
                    PosSessionManager.getFullName(app)


                Log.d(
                    "DAY_CLOSE",
                    "Closed By: $closedById - $closedByName"
                )


                Log.d(
                    "DAY_CLOSE",
                    "Current Business Day = $businessDay"
                )



                /*
                    IMPORTANT CHECK

                    If tomorrow business day is already created,
                    stop here.

                    Do not save closing.
                    Do not change status.
                */

                val canCreate =
                    businessDayRepository
                        .canCreateNextBusinessDay()



                if (!canCreate) {


                    Log.d(
                        "DAY_CLOSE",
                        "Business Day Already Prepared For Tomorrow"
                    )


                    _uiState.value =
                        _uiState.value.copy(

                            errorMessage =
                                "Tomorrow business day is already open."

                        )


                    return@launch
                }





                val summary =
                    dayClosingRepository
                        .getSummary(
                            businessDay.businessDate
                        )




                val actualCash =
                    _uiState.value.actualCash
                        .toDoubleOrNull()
                        ?: 0.0

                val cashHandedOver =
                    _uiState.value.cashHandedOver
                        .toDoubleOrNull()
                        ?: 0.0

                if (cashHandedOver < 0.0) {
                    throw IllegalArgumentException(
                        "Cash handed over cannot be negative."
                    )
                }

                if (cashHandedOver > actualCash) {
                    throw IllegalArgumentException(
                        "Cash handed over cannot be greater than actual cash."
                    )
                }


                val expectedCash =
                    businessDay.openingCash +
                            summary.cashSales




                val difference =
                    actualCash - expectedCash




                val dayClosing =
                    PosDayClosingEntity(


                        id =
                            businessDay.businessDate,


                        businessDate =
                            businessDay.businessDate,


                        openedAt =
                            businessDay.openedAt,


                        closedAt =
                            System.currentTimeMillis(),



                        openedById =
                            businessDay.openedById,


                        openedByName =
                            businessDay.openedByName,



//                        closedById =
//                            businessDay.openedById,
//                        closedByName =
//                            businessDay.openedByName,

                        closedById =
                            closedById?:"",

                        closedByName =
                            closedByName?:"",

                        openingCash =
                            businessDay.openingCash,



                        expectedCash =
                            expectedCash,


                        actualCash =
                            actualCash,

                        cashHandedOver =
                            cashHandedOver,

                        cashDifference =
                            difference,



                        totalSales =
                            summary.totalSales,


                        totalRefund =
                            0.0,


                        totalDiscount =
                            summary.totalDiscount,


                        totalTax =
                            summary.totalTax,



                        cashSales =
                            summary.cashSales,


                        cardSales =
                            summary.cardSales,


                        upiSales =
                            summary.upiSales,


                        walletSales =
                            summary.walletSales,


                        creditSales =
                            summary.creditSales,


                        complimentarySales =
                            summary.complimentarySales,



                        totalOrders =
                            summary.totalOrders,



                        syncStatus =
                            "PENDING",



                        createdAt =
                            System.currentTimeMillis()
                    )




                /*
                    STEP 1
                    Save closing history
                */

                dayClosingRepository.save(
                    dayClosing
                )


                Log.d(
                    "DAY_CLOSE",
                    "Day Closing Saved"
                )




                /*
                    STEP 2
                    Close current business day
                */

                businessDayRepository.closeCurrentBusinessDay(

                    closedById =
                        closedById ?: "",

                    closedByName =
                        closedByName ?: ""
                )


                Log.d(
                    "DAY_CLOSE",
                    "Business Day Closed"
                )





                /*
                    STEP 3
                    Create next business day
                */

                businessDayRepository.createNextBusinessDay(

                    openingCash =
                        actualCash - cashHandedOver,

                    openedById =
                        closedById ?: "",

                    openedByName =
                        closedByName ?: ""
                )



                Log.d(
                    "DAY_CLOSE",
                    "Next Business Day Created"
                )



                loadData()



                Log.d(
                    "DAY_CLOSE",
                    "Day Closed Successfully"
                )



            } catch (e: Exception) {


                Log.e(
                    "DAY_CLOSE",
                    "Day Closing Failed",
                    e
                )



                _uiState.value =
                    _uiState.value.copy(

                        errorMessage =
                            e.message
                    )



            } finally {


                isClosing = false

            }
        }
    }

    fun updateActualCash(value: String) {

        val currentHandedOver =
            _uiState.value.cashHandedOver

        val currentActualCash =
            _uiState.value.actualCash

        _uiState.value =
            _uiState.value.copy(
                actualCash = value,
                cashHandedOver =
                    if (
                        currentHandedOver.isEmpty() ||
                        currentHandedOver == currentActualCash
                    ) {
                        value
                    } else {
                        currentHandedOver
                    }
            )
    }

    fun updateCashHandedOver(value: String) {

        _uiState.value =
            _uiState.value.copy(
                cashHandedOver = value
            )
    }

    fun updateNotes(value: String) {

        _uiState.value =
            _uiState.value.copy(
                notes = value
            )
    }
    private fun loadDayData(date: LocalDate) {

        viewModelScope.launch {

            val summary =
                dayClosingRepository.getSummary(date.toString())

            _uiState.value = _uiState.value.copy(

                businessDate = date.toString(),

                totalOrders = summary.totalOrders,

                totalSales = summary.totalSales,

                totalDiscount = summary.totalDiscount,

                totalTax = summary.totalTax,

                complimentarySales = summary.complimentarySales,

                cashSales = summary.cashSales,

                cardSales = summary.cardSales,

                upiSales = summary.upiSales,

                walletSales = summary.walletSales,

                creditSales = summary.creditSales
            )
        }
    }
//FOR TESTING



}