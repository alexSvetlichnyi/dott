package com.example.dott.data.repositories

import com.example.dott.data.models.placedetails.PlaceDetails
import com.example.dott.data.models.placeslist.PlacesResponse
import com.example.dott.data.network.Resource
import com.google.android.gms.maps.model.LatLng

/**
 * Interface that provides Places API
 */
interface PlacesRepository {

    /**
     * Get places from the FourSquare API
     *
     * @param latLng The latitude/longitude around which to retrieve place information
     * @param radius Defines the distance (in meters) within which to bias place results.
     * The maximum allowed radius is 100,000 meters.
     *
     * @return resource with list of places or error
     */
    suspend fun getPlaces(latLng: LatLng, radius: Int): Resource<PlacesResponse>

    /**
     * Get place details from the FourSquare API
     *
     * @param placeId A unique string identifier for a FSQ Place
     *
     * @return resource with place details or error
     */
    suspend fun getPlaceDetails(placeId: String): Resource<PlaceDetails>
}