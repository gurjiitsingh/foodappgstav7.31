package com.it10x.foodappgstav7_31.data.online.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.PosUserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserSyncRepository(
    private val db: AppDatabase
) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncUsers() = withContext(Dispatchers.IO) {

        try {

            Log.d("USER_SYNC", "Starting user sync...")

            val snapshot = firestore
                .collection("users")
            //    .whereEqualTo("allowPosLogin", true)
                .get()
                .await()

            Log.d(
                "USER_SYNC",
                "Firestore returned ${snapshot.size()} documents"
            )

            snapshot.documents.forEach { doc ->
                Log.d(
                    "USER_SYNC",
                    "Doc=${doc.id}, data=${doc.data}"
                )
            }

            val list = snapshot.documents.mapNotNull { doc ->

                val data = doc.data ?: return@mapNotNull null

                PosUserEntity(

                    userId = doc.id,

                    outletId = data["outletId"] as? String ?: "",

                    fullName = data["fullName"] as? String ?: "",

                    username = data["username"] as? String ?: "",

                    mobile = data["mobile"] as? String ?: "",

                    employeeId = data["employeeId"] as? String ?: "",

                    role = data["role"] as? String ?: "cashier",

                    loginPin = data["loginPin"] as? String ?: "",

                    allowPosLogin =
                        data["allowPosLogin"] as? Boolean ?: false,

                    isActive =
                        (data["status"] as? String ?: "active") == "active",

                    createdAt =
                        (data["createdAt"] as? Timestamp)
                            ?.toDate()
                            ?.time
                            ?: System.currentTimeMillis(),

                    updatedAt =
                        (data["updatedAt"] as? Timestamp)
                            ?.toDate()
                            ?.time
                            ?: System.currentTimeMillis(),

                    syncStatus = "SYNCED",

                    lastSyncedAt = System.currentTimeMillis()
                )
            }

            Log.d(
                "USER_SYNC",
                "Mapped ${list.size} users"
            )

            list.forEach {
                Log.d(
                    "USER_SYNC",
                    "User=${it.fullName}, PIN=${it.loginPin}"
                )
            }

            db.posUserDao().deleteAll()
            db.posUserDao().insertAll(list)

            val count = db.posUserDao().countUsers()
            Log.d("USER_SYNC", "Room Count = $count")

            val users = db.posUserDao().getAllUsers()

            users.forEach {
                Log.d(
                    "USER_SYNC",
                    "ROOM -> ${it.fullName}, active=${it.isActive}, allow=${it.allowPosLogin}"
                )
            }

            Log.d(
                "USER_SYNC",
                "Inserted ${list.size} users into Room"
            )

        } catch (e: Exception) {

            Log.e(
                "USER_SYNC",
                "Sync failed",
                e
            )
        }
    }
}