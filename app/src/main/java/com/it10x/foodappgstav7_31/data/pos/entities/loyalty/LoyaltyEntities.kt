
package com.it10x.foodappgstav7_31.data.pos.entities.loyalty

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.it10x.foodappgstav7_31.data.pos.entities.PosCustomerEntity


// ============================================================
// 1. LOYALTY PROGRAM
// ============================================================

@Entity(
    tableName = "loyalty_programs",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["isActive"]),
        Index(value = ["ownerId", "programName"])
    ]
)
data class LoyaltyProgramEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var programName: String = "Default Loyalty",

    var description: String? = null,

    var currencyCode: String = "EUR",

    // SHARED = all participating outlets
    // OUTLET = individual outlet programme
    var scope: LoyaltyProgramScope = LoyaltyProgramScope.SHARED,

    // Earn rules
    var earnSpendMinor: Long = 10000L,
    var earnPoints: Int = 100,

    // Redemption rules
    var redeemPoints: Int = 100,
    var redeemValueMinor: Long = 100L,

    var minRedeemPoints: Int = 100,
    var maxRedeemPercentBps: Int = 10000,

    var pointsExpiryDays: Int = 365,

    var allowPartialRedemption: Boolean = true,

    var allowEarnOnDiscountedBills: Boolean = false,

    var allowEarnOnRedeemedAmount: Boolean = false,

    var isActive: Boolean = true,

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long? = null
)


// ============================================================
// 2. PROGRAM OUTLET MAPPING
// ============================================================

@Entity(
    tableName = "loyalty_program_outlets",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(
            value = ["programId", "outletId"],
            unique = true
        ),
        Index(value = ["outletId"])
    ]
)
data class LoyaltyProgramOutletEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var programId: String = "",

    var outletId: String = "",

    var isActive: Boolean = true,

    var createdAt: Long = System.currentTimeMillis()
)


// ============================================================
// 3. LOYALTY TIER
// ============================================================

@Entity(
    tableName = "loyalty_tiers",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["programId"]),
        Index(
            value = ["programId", "tierName"],
            unique = true
        )
    ]
)
data class LoyaltyTierEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var programId: String = "",

    var tierName: String = "",

    var description: String? = null,

    var minLifetimeSpendMinor: Long = 0L,

    var minLifetimeEarnedPoints: Int = 0,

    // 10000 = 1x multiplier
    // 15000 = 1.5x multiplier
    var earnMultiplierBps: Int = 10000,

    var bonusPoints: Int = 0,

    // 100 = 1% discount
    var discountPercentBps: Int = 0,

    var priority: Int = 0,

    var isActive: Boolean = true,

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long? = null
)


// ============================================================
// 4. LOYALTY ACCOUNT
// ============================================================

@Entity(
    tableName = "loyalty_accounts",

    foreignKeys = [
        ForeignKey(
            entity = PosCustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyTierEntity::class,
            parentColumns = ["id"],
            childColumns = ["tierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],

    indices = [
        Index(value = ["customerId"]),
        Index(value = ["programId"]),
        Index(value = ["ownerId"]),
        Index(value = ["tierId"]),
        Index(
            value = ["customerId", "programId", "scopeKey"],
            unique = true
        )
    ]
)
data class LoyaltyAccountEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var customerId: String = "",

    var programId: String = "",

    var tierId: String? = null,

    // SHARED account: scopeKey = "SHARED"
    // OUTLET account: scopeKey = outletId
    var scopeKey: String = "SHARED",

    // Null for shared account
    var outletId: String? = null,

    var balancePoints: Int = 0,

    var lifetimeEarned: Int = 0,

    var lifetimeRedeemed: Int = 0,

    var lifetimeExpired: Int = 0,

    var lifetimeAdjusted: Int = 0,

    var lifetimeSpendMinor: Long = 0L,

    var joinedAt: Long = System.currentTimeMillis(),

    var lastActivityAt: Long? = null,

    var isActive: Boolean = true,

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long? = null
)


// ============================================================
// 5. LOYALTY TRANSACTION
// ============================================================

@Entity(
    tableName = "loyalty_transactions",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["referenceTransactionId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyCampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],

    indices = [
        Index(value = ["accountId", "createdAt"]),
        Index(value = ["orderId"]),
        Index(value = ["outletId"]),
        Index(value = ["businessDate"]),
        Index(value = ["campaignId"]),
        Index(value = ["idempotencyKey"], unique = true),
        Index(value = ["referenceTransactionId"])
    ]
)
data class LoyaltyTransactionEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var accountId: String = "",

    var transactionType: LoyaltyTransactionType =
        LoyaltyTransactionType.EARN,

    // Positive = points added
    // Negative = points deducted
    var points: Int = 0,

    var balanceAfter: Int = 0,

    var orderId: String? = null,

    var orderNo: String? = null,

    var outletId: String? = null,

    var referenceTransactionId: String? = null,

    var campaignId: String? = null,

    var rewardRedemptionId: String? = null,

    // Prevent duplicate earning/redemption
    var idempotencyKey: String? = null,

    var description: String? = null,

    var businessDate: String? = null,

    var createdBy: String? = null,

    var createdAt: Long = System.currentTimeMillis()
)


// ============================================================
// 6. LOYALTY POINT LOT
// ============================================================

@Entity(
    tableName = "loyalty_point_lots",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["accountId", "status"]),
        Index(value = ["transactionId"], unique = true),
        Index(value = ["expiresAt", "status"])
    ]
)
data class LoyaltyPointLotEntity(

    @PrimaryKey
    var id: String = "",

    var accountId: String = "",

    var transactionId: String = "",

    var originalPoints: Int = 0,

    var remainingPoints: Int = 0,

    var earnedAt: Long = System.currentTimeMillis(),

    var expiresAt: Long? = null,

    var status: LoyaltyPointLotStatus =
        LoyaltyPointLotStatus.ACTIVE,

    var createdAt: Long = System.currentTimeMillis()
)


