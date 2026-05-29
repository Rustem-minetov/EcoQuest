package com.example.waynixgoapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waynixgoapp.data.Ride
import com.example.waynixgoapp.data.network.ApiService
import com.example.waynixgoapp.data.network.toRide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val apiService = ApiService.create()

    private val _rides = MutableStateFlow<List<Ride>>(emptyList())
    val rides: StateFlow<List<Ride>> = _rides

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        refreshRides()
    }

    fun refreshRides(to: String? = null, from: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getRides(
                    to = to,
                    from = from
                )
                _rides.value = response.results.map { it.toRide() }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "Не удалось загрузить рейсы: ${e.localizedMessage}"
                // Оставляем старые данные на экране
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
