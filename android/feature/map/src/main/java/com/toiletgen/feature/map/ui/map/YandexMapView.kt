package com.toiletgen.feature.map.ui.map

import android.graphics.PointF
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.toiletgen.core.domain.model.Toilet
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

/**
 * Compose-обёртка для Yandex MapView.
 * Отображает маркеры туалетов на карте с кластеризацией.
 */
@Composable
fun YandexMap(
    toilets: List<Toilet>,
    userLat: Double,
    userLon: Double,
    isPlacementMode: Boolean,
    routeTarget: Point?,
    onToiletClick: (Toilet) -> Unit,
    onCameraMoved: (Double, Double) -> Unit,
    onMapTap: (Double, Double) -> Unit,
    onLocationUpdated: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val toiletsState = rememberUpdatedState(toilets)
    val onToiletClickState = rememberUpdatedState(onToiletClick)
    val onCameraMovedState = rememberUpdatedState(onCameraMoved)
    val onMapTapState = rememberUpdatedState(onMapTap)
    val isPlacementModeState = rememberUpdatedState(isPlacementMode)
    val onLocationUpdatedState = rememberUpdatedState(onLocationUpdated)

    // Lifecycle management — MapKit requires start/stop
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView?.onStart()
                }
                Lifecycle.Event.ON_STOP -> {
                    mapView?.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                // Начальная позиция камеры
                mapWindow.map.move(
                    com.yandex.mapkit.map.CameraPosition(
                        Point(userLat, userLon),
                        /* zoom */ 14.0f,
                        /* azimuth */ 0.0f,
                        /* tilt */ 0.0f,
                    )
                )

                // Отдельные коллекции для маркеров и маршрута
                mapContextRef = context
                markersCollectionRef = mapWindow.map.mapObjects.addCollection()
                routeCollectionRef = mapWindow.map.mapObjects.addCollection()

                // Геолокация пользователя — MapKit рисует синюю точку
                var firstLocationFix = true
                userLocationHelperRef = UserLocationHelper(context, this) { lat, lon ->
                    onLocationUpdatedState.value(lat, lon)
                    // При первом GPS-фиксе перемещаем камеру к пользователю
                    if (firstLocationFix) {
                        firstLocationFix = false
                        mapWindow.map.move(
                            com.yandex.mapkit.map.CameraPosition(
                                Point(lat, lon),
                                /* zoom */ 14.0f,
                                /* azimuth */ 0.0f,
                                /* tilt */ 0.0f,
                            )
                        )
                    }
                }
                userLocationHelperRef?.activate()

                // Слушатель движения камеры (strong ref to prevent GC)
                val camListener = object : CameraListener {
                    override fun onCameraPositionChanged(
                        map: Map,
                        cameraPosition: CameraPosition,
                        cameraUpdateReason: CameraUpdateReason,
                        finished: Boolean,
                    ) {
                        if (finished && cameraUpdateReason == CameraUpdateReason.GESTURES) {
                            onCameraMovedState.value(
                                cameraPosition.target.latitude,
                                cameraPosition.target.longitude,
                            )
                        }
                    }
                }
                cameraListenerRef = camListener
                mapWindow.map.addCameraListener(camListener)

                // Tap в режиме размещения — выбор точки (strong ref to prevent GC)
                val inpListener = object : InputListener {
                    override fun onMapTap(map: Map, point: Point) {
                        if (isPlacementModeState.value) {
                            onMapTapState.value(point.latitude, point.longitude)
                        }
                    }
                    override fun onMapLongTap(map: Map, point: Point) {}
                }
                inputListenerRef = inpListener
                mapWindow.map.addInputListener(inpListener)

                mapView = this
            }
        },
        update = { view ->
            updateMarkers(toiletsState.value, onToiletClickState.value)
        },
        modifier = modifier,
    )

    // Построение пешеходного маршрута при изменении routeTarget
    LaunchedEffect(routeTarget) {
        val view = mapView ?: return@LaunchedEffect
        // Очищаем коллекцию маршрута
        routeCollectionRef?.clear()

        val target = routeTarget ?: return@LaunchedEffect
        val from = Point(userLat, userLon)

        buildPedestrianRoute(view, from, target)
    }
}

// Strong references — MapKit holds only weak refs for all listeners
private val tapListeners = mutableListOf<MapObjectTapListener>()
private var cameraListenerRef: CameraListener? = null
private var inputListenerRef: InputListener? = null
private var userLocationHelperRef: UserLocationHelper? = null
private var markersCollectionRef: MapObjectCollection? = null
private var routeCollectionRef: MapObjectCollection? = null
private var mapContextRef: android.content.Context? = null
private var routeSessionRef: Session? = null
private var routeListenerRef: Session.RouteListener? = null

private fun buildPedestrianRoute(mapView: MapView, from: Point, to: Point) {
    val router = TransportFactory.getInstance().createPedestrianRouter()

    val points = listOf(
        RequestPoint(from, RequestPointType.WAYPOINT, null, null),
        RequestPoint(to, RequestPointType.WAYPOINT, null, null),
    )

    val listener = object : Session.RouteListener {
        override fun onMasstransitRoutes(routes: MutableList<Route>) {
            if (routes.isEmpty()) return
            val geometry = routes.first().geometry
            routeCollectionRef?.addPolyline(geometry)?.apply {
                setStrokeColor(android.graphics.Color.argb(200, 33, 150, 243))
                strokeWidth = 5f
                outlineColor = android.graphics.Color.argb(80, 33, 150, 243)
                outlineWidth = 1f
            }
        }

        override fun onMasstransitRoutesError(error: Error) {}
    }
    routeListenerRef = listener

    val routeOptions = com.yandex.mapkit.transport.masstransit.RouteOptions(
        com.yandex.mapkit.transport.masstransit.FitnessOptions(false)
    )
    routeSessionRef = router.requestRoutes(
        points,
        TimeOptions(),
        routeOptions,
        listener,
    )
}

/**
 * Обновляет маркеры на карте.
 * Удаляет старые, добавляет новые с цветовой кодировкой по типу.
 * Использует отдельную коллекцию чтобы не затрагивать маршрут.
 */
private fun updateMarkers(
    toilets: List<Toilet>,
    onToiletClick: (Toilet) -> Unit,
) {
    val collection = markersCollectionRef ?: return
    val context = mapContextRef ?: return

    // Очищаем только маркеры (маршрут в другой коллекции)
    collection.clear()
    tapListeners.clear()

    // Добавляем маркеры туалетов
    for (toilet in toilets) {
        val point = Point(toilet.latitude, toilet.longitude)

        val markerBitmap = ToiletMarkerDrawer.draw(
            context = context,
            type = toilet.type,
            hasToiletPaper = toilet.hasToiletPaper,
            rating = toilet.avgRating,
        )

        val placemark = collection.addPlacemark().apply {
            geometry = point
            setIcon(ImageProvider.fromBitmap(markerBitmap))
            setIconStyle(
                IconStyle().apply {
                    anchor = PointF(0.4f, 1.0f)
                    scale = 1.0f
                }
            )
        }

        val listener = MapObjectTapListener { _, _ ->
            onToiletClick(toilet)
            true
        }
        tapListeners.add(listener)
        placemark.addTapListener(listener)
    }
}
