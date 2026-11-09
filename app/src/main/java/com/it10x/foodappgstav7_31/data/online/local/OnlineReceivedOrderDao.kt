
package com.it10x.foodappgstav7_31.data.online.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OnlineReceivedOrderDao {

    // =====================================================
    // INSERT ORDER HEADER
    // =====================================================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrder(order: OnlineReceivedOrderEntity): Long

    // =====================================================
    // INSERT ORDER PRODUCTS
    // =====================================================

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrderProducts(
        products: List<OnlineReceivedOrderProductEntity>
    ): List<Long>


    @Query("""
    UPDATE online_received_orders
    SET orderStatus = :status
    WHERE id = :orderId
""")
    suspend fun updateOrderStatus(
        orderId: String,
        status: String
    )
    // =====================================================
    // CHECK ORDER
    // =====================================================

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM online_received_orders
            WHERE id = :orderId
        )
        """
    )
    suspend fun orderExists(orderId: String): Boolean

    // =====================================================
    // GET ORDER HEADER
    // =====================================================

    @Query(
        """
        SELECT *
        FROM online_received_orders
        WHERE id = :orderId
        LIMIT 1
        """
    )
    suspend fun getOrderById(
        orderId: String
    ): OnlineReceivedOrderEntity?

    // =====================================================
    // GET PRODUCTS FOR AN ORDER
    // =====================================================

    @Query(
        """
        SELECT *
        FROM online_order_products
        WHERE orderMasterId = :orderId
        ORDER BY rowid ASC
        """
    )
    suspend fun getProductsForOrder(
        orderId: String
    ): List<OnlineReceivedOrderProductEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM online_order_products
        WHERE orderMasterId = :orderId
        """
    )
    suspend fun getProductCountForOrder(
        orderId: String
    ): Int

    // =====================================================
    // SAVE HEADER + PRODUCTS ATOMICALLY
    // =====================================================

    @Transaction
    suspend fun saveOrderWithProducts(
        order: OnlineReceivedOrderEntity,
        products: List<OnlineReceivedOrderProductEntity>
    ): Boolean {

        require(order.id.isNotBlank()) {
            "Online order ID cannot be blank"
        }

        // Insert the header. If it already exists, IGNORE keeps it.
        insertOrder(order)

        // Verify the header exists before saving products.
        val savedOrder = getOrderById(order.id)
            ?: throw IllegalStateException(
                "Order header could not be saved: ${order.id}"
            )

        // Ensure every product belongs to this order.
        require(products.all { it.orderMasterId == savedOrder.id }) {
            "A product belongs to a different order"
        }

        // Insert products. Existing product IDs are ignored.
        if (products.isNotEmpty()) {
            insertOrderProducts(products)
        }

        // Verify the local data after insertion.
        val verifiedOrder = getOrderById(order.id)
            ?: throw IllegalStateException(
                "Order header verification failed: ${order.id}"
            )

        val savedProductCount = getProductCountForOrder(order.id)

        if (products.isNotEmpty() && savedProductCount < products.size) {
            throw IllegalStateException(
                "Not all products were saved for order ${order.id}. " +
                        "Expected at least ${products.size}, found $savedProductCount"
            )
        }

        return verifiedOrder.id == order.id
    }

    // =====================================================
    // OBSERVE ORDERS — NEWEST FIRST
    // =====================================================

    @Query(
        """
        SELECT *
        FROM online_received_orders
        ORDER BY createdAtMillis DESC, localReceivedAtMillis DESC
        """
    )
    fun observeAllOrders(): Flow<List<OnlineReceivedOrderEntity>>

    // =====================================================
    // GET ALL ORDERS — NEWEST FIRST
    // =====================================================

    @Query(
        """
        SELECT *
        FROM online_received_orders
        ORDER BY createdAtMillis DESC, localReceivedAtMillis DESC
        """
    )
    suspend fun getAllOrders(): List<OnlineReceivedOrderEntity>

    // =====================================================
    // COUNT ORDERS
    // =====================================================

    @Query(
        """
        SELECT COUNT(*)
        FROM online_received_orders
        """
    )
    suspend fun getOrderCount(): Int
}
