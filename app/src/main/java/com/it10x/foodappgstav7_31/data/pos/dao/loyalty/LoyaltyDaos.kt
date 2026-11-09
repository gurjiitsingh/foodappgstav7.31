
package com.it10x.foodappgstav7_31.data.pos.dao.loyalty

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.*
import kotlinx.coroutines.flow.Flow

// ============================================================
// 1. LOYALTY PROGRAM DAO
// ============================================================

@Dao
interface LoyaltyProgramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(program: LoyaltyProgramEntity)

    @Update
    suspend fun update(program: LoyaltyProgramEntity)

    @Query("SELECT * FROM loyalty_programs WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoyaltyProgramEntity?

    @Query("""
        SELECT * FROM loyalty_programs
        WHERE ownerId = :ownerId
        ORDER BY createdAt DESC
    """)
    fun getByOwner(ownerId: String): Flow<List<LoyaltyProgramEntity>>

    @Query("""
        SELECT * FROM loyalty_programs
        WHERE ownerId = :ownerId AND isActive = 1
        ORDER BY createdAt DESC
    """)
    suspend fun getActivePrograms(ownerId: String): List<LoyaltyProgramEntity>

    @Query("DELETE FROM loyalty_programs WHERE id = :id")
    suspend fun deleteById(id: String)
}


// ============================================================
// 2. LOYALTY PROGRAM OUTLET DAO
// ============================================================

@Dao
interface LoyaltyProgramOutletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: LoyaltyProgramOutletEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mappings: List<LoyaltyProgramOutletEntity>)

    @Update
    suspend fun update(mapping: LoyaltyProgramOutletEntity)

    @Query("""
        SELECT * FROM loyalty_program_outlets
        WHERE programId = :programId
        ORDER BY createdAt DESC
    """)
    fun getByProgram(programId: String): Flow<List<LoyaltyProgramOutletEntity>>

    @Query("""
        SELECT * FROM loyalty_program_outlets
        WHERE outletId = :outletId AND isActive = 1
    """)
    suspend fun getActiveByOutlet(
        outletId: String
    ): List<LoyaltyProgramOutletEntity>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM loyalty_program_outlets
            WHERE programId = :programId
            AND outletId = :outletId
            AND isActive = 1
        )
    """)
    suspend fun isOutletLinked(
        programId: String,
        outletId: String
    ): Boolean
}


// ============================================================
// 3. LOYALTY TIER DAO
// ============================================================

@Dao
interface LoyaltyTierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tier: LoyaltyTierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tiers: List<LoyaltyTierEntity>)

    @Update
    suspend fun update(tier: LoyaltyTierEntity)

    @Query("SELECT * FROM loyalty_tiers WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoyaltyTierEntity?

    @Query("""
        SELECT * FROM loyalty_tiers
        WHERE programId = :programId AND isActive = 1
        ORDER BY priority DESC, minLifetimeSpendMinor ASC
    """)
    fun getActiveByProgram(
        programId: String
    ): Flow<List<LoyaltyTierEntity>>

    @Query("""
        SELECT * FROM loyalty_tiers
        WHERE programId = :programId AND isActive = 1
        ORDER BY priority DESC, minLifetimeSpendMinor ASC
    """)
    suspend fun getActiveTiers(
        programId: String
    ): List<LoyaltyTierEntity>

    @Query("DELETE FROM loyalty_tiers WHERE id = :id")
    suspend fun deleteById(id: String)
}


// ============================================================
// 4. LOYALTY ACCOUNT DAO
// ============================================================

@Dao
interface LoyaltyAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: LoyaltyAccountEntity)

    @Update
    suspend fun update(account: LoyaltyAccountEntity)

    @Query("SELECT * FROM loyalty_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoyaltyAccountEntity?

    @Query("""
        SELECT * FROM loyalty_accounts
        WHERE customerId = :customerId
        AND programId = :programId
        AND scopeKey = :scopeKey
        LIMIT 1
    """)
    suspend fun getAccount(
        customerId: String,
        programId: String,
        scopeKey: String
    ): LoyaltyAccountEntity?

    @Query("""
        SELECT * FROM loyalty_accounts
        WHERE customerId = :customerId
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getByCustomer(
        customerId: String
    ): Flow<List<LoyaltyAccountEntity>>

    @Query("""
        SELECT * FROM loyalty_accounts
        WHERE ownerId = :ownerId
        AND outletId = :outletId
        AND isActive = 1
    """)
    fun getByOutlet(
        ownerId: String,
        outletId: String
    ): Flow<List<LoyaltyAccountEntity>>


    @Query("""
    SELECT * FROM loyalty_accounts
    WHERE customerId = :customerId
    AND isActive = 1
    ORDER BY createdAt DESC
""")
    suspend fun getCustomerAccounts(
        customerId: String
    ): List<LoyaltyAccountEntity>

    @Query("""
        UPDATE loyalty_accounts
        SET balancePoints = :balance,
            updatedAt = :updatedAt,
            lastActivityAt = :updatedAt
        WHERE id = :accountId
    """)
    suspend fun updateBalance(
        accountId: String,
        balance: Int,
        updatedAt: Long
    )

    @Query("""
        UPDATE loyalty_accounts
        SET balancePoints = balancePoints + :points,
            lifetimeEarned = lifetimeEarned + :points,
            lifetimeSpendMinor = lifetimeSpendMinor + :spendMinor,
            updatedAt = :updatedAt,
            lastActivityAt = :updatedAt
        WHERE id = :accountId
    """)
    suspend fun addEarnedPoints(
        accountId: String,
        points: Int,
        spendMinor: Long,
        updatedAt: Long
    )

    @Query("""
        UPDATE loyalty_accounts
        SET balancePoints = balancePoints - :points,
            lifetimeRedeemed = lifetimeRedeemed + :points,
            updatedAt = :updatedAt,
            lastActivityAt = :updatedAt
        WHERE id = :accountId
        AND balancePoints >= :points
    """)
    suspend fun redeemPoints(
        accountId: String,
        points: Int,
        updatedAt: Long
    ): Int

    @Query("""
        SELECT balancePoints FROM loyalty_accounts
        WHERE id = :accountId LIMIT 1
    """)
    suspend fun getBalance(accountId: String): Int?

    @Query("DELETE FROM loyalty_accounts WHERE id = :id")
    suspend fun deleteById(id: String)
}


// ============================================================
// 5. LOYALTY TRANSACTION DAO
// ============================================================

@Dao
interface LoyaltyTransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: LoyaltyTransactionEntity): Long

    @Update
    suspend fun update(transaction: LoyaltyTransactionEntity)

    @Query("SELECT * FROM loyalty_transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoyaltyTransactionEntity?

    @Query("""
        SELECT * FROM loyalty_transactions
        WHERE accountId = :accountId
        ORDER BY createdAt DESC
    """)
    fun getByAccount(
        accountId: String
    ): Flow<List<LoyaltyTransactionEntity>>

    @Query("""
        SELECT * FROM loyalty_transactions
        WHERE accountId = :accountId
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    suspend fun getRecent(
        accountId: String,
        limit: Int
    ): List<LoyaltyTransactionEntity>

    @Query("""
        SELECT * FROM loyalty_transactions
        WHERE idempotencyKey = :key
        LIMIT 1
    """)
    suspend fun getByIdempotencyKey(
        key: String
    ): LoyaltyTransactionEntity?

    @Query("""
        SELECT * FROM loyalty_transactions
        WHERE orderId = :orderId
        ORDER BY createdAt DESC
    """)
    suspend fun getByOrder(
        orderId: String
    ): List<LoyaltyTransactionEntity>
}


// ============================================================
// 6. LOYALTY POINT LOT DAO
// ============================================================

@Dao
interface LoyaltyPointLotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lot: LoyaltyPointLotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lots: List<LoyaltyPointLotEntity>)

    @Update
    suspend fun update(lot: LoyaltyPointLotEntity)

    @Query("SELECT * FROM loyalty_point_lots WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoyaltyPointLotEntity?

    // FIFO: use the oldest points first.
    @Query("""
        SELECT * FROM loyalty_point_lots
        WHERE accountId = :accountId
        AND remainingPoints > 0
        AND status IN ('ACTIVE', 'PARTIALLY_USED')
        AND (expiresAt IS NULL OR expiresAt > :now)
        ORDER BY
            CASE WHEN expiresAt IS NULL THEN 1 ELSE 0 END,
            expiresAt ASC,
            earnedAt ASC
    """)
    suspend fun getAvailableLots(
        accountId: String,
        now: Long
    ): List<LoyaltyPointLotEntity>

    @Query("""
        SELECT * FROM loyalty_point_lots
        WHERE accountId = :accountId
        AND remainingPoints > 0
        ORDER BY earnedAt ASC
    """)
    fun observeLots(
        accountId: String
    ): Flow<List<LoyaltyPointLotEntity>>

    @Query("""
        UPDATE loyalty_point_lots
        SET remainingPoints = :remainingPoints,
            status = :status
        WHERE id = :id
    """)
    suspend fun updateRemaining(
        id: String,
        remainingPoints: Int,
        status: LoyaltyPointLotStatus
    )

    @Query("""
        SELECT * FROM loyalty_point_lots
        WHERE remainingPoints > 0
        AND expiresAt IS NOT NULL
        AND expiresAt <= :now
        AND status IN ('ACTIVE', 'PARTIALLY_USED')
    """)
    suspend fun getExpiredLots(
        now: Long
    ): List<LoyaltyPointLotEntity>
}


// ============================================================
// 7. LOYALTY POINT ALLOCATION DAO
// ============================================================

@Dao
interface LoyaltyPointAllocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(allocation: LoyaltyPointAllocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        allocations: List<LoyaltyPointAllocationEntity>
    )

    @Query("""
        SELECT * FROM loyalty_point_allocations
        WHERE redemptionTransactionId = :transactionId
    """)
    suspend fun getByRedemption(
        transactionId: String
    ): List<LoyaltyPointAllocationEntity>

    @Query("""
        SELECT * FROM loyalty_point_allocations
        WHERE pointLotId = :lotId
    """)
    suspend fun getByLot(
        lotId: String
    ): List<LoyaltyPointAllocationEntity>
}


// ============================================================
// 8. LOYALTY REWARD DAO
// ============================================================

@Dao
interface LoyaltyRewardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reward: LoyaltyRewardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rewards: List<LoyaltyRewardEntity>)

    @Update
    suspend fun update(reward: LoyaltyRewardEntity)

    @Query("SELECT * FROM loyalty_rewards WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoyaltyRewardEntity?

    @Query("""
        SELECT * FROM loyalty_rewards
        WHERE programId = :programId
        AND isActive = 1
        AND (validFrom IS NULL OR validFrom <= :now)
        AND (validUntil IS NULL OR validUntil >= :now)
        ORDER BY pointsRequired ASC
    """)
    fun getAvailableRewards(
        programId: String,
        now: Long
    ): Flow<List<LoyaltyRewardEntity>>

    @Query("DELETE FROM loyalty_rewards WHERE id = :id")
    suspend fun deleteById(id: String)
}


// ============================================================
// 9. LOYALTY REWARD REDEMPTION DAO
// ============================================================

@Dao
interface LoyaltyRewardRedemptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(redemption: LoyaltyRewardRedemptionEntity)

    @Update
    suspend fun update(redemption: LoyaltyRewardRedemptionEntity)

    @Query("""
        SELECT * FROM loyalty_reward_redemptions
        WHERE id = :id LIMIT 1
    """)
    suspend fun getById(id: String): LoyaltyRewardRedemptionEntity?

    @Query("""
        SELECT * FROM loyalty_reward_redemptions
        WHERE accountId = :accountId
        ORDER BY redeemedAt DESC
    """)
    fun getByAccount(
        accountId: String
    ): Flow<List<LoyaltyRewardRedemptionEntity>>

    @Query("""
        SELECT * FROM loyalty_reward_redemptions
        WHERE accountId = :accountId
        AND status = :status
        ORDER BY redeemedAt DESC
    """)
    suspend fun getByStatus(
        accountId: String,
        status: LoyaltyRewardStatus
    ): List<LoyaltyRewardRedemptionEntity>
}


// ============================================================
// 10. LOYALTY VOUCHER DAO
// ============================================================

@Dao
interface LoyaltyVoucherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voucher: LoyaltyVoucherEntity)

    @Update
    suspend fun update(voucher: LoyaltyVoucherEntity)

    @Query("""
        SELECT * FROM loyalty_vouchers
        WHERE id = :id LIMIT 1
    """)
    suspend fun getById(id: String): LoyaltyVoucherEntity?

    @Query("""
        SELECT * FROM loyalty_vouchers
        WHERE voucherCode = :code LIMIT 1
    """)
    suspend fun getByCode(
        code: String
    ): LoyaltyVoucherEntity?

    @Query("""
        SELECT * FROM loyalty_vouchers
        WHERE accountId = :accountId
        AND status = 'ACTIVE'
        AND (validFrom IS NULL OR validFrom <= :now)
        AND (validUntil IS NULL OR validUntil >= :now)
        ORDER BY createdAt DESC
    """)
    fun getActiveByAccount(
        accountId: String,
        now: Long
    ): Flow<List<LoyaltyVoucherEntity>>

    @Query("""
        UPDATE loyalty_vouchers
        SET usedCount = usedCount + 1,
            status = CASE
                WHEN usedCount + 1 >= usageLimit THEN 'USED'
                ELSE status
            END,
            updatedAt = :now
        WHERE id = :id
        AND status = 'ACTIVE'
        AND usedCount < usageLimit
        AND (validFrom IS NULL OR validFrom <= :now)
        AND (validUntil IS NULL OR validUntil >= :now)
    """)
    suspend fun incrementUsage(
        id: String,
        now: Long
    ): Int
}


// ============================================================
// 11. LOYALTY VOUCHER USAGE DAO
// ============================================================

@Dao
interface LoyaltyVoucherUsageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(usage: LoyaltyVoucherUsageEntity): Long

    @Query("""
        SELECT * FROM loyalty_voucher_usage
        WHERE voucherId = :voucherId
        ORDER BY usedAt DESC
    """)
    suspend fun getByVoucher(
        voucherId: String
    ): List<LoyaltyVoucherUsageEntity>

    @Query("""
        SELECT * FROM loyalty_voucher_usage
        WHERE orderId = :orderId
    """)
    suspend fun getByOrder(
        orderId: String
    ): List<LoyaltyVoucherUsageEntity>
}


// ============================================================
// 12. LOYALTY CAMPAIGN DAO
// ============================================================

@Dao
interface LoyaltyCampaignDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(campaign: LoyaltyCampaignEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(campaigns: List<LoyaltyCampaignEntity>)

    @Update
    suspend fun update(campaign: LoyaltyCampaignEntity)

    @Query("""
        SELECT * FROM loyalty_campaigns
        WHERE id = :id LIMIT 1
    """)
    suspend fun getById(id: String): LoyaltyCampaignEntity?

    @Query("""
        SELECT * FROM loyalty_campaigns
        WHERE programId = :programId
        AND isActive = 1
        AND startDate <= :now
        AND endDate >= :now
        AND (outletId IS NULL OR outletId = :outletId)
        ORDER BY createdAt DESC
    """)
    suspend fun getActiveCampaigns(
        programId: String,
        outletId: String,
        now: Long
    ): List<LoyaltyCampaignEntity>

    @Query("""
        SELECT * FROM loyalty_campaigns
        WHERE programId = :programId
        ORDER BY createdAt DESC
    """)
    fun getByProgram(
        programId: String
    ): Flow<List<LoyaltyCampaignEntity>>
}


// ============================================================
// 13. LOYALTY CAMPAIGN USAGE DAO
// ============================================================

@Dao
interface LoyaltyCampaignUsageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(usage: LoyaltyCampaignUsageEntity): Long

    @Query("""
        SELECT * FROM loyalty_campaign_usage
        WHERE campaignId = :campaignId
        AND accountId = :accountId
    """)
    suspend fun getByCampaignAndAccount(
        campaignId: String,
        accountId: String
    ): List<LoyaltyCampaignUsageEntity>

    @Query("""
        SELECT * FROM loyalty_campaign_usage
        WHERE campaignId = :campaignId
        AND orderId = :orderId
        LIMIT 1
    """)
    suspend fun getByCampaignAndOrder(
        campaignId: String,
        orderId: String
    ): LoyaltyCampaignUsageEntity?
}


// ============================================================
// 14. LOYALTY REFERRAL DAO
// ============================================================

@Dao
interface LoyaltyReferralDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(referral: LoyaltyReferralEntity): Long

    @Update
    suspend fun update(referral: LoyaltyReferralEntity)

    @Query("""
        SELECT * FROM loyalty_referrals
        WHERE id = :id LIMIT 1
    """)
    suspend fun getById(id: String): LoyaltyReferralEntity?

    @Query("""
        SELECT * FROM loyalty_referrals
        WHERE referralCode = :code LIMIT 1
    """)
    suspend fun getByReferralCode(
        code: String
    ): LoyaltyReferralEntity?

    @Query("""
        SELECT * FROM loyalty_referrals
        WHERE referrerAccountId = :accountId
        ORDER BY createdAt DESC
    """)
    fun getByReferrer(
        accountId: String
    ): Flow<List<LoyaltyReferralEntity>>

    @Query("""
        SELECT * FROM loyalty_referrals
        WHERE programId = :programId
        AND referredCustomerId = :customerId
        LIMIT 1
    """)
    suspend fun getByReferredCustomer(
        programId: String,
        customerId: String
    ): LoyaltyReferralEntity?
}
