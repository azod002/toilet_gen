package com.toiletgen.feature.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toiletgen.core.common.Resource
import com.toiletgen.core.domain.model.Toilet
import com.toiletgen.core.domain.model.ToiletType
import com.toiletgen.core.domain.usecase.GetNearbyToiletsUseCase
import com.toiletgen.core.network.api.StampApi
import com.toiletgen.core.network.api.ToiletApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

data class RouteTarget(
    val toiletId: String,
    val lat: Double,
    val lon: Double,
    val toiletName: String,
)

data class MapUiState(
    val toilets: List<Toilet> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedToilet: Toilet? = null,
    val userLat: Double = 55.7558, // Moscow default
    val userLon: Double = 37.6173,
    val routeTarget: RouteTarget? = null,
    val arrivedMessage: String? = null,
)

sealed interface MapEvent {
    data class LoadToilets(val lat: Double, val lon: Double, val radius: Double = 2.0) : MapEvent
    data class SelectToilet(val toilet: Toilet) : MapEvent
    data object DismissToilet : MapEvent
    data class UpdateUserLocation(val lat: Double, val lon: Double) : MapEvent
    data class BuildRouteToNearest(val free: Boolean) : MapEvent
    data object ClearRoute : MapEvent
    data object DismissArrived : MapEvent
}

class MapViewModel(
    private val getNearbyToiletsUseCase: GetNearbyToiletsUseCase,
    private val toiletApi: ToiletApi,
    private val stampApi: StampApi,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    companion object {
        private const val ARRIVAL_RADIUS_METERS = 25.0
    }

    init {
        loadToilets(_uiState.value.userLat, _uiState.value.userLon)
    }

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.LoadToilets -> loadToilets(event.lat, event.lon, event.radius)
            is MapEvent.SelectToilet -> _uiState.update { it.copy(selectedToilet = event.toilet) }
            is MapEvent.DismissToilet -> _uiState.update { it.copy(selectedToilet = null) }
            is MapEvent.UpdateUserLocation -> {
                _uiState.update { it.copy(userLat = event.lat, userLon = event.lon) }
                checkArrival(event.lat, event.lon)
                loadToilets(event.lat, event.lon)
            }
            is MapEvent.BuildRouteToNearest -> buildRouteToNearest(event.free)
            is MapEvent.ClearRoute -> _uiState.update { it.copy(routeTarget = null) }
            is MapEvent.DismissArrived -> _uiState.update { it.copy(arrivedMessage = null) }
        }
    }

    fun refreshToilets() {
        loadToilets(_uiState.value.userLat, _uiState.value.userLon)
    }

    private fun checkArrival(userLat: Double, userLon: Double) {
        val target = _uiState.value.routeTarget ?: return
        val distance = distanceBetween(userLat, userLon, target.lat, target.lon)
        if (distance <= ARRIVAL_RADIUS_METERS) {
            // Arrived — record visit and clear route
            _uiState.update {
                it.copy(
                    routeTarget = null,
                    arrivedMessage = "Вы добрались до «${target.toiletName}»!",
                )
            }
            viewModelScope.launch {
                try {
                    toiletApi.visitToilet(target.toiletId)
                } catch (_: Exception) {}
            }
            // Автоматический сбор марки при прибытии
            viewModelScope.launch {
                try {
                    stampApi.collectStamp(target.toiletId)
                } catch (_: Exception) {}
            }
        }
    }

    private fun buildRouteToNearest(free: Boolean) {
        val state = _uiState.value
        val targetTypes = if (free) {
            setOf(ToiletType.FREE, ToiletType.REGULAR, ToiletType.USER_ADDED)
        } else {
            setOf(ToiletType.PAID)
        }

        val nearest = state.toilets
            .filter { it.type in targetTypes }
            .minByOrNull { distanceBetween(state.userLat, state.userLon, it.latitude, it.longitude) }

        if (nearest != null) {
            _uiState.update {
                it.copy(routeTarget = RouteTarget(nearest.id, nearest.latitude, nearest.longitude, nearest.name))
            }
            // Check immediately — user might already be at the target
            checkArrival(state.userLat, state.userLon)
        } else {
            _uiState.update {
                it.copy(error = if (free) "Бесплатных туалетов поблизости не найдено" else "Платных туалетов поблизости не найдено")
            }
        }
    }

    private fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun loadToilets(lat: Double, lon: Double, radius: Double = 2.0) {
        viewModelScope.launch {
            getNearbyToiletsUseCase(lat, lon, radius).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(isLoading = false, toilets = resource.data, error = null)
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isLoading = false, error = resource.message)
                    }
                }
            }
        }
    }
}
