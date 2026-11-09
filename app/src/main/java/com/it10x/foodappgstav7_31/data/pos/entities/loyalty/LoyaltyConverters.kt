
package com.it10x.foodappgstav7_31.data.pos.entities.loyalty

import androidx.room.TypeConverter

class LoyaltyConverters {

    @TypeConverter
    fun programScopeToString(value: LoyaltyProgramScope?): String? =
        value?.name

    @TypeConverter
    fun stringToProgramScope(value: String?): LoyaltyProgramScope? =
        value?.let { LoyaltyProgramScope.valueOf(it) }


    @TypeConverter
    fun transactionTypeToString(value: LoyaltyTransactionType?): String? =
        value?.name

    @TypeConverter
    fun stringToTransactionType(value: String?): LoyaltyTransactionType? =
        value?.let { LoyaltyTransactionType.valueOf(it) }


    @TypeConverter
    fun pointLotStatusToString(value: LoyaltyPointLotStatus?): String? =
        value?.name

    @TypeConverter
    fun stringToPointLotStatus(value: String?): LoyaltyPointLotStatus? =
        value?.let { LoyaltyPointLotStatus.valueOf(it) }


    @TypeConverter
    fun rewardTypeToString(value: LoyaltyRewardType?): String? =
        value?.name

    @TypeConverter
    fun stringToRewardType(value: String?): LoyaltyRewardType? =
        value?.let { LoyaltyRewardType.valueOf(it) }


    @TypeConverter
    fun rewardStatusToString(value: LoyaltyRewardStatus?): String? =
        value?.name

    @TypeConverter
    fun stringToRewardStatus(value: String?): LoyaltyRewardStatus? =
        value?.let { LoyaltyRewardStatus.valueOf(it) }


    @TypeConverter
    fun voucherTypeToString(value: LoyaltyVoucherType?): String? =
        value?.name

    @TypeConverter
    fun stringToVoucherType(value: String?): LoyaltyVoucherType? =
        value?.let { LoyaltyVoucherType.valueOf(it) }


    @TypeConverter
    fun voucherStatusToString(value: LoyaltyVoucherStatus?): String? =
        value?.name

    @TypeConverter
    fun stringToVoucherStatus(value: String?): LoyaltyVoucherStatus? =
        value?.let { LoyaltyVoucherStatus.valueOf(it) }


    @TypeConverter
    fun campaignTypeToString(value: LoyaltyCampaignType?): String? =
        value?.name

    @TypeConverter
    fun stringToCampaignType(value: String?): LoyaltyCampaignType? =
        value?.let { LoyaltyCampaignType.valueOf(it) }


    @TypeConverter
    fun referralStatusToString(value: LoyaltyReferralStatus?): String? =
        value?.name

    @TypeConverter
    fun stringToReferralStatus(value: String?): LoyaltyReferralStatus? =
        value?.let { LoyaltyReferralStatus.valueOf(it) }
}
