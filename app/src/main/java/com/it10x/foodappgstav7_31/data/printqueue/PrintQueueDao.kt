package com.it10x.foodappgstav7_31.data.printqueue

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.it10x.foodappgstav7_31.data.printqueue.PrintQueueEntity

@Dao
interface PrintQueueDao {

    @Insert
    suspend fun insert(job: PrintQueueEntity)

    @Query("SELECT * FROM print_queue WHERE status='PENDING' ORDER BY createdAt ASC")
    suspend fun getPending(): List<PrintQueueEntity>

    @Query("UPDATE print_queue SET status=:status, retryCount=:retry WHERE id=:id")
    suspend fun updateStatus(id: String, status: String, retry: Int)

    @Query("DELETE FROM print_queue WHERE id=:id")
    suspend fun delete(id: String)


    @Query("""
SELECT EXISTS(
    SELECT 1 FROM print_queue 
    WHERE referenceId = :referenceId
)
""")
    suspend fun existsByReferenceId(
        referenceId: String
    ): Boolean

    @Query("""
UPDATE print_queue
SET status=:newStatus
WHERE id=:id
AND status=:oldStatus
""")
    suspend fun updateStatusIfPending(
        id:String,
        oldStatus:String,
        newStatus:String
    ):Int
}