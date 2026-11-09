package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.PosUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PosUserDao {

    @Query("""
        SELECT *
        FROM pos_users
        WHERE isActive = 1
        AND allowPosLogin = 1
        ORDER BY fullName
    """)
    fun observeUsers(): Flow<List<PosUserEntity>>

    @Query("""
        SELECT *
        FROM pos_users
        WHERE userId = :userId
        LIMIT 1
    """)
    suspend fun getUser(
        userId: String
    ): PosUserEntity?

    // ⭐ Add this
    @Query("SELECT * FROM pos_users")
    suspend fun getAllUsers(): List<PosUserEntity>

    // ⭐ Add this
    @Query("SELECT COUNT(*) FROM pos_users")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        users: List<PosUserEntity>
    )

    @Query("DELETE FROM pos_users")
    suspend fun deleteAll()
}