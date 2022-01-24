package com.example.dott.data.models.placedetails


data class PlaceDetails (
  var description : String?,
  var location : Location?,
  var name : String,
  var photos : ArrayList<Photo> = arrayListOf(),
  var tel : String?,
  var price : Int?,
  var rating : Float?
)