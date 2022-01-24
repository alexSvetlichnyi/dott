package com.example.dott.data.repositories

import com.example.dott.data.models.ErrorMessage
import com.example.dott.data.models.placedetails.PlaceDetails
import com.example.dott.data.models.placeslist.PlacesResponse
import com.example.dott.data.network.FourSquareApiService
import com.example.dott.data.network.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

import javax.inject.Inject

/**
 * Foursquare implementation that provides Places API
 */
@Suppress("UNCHECKED_CAST")
class PlacesRepositoryImpl @Inject constructor(
    private val fourSquareApiService: FourSquareApiService) : PlacesRepository  {

    /**
     * Get places from the FourSquare API
     *
     * @param latLng The latitude/longitude around which to retrieve place information
     * @param radius Defines the distance (in meters) within which to bias place results.
     * The maximum allowed radius is 100,000 meters.
     *
     * @return resource with list of places or error
     */
    override suspend fun getPlaces(latLng: LatLng, radius: Int): Resource<PlacesResponse>
    = withContext(Dispatchers.IO) {
        handleResult(fourSquareApiService.getNearbyPlaces(
                "${latLng.latitude},${latLng.longitude}",
                RESTAURANTS_CAT_ID, RESULTS_LIMIT, radius
            )
        ) as Resource<PlacesResponse>
    }

    /**
     * Get place details from the FourSquare API
     *
     * @param placeId A unique string identifier for a FSQ Place
     *
     * @return resource with place details or error
     */
    override suspend fun getPlaceDetails(placeId: String): Resource<PlaceDetails>
            = withContext(Dispatchers.IO) {
        handleResult(fourSquareApiService.getPlaceDetails(placeId, FIELDS))
                as Resource<PlaceDetails>
    }

    private fun <T> handleResult(result: Response<T>): Resource<Any> {
        val resource = if (result.isSuccessful) {
            result.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error(result.message())
        } else {
            try {
                val errorMessage = Gson().fromJson(result.errorBody()?.charStream(), ErrorMessage::class.java)
                Resource.Error(errorMessage.message ?: DEFAULT_ERROR)
            } catch (e: JsonSyntaxException) {
                Resource.Error(DEFAULT_ERROR)
            }
        }
        return resource
    }

    companion object {
        const val RESULTS_LIMIT = 50
        const val RESTAURANTS_CAT_ID = "13000"
        const val FIELDS = "photos,name,description,tel,location,email,menu,price,rating"
        const val DEFAULT_ERROR = "Something went wrong"
    }
}