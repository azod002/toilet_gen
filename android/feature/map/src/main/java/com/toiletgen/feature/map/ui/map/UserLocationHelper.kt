package com.toiletgen.feature.map.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.mapview.MapView

/**
 * Включает слой геолокации пользователя на карте.
 * Также подписывается на обновления местоположения.
 */
class UserLocationHelper(
    private val context: Context,
    private val mapView: MapView,
    private val onLocationUpdated: (lat: Double, lon: Double) -> Unit,
) {
    // Strong references to prevent GC (MapKit holds weak refs)
    private var locationListener: LocationListener? = null
    private var locationManager: com.yandex.mapkit.location.LocationManager? = null

    fun activate() {
        if (!hasLocationPermission()) return

        // Показываем точку пользователя на карте
        val userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        // Подписка на обновления геолокации через MapKit
        locationManager = MapKitFactory.getInstance().createLocationManager()
        locationListener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                onLocationUpdated(location.position.latitude, location.position.longitude)
            }

            override fun onLocationStatusUpdated(status: LocationStatus) {}
        }
        locationManager!!.subscribeForLocationUpdates(
            0.0,
            3000L,
            10.0,
            false,
            FilteringMode.ON,
            Purpose.GENERAL,
            locationListener!!,
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
