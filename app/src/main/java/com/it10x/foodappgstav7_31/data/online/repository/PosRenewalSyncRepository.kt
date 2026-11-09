package com.it10x.foodappgstav7_31.data.online.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.PosRenewalEntity
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosRenewalSyncRepository(
    private val db: AppDatabase,
    private val firestore: FirebaseFirestore
) {

    // =====================================================
    // FIRESTORE CHECK INTERVAL
    // =====================================================

    private val checkIntervalMillis =
        5L * 24L * 60L * 60L * 1000L


    // =====================================================
    // DATE FORMAT
    // =====================================================

    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.US)


    // =====================================================
    // GET TODAY
    // =====================================================

    private fun getToday(): String {
        return dateFormat.format(Date())
    }


    // =====================================================
    // CHECK WHETHER FIRESTORE CHECK IS DUE
    // =====================================================

    private fun isFirestoreCheckDue(
        updatedAt: Long?
    ): Boolean {

        if (updatedAt == null) {
            return true
        }

        val elapsed =
            System.currentTimeMillis() - updatedAt

        return elapsed >= checkIntervalMillis
    }


    // =====================================================
    // CHECK POS RENEWAL
    //
    // TRUE  = POS CAN START
    // FALSE = POS MUST CLOSE
    // =====================================================

    suspend fun checkRenewalAtStartup(): Boolean {

        val today =
            getToday()

        Log.d(
            "POS_RENEWAL",
            "Today = $today"
        )


        // =================================================
        // GET LOCAL OUTLET
        // =================================================

        val localOutlet =
            db.outletDao().getOutlet()

        val localRenewDate =
            localOutlet?.renewDate

        Log.d(
            "POS_RENEWAL",
            "Local Outlet ID = ${localOutlet?.outletId}"
        )

        Log.d(
            "POS_RENEWAL",
            "Local outlet.renewDate = $localRenewDate"
        )


        // =================================================
        // GET LAST FIRESTORE CHECK
        // =================================================

        val localRenewalCheck =
            db.posRenewalDao().getRenewal()

        val lastCheckedAt =
            localRenewalCheck?.updatedAt

        Log.d(
            "POS_RENEWAL",
            "Last Firestore check = $lastCheckedAt"
        )


        // =================================================
        // CHECK LOCAL RENEWAL
        // =================================================

        val localValid =
            isRenewDateValid(
                localRenewDate,
                today
            )

        Log.d(
            "POS_RENEWAL",
            "Local renewal valid = $localValid"
        )


        // =================================================
        // DECIDE FIRESTORE CHECK
        //
        // Local expired/missing
        //      -> CHECK NOW
        //
        // Local valid + less than 5 days
        //      -> USE LOCAL
        //
        // Local valid + 5 days reached
        //      -> CHECK FIRESTORE
        // =================================================

        val firestoreCheckRequired =
            !localValid ||
                    isFirestoreCheckDue(lastCheckedAt)

        Log.d(
            "POS_RENEWAL",
            "Firestore check required = $firestoreCheckRequired"
        )


        // =================================================
        // LOCAL DATE STILL VALID
        // AND FIRESTORE CHECK NOT DUE
        // =================================================

        if (!firestoreCheckRequired) {

            Log.d(
                "POS_RENEWAL",
                "5-day Firestore check not due"
            )

            Log.d(
                "POS_RENEWAL",
                "Using local outlet.renewDate = $localRenewDate"
            )

            Log.d(
                "POS_RENEWAL",
                "POS CAN START"
            )

            return true
        }


        // =================================================
        // FIRESTORE CHECK
        // =================================================

        try {

            Log.d(
                "POS_RENEWAL",
                "Checking Firestore outlet.renewDate..."
            )


            // =================================================
            // GET OUTLET
            // =================================================

            val snapshot =
                firestore
                    .collection("outlets")
                    .limit(1)
                    .get()
                    .await()


            // =================================================
            // NO OUTLET
            // =================================================

            if (snapshot.isEmpty) {

                Log.d(
                    "POS_RENEWAL",
                    "No outlet found in Firestore"
                )

                if (localValid) {

                    Log.d(
                        "POS_RENEWAL",
                        "Local outlet.renewDate is still valid"
                    )

                    Log.d(
                        "POS_RENEWAL",
                        "POS CAN START"
                    )

                    return true
                }

                Log.d(
                    "POS_RENEWAL",
                    "No valid renewal date"
                )

                Log.d(
                    "POS_RENEWAL",
                    "POS MUST CLOSE"
                )

                return false
            }


            // =================================================
            // FIRESTORE OUTLET
            // =================================================

            val outletDocument =
                snapshot.documents.first()

            Log.d(
                "POS_RENEWAL",
                "Firestore Outlet ID = ${outletDocument.id}"
            )


            // =================================================
            // GET outlet.renewDate
            // =================================================

            val firestoreRenewDate =
                outletDocument.getString("renewDate")

            Log.d(
                "POS_RENEWAL",
                "Firestore outlet.renewDate = $firestoreRenewDate"
            )


            // =================================================
            // EMPTY FIRESTORE RENEWAL DATE
            // =================================================

            if (firestoreRenewDate.isNullOrBlank()) {

                Log.d(
                    "POS_RENEWAL",
                    "Firestore outlet.renewDate is empty"
                )

                if (localValid) {

                    Log.d(
                        "POS_RENEWAL",
                        "Using valid local outlet.renewDate"
                    )

                    return true
                }

                Log.d(
                    "POS_RENEWAL",
                    "No valid renewal date"
                )

                Log.d(
                    "POS_RENEWAL",
                    "POS MUST CLOSE"
                )

                return false
            }


            // =================================================
            // UPDATE LOCAL OUTLET RENEW DATE
            //
            // Firestore is now the source of truth.
            // =================================================

            val updatedOutlet =
                localOutlet?.copy(
                    renewDate = firestoreRenewDate
                )

            if (updatedOutlet != null) {

                db.outletDao().saveOutlet(
                    updatedOutlet
                )

                Log.d(
                    "POS_RENEWAL",
                    "LOCAL outlet.renewDate UPDATED = $firestoreRenewDate"
                )
            }


            // =================================================
            // SAVE LAST SUCCESSFUL FIRESTORE CHECK
            // =================================================

            db.posRenewalDao().save(
                PosRenewalEntity(
                    updatedAt =
                        System.currentTimeMillis()
                )
            )


            // =================================================
            // VALIDATE FIRESTORE RENEWAL DATE
            // =================================================

            val valid =
                isRenewDateValid(
                    firestoreRenewDate,
                    today
                )

            Log.d(
                "POS_RENEWAL",
                "FINAL RENEWAL VALID = $valid"
            )


            if (valid) {

                Log.d(
                    "POS_RENEWAL",
                    "RENEWAL VALID - POS CAN START"
                )

            } else {

                Log.d(
                    "POS_RENEWAL",
                    "RENEWAL EXPIRED - POS MUST CLOSE"
                )
            }

            return valid

        }
        catch (e: Exception) {

            Log.e(
                "POS_RENEWAL",
                "Firestore renewal check failed",
                e
            )

            // Local renewal is expired.
            // Allow maximum 10 offline startup attempts.

            val renewal = db.posRenewalDao().getRenewal()
            val attempts = renewal?.offlineAttempts ?: 0

            if (attempts < 10) {

                val newAttempts = attempts + 1

                db.posRenewalDao().save(
                    PosRenewalEntity(
                        updatedAt = renewal?.updatedAt
                            ?: System.currentTimeMillis(),
                        offlineAttempts = newAttempts
                    )
                )

                Log.d(
                    "POS_RENEWAL",
                    "Offline startup allowed: $newAttempts / 10"
                )

                return true
            }

            Log.d(
                "POS_RENEWAL",
                "Offline startup blocked: 10 / 10 attempts used"
            )

            return false
        }
    }


    // =====================================================
    // DATE VALIDATION
    //
    // renewDate >= today = VALID
    // renewDate < today  = EXPIRED
    // =====================================================

    private fun isRenewDateValid(
        renewDate: String?,
        today: String
    ): Boolean {

        if (renewDate.isNullOrBlank()) {
            return false
        }

        return try {

            val renew =
                dateFormat.parse(renewDate)

            val current =
                dateFormat.parse(today)

            renew != null &&
                    current != null &&
                    !renew.before(current)

        } catch (e: Exception) {

            Log.e(
                "POS_RENEWAL",
                "Invalid renewal date: $renewDate",
                e
            )

            false
        }
    }
}