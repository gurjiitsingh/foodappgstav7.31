package com.it10x.foodappgstav7_31.data.online.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local SQLite/Room entity for incoming WEB / APP orders.
 *
 * Mirrors OnlineOrderMaster but uses SQLite-compatible types.
 * This entity does not replace the existing POS order tables.
 */
@Entity(tableName = "online_received_orders")
data class OnlineReceivedOrderEntity(

    // =====================================================
    // CORE IDENTIFIERS
    // =====================================================

    @PrimaryKey
    val id: String,

    val srno: String = "",
    val source: String? = null,
    val orderNumber: String = "",

    // =====================================================
    // OWNER / OUTLET
    // =====================================================

    val ownerId: String? = null,
    val outletId: String? = null,
    val outletName: String? = null,

    // =====================================================
    // CUSTOMER
    // =====================================================

    val customerId: String? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val customerCountryCode: String? = null,
    val email: String? = null,
    val addressId: String? = null,

    // =====================================================
    // DELIVERY ADDRESS
    // =====================================================

    val dAddressLine1: String? = null,
    val dAddressLine2: String? = null,
    val dCity: String? = null,
    val dState: String? = null,
    val dZipcode: String? = null,
    val dLandmark: String? = null,

    // =====================================================
    // ORDER TYPE
    // =====================================================

    val orderType: String? = null,
    val tableNo: String? = null,
    val isScheduled: Boolean = false,

    // Firestore Timestamp converted to milliseconds.
    val scheduledAtMillis: Long? = null,

    // =====================================================
    // AMOUNTS
    // =====================================================

    val itemTotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val subTotal: Double = 0.0,
    val taxBeforeDiscount: Double = 0.0,
    val taxTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val grandTotal: Double = 0.0,

    // =====================================================
    // PAYMENT
    // =====================================================

    val paymentMode: String? = null,
    val paymentProvider: String? = null,
    val paymentMethod: String? = null,
    val paymentStatus: String? = null,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,

    // =====================================================
    // ORDER STATUS
    // =====================================================

    val orderStatus: String? = null,
    val status: String? = null,
    val notes: String? = null,
    val staffId: String? = null,
    val productsCount: Int = 0,

    // =====================================================
    // TIMESTAMPS
    // =====================================================

    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long? = null,
    val closedAtMillis: Long? = null,
    val lastSyncedAtMillis: Long? = null,

    // =====================================================
    // AUTOMATION
    // =====================================================

    val printed: Boolean = false,
    val acknowledged: Boolean = false,

    // =====================================================
    // SYNC CONTROL
    // =====================================================

    val syncStatus: String? = null,

    // Local bookkeeping; separate from Firestore syncStatus.
    val localReceivedAtMillis: Long = System.currentTimeMillis(),
    val localSaveStatus: String = "SAVED",

    // =====================================================
    // DISCOUNTS / COUPONS
    // =====================================================

    val couponCode: String? = null,
    val couponFlat: Double = 0.0,
    val pickUpDiscount: Double = 0.0,
    val couponPercent: Double = 0.0,


    val paymentType: String = "",
    val orderDate: String = "",
    val orderMonth: String = "",
    val orderYear: Int = 0,
)
