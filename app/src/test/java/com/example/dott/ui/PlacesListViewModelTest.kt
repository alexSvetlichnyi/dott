package com.example.dott.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.dott.R
import com.example.dott.data.models.placeslist.Place
import com.example.dott.data.models.placeslist.PlacesResponse
import com.example.dott.data.network.Resource
import com.example.dott.data.repositories.PlacesRepository
import com.example.dott.ui.placeslist.PlacesListViewModel
import com.google.android.gms.maps.model.LatLng
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class PlacesListViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PlacesListViewModel
    @MockK
    private lateinit var application: Application
    @MockK
    private lateinit var placesRepository: PlacesRepository
    @MockK(relaxed = true)
    private lateinit  var observerError: Observer<String>
    @MockK(relaxed = true)
    private lateinit var observer: Observer<List<Place>>

    @Before
    fun setupMyTripViewModel() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
        viewModel = PlacesListViewModel(application, placesRepository)
        coEvery { placesRepository.getPlaces(LatLng(1.0, 1.0), 100) } returns Resource.Success(
            PlacesResponse(placesList())
        )
        coEvery { placesRepository.getPlaces(LatLng(1.0, 1.0), 0) } returns Resource.Error(
            "Error"
        )
        mockStrings()
    }

    @Test
    fun testShowPlacesLiveDataSetProperly()  {
        runTest {
            viewModel.showPlaces.observeForever(observer)

            viewModel.findCurrentPlaces(LatLng(1.0, 1.0), 100)

            assertEquals(placesList(), viewModel.showPlaces.value)
        }
    }

    @Test
    fun testSendErrorIfRadiusIsTooBig()  {
        runTest {
            viewModel.errorMessage.observeForever(observerError)

            viewModel.findCurrentPlaces(LatLng(1.0, 1.0), 100001)

            assertEquals("Your radius should be smaller then 100km", viewModel.errorMessage.value)
        }
    }

    @Test
    fun testSendErrorIfResultIsError()  {
        runTest {
            viewModel.errorMessage.observeForever(observerError)

            viewModel.findCurrentPlaces(LatLng(1.0, 1.0), 0)

            assertEquals("Error", viewModel.errorMessage.value)
        }
    }

    @Test
    fun testStoreShouldNotBeDisplayedIfExistInCache()  {
        runTest {
            viewModel.allPlaces.add(
                Place("id", null, "New place 1", null)
            )
            viewModel.showPlaces.observeForever(observer)

            viewModel.findCurrentPlaces(LatLng(1.0, 1.0), 100)

            assertEquals(arrayListOf(Place("id1", null, "New place 2", null)),
                viewModel.showPlaces.value)
        }
    }

    private fun placesList() = arrayListOf(Place("id", null, "New place 1", null),
        Place("id1", null, "New place 2", null))

    private fun mockStrings() {
        every { application.getString(R.string.radius_error) } returns
                "Your radius should be smaller then 100km"
    }
}