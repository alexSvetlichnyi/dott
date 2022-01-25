package com.example.dott.data.models.placedetails

data class PlaceDetails (
  var description : String? = null,
  var location : Location? = null,
  var name : String = "",
  var photos : ArrayList<Photo> = arrayListOf(),
  var tel : String? = null,
  var price : Int? = null,
  var rating : Float? = null
)