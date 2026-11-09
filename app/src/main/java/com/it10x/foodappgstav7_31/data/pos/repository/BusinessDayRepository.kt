package com.it10x.foodappgstav7_31.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_31.data.pos.dao.BusinessDayDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosBusinessDayEntity

import java.util.Date

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BusinessDayRepository(
    private val businessDayDao: BusinessDayDao
) {

    /**
     * Returns the current business day.
     * If none exists (first app launch), creates one automatically.
     */
    suspend fun getCurrentBusinessDay(): PosBusinessDayEntity {

        val existing = businessDayDao.getCurrentBusinessDay()

        if (existing != null) {
            return existing
        }

        val now = System.currentTimeMillis()

        val today = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date(now))

        val firstDay = PosBusinessDayEntity(
            id = "CURRENT",
            businessDate = today,
            openedAt = now,
            openedById = "",
            openedByName = "",
            openingCash = 0.0,
            isClosed = false,
            status = "OPEN",
            updatedAt = now
        )

        businessDayDao.save(firstDay)

        return firstDay
    }

    /**
     * Returns only the business date.
     */
    suspend fun getBusinessDate(): String {
        return getCurrentBusinessDay().businessDate
    }

    /**
     * Opens the next business day.
     * Call this ONLY after a successful day closing.
     */
    suspend fun openNextBusinessDay(
        openedById: String,
        openedByName: String
    ) {

        val now = System.currentTimeMillis()

        val nextDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date(now))

        val nextBusinessDay = PosBusinessDayEntity(
            id = "CURRENT",
            businessDate = nextDate,
            openedAt = now,
            openedById = openedById,
            openedByName = openedByName,
            openingCash = 0.0,
            isClosed = false,
            status = "OPEN",
            updatedAt = now
        )

        businessDayDao.save(nextBusinessDay)
    }


    suspend fun closeCurrentBusinessDay(
        closedById: String,
        closedByName: String
    ) {

        val current = getCurrentBusinessDay()

        val closed = current.copy(
            isClosed = true,
            status = "CLOSED",
            closedAt = System.currentTimeMillis(),
            closedById = closedById,
            closedByName = closedByName,
            updatedAt = System.currentTimeMillis()
        )

        businessDayDao.save(closed)
    }


    suspend fun createNextBusinessDay(
        openingCash: Double,
        openedById: String,
        openedByName: String
    ) {

        Log.d(
            "DAY_CLOSE",
            "========== createNextBusinessDay =========="
        )


        val current =
            getCurrentBusinessDay()


        val sdf =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )


        Log.d(
            "DAY_CLOSE",
            "Current Business Date = ${current.businessDate}"
        )


        val currentBusinessDate =
            sdf.parse(
                current.businessDate
            )!!


        val todayDate =
            sdf.parse(
                sdf.format(
                    Date()
                )
            )!!


        Log.d(
            "DAY_CLOSE",
            "Real Today = ${sdf.format(todayDate)}"
        )



        val nextCalendar =
            Calendar.getInstance()


        nextCalendar.time =
            currentBusinessDate


        nextCalendar.add(
            Calendar.DAY_OF_MONTH,
            1
        )


        val candidateDate =
            nextCalendar.time



        Log.d(
            "DAY_CLOSE",
            "Candidate Next Date = ${sdf.format(candidateDate)}"
        )



        val nextDate = when {


            /*
                Example:

                Today = 01/08
                Current business = 02/08

                Means tomorrow is already opened.
                User clicked close twice.
            */

            currentBusinessDate.after(todayDate) -> {


                Log.d(
                    "DAY_CLOSE",
                    "BLOCKED: Future business day already exists"
                )


                throw Exception(
                    "Business day already prepared for tomorrow."
                )
            }



            /*
                Normal case

                Today = 01/08
                Current = 01/08

                Create 02/08
            */

            currentBusinessDate.equals(todayDate) -> {


                Log.d(
                    "DAY_CLOSE",
                    "Normal close: moving one day ahead"
                )


                sdf.format(
                    candidateDate
                )
            }



            /*
                User forgot closing previous days

                Today = 05/08
                Current = 01/08

                Directly move to 05/08
            */

            currentBusinessDate.before(todayDate) -> {


                Log.d(
                    "DAY_CLOSE",
                    "Business date behind today. Moving to today"
                )


                sdf.format(
                    todayDate
                )
            }



            else -> {


                sdf.format(
                    candidateDate
                )
            }
        }



        Log.d(
            "DAY_CLOSE",
            "Final Next Business Date = $nextDate"
        )



        val now =
            System.currentTimeMillis()



        val nextDay =
            PosBusinessDayEntity(

                id = "CURRENT",


                businessDate =
                    nextDate,


                openedAt =
                    now,


                openedById =
                    openedById,


                openedByName =
                    openedByName,


                openingCash =
                    openingCash,


                isClosed =
                    false,


                status =
                    "OPEN",


                updatedAt =
                    now
            )



        Log.d(
            "DAY_CLOSE",
            "Saving next business day..."
        )



        businessDayDao.save(
            nextDay
        )



        Log.d(
            "DAY_CLOSE",
            "Next business day saved successfully."
        )
    }

    suspend fun canCreateNextBusinessDay(): Boolean {


        val current =
            getCurrentBusinessDay()



        val sdf =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )



        val currentDate =
            sdf.parse(
                current.businessDate
            )!!



        val today =
            sdf.parse(
                sdf.format(
                    Date()
                )
            )!!



        Log.d(
            "DAY_CLOSE",
            "CHECK CURRENT=${sdf.format(currentDate)} TODAY=${sdf.format(today)}"
        )



        /*
            Current 01/08
            Today 01/08
            Allow -> create 02/08


            Current 02/08
            Today 01/08
            Block -> already prepared tomorrow
        */


        return !currentDate.after(today)

    }

