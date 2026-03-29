package datus.app.com.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import datus.app.com.data.db.TasaHistorica
import datus.app.com.data.db.TasaHistoricaDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyHistoryViewModel @Inject constructor(
    private val tasaHistoricaDao: TasaHistoricaDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currency: String = checkNotNull(savedStateHandle["currency"])

    val history: StateFlow<List<TasaHistorica>> =
        tasaHistoricaDao.getHistorialPorMoneda(currency)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun deleteHistory() {
        viewModelScope.launch {
            tasaHistoricaDao.eliminarHistorialPorMoneda(currency)
        }
    }
}
