package com.it10x.foodappgstav7_31.data.pos

import androidx.room.Database
import androidx.room.RoomDatabase
import com.it10x.foodappgstav7_31.data.inventory.dao.ProductRecipeDao
import com.it10x.foodappgstav7_31.data.online.sync.SyncQueueDao
import com.it10x.foodappgstav7_31.data.online.sync.SyncQueueEntity
import com.it10x.foodappgstav7_31.data.printqueue.PrintQueueEntity
import com.it10x.foodappgstav7_31.data.pos.dao.*
import com.it10x.foodappgstav7_31.data.pos.entities.*
import com.it10x.foodappgstav7_31.data.pos.entities.config.*
import com.it10x.foodappgstav7_31.data.printqueue.PrintQueueDao
import com.it10x.foodappgstav7_31.data.pos.entity.ProductRecipeEntity

import androidx.room.Entity
import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderDao
import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderEntity
import com.it10x.foodappgstav7_31.data.online.local.OnlineReceivedOrderProductEntity
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.*
import androidx.room.TypeConverters
import com.it10x.foodappgstav7_31.data.pos.entities.loyalty.LoyaltyConverters
import com.it10x.foodappgstav7_31.data.pos.dao.loyalty.*
@TypeConverters(

    LoyaltyConverters::class

)
@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        PosOrderMasterEntity::class,
        PosOrderItemEntity::class,
        PosCartEntity::class,
        OutletEntity::class,
        TableEntity::class,
        PosKotItemEntity::class,
        PosKotBatchEntity::class,
        OrderSequenceEntity::class,
        PosOrderPaymentEntity::class,
        PosCustomerEntity::class,
        PosCustomerLedgerEntity::class,
        ProcessedCloudOrderEntity::class,
        VirtualTableEntity:: class,
        VirtualTableCounterEntity::class,
        PrinterEntity::class,
        PosPreferenceEntity::class,
        PosDeviceEntity::class,
        PrintQueueEntity::class,
        SyncQueueEntity::class,
        ModifierGroupEntity::class,
        ModifierItemEntity::class,
        ProductModifierEntity::class,
        ProductRecipeEntity::class,
        InventorySyncEntity::class,
        PosKotHistoryEntity::class,
        PosUserEntity:: class,
        PosDayClosingEntity::class,
        PosBusinessDayEntity::class,
        OrderCounterEntity::class,
        OrderSerialMapEntity::class,
        PosRenewalEntity::class,
        OnlineReceivedOrderEntity::class,
        OnlineReceivedOrderProductEntity::class,


        LoyaltyProgramEntity::class,
        LoyaltyProgramOutletEntity::class,
        LoyaltyTierEntity::class,
        LoyaltyAccountEntity::class,
        LoyaltyTransactionEntity::class,
        LoyaltyPointLotEntity::class,
        LoyaltyPointAllocationEntity::class,
        LoyaltyRewardEntity::class,
        LoyaltyRewardRedemptionEntity::class,
        LoyaltyVoucherEntity::class,
        LoyaltyVoucherUsageEntity::class,
        LoyaltyCampaignEntity::class,
        LoyaltyCampaignUsageEntity::class,
        LoyaltyReferralEntity::class,
    ],
    version = 165  ,              // ⬆️ increment version since schema changed
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderMasterDao(): OrderMasterDao
    abstract fun orderProductDao(): OrderProductDao
    abstract fun outletDao(): OutletDao
    abstract fun cartDao(): CartDao
    abstract fun tableDao(): TableDao
        abstract fun kotBatchDao(): KotBatchDao
    abstract fun kotItemDao(): KotItemDao
    abstract fun orderSequenceDao(): OrderSequenceDao

    abstract fun salesMasterDao(): SalesMasterDao

    abstract fun posOrderPaymentDao(): PosOrderPaymentDao

    abstract fun posCustomerDao(): PosCustomerDao
    abstract fun posCustomerLedgerDao(): PosCustomerLedgerDao
    abstract fun processedCloudOrderDao(): ProcessedCloudOrderDao
    abstract fun virtualTableDao(): VirtualTableDao

    abstract fun virtualTableCounterDao(): VirtualTableCounterDao

    abstract fun printerDao(): PrinterDao

    abstract fun printQueueDao(): PrintQueueDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun modifierGroupDao(): ModifierGroupDao
    abstract fun modifierItemDao(): ModifierItemDao
    abstract fun productModifierDao(): ProductModifierDao

    abstract fun productRecipeDao(): ProductRecipeDao

    abstract fun inventorySyncDao(): InventorySyncDao

    abstract fun posUserDao(): PosUserDao

    abstract fun dayClosingDao(): DayClosingDao
    abstract fun businessDayDao(): BusinessDayDao

    abstract fun orderCounterDao(): OrderCounterDao

    abstract fun orderSerialMapDao(): OrderSerialMapDao

    abstract fun posRenewalDao(): PosRenewalDao

    abstract fun onlineReceivedOrderDao(): OnlineReceivedOrderDao


    // Loyalty DAOs
    abstract fun loyaltyProgramDao(): LoyaltyProgramDao
    abstract fun loyaltyProgramOutletDao(): LoyaltyProgramOutletDao
    abstract fun loyaltyTierDao(): LoyaltyTierDao
    abstract fun loyaltyAccountDao(): LoyaltyAccountDao
    abstract fun loyaltyTransactionDao(): LoyaltyTransactionDao
    abstract fun loyaltyPointLotDao(): LoyaltyPointLotDao
    abstract fun loyaltyPointAllocationDao(): LoyaltyPointAllocationDao
    abstract fun loyaltyRewardDao(): LoyaltyRewardDao
    abstract fun loyaltyRewardRedemptionDao(): LoyaltyRewardRedemptionDao
    abstract fun loyaltyVoucherDao(): LoyaltyVoucherDao
    abstract fun loyaltyVoucherUsageDao(): LoyaltyVoucherUsageDao
    abstract fun loyaltyCampaignDao(): LoyaltyCampaignDao
    abstract fun loyaltyCampaignUsageDao(): LoyaltyCampaignUsageDao
    abstract fun loyaltyReferralDao(): LoyaltyReferralDao

}