//    suspend fun createNextBusinessDay(
//        openingCash: Double,
//        openedById: String,
//        openedByName: String
//    ) {
//
//        val current = getCurrentBusinessDay()
//
//        val sdf = SimpleDateFormat(
//            "yyyy-MM-dd",
//            Locale.getDefault()
//        )
//
//        // Current business day
//        val currentBusinessDate =
//            sdf.parse(current.businessDate)!!
//
//        // Real today
//        val todayCalendar =
//            Calendar.getInstance()
//
//        val todayDate =
//            sdf.parse(
//                sdf.format(todayCalendar.time)
//            )!!
//
//        // Candidate = current business day + 1
//        val nextCalendar =
//            Calendar.getInstance()
//
//        nextCalendar.time =
//            currentBusinessDate
//
//        nextCalendar.add(
//            Calendar.DAY_OF_MONTH,
//            1
//        )
//
//        val candidateDate =
//            nextCalendar.time
//
//        /*
//           Rules
//
//           Today = 01 Aug
//           Business = 01 Aug
//           -> Next = 02 Aug
//
//           Today = 01 Aug
//           Business = 02 Aug
//           -> STOP (already prepared tomorrow)
//
//           Today = 02 Aug
//           Business = 01 Aug
//           -> Next = 02 Aug
//
//           Today = 06 Aug
//           Business = 01 Aug
//           -> Next = 06 Aug
//        */
//
//        val nextDate = when {
//
//            // Already created tomorrow
//            candidateDate.after(todayDate) -> {
//                throw Exception(
//                    "Business day already closed for today."
//                )
//            }
//
//            // Forgot to close for one or more days
//            currentBusinessDate.before(todayDate) -> {
//                sdf.format(todayDate)
//            }
//
//            // Normal case
//            else -> {
//                sdf.format(candidateDate)
//            }
//        }
//
//        val now =
//            System.currentTimeMillis()
//
//        val nextDay =
//            PosBusinessDayEntity(
//
//                id = "CURRENT",
//
//                businessDate = nextDate,
//
//                openedAt = now,
//
//                openedById = openedById,
//
//                openedByName = openedByName,
//
//                openingCash = openingCash,
//
//                isClosed = false,
//
//                status = "OPEN",
//
//                updatedAt = now
//            )
//
//        businessDayDao.save(nextDay)
//    }


//    suspend fun createNextBusinessDay(
//        openingCash: Double,
//        openedById: String,
//        openedByName: String
//    ) {
//
//        val current = getCurrentBusinessDay()
//
//        val sdf = SimpleDateFormat(
//            "yyyy-MM-dd",
//            Locale.getDefault()
//        )
//
//        val calendar = Calendar.getInstance()
//
//        calendar.time = sdf.parse(current.businessDate)!!
//
//        // Move to next business day
//        calendar.add(Calendar.DAY_OF_MONTH, 1)
//
//        val nextDate = sdf.format(calendar.time)
//
//        val now = System.currentTimeMillis()
//
//        val nextDay = PosBusinessDayEntity(
//            id = "CURRENT",
//            businessDate = nextDate,
//            openedAt = now,
//            openedById = openedById,
//            openedByName = openedByName,
//            openingCash = openingCash,
//            isClosed = false,
//            status = "OPEN",
//            updatedAt = now
//        )
//
//        businessDayDao.save(nextDay)
//    }

    suspend fun resetBusinessDayForTesting() {

        businessDayDao.deleteAll()

        val sdf = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        val today = sdf.format(Date())

        val now = System.currentTimeMillis()

        businessDayDao.save(
            PosBusinessDayEntity(
                id = "CURRENT",
                businessDate = today,
                openedAt = now,
                openedById = "TEST",
                openedByName = "TEST",
                openingCash = 0.0,
                isClosed = false,
                status = "OPEN",
                updatedAt = now
            )
        )

        // testing only
//        orderDao.deleteAll()
//        paymentDao.deleteAll()
//        kotDao.deleteAll()
    }

}