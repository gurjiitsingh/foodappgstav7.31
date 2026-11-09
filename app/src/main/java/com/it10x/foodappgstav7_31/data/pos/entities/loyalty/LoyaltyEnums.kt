
package com.it10x.foodappgstav7_31.data.pos.entities.loyalty

enum class LoyaltyProgramScope {
    SHARED,
    OUTLET
}

enum class LoyaltyTransactionType {
    EARN,
    REDEEM,
    EXPIRE,
    ADJUST,
    REVERSAL,
    REFERRAL,
    BONUS
}

enum class LoyaltyPointLotStatus {
    ACTIVE,
    PARTIALLY_USED,
    USED,
    EXPIRED,
    REVERSED
}

enum class LoyaltyRewardType {
    DISCOUNT,
    FREE_PRODUCT,
    FREE_ITEM,
    FIXED_VALUE,
    CUSTOM
}

enum class LoyaltyRewardStatus {
    PENDING,
    ISSUED,
    USED,
    EXPIRED,
    CANCELLED
}

enum class LoyaltyVoucherType {
    PERCENT,
    FIXED,
    FREE_ITEM
}

enum class LoyaltyVoucherStatus {
    ACTIVE,
    USED,
    EXPIRED,
    CANCELLED
}

enum class LoyaltyCampaignType {
    DOUBLE_POINTS,
    BONUS_POINTS,
    MULTIPLIER,
    SPECIAL
}

enum class LoyaltyReferralStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