// ============================================================
// 7. LOYALTY POINT ALLOCATION
// ============================================================

@Entity(
    tableName = "loyalty_point_allocations",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["redemptionTransactionId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyPointLotEntity::class,
            parentColumns = ["id"],
            childColumns = ["pointLotId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["redemptionTransactionId", "pointLotId"], unique = true),
        Index(value = ["pointLotId"])
    ]
)
data class LoyaltyPointAllocationEntity(

    @PrimaryKey
    var id: String = "",

    var redemptionTransactionId: String = "",

    var pointLotId: String = "",

    var pointsUsed: Int = 0,

    var createdAt: Long = System.currentTimeMillis()
)


// ============================================================
// 8. LOYALTY REWARD
// ============================================================

@Entity(
    tableName = "loyalty_rewards",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["programId", "isActive"]),
        Index(value = ["productId"])
    ]
)
data class LoyaltyRewardEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var programId: String = "",

    var rewardName: String = "",

    var description: String? = null,

    var rewardType: LoyaltyRewardType =
        LoyaltyRewardType.DISCOUNT,

    var pointsRequired: Int = 0,

    // Existing POS product ID, if applicable
    var productId: String? = null,

    var rewardValueMinor: Long = 0L,

    var stockQuantity: Int? = null,

    var maxPerCustomer: Int? = null,

    var validFrom: Long? = null,

    var validUntil: Long? = null,

    var isActive: Boolean = true,

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long? = null
)


// ============================================================
// 9. LOYALTY REWARD REDEMPTION
// ============================================================

@Entity(
    tableName = "loyalty_reward_redemptions",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyRewardEntity::class,
            parentColumns = ["id"],
            childColumns = ["rewardId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["accountId", "status"]),
        Index(value = ["rewardId"]),
        Index(value = ["transactionId"], unique = true)
    ]
)
data class LoyaltyRewardRedemptionEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var accountId: String = "",

    var rewardId: String = "",

    var transactionId: String? = null,

    // Voucher ID is linked after voucher creation
    var voucherId: String? = null,

    var pointsSpent: Int = 0,

    var status: LoyaltyRewardStatus =
        LoyaltyRewardStatus.PENDING,

    var redeemedAt: Long = System.currentTimeMillis(),

    var usedAt: Long? = null,

    var expiresAt: Long? = null,

    var outletId: String? = null,

    var orderId: String? = null,

    var createdBy: String? = null,

    var createdAt: Long = System.currentTimeMillis()
)


// ============================================================
// 10. LOYALTY VOUCHER
// ============================================================

