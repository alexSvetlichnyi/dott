package com.example.dott.repositories

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.dott.R
import com.example.dott.data.models.placedetails.PlaceDetails
import com.example.dott.data.models.placeslist.Place
import com.example.dott.data.models.placeslist.PlacesResponse
import com.example.dott.data.network.FourSquareApiService
import com.example.dott.data.network.Resource
import com.example.dott.data.repositories.PlacesRepository
import com.example.dott.data.repositories.PlacesRepositoryImpl
import com.example.dott.ui.placeslist.PlacesListViewModel
import com.google.android.gms.maps.model.LatLng
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Response

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class PlacesRepositoryTest {
    private lateinit var placesRepository: PlacesRepository
    @MockK
    private lateinit var fourSquareApiService: FourSquareApiService

    @Before
    fun setupMyTripViewModel() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
        placesRepository = PlacesRepositoryImpl(fourSquareApiService)
        coEvery { fourSquareApiService.getPlaces(any(), any(), any(), 100, any()) } returns
                Response.success(getPlacesResponse())
        coEvery { fourSquareApiService.getPlaces(any(), any(), any(), 0, any()) } returns
                error("message")
        coEvery { fourSquareApiService.getPlaces(any(), any(), any(), 50, any()) } returns
                error("broken_key")
    }

    private fun getPlacesResponse() = PlacesResponse(arrayListOf(Place(fsqId = "id")))

    @Test
    fun testGetPlacesApiSuccessConvertedToResourceProperly()  {
        runTest {
            val response = placesRepository.getPlaces(LatLng(1.0, 1.0), 100) as Resource.Success

            assertEquals(getPlacesResponse(), response.data)
        }
    }

    @Test
    fun testGetPlacesApiBrokenErrorConvertedToResourceProperly()  {
        runTest {
            val response = placesRepository.getPlaces(LatLng(1.0, 1.0), 50) as Resource.Error

            assertEquals("Something went wrong", response.errorMessage)
        }
    }

    @Test
    fun testGetPlacesApiErrorConvertedToResourceProperly()  {
        runTest {
            val response = placesRepository.getPlaces(LatLng(1.0, 1.0), 0) as Resource.Error

            assertEquals("error", response.errorMessage)
        }
    }

    private fun <T> error(key: String): Response<T> =
        Response.error(
            403,
            ResponseBody.create(
                MediaType.parse("application/json"),
                "{\"${key}\":\"error\"}"
            )
        )
}