package com.it10x.foodappgstav7_31.auth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getPosUsers(): List<PosUser> {

        return try {

            val snapshot = firestore
                .collection("users")
                .whereEqualTo("allowPosLogin", true)
                .whereEqualTo("active", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->

                try {

                    PosUser(
                        id = doc.id,
                        fullName = doc.getString("fullName") ?: "",
                        mobile = doc.getString("mobile") ?: "",
                        employeeId = doc.getString("employeeId") ?: "",
                        role = doc.getString("role") ?: "",
                        allowPosLogin = doc.getBoolean("allowPosLogin") ?: false,
                        active = doc.getBoolean("active") ?: false
                    )

                } catch (e: Exception) {
                    null
                }

            }.sortedBy {
                it.fullName.lowercase()
            }

        } catch (e: Exception) {

            emptyList()

        }

    }

}