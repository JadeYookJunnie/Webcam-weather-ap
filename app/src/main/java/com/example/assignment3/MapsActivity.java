package com.example.assignment3;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.assignment3.R;
import com.example.assignment3.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.assignment3.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.internal.ui.AutocompleteImplFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
/**
 * Assignment 3 Application
 * Map view with location marker, weather, web cameras and search bar
 * @author Quinn Scouller
 * @author Jade Thomas
 * */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    //SearchView searchView;
    FusedLocationProviderClient fusedLocationClient;
    SupportMapFragment supportMapFragment;
    boolean permissionGranted;
    double lat;
    double lon;

    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    public static String TAG = "findLocation";

    public String weatherLat;
    public String weatherLon;
    //Weather information
    private String url = "https://api.openweathermap.org/data/2.5/weather?";
    private final String weatherAPIKey = "ea657cb5b971c1d927a642fce9b91f80";
    DecimalFormat df = new DecimalFormat("#.##");
    public String weatherUrlUpdated = "";

    private String nearbyUrl = "https://api.windy.com/api/webcams/v2/list/nearby=";
    private final String camAPIKey = "N19oL3N0ibP6uKjQ0VVDXR2cgB5CGosj";
    public String nearUrlUpdated = "";

//camera info
    public String[] textArray = new String[5];
    public String[] urlArray = new String[5];


    /**
     * on Create()
     * check location permissions
     * initialize search bar
     * call getWeather() and getCams()
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        //check location permissions
        checkPermissions();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyA5pUxD_2Xi1s-bga4itPVaq-VblEHmxg8", Locale.getDefault());
        }

        //initialise autocomplete for search bar
        AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.search);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Toast toast = Toast.makeText(MapsActivity.this, "Test failed", Toast.LENGTH_SHORT);
            }
            /**
             * OnPlaceSelected()
             * when a user has searched for a place, get its location
             * */
            @Override
            public void onPlaceSelected(Place place) {
                LatLng latLng = place.getLatLng();

                weatherLon = Double.toString(latLng.longitude);
                weatherLat = Double.toString(latLng.latitude);

                String radius = "250";

                weatherUrlUpdated = url + "lat="+weatherLat+"&lon="+weatherLon+"&appid="+weatherAPIKey;
                nearUrlUpdated = nearbyUrl + weatherLat + "," + weatherLon + "," + radius +"/orderby=distance/limit=5?show=webcams:location,image&key=N19oL3N0ibP6uKjQ0VVDXR2cgB5CGosj";
                Log.d(TAG, "onPlaceSelected: "+weatherUrlUpdated);
                Log.d(TAG, "onPlaceSelected:"+nearUrlUpdated);
                Log.d(TAG, "Methods called");
                getWeather(); //creates markers
                getCams();

            }
        });
        supportMapFragment.getMapAsync(this);
    }



    /**
     * getWeather method()
     * get the weather info from openweatherAPI
     * */
    public void getWeather(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, weatherUrlUpdated, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                String weatherType;
                try{
                    //get info from API
                    DecimalFormat df = new DecimalFormat("0.00");
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");

                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                    weatherType = jsonObjectWeather.getString("main");
                    String description = jsonObjectWeather.getString("description");

                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                    double temp = jsonObjectMain.getDouble("temp");
                    //change temp value to degrees celcius
                    String finalTemp = df.format(temp - 273.15) + (char)0x00B0 + "C";
                    String humidity = jsonObjectMain.getString("humidity");

                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                    String windSpeed = jsonObjectWind.getString("speed");

                    //String to display snippet.
                    String snip = description +", " + finalTemp + ", " + "Humidity: "+ humidity
                            + ", Wind Speed: " + windSpeed;

                    LatLng latLng = new LatLng(Double.parseDouble(weatherLat), Double.parseDouble(weatherLon));
                    if (weatherType.equals("Clouds")){
                        MarkerOptions cloudMarker = new MarkerOptions().position(latLng).title("Cloud").snippet(snip).icon(BitmapDescriptorFactory.fromResource(R.drawable.clouds));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        mMap.addMarker(cloudMarker);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    }
                    else if (weatherType.equals("Rain")){
                        MarkerOptions cloudMarker = new MarkerOptions().position(latLng).title("Rain").snippet(snip).icon(BitmapDescriptorFactory.fromResource(R.drawable.rain));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        mMap.addMarker(cloudMarker);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    }
                    else if (weatherType.equals("Clear")){
                        MarkerOptions cloudMarker = new MarkerOptions().position(latLng).title("Clear").snippet(snip).icon(BitmapDescriptorFactory.fromResource(R.drawable.clear));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        mMap.addMarker(cloudMarker);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    }
                    else if (weatherType.equals("Drizzle")){
                        MarkerOptions cloudMarker = new MarkerOptions().position(latLng).title("Drizzle").snippet(snip).icon(BitmapDescriptorFactory.fromResource(R.drawable.drizzle));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        mMap.addMarker(cloudMarker);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    }
                    else if (weatherType.equals("Thunderstorm")){
                        MarkerOptions cloudMarker = new MarkerOptions().position(latLng).title("Thunderstorm").snippet(snip).icon(BitmapDescriptorFactory.fromResource(R.drawable.thunderstorm));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        mMap.addMarker(cloudMarker);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    }
                    else if (weatherType.equals("Snow")){
                        MarkerOptions cloudMarker = new MarkerOptions().position(latLng).title("Snow").snippet(snip).icon(BitmapDescriptorFactory.fromResource(R.drawable.snow));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        mMap.addMarker(cloudMarker);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    }
                    else{
                        MarkerOptions cloudMarker = new MarkerOptions().position(latLng).title("Atmoshphere").snippet(snip).icon(BitmapDescriptorFactory.fromResource(R.drawable.atmosphere));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                        mMap.addMarker(cloudMarker);
                        supportMapFragment.getMapAsync(MapsActivity.this);
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(MapsActivity.this, "Could not find location", Toast.LENGTH_LONG);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    /**
     * Get cams method
     * Get the closest 5 webcams in a location
     * */
    public void getCams() {
        Log.d(TAG, "GetCams started");
        //error from here
        //Log.d(TAG, "404");
        StringRequest camRequest = new StringRequest(Request.Method.GET, nearUrlUpdated, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, "Response received");
                Log.d(TAG, "the response: " + response);
                JSONObject camLocation;
                JSONObject camInfo;
                JSONObject imageView;

                //GET API information
                try {
                    Log.d(TAG, "try started");
                    JSONObject jsonResponse = new JSONObject(response);
                    Log.d(TAG, "obj response" + jsonResponse);
                    JSONObject jsonResult =jsonResponse.getJSONObject("result");
                    JSONArray jsonArray = jsonResult.getJSONArray("webcams");
                    Log.d(TAG, "webcams"+jsonArray.length());

                    //for each camera
                    for(int i=0; i <= jsonArray.length(); i++) {

                        JSONObject webcamObject = jsonArray.getJSONObject(i);

                        //JSONObject imageObject = jsonImage.getJSONObject(i);
                        //String camLoc= webcamObject.getString("location");
                        camLocation = webcamObject.getJSONObject("location");
                        double camLat = camLocation.getDouble("latitude");
                        double camLon = camLocation.getDouble("longitude");
                        String webcamCity = camLocation.getString("city");
                        String webcamRegion = camLocation.getString("region");


                        LatLng latLng = new LatLng(camLat, camLon);

                        //do for every location
                        MarkerOptions camMarker = new MarkerOptions().position(latLng).title("Camera").icon(BitmapDescriptorFactory.fromResource(R.drawable.camera));

                        Marker marker = mMap.addMarker(camMarker);
                        //set tag for individual markers
                        marker.setTag(i);

                        supportMapFragment.getMapAsync(MapsActivity.this);

                        //get image
                        imageView = webcamObject.getJSONObject("image");
                        JSONObject image = imageView.getJSONObject("current");
                        String previewImage = image.getString("preview");

                        //camInfo = webcamObject.getJSONObject("webcams");
                        String webcamTitle = webcamObject.getString("title");

                        Log.d(TAG,"image: "+ previewImage);
                        Log.d(TAG,"Long test " + webcamTitle + webcamCity + webcamRegion);

                        //create location title
                        String text = webcamTitle + " " + webcamCity + ", " + webcamRegion;

                        //send to array so cameras can be selected seperately
                        textArray[i] = text;
                        urlArray[i] = previewImage;

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(@NonNull Marker marker) {

                                if(marker.getTitle().equals("Camera")){
                                    Integer index = (Integer) marker.getTag();

                                    String text = textArray[index];
                                    String url = urlArray[index];

                                    Intent cameraIntent = new Intent(MapsActivity.this, webcamactivity.class);

                                    //send information to activity
                                    cameraIntent.putExtra("title", text);
                                    cameraIntent.putExtra("image", url);

                                    //start activity
                                    startActivity(cameraIntent);

                                    Log.d(TAG, "onMarkerClick: " + text + " " + url);
                                }

                                return false;
                            }
                        });
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(MapsActivity.this, "Could not find location", Toast.LENGTH_LONG);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(camRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    /**
     * checkPermissions()
     * if permission not granted then ask for it
     * otherwise continue
     * */
    public void checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            askForPermission();
            getCurrentLocation();
        }
        else{
            permissionGranted = true;
            getCurrentLocation();
        }
    }

    /**
     * Asks user for location permission
     * uses ACCESS fine and coarse location
     * */
    public void askForPermission(){
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            // Check if permission has now been granted
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                Toast toast = Toast.makeText(this, "Location access granted", Toast.LENGTH_SHORT);
                                getCurrentLocation();
                                //Log.d(TAG, "findLocation " + lat + " " + lon);
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                Toast toast = Toast.makeText(this, "approximate location access granted", Toast.LENGTH_SHORT);
                                getCurrentLocation();
                            } else {
                                // No location access granted.
                                Toast toast = Toast.makeText(this, "access not granted", Toast.LENGTH_SHORT);
                                //Log.d(TAG, "findLocation " + lat + " " + lon);
                            }
                        }
                );

        // Launch the location permission request
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }



    /**
     * gets the currentlocation
     * uses the fusedLocationClient to get the location
     * */
    private void getCurrentLocation(){
        //initialise location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //if true
                if(location != null){
                    lat = location.getLatitude();
                    lon = location.getLongitude();

                    LatLng latLng= new LatLng(lat, lon);
                    Log.d(TAG, "findLocation " + lat + " " + lon);
                    //set marker
                    MarkerOptions options = new MarkerOptions().position(latLng).title("current location");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    mMap.addMarker(options);
                }
                else{
                    Toast toast = Toast.makeText(MapsActivity.this, "location not found", Toast.LENGTH_SHORT);
                }
            }
        });
        supportMapFragment.getMapAsync(this::onMapReady);
    }
}