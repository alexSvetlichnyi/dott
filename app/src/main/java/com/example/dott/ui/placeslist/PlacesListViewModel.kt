package com.example.dott.ui.placeslist

import android.app.Application
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.animation.Interpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.viewModelScope
import com.example.dott.R
import com.example.dott.application.BaseAndroidViewModel
import com.example.dott.application.SingleLiveData
import com.example.dott.data.models.placeslist.Place
import com.example.dott.data.network.Resource
import com.example.dott.data.repositories.PlacesRepository
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.VisibleRegion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacesListViewModel @Inject constructor (
    app: Application,
    private val placesRepository: PlacesRepository
) : BaseAndroidViewModel(app) {

    var lastKnownLocation: LatLng = LatLng(DEFAULT_LAT, DEFAULT_LONG)
    val errorMessage: SingleLiveData<String> by lazy { SingleLiveData() }
    val showPlaces: SingleLiveData<List<Place>> by lazy { SingleLiveData() }

    // List of known locations
    private var allPlaces = mutableSetOf<Place>()

    fun findCurrentPlaces(latLng: LatLng, visibleRegion: VisibleRegion) {
        viewModelScope.launch {
            val radius = getMapVisibleRadius(visibleRegion)
            if (radius > MAX_RADIUS) {
                errorMessage.value = getString(R.string.radius_error)
            } else {
                when (val result =
                    placesRepository.getPlaces(latLng, getMapVisibleRadius(visibleRegion))) {
                    is Resource.Success -> {
                        result.data.results.filter {
                            it.fsqId !in allPlaces.map{ item -> item.fsqId }
                        }.apply {
                            showPlaces.value = this
                            allPlaces.addAll(this)
                        }
                    }
                    is Resource.Error -> errorMessage.value = result.errorMessage
                }
            }
        }
    }

    /**
     * Add marker custom animation.
     */
    fun startDropMarkerAnimation(marker: Marker, projection: Projection) {
        val target = marker.position
        val handler = Handler(Looper.getMainLooper())
        val start = SystemClock.uptimeMillis()
        val duration = (200 + projection.toScreenLocation(target).y * 0.6).toLong()
        val startPoint = projection.toScreenLocation(marker.position).apply {
            y = 0
        }
        val startLatLng = projection.fromScreenLocation(startPoint)
        val interpolator: Interpolator = LinearOutSlowInInterpolator()

        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t: Float = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val lng = t * target.longitude + (1 - t) * startLatLng.longitude
                val lat = t * target.latitude + (1 - t) * startLatLng.latitude
                marker.position = LatLng(lat, lng)
                if (t < 1.0) {
                    // Post again 16ms later == 60 frames per second
                    handler.postDelayed(this, ANIMATION_DELAY)
                }
            }
        })
    }

    private fun getMapVisibleRadius(visibleRegion: VisibleRegion): Int {
        val distance = FloatArray(1)

        val farLeft: LatLng = visibleRegion.farLeft
        val nearRight: LatLng = visibleRegion.nearRight

        Location.distanceBetween(
            farLeft.latitude,
            farLeft.longitude,
            nearRight.latitude,
            nearRight.longitude,
            distance
        )

        return (distance[0] / 2).toInt()
    }

    companion object {
        const val DEFAULT_LAT = -33.8523341
        const val DEFAULT_LONG = 151.2106085
        const val ANIMATION_DELAY = 16L
        const val MAX_RADIUS = 100000
    }
}
