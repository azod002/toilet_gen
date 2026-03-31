package com.toiletgen.feature.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Toilet
import com.toiletgen.core.domain.model.ToiletType
import com.toiletgen.core.domain.usecase.CreateToiletUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddToiletUiState(
    val name: String = "",
    val description: String = "",
    val type: ToiletType = ToiletType.FREE,
    val isPaid: Boolean = false,
    val price: String = "",
    val hasToiletPaper: Boolean = true,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

class AddToiletViewModel(
    private val createToiletUseCase: CreateToiletUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddToiletUiState())
    val uiState: StateFlow<AddToiletUiState> = _uiState.asStateFlow()

    fun setCoordinates(lat: Double, lon: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lon) }
    }

    fun onNameChange(name: String) { _uiState.update { it.copy(name = name) } }
    fun onDescriptionChange(desc: String) { _uiState.update { it.copy(description = desc) } }
    fun onTypeChange(type: ToiletType) {
        _uiState.update {
            it.copy(
                type = type,
                isPaid = type == ToiletType.PAID,
            )
        }
    }
    fun onPaidChange(isPaid: Boolean) { _uiState.update { it.copy(isPaid = isPaid) } }
    fun onPriceChange(price: String) { _uiState.update { it.copy(price = price) } }
    fun onToiletPaperChange(has: Boolean) { _uiState.update { it.copy(hasToiletPaper = has) } }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Введите название") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val toilet = Toilet(
                id = "",
                ownerId = null,
                name = state.name,
                description = state.description,
                type = state.type,
                latitude = state.latitude,
                longitude = state.longitude,
                isPaid = state.isPaid,
                price = state.price.toDoubleOrNull(),
                hasToiletPaper = state.hasToiletPaper,
                avgRating = 0.0,
                avgCleanliness = 0.0,
                reviewCount = 0,
                createdAt = System.currentTimeMillis(),
            )
            when (val result = createToiletUseCase(toilet)) {
                is Resource.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
