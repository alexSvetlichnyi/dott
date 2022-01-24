package com.example.dott.ui.placedetails

import android.app.Application
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.dott.R
import com.example.dott.application.BaseAndroidViewModel
import com.example.dott.application.SingleLiveData
import com.example.dott.data.models.placedetails.PlaceDetails
import com.example.dott.data.network.Resource
import com.example.dott.data.repositories.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaceDetailsViewModel @Inject constructor (
    app: Application,
    private val placesRepository: PlacesRepository
) : BaseAndroidViewModel(app) {

    val imageUrl: ObservableField<String> by lazy { ObservableField<String>() }
    val description: ObservableField<String> by lazy { ObservableField<String>() }
    val address: ObservableField<String> by lazy { ObservableField<String>() }
    val phone: ObservableField<String> by lazy { ObservableField() }
    val price: ObservableField<String> by lazy { ObservableField() }
    val rating: ObservableField<String> by lazy { ObservableField() }

    val updateToolBar: SingleLiveData<String> by lazy { SingleLiveData() }
    val errorMessage: SingleLiveData<String> by lazy { SingleLiveData() }

    fun getPlaceDetails(itemId: String) {
        viewModelScope.launch {
            when(val result = placesRepository.getPlaceDetails(itemId)) {
                is Resource.Success -> {
                    bindData(result)
                    updateToolBar.postValue(result.data.name)
                }
                is Resource.Error -> errorMessage.postValue(result.errorMessage)
            }
        }
    }

    private fun bindData(result: Resource.Success<PlaceDetails>) {
        val photos = result.data.photos
        if (photos.isNotEmpty()) {
            val photo = photos[0]
            imageUrl.set("${photo.prefix}original${photo.suffix}")
        }
        // Set place description if available
        description.set(result.data.description)
        result.data.location?.let {
            address.set(
                getString(R.string.address, it.country, it.locality, it.postcode, it.address)
            )
        }

        // Set place phone if available
        result.data.tel?.let {
            phone.set(getString(R.string.phone, result.data.tel))
        }

        // Set place price if available
        result.data.price?.let {
            val price = getString(
                when (result.data.price) {
                    1 -> R.string.cheap
                    2 -> R.string.moderate
                    3 -> R.string.expensive
                    4 -> R.string.very_expensive
                    else -> R.string.unknown
                }
            )
            phone.set(getString(R.string.price, price))
        }

        // Set place rating if available
        result.data.rating?.let {
            phone.set(getString(R.string.rating, result.data.rating))
        }
    }

    companion object {
        @BindingAdapter("imageUrl")
        @JvmStatic
        fun setImageSource(imageView: ImageView, imageUrl: String?) {
            Glide.with(imageView).asBitmap().load(imageUrl).into(imageView)
        }
    }
}
