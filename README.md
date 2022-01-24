# Dott

**Assignment:**<br />
1) Display restaurants around the user’s current location on a map<br />
Use the FourSquare Search API to query for restaurants:
https://developer.foursquare.com/docs/api/venues/search<br />
2) Load more restaurants when the user pans the map.<br />
3) Cache results in-memory (no need to persist the cache).<br />
4) Read restaurants from the cache to show results early, but only if the restaurantsfit within the user’s current viewport.<br />
5) Include a simple restaurant detail page.<br /><br />

**Steps to install:**<br />
1) Download application from github<br />
2) Import project in Android Studio<br />
3) Please ensure that you have latest Android studio & build tools and emulator with Google Play services to have working maps feature <br />
4) Run application and enjoy:).<br />

**Third party libraries:**<br />
1) Hilt - DI framework <br />
2) Retrofit 2 - used for Network requests <br />
3) Glide - used for downloading images by Url<br />
4) play-services-maps - used for Google Maps <br />

**Working with the application:**<br />
You will be prompted to get location access during application cold start. If the user allows location services then the camera will use the user's current location to navigate to it and show the nearest 50 restaurants. After zoom or pan actions application will add more places to the map. The user is able to select any place to see short InfoWindow, if the user presses the window then the PlaceDetails page will be open with the image, address, phone number, rating, and price.
