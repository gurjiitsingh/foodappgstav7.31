package com.it10x.foodappgstav7_31.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.pos.entities.KotHistorySummary
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_31.data.pos.entities.PosKotHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KotBatchDao {

    // =====================================================
    // KOT BATCH
    // =====================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: PosKotBatchEntity)





    @Query("""
        SELECT *
        FROM pos_kot_batch
        WHERE tableNo = :tableNo
        ORDER BY createdAt ASC
    """)
    fun getBatchesForTable(
        tableNo: String
    ): Flow<List<PosKotBatchEntity>>

    @Query("""
        SELECT *
        FROM pos_kot_batch
        WHERE id = :batchId
        LIMIT 1
    """)
    suspend fun getById(
        batchId: String
    ): PosKotBatchEntity?

    @Query("""
        DELETE FROM pos_kot_batch
        WHERE tableNo = :tableNo
    """)
    suspend fun clearForTable(
        tableNo: String
    )

    // =====================================================
    // KOT HISTORY
    // =====================================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        items: List<PosKotHistoryEntity>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(
        item: PosKotHistoryEntity
    )

    // -----------------------------------------------------
    // SUMMARY (One row per KOT)
    // -----------------------------------------------------

    @Query("""
SELECT
    MIN(batchId) AS batchId,
    kotNumber,
    MIN(createdAt) AS createdAt,
    tableNo,
    tableName,
    createdByName,
    orderType,
    source,

    CASE
        WHEN SUM(CASE WHEN status='ACTIVE' THEN 1 ELSE 0 END)=COUNT(*)
            THEN 'ACTIVE'

        WHEN SUM(CASE WHEN status='PAID' THEN 1 ELSE 0 END)=COUNT(*)
            THEN 'PAID'

        WHEN SUM(CASE WHEN status='DELETED' THEN 1 ELSE 0 END)=COUNT(*)
            THEN 'DELETED'

        ELSE 'PARTIAL'
    END AS status,

    COUNT(*) AS totalLines,
    SUM(quantity) AS totalQty,

    SUM(CASE WHEN status='ACTIVE' THEN 1 ELSE 0 END) AS activeCount,
    SUM(CASE WHEN status='PAID' THEN 1 ELSE 0 END) AS paidCount,
    SUM(CASE WHEN status='DELETED' THEN 1 ELSE 0 END) AS deletedCount

FROM kot_history
GROUP BY kotNumber
ORDER BY MIN(createdAt) DESC
LIMIT 200
""")
    fun getHistorySummary(): Flow<List<KotHistorySummary>>

    // -----------------------------------------------------
    // DETAIL (One KOT)
    // -----------------------------------------------------

    @Query("""
        SELECT *
        FROM kot_history
        WHERE kotNumber = :kotNumber
        ORDER BY createdAt ASC
    """)
    fun getHistoryByKotNumber(
        kotNumber: String
    ): Flow<List<PosKotHistoryEntity>>

    @Query("""
        SELECT *
        FROM kot_history
        WHERE kotNumber = :kotNumber
        ORDER BY createdAt ASC
    """)
    suspend fun getHistoryByKotNumberOnce(
        kotNumber: String
    ): List<PosKotHistoryEntity>

    // -----------------------------------------------------
    // EXISTING QUERIES
    // -----------------------------------------------------

    @Query("""
        SELECT *
        FROM kot_history
        WHERE batchId = :batchId
        ORDER BY createdAt ASC
    """)
    fun getHistoryByBatch(
        batchId: String
    ): Flow<List<PosKotHistoryEntity>>

    @Query("""
        SELECT *
        FROM kot_history
        WHERE tableNo = :tableNo
        ORDER BY createdAt DESC
    """)
    fun getHistoryByTable(
        tableNo: String
    ): Flow<List<PosKotHistoryEntity>>

    @Query("""
        SELECT *
        FROM kot_history
        ORDER BY createdAt DESC
    """)
    fun getAllHistory(): Flow<List<PosKotHistoryEntity>>

    @Query("""
        SELECT *
        FROM kot_history
        ORDER BY createdAt DESC
    """)
    fun getHistory(): Flow<List<PosKotHistoryEntity>>

    @Query("""
        SELECT *
        FROM kot_history
        WHERE orderId = :orderId
        ORDER BY createdAt ASC
    """)
    fun getHistoryByOrder(
        orderId: String
    ): Flow<List<PosKotHistoryEntity>>

    // =====================================================
    // UPDATE STATUS
    // =====================================================

    @Query("""
        UPDATE kot_history
        SET status = :status
        WHERE batchId = :batchId
    """)
    suspend fun updateHistoryStatus(
        batchId: String,
        status: String
    )

    @Query("""
        UPDATE kot_history
        SET
            deleted = 1,
            deletedBy = :deletedBy,
            deletedReason = :reason,
            deletedAt = :deletedAt,
            status = 'DELETED'
        WHERE batchId = :batchId
    """)
    suspend fun deleteHistoryBatch(
        batchId: String,
        deletedBy: String,
        reason: String,
        deletedAt: Long
    )

    @Query("""
        UPDATE kot_history
        SET
            paidAt = :paidAt,
            orderId = :orderId,
            status = 'PAID'
        WHERE batchId = :batchId
    """)
    suspend fun markHistoryPaid(
        batchId: String,
        orderId: String,
        paidAt: Long
    )

    @Query("""
        UPDATE kot_history
        SET
            status = 'DELETED',
            deleted = 1,
            deletedAt = :deletedAt
        WHERE id = :itemId
    """)
    suspend fun markItemDeleted_old(
        itemId: String,
        deletedAt: Long
    )

    @Query("""
    UPDATE kot_history
    SET
        status = 'DELETED',
        deleted = 1,
        deletedAt = :deletedAt,
        note = :reason
    WHERE id = :itemId
""")
    suspend fun markItemDeleted(
        itemId: String,
        reason: String,
        deletedAt: Long
    )

    @Query("""
        UPDATE kot_history
        SET
            status = 'PAID',
            paidAt = :paidAt,
            orderId = :orderId
        WHERE tableNo = :tableNo
          AND status != 'DELETED'
    """)
    suspend fun markTablePaid(
        tableNo: String,
        orderId: String,
        paidAt: Long
    ): Int

    @Query("""
        UPDATE kot_history
        SET
            status = 'COMPLIMENTARY',
            paidAt = :complimentaryAt,
            orderId = :orderId
        WHERE tableNo = :tableNo
          AND status = 'ACTIVE'
    """)
    suspend fun markTableComplimentary(
        tableNo: String,
        orderId: String,
        complimentaryAt: Long
    ): Int

    // =====================================================
    // MAINTENANCE
    // =====================================================
    @Query("""
    DELETE FROM kot_history
    WHERE createdAt < :cutoffTime
""")
    suspend fun deleteHistoryOlderThan(
        cutoffTime: Long
    )
    @Query("""
        DELETE FROM kot_history
    """)
    suspend fun clearHistory()

    @Query("""
        DELETE FROM kot_history
        WHERE batchId = :batchId
    """)
    suspend fun deleteHistoryByBatch(
        batchId: String
    )

    @Query("""
        SELECT COUNT(*)
        FROM kot_history
    """)
    suspend fun historyCount(): Int
}