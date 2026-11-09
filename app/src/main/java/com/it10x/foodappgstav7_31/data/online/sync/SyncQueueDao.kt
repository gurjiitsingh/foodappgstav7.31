package com.it10x.foodappgstav7_31.data.online.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SyncQueueDao {

    @Insert
    suspend fun insert(job: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE status='PENDING' ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNext(): SyncQueueEntity?


    @Query("DELETE FROM sync_queue WHERE id=:id")
    suspend fun deleteJob(id: String)


}