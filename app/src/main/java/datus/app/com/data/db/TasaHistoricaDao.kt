package datus.app.com.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TasaHistoricaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tasaHistorica: TasaHistorica)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasas: List<TasaHistorica>)

    @Query("SELECT * FROM tasas_historicas WHERE currency = :currency ORDER BY timestamp DESC")
    fun getHistorialPorMoneda(currency: String): Flow<List<TasaHistorica>>

    @Query("SELECT * FROM tasas_historicas WHERE currency = :currency AND timestamp >= :sinceTimestamp")
    suspend fun getTasasDesde(currency: String, sinceTimestamp: Long): List<TasaHistorica>

    @Query("SELECT MAX(tasa) FROM tasas_historicas WHERE currency = :currency AND timestamp >= :sinceTimestamp")
    suspend fun getTasaMasAlta(currency: String, sinceTimestamp: Long): Double?

    @Query("SELECT MIN(tasa) FROM tasas_historicas WHERE currency = :currency AND timestamp >= :sinceTimestamp")
    suspend fun getTasaMasBaja(currency: String, sinceTimestamp: Long): Double?

    @Query("SELECT currency, MAX(tasa) as maxRate, MIN(tasa) as minRate FROM tasas_historicas WHERE currency IN (:currencies) AND timestamp >= :sinceTimestamp GROUP BY currency")
    suspend fun getMinMaxRates(currencies: List<String>, sinceTimestamp: Long): List<CurrencyMinMax>

    @Query("DELETE FROM tasas_historicas WHERE timestamp < :olderThanTimestamp")
    suspend fun eliminarAntiguas(olderThanTimestamp: Long)

    @Query("DELETE FROM tasas_historicas WHERE currency = :currency")
    suspend fun eliminarHistorialPorMoneda(currency: String)

    @Query("DELETE FROM tasas_historicas")
    suspend fun eliminarTodoElHistorial()
}

data class CurrencyMinMax(
    val currency: String,
    val maxRate: Double,
    val minRate: Double
)
