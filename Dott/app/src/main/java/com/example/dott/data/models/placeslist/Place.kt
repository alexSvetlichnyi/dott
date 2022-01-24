package com.example.dott.data.models.placeslist

import com.example.dott.data.models.placedetails.Location
import com.google.gson.annotations.SerializedName

data class Place (
  @SerializedName("fsq_id") var fsqId: String,
  var geocodes: Geocodes? = null,
  var name: String,
  var location : Location?,
)