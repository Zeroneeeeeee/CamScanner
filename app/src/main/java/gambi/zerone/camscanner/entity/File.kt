package gambi.zerone.camscanner.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class File(
    @PrimaryKey val id:Long,
    val uri: String,
    val name: String,
    val size: Double
)