@Entity(
    tableName = "loyalty_vouchers",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyRewardRedemptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["rewardRedemptionId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyCampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["voucherCode"], unique = true),
        Index(value = ["accountId"]),
        Index(value = ["rewardRedemptionId"]),
        Index(value = ["campaignId"])
    ]
)
data class LoyaltyVoucherEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var accountId: String? = null,

    var rewardRedemptionId: String? = null,

    var campaignId: String? = null,

    var voucherCode: String = "",

    var voucherType: LoyaltyVoucherType =
        LoyaltyVoucherType.PERCENT,

    // Percentage: 1000 = 10%
    var discountPercentBps: Int = 0,

    var discountAmountMinor: Long = 0L,

    var maxDiscountMinor: Long? = null,

    var minOrderAmountMinor: Long = 0L,

    var usageLimit: Int = 1,

    var usedCount: Int = 0,

    var validFrom: Long? = null,

    var validUntil: Long? = null,

    var status: LoyaltyVoucherStatus =
        LoyaltyVoucherStatus.ACTIVE,

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long? = null
)


// ============================================================
// 11. LOYALTY VOUCHER USAGE
// ============================================================

@Entity(
    tableName = "loyalty_voucher_usage",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyVoucherEntity::class,
            parentColumns = ["id"],
            childColumns = ["voucherId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["voucherId", "orderId"], unique = true),
        Index(value = ["accountId"]),
        Index(value = ["orderId"])
    ]
)
data class LoyaltyVoucherUsageEntity(

    @PrimaryKey
    var id: String = "",

    var voucherId: String = "",

    var accountId: String? = null,

    var orderId: String = "",

    var orderNo: String? = null,

    var outletId: String? = null,

    var discountAmountMinor: Long = 0L,

    var usedAt: Long = System.currentTimeMillis(),

    var createdBy: String? = null
)


// ============================================================
// 12. LOYALTY CAMPAIGN
// ============================================================

@Entity(
    tableName = "loyalty_campaigns",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["programId", "isActive"]),
        Index(value = ["startDate", "endDate"]),
        Index(value = ["outletId"])
    ]
)
data class LoyaltyCampaignEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var programId: String = "",

    var campaignName: String = "",

    var description: String? = null,

    var campaignType: LoyaltyCampaignType =
        LoyaltyCampaignType.DOUBLE_POINTS,

    // 20000 = 2x points
    var earnMultiplierBps: Int = 10000,

    var bonusPoints: Int = 0,

    var minOrderAmountMinor: Long = 0L,

    var maxBonusPoints: Int? = null,

    // Null outletId = all participating outlets
    var outletId: String? = null,

    var startDate: Long = 0L,

    var endDate: Long = 0L,

    var usageLimit: Int? = null,

    var usedCount: Int = 0,

    var isActive: Boolean = true,

    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long? = null
)


// ============================================================
// 13. LOYALTY CAMPAIGN USAGE
// ============================================================

@Entity(
    tableName = "loyalty_campaign_usage",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyCampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["campaignId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["campaignId", "accountId", "orderId"], unique = true),
        Index(value = ["accountId"])
    ]
)
data class LoyaltyCampaignUsageEntity(

    @PrimaryKey
    var id: String = "",

    var campaignId: String = "",

    var accountId: String = "",

    var orderId: String = "",

    var pointsAwarded: Int = 0,

    var usedAt: Long = System.currentTimeMillis()
)


// ============================================================
// 14. LOYALTY REFERRAL
// ============================================================

@Entity(
    tableName = "loyalty_referrals",

    foreignKeys = [
        ForeignKey(
            entity = LoyaltyProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["referrerAccountId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = PosCustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["referredCustomerId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["referrerTransactionId"],
            onDelete = ForeignKey.RESTRICT
        ),

        ForeignKey(
            entity = LoyaltyTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["referredTransactionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],

    indices = [
        Index(value = ["referralCode"], unique = true),
        Index(value = ["referrerAccountId"]),
        Index(value = ["referredCustomerId"]),
        Index(value = ["programId", "referredCustomerId"], unique = true)
    ]
)
data class LoyaltyReferralEntity(

    @PrimaryKey
    var id: String = "",

    var ownerId: String = "",

    var programId: String = "",

    var referrerAccountId: String = "",

    var referredCustomerId: String = "",

    var referralCode: String = "",

    var referralStatus: LoyaltyReferralStatus =
        LoyaltyReferralStatus.PENDING,

    var referrerBonusPoints: Int = 0,

    var referredBonusPoints: Int = 0,

    var referrerTransactionId: String? = null,

    var referredTransactionId: String? = null,

    var completedOrderId: String? = null,

    var referredAt: Long = System.currentTimeMillis(),

    var completedAt: Long? = null,

    var createdAt: Long = System.currentTimeMillis(),
    var syncStatus: String = "PENDING",
    var lastSyncedAt: Long? = null
)
