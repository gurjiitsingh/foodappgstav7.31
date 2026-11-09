package  com.it10x.foodappgstav7_31.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kot_history")
data class PosKotHistoryEntity(

    @PrimaryKey
    val id: String,
    val kotNumber: String,
    val tableName: String,
    val createdByName: String,
    // Batch
    val batchId: String,
    val sessionId: String,
    val orderId: String? = null,

    // Table
    val tableNo: String,
    val orderType: String,

    // Product Snapshot
    val productId: String,
    val name: String,

    val quantity: Int,

    val modifierTotal: Double,

    val note: String?,
    val modifiersJson: String,

    // KOT Status
    val status: String,

    val deleted: Boolean = false,
    val deletedBy: String? = null,
    val deletedReason: String? = null,
    val deletedAt: Long? = null,

    val paidAt: Long? = null,

    // Source
    val source: String,

    val deviceId: String,
    val deviceName: String?,

    val createdAt: Long
)