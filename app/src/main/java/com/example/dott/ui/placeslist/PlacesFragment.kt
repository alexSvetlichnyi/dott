package com.example.dott.ui.placeslist

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dott.R
import com.example.dott.data.models.placeslist.Place
import com.example.dott.databinding.FragmentItemListBinding
import com.example.dott.ui.placedetails.PlaceDetailsFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/* A fragment representing list of places. */
@AndroidEntryPoint
class PlacesFragment : Fragment(), OnMapReadyCallback {

    private val viewModel: PlacesListViewModel by viewModels()
    private var _binding: FragmentItemListBinding? = null
    private val binding get() = _binding!!
    private var snackbar: Snackbar? = null

    // Google maps
    private lateinit var map: GoogleMap
    private var mapFragment: SupportMapFragment? = null
    private var mapMarkers = mutableListOf<Marker>()

    // Location Provider entities.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                locationPermissionGranted = true
                updateLocationUI()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_list, container, false)
        binding.viewModel = viewModel
        viewModel.showPlaces.observe(viewLifecycleOwner, { places -> addMarkers(places) })
        viewModel.errorMessage.observe(viewLifecycleOwner, { error ->
            if (snackbar?.isShown != true) {
                snackbar = Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.close)) { }.apply { show() }
            }
        })
        binding.removeMarkers.setOnClickListener {
            mapMarkers.forEach { marker -> marker.remove() }
            mapMarkers.clear()
            viewModel.clearCache()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init SupportMapFragment
        if (mapFragment == null) {
            mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment?.getMapAsync(this)
        }


        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

        // Find new places when user stop moving camera.
        map.setOnCameraIdleListener {
            viewModel.findCurrentPlaces(map.cameraPosition.target, getMapVisibleRadius())
        }

        // Use a custom info window.
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(R.layout.item_list_content, null)
                infoWindow.findViewById<TextView>(R.id.name).apply {
                    text = marker.title
                }
                infoWindow.findViewById<TextView>(R.id.content).apply {
                    text = marker.snippet
                }
                return infoWindow
            }


        })

        // Open PlaceDetails when user pressed infoWindow
        map.setOnInfoWindowClickListener {
            val bundle = Bundle()
            bundle.putString(
                PlaceDetailsFragment.ARG_ITEM_ID,
                it.tag as String
            )
            findNavController().navigate(R.id.show_item_detail, bundle)
        }

        // Prompt the user for permission.
        getLocationPermission()
    }

    /**
     * Add markers on the google map.
     */
    private fun addMarkers(places: List<Place>) {
        places.forEach { place ->
            place.geocodes?.main?.let {
                map.addMarker(
                    MarkerOptions()
                        .title(place.name)
                        .snippet(place.location?.address)
                        .position(LatLng(it.latitude, it.longitude))
                )?.apply {
                    // we need this tag to open PlaceDetails
                    tag = place.fsqId
                    mapMarkers.add(this)

                    // start smooth animation
                    viewModel.startDropMarkerAnimation(this, map.projection)
                }
            }
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    var lastKnownLocation = viewModel.lastKnownLocation
                    if (task.isSuccessful) {
                        lastKnownLocation = LatLng(task.result.latitude, task.result.longitude)
                        viewModel.lastKnownLocation = lastKnownLocation
                    }
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            lastKnownLocation, DEFAULT_ZOOM.toFloat()
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    /**
     * Request location permission, so that we can get the location of the
     * device.
     */
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            permReqLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
                getDeviceLocation()
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    private fun getMapVisibleRadius(): Int {
        val distance = FloatArray(1)

        val farLeft: LatLng = map.projection.visibleRegion.farLeft
        val nearRight: LatLng = map.projection.visibleRegion.nearRight

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
        private const val DEFAULT_ZOOM = 15
    }
}