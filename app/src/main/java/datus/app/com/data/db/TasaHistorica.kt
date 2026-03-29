package datus.app.com.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasas_historicas",
    indices = [
        Index(value = ["currency", "timestamp"]),
        Index(value = ["timestamp"])
    ]
)
data class TasaHistorica(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val currency: String,
    val tasa: Double,
    val timestamp: Long
)
