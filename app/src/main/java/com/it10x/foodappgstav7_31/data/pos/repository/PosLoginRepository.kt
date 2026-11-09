package com.it10x.foodappgstav7_31.data.pos.repository

import com.it10x.foodappgstav7_31.data.pos.dao.PosUserDao
import com.it10x.foodappgstav7_31.data.pos.entities.PosUserEntity
import kotlinx.coroutines.flow.Flow

class PosLoginRepository(
    private val dao: PosUserDao
) {

    fun observeUsers(): Flow<List<PosUserEntity>> {
        return dao.observeUsers()
    }

    suspend fun login(
        userId: String,
        pin: String
    ): PosUserEntity? {

        val user = dao.getUser(userId) ?: return null

        if (!user.isActive) return null

        if (!user.allowPosLogin) return null

        if (user.loginPin != pin) return null

        return user
    }
}