
package com.it10x.foodappgstav7_31.data.pos.repository.loyalty

import androidx.room.withTransaction
import com.it10x.foodappgstav7_31.data.pos.AppDatabase
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import androidx.room.withTransaction
import kotlinx.coroutines.flow.first

class LoyaltyRepository(
    private val db: AppDatabase
) {

    private val programDao = db.loyaltyProgramDao()
    private val programOutletDao = db.loyaltyProgramOutletDao()
    private val tierDao = db.loyaltyTierDao()
    private val accountDao = db.loyaltyAccountDao()
    private val transactionDao = db.loyaltyTransactionDao()
    private val pointLotDao = db.loyaltyPointLotDao()
    private val allocationDao = db.loyaltyPointAllocationDao()

    private val rewardDao = db.loyaltyRewardDao()
    private val redemptionDao = db.loyaltyRewardRedemptionDao()

    private val voucherDao = db.loyaltyVoucherDao()
    private val voucherUsageDao = db.loyaltyVoucherUsageDao()

    private val campaignDao = db.loyaltyCampaignDao()
    private val campaignUsageDao = db.loyaltyCampaignUsageDao()

    private val referralDao = db.loyaltyReferralDao()


    // ============================================================
    // 1. GET OR CREATE CUSTOMER LOYALTY ACCOUNT
    // ============================================================

    suspend fun getOrCreateAccount(
        customerId: String,
        programId: String,
        outletId: String
    ): LoyaltyAccountEntity = db.withTransaction {

        require(customerId.isNotBlank()) {
            "Customer ID is required"
        }

        require(outletId.isNotBlank()) {
            "Outlet ID is required"
        }

        val program = programDao.getById(programId)
            ?: throw IllegalStateException("Loyalty programme not found")

        require(program.isActive) {
            "Loyalty programme is inactive"
        }

        val scopeKey: String
        val accountOutletId: String?

        when (program.scope) {

            LoyaltyProgramScope.SHARED -> {

                val participating = programOutletDao.isOutletLinked(
                    programId = programId,
                    outletId = outletId
                )

                require(participating) {
                    "This outlet is not participating in the loyalty programme"
                }

                scopeKey = "SHARED"
                accountOutletId = null
            }

            LoyaltyProgramScope.OUTLET -> {

                scopeKey = outletId
                accountOutletId = outletId
            }
        }

        val existing = accountDao.getAccount(
            customerId = customerId,
            programId = programId,
            scopeKey = scopeKey
        )

        if (existing != null) {
            return@withTransaction existing
        }

        val now = System.currentTimeMillis()

        val account = LoyaltyAccountEntity(
            id = UUID.randomUUID().toString(),
            ownerId = program.ownerId,
            customerId = customerId,
            programId = programId,
            scopeKey = scopeKey,
            outletId = accountOutletId,
            balancePoints = 0,
            lifetimeEarned = 0,
            lifetimeRedeemed = 0,
            lifetimeExpired = 0,
            lifetimeAdjusted = 0,
            lifetimeSpendMinor = 0L,
            joinedAt = now,
            lastActivityAt = now,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )

        accountDao.insert(account)

        account
    }


    // ============================================================
    // 2. GET CUSTOMER ACCOUNT
    // ============================================================

    suspend fun getAccount(
        customerId: String,
        programId: String,
        outletId: String
    ): LoyaltyAccountEntity? {

        val program = programDao.getById(programId) ?: return null

        val scopeKey = when (program.scope) {
            LoyaltyProgramScope.SHARED -> "SHARED"
            LoyaltyProgramScope.OUTLET -> outletId
        }

        return accountDao.getAccount(
            customerId = customerId,
            programId = programId,
            scopeKey = scopeKey
        )
    }


    // ============================================================
    // 3. EARN LOYALTY POINTS
    // ============================================================

    suspend fun earnPoints(
        accountId: String,
        points: Int,
        spendMinor: Long,
        idempotencyKey: String,
        orderId: String? = null,
        orderNo: String? = null,
        outletId: String? = null,
        businessDate: String? = null,
        createdBy: String? = null,
        description: String? = null
    ): LoyaltyTransactionEntity = db.withTransaction {

        require(points > 0) {
            "Earned points must be greater than zero"
        }

        require(spendMinor >= 0L) {
            "Spend amount cannot be negative"
        }

        require(idempotencyKey.isNotBlank()) {
            "Idempotency key is required"
        }

        // Prevent duplicate earning for the same event.
        val existing = transactionDao.getByIdempotencyKey(
            idempotencyKey
        )

        if (existing != null) {
            return@withTransaction existing
        }

        val account = accountDao.getById(accountId)
            ?: throw IllegalStateException("Loyalty account not found")

        require(account.isActive) {
            "Loyalty account is inactive"
        }

        val program = programDao.getById(account.programId)
            ?: throw IllegalStateException("Loyalty programme not found")

        val now = System.currentTimeMillis()

        val newBalance = account.balancePoints + points

        val transactionId = UUID.randomUUID().toString()

        val loyaltyTransaction = LoyaltyTransactionEntity(
            id = transactionId,
            ownerId = account.ownerId,
            accountId = accountId,
            transactionType = LoyaltyTransactionType.EARN,
            points = points,
            balanceAfter = newBalance,
            orderId = orderId,
            orderNo = orderNo,
            outletId = outletId,
            idempotencyKey = idempotencyKey,
            description = description ?: "Points earned",
            businessDate = businessDate,
            createdBy = createdBy,
            createdAt = now
        )

        val inserted = transactionDao.insert(loyaltyTransaction)

        if (inserted == -1L) {
            return@withTransaction transactionDao
                .getByIdempotencyKey(idempotencyKey)
                ?: throw IllegalStateException(
                    "Could not save loyalty transaction"
                )
        }

        // Update customer account balance and lifetime earnings.
        accountDao.addEarnedPoints(
            accountId = accountId,
            points = points,
            spendMinor = spendMinor,
            updatedAt = now
        )

        // Create a separate point lot for expiry tracking.
        val expiresAt = if (program.pointsExpiryDays > 0) {
            now + program.pointsExpiryDays * 24L * 60L * 60L * 1000L
        } else {
            null
        }

        val pointLot = LoyaltyPointLotEntity(
            id = UUID.randomUUID().toString(),
            accountId = accountId,
            transactionId = transactionId,
            originalPoints = points,
            remainingPoints = points,
            earnedAt = now,
            expiresAt = expiresAt,
            status = LoyaltyPointLotStatus.ACTIVE,
            createdAt = now
        )

        pointLotDao.insert(pointLot)

        loyaltyTransaction
    }


    // ============================================================
    // 4. REDEEM LOYALTY POINTS
    // ============================================================

    suspend fun redeemPoints(
        accountId: String,
        points: Int,
        idempotencyKey: String,
        orderId: String? = null,
        orderNo: String? = null,
        outletId: String? = null,
        businessDate: String? = null,
        createdBy: String? = null,
        description: String? = null
    ): LoyaltyTransactionEntity = db.withTransaction {

        require(points > 0) {
            "Redemption points must be greater than zero"
        }

        require(idempotencyKey.isNotBlank()) {
            "Idempotency key is required"
        }

        // Prevent duplicate redemption.
        val existing = transactionDao.getByIdempotencyKey(
            idempotencyKey
        )

        if (existing != null) {
            return@withTransaction existing
        }

        val account = accountDao.getById(accountId)
            ?: throw IllegalStateException("Loyalty account not found")

        require(account.isActive) {
            "Loyalty account is inactive"
        }

        val program = programDao.getById(account.programId)
            ?: throw IllegalStateException("Loyalty programme not found")

        require(points >= program.minRedeemPoints) {
            "Minimum redemption is ${program.minRedeemPoints} points"
        }

        require(account.balancePoints >= points) {
            "Insufficient loyalty points"
        }

        val now = System.currentTimeMillis()

        // Get available lots, ordered by expiry date.
        val availableLots = pointLotDao.getAvailableLots(
            accountId = accountId,
            now = now
        )

        val availablePoints = availableLots.sumOf {
            it.remainingPoints
        }

        require(availablePoints >= points) {
            "Available point lots do not cover the account balance"
        }

        val transactionId = UUID.randomUUID().toString()

        val loyaltyTransaction = LoyaltyTransactionEntity(
            id = transactionId,
            ownerId = account.ownerId,
            accountId = accountId,
            transactionType = LoyaltyTransactionType.REDEEM,
            points = -points,
            balanceAfter = account.balancePoints - points,
            orderId = orderId,
            orderNo = orderNo,
            outletId = outletId,
            idempotencyKey = idempotencyKey,
            description = description ?: "Points redeemed",
            businessDate = businessDate,
            createdBy = createdBy,
            createdAt = now
        )

        val inserted = transactionDao.insert(loyaltyTransaction)

        if (inserted == -1L) {
            return@withTransaction transactionDao
                .getByIdempotencyKey(idempotencyKey)
                ?: throw IllegalStateException(
                    "Could not save redemption transaction"
                )
        }

        // Atomically deduct account balance.
        val updated = accountDao.redeemPoints(
            accountId = accountId,
            points = points,
            updatedAt = now
        )

        require(updated > 0) {
            "Point redemption failed. Please check the balance."
        }

        // Allocate points from the earliest-expiring lots first.
        var remainingToRedeem = points

        val allocations = mutableListOf<LoyaltyPointAllocationEntity>()

        for (lot in availableLots) {

            if (remainingToRedeem <= 0) break

            val used = minOf(
                lot.remainingPoints,
                remainingToRedeem
            )

            val newRemaining = lot.remainingPoints - used

            val newStatus = if (newRemaining == 0) {
                LoyaltyPointLotStatus.USED
            } else {
                LoyaltyPointLotStatus.PARTIALLY_USED
            }

            pointLotDao.updateRemaining(
                id = lot.id,
                remainingPoints = newRemaining,
                status = newStatus
            )

            allocations.add(
                LoyaltyPointAllocationEntity(
                    id = UUID.randomUUID().toString(),
                    redemptionTransactionId = transactionId,
                    pointLotId = lot.id,
                    pointsUsed = used,
                    createdAt = now
                )
            )

            remainingToRedeem -= used
        }

        require(remainingToRedeem == 0) {
            "Could not allocate all redeemed points"
        }

        allocationDao.insertAll(allocations)

        loyaltyTransaction
    }


    // ============================================================
    // 5. CUSTOMER TRANSACTION HISTORY
    // ============================================================

    fun observeTransactions(
        accountId: String
    ): Flow<List<LoyaltyTransactionEntity>> {

        return transactionDao.getByAccount(accountId)
    }


    suspend fun getRecentTransactions(
        accountId: String,
        limit: Int = 20
    ): List<LoyaltyTransactionEntity> {

        return transactionDao.getRecent(
            accountId = accountId,
            limit = limit
        )
    }


    suspend fun getTransactionsByOrder(
        orderId: String
    ): List<LoyaltyTransactionEntity> {

        return transactionDao.getByOrder(orderId)
    }


    // ============================================================
    // 6. CUSTOMER ACCOUNTS
    // ============================================================

    fun observeCustomerAccounts(
        customerId: String
    ): Flow<List<LoyaltyAccountEntity>> {

        return accountDao.getByCustomer(customerId)
    }


    suspend fun getCustomerAccounts(
        customerId: String
    ): List<LoyaltyAccountEntity> {

        return accountDao.getCustomerAccounts(customerId)
    }


    // ============================================================
    // 7. PROGRAMMES
    // ============================================================

    fun observePrograms(
        ownerId: String
    ): Flow<List<LoyaltyProgramEntity>> {

        return programDao.getByOwner(ownerId)
    }


    suspend fun getActivePrograms(
        ownerId: String
    ): List<LoyaltyProgramEntity> {

        return programDao.getActivePrograms(ownerId)
    }


    // ============================================================
    // 8. AVAILABLE POINT LOTS
    // ============================================================

    suspend fun getAvailablePoints(
        accountId: String
    ): Int {

        val account = accountDao.getById(accountId)
            ?: return 0

        return account.balancePoints
    }


    fun observePointLots(
        accountId: String
    ): Flow<List<LoyaltyPointLotEntity>> {

        return pointLotDao.observeLots(accountId)
    }


    // ============================================================
    // 9. REWARDS
    // ============================================================

    fun observeAvailableRewards(
        programId: String
    ): Flow<List<LoyaltyRewardEntity>> {

        return rewardDao.getAvailableRewards(
            programId = programId,
            now = System.currentTimeMillis()
        )
    }


    // ============================================================
    // 10. VOUCHERS
    // ============================================================

    suspend fun getVoucherByCode(
        code: String
    ): LoyaltyVoucherEntity? {

        return voucherDao.getByCode(code.trim())
    }


    fun observeActiveVouchers(
        accountId: String
    ): Flow<List<LoyaltyVoucherEntity>> {

        return voucherDao.getActiveByAccount(
            accountId = accountId,
            now = System.currentTimeMillis()
        )
    }


    // ============================================================
    // 11. CAMPAIGNS
    // ============================================================

    suspend fun getActiveCampaigns(
        programId: String,
        outletId: String
    ): List<LoyaltyCampaignEntity> {

        return campaignDao.getActiveCampaigns(
            programId = programId,
            outletId = outletId,
            now = System.currentTimeMillis()
        )
    }


    // ============================================================
    // 12. TIERS
    // ============================================================

    suspend fun getActiveTiers(
        programId: String
    ): List<LoyaltyTierEntity> {

        return tierDao.getActiveTiers(programId)
    }


    // ============================================================
// CREATE DEFAULT LOYALTY PROGRAM IF IT DOES NOT EXIST
// ============================================================

    suspend fun ensureDefaultProgram(
        ownerId: String,
        outletId: String
    ): LoyaltyProgramEntity {

        return db.withTransaction {

            // Look for an existing Default Loyalty programme.
            val existingPrograms = programDao.getByOwner(ownerId).first()

            var program = existingPrograms.firstOrNull {
                it.programName == "Default Loyalty"
            }

            // Create the programme only if it does not already exist.
            if (program == null) {

                val defaultProgramId = "DEFAULT_LOYALTY_PROGRAM"

                // Avoid overwriting a programme belonging to another owner.
                val existingId = programDao.getById(defaultProgramId)

                val newProgramId = if (
                    existingId == null || existingId.ownerId == ownerId
                ) {
                    defaultProgramId
                } else {
                    UUID.randomUUID().toString()
                }

                program = LoyaltyProgramEntity(
                    id = newProgramId,
                    ownerId = ownerId,
                    programName = "Default Loyalty",
                    scope = LoyaltyProgramScope.SHARED
                )

                programDao.insert(program)
            }

            // SHARED programmes must be linked to the outlet.
            val isLinked = programOutletDao.isOutletLinked(
                programId = program.id,
                outletId = outletId
            )

            if (!isLinked) {

                val mapping = LoyaltyProgramOutletEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    programId = program.id,
                    outletId = outletId,
                    isActive = true
                )

                programOutletDao.insert(mapping)
            }

            program
        }
    }



}
