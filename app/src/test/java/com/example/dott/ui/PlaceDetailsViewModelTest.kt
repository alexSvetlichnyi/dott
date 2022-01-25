package com.example.dott.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.dott.R
import com.example.dott.data.models.placedetails.Location
import com.example.dott.data.models.placedetails.Photo
import com.example.dott.data.models.placedetails.PlaceDetails
import com.example.dott.data.network.Resource
import com.example.dott.data.repositories.PlacesRepository
import com.example.dott.ui.placedetails.PlaceDetailsViewModel
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
class PlaceDetailsViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PlaceDetailsViewModel
    @MockK
    private lateinit var application: Application
    @MockK
    private lateinit var placesRepository: PlacesRepository
    @MockK(relaxed = true)
    private lateinit  var observer: Observer<String>

    @Before
    fun setupMyTripViewModel() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        MockKAnnotations.init(this)
        viewModel = PlaceDetailsViewModel(application, placesRepository)
        coEvery { placesRepository.getPlaceDetails(eq("id")) } returns Resource.Success(
            getPlaceObject()
        )
        coEvery { placesRepository.getPlaceDetails(eq("error")) } returns Resource.Error(
            "Error"
        )
        coEvery { placesRepository.getPlaceDetails(eq("empty")) } returns Resource.Success(
            getEmptyPlaceObject()
        )
        mockStrings()
    }

    @Test
    fun testToolBarUpdate()  {
        runTest {
            viewModel.updateToolBar.observeForever(observer)

            viewModel.getPlaceDetails("id")

            assertEquals("name", viewModel.updateToolBar.value)
        }
    }

    @Test
    fun testErrorMessage()  {
        runTest {
            viewModel.errorMessage.observeForever(observer)

            viewModel.getPlaceDetails("error")

            assertEquals("Error", viewModel.errorMessage.value)
        }
    }

    @Test
    fun testDataParsedProperly()  {
        runTest {
            viewModel.getPlaceDetails("id")

            assertEquals("description", viewModel.description.get())
            assertEquals("Phone: 1234567890", viewModel.phone.get())
            assertEquals("Price: Cheap", viewModel.price.get())
            assertEquals("Rating: 9.1", viewModel.rating.get())
            assertEquals("https://photo/original/test", viewModel.imageUrl.get())
            assertEquals("Address: US, New York, 10001, main street", viewModel.address.get())
        }
    }

    @Test
    fun testEmptyObject()  {
        runTest {
            viewModel.getPlaceDetails("empty")

            assertEquals(null, viewModel.description.get())
            assertEquals(null, viewModel.phone.get())
            assertEquals(null, viewModel.price.get())
            assertEquals(null, viewModel.rating.get())
            assertEquals(null, viewModel.address.get())
        }
    }

    @Test
    fun testMapPrice()  {
        viewModel.mapPrice(1)
        assertEquals("Cheap", viewModel.mapPrice(1))
        assertEquals("Moderate", viewModel.mapPrice(2))
        assertEquals("Expensive", viewModel.mapPrice(3))
        assertEquals("Very Expensive", viewModel.mapPrice(4))
        assertEquals("Unknown", viewModel.mapPrice(5))
    }

    private fun getPlaceObject() = PlaceDetails(
        description = "description",
        location = Location(
            address = "main street", country = "US",
            locality = "New York", postcode = "10001"
        ),
        name = "name",
        photos = arrayListOf(Photo("https://photo/", "/test")),
        tel = "1234567890",
        price = 1,
        rating = 9.1f
    )

    private fun getEmptyPlaceObject() = PlaceDetails(
        description = null,
        location = null,
        name = "name",
        photos = arrayListOf(),
        tel = null,
        price = null,
        rating = null
    )

    private fun mockStrings() {
        every {
            application.getString(
                R.string.address, "US", "New York", "10001",
                "main street"
            )
        } returns "Address: US, New York, 10001, main street"
        every { application.getString(R.string.phone, "1234567890") } returns
                "Phone: 1234567890"
        every { application.getString(R.string.price, "Cheap") } returns
                "Price: Cheap"
        every { application.getString(R.string.rating, "9.1") } returns
                "Rating: 9.1"
        every { application.getString(R.string.phone, "1234567890") } returns
                "Phone: 1234567890"
        every { application.getString(R.string.cheap) } returns
                "Cheap"
        every { application.getString(R.string.moderate) } returns
                "Moderate"
        every { application.getString(R.string.expensive) } returns
                "Expensive"
        every { application.getString(R.string.very_expensive) } returns
                "Very Expensive"
        every { application.getString(R.string.unknown) } returns
                "Unknown"
    }
}