package datus.app.com.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import datus.app.com.ui.screens.Promotion
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PromotionsViewModel"

@HiltViewModel
class PromotionsViewModel @Inject constructor(
    private val postgrest: Postgrest
) : ViewModel() {

    private val _promotions = MutableStateFlow<List<Promotion>>(emptyList())
    val promotions: StateFlow<List<Promotion>> = _promotions

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentPage = 0
    private val itemsPerPage = 10
    private var hasMore = true

    init {
        loadPromos()
    }

    fun loadPromos() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                currentPage = 0
                hasMore = true
                Log.d(TAG, "Fetching promotions...")
                val result = postgrest.from("promociones").select {
                    filter { 
                        eq("active", true)
                    }
                    range(0, itemsPerPage - 1L)
                }.decodeList<Promotion>().shuffled()
                Log.d(TAG, "Promotions fetched: ${result.size}")
                _promotions.value = result
                hasMore = result.size == itemsPerPage
            } catch (e: Exception) {
                _error.value = "Error al cargar promociones: ${e.localizedMessage ?: e.toString()}"
                Log.e(TAG, "Error loading promotions: ${e.localizedMessage ?: e.toString()}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMorePromos() {
        if (_loading.value || _loadingMore.value || !hasMore) return
        
        viewModelScope.launch {
            try {
                _loadingMore.value = true
                Log.d(TAG, "Fetching more promotions...")
                val from = ((currentPage + 1) * itemsPerPage).toLong()
                val result = postgrest.from("promociones").select {
                    filter { 
                        eq("active", true)
                    }
                    range(from, from + itemsPerPage - 1)
                }.decodeList<Promotion>().shuffled()
                Log.d(TAG, "More promotions fetched: ${result.size}")
                
                currentPage++
                val currentPromotions = _promotions.value.toMutableList()
                currentPromotions.addAll(result)
                _promotions.value = currentPromotions
                hasMore = result.size == itemsPerPage
            } catch (e: Exception) {
                _error.value = "Error al cargar más promociones: ${e.localizedMessage ?: e.toString()}"
                Log.e(TAG, "Error loading more promotions: ${e.localizedMessage ?: e.toString()}")
            } finally {
                _loadingMore.value = false
            }
        }
    }
}