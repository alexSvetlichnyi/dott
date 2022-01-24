# Dott

**Assignment:**<br />
Display restaurants around the user’s current location on a map<br />
○ Use the FourSquare Search API to query for restaurants:
https://developer.foursquare.com/docs/api/venues/search<br />

● Load more restaurants when the user pans the map.<br />
○ Cache results in-memory (no need to persist the cache).<br />
○ Read restaurants from the cache to show results early, but only if the restaurants
fit within the user’s current viewport.<br />
● Include a simple restaurant detail page.<br /><br />

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

**Working with application:**<br />
You will be promted to get location access during application cold start. If user allow location services then camera will use user's current location to navigate to it and show nearest 50 restaraunts. After zoom or pan actions application will add more places to the map. User is able to select any place to see short InfoWindow, if user press the window then PlaceDetails page will be open with image, address, phone number, rating and price.
