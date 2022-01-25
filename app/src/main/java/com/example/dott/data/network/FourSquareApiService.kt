package com.example.dott.data.network

import com.example.dott.data.models.placedetails.PlaceDetails
import com.example.dott.data.models.placeslist.PlacesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FourSquareApiService {
    @GET("v3/places/search")
    suspend fun getPlaces(@Query ("ll") ll: String,
                          @Query ("categories") categories: String,
                          @Query ("limit") limit: Int,
                          @Query ("radius") radius: Int,
                          @Query ("sort") sortedBy: String): Response<PlacesResponse>

    @GET("v3/places/{id}")
    suspend fun getPlaceDetails(@Path("id") placeId: String,
                                @Query ("fields") categories: String): Response<PlaceDetails>
}