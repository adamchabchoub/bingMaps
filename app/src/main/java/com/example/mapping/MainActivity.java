package com.example.mapping;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private MapView mMapView;
    private LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the map view
        mMapView = new MapView(this, MapRenderMode.VECTOR);
        mMapView.setCredentialsKey(getString(R.string.maps_api_key));
        ((FrameLayout) findViewById(R.id.map_view)).addView(mMapView);

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Get the LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 10, this);
        }

        // Setup the button click listener
        Button btnShowLocation = findViewById(R.id.btnShowLocation);
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserLocation();
            }
        });

        // Setup the SearchView and its listener
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when the user submits the query
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle changes to the query text if needed
                return false;
            }
        });
    }

    private void showUserLocation() {
        if (mMapView != null) {
            // Get the last known location
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    Geopoint userLocation = new Geopoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMapView.setScene(MapScene.createFromLocationAndZoomLevel(userLocation, 15), MapAnimationKind.BOW);
                }
            }
        }
    }

    private void performSearch(String query) {
        // Execute the LocalSearchTask to perform the search
        LocalSearchTask localSearchTask = new LocalSearchTask();
        localSearchTask.execute(query);
    }

    // Implement the LocationListener methods
    @Override
    public void onLocationChanged(Location location) {
        // Update the map with the new location
        if (mMapView != null) {
            Geopoint userLocation = new Geopoint(location.getLatitude(), location.getLongitude());
            mMapView.setScene(MapScene.createFromLocationAndZoomLevel(userLocation, 15), MapAnimationKind.BOW);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status changes if needed
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Handle provider enabled
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Handle provider disabled
    }

    // Override other lifecycle methods as needed

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private class LocalSearchTask extends AsyncTask<String, Void, List<MapElement>> {


        private static final String BING_MAPS_SEARCH_API_URL = "https://dev.virtualearth.net/REST/v1/LocalSearch/?query=%s&key=%s";

        @Override
        protected List<MapElement> doInBackground(String... params) {
            String query = params[0];
            List<MapElement> searchResults = new ArrayList<>();

            try {
                // Construct the Bing Maps Local Search API URL
                String apiUrl = String.format(BING_MAPS_SEARCH_API_URL, query, R.string.maps_api_key);
                URL url = new URL(apiUrl);

                // Open connection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources");
                Toast.makeText(MainActivity.this,"hello"+ results, Toast.LENGTH_SHORT).show();
                // Extract information from each result
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    JSONObject point = result.getJSONObject("point");
                    JSONArray coordinates = point.getJSONArray("coordinates");

                    double latitude = coordinates.getDouble(0);
                    double longitude = coordinates.getDouble(1);

                    // Create a MapIcon for each location
                    MapIcon mapIcon = new MapIcon();
                    mapIcon.setLocation(new Geopoint(latitude, longitude));
                    searchResults.add(mapIcon);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return searchResults;
        }

        @Override
        protected void onPostExecute(List<MapElement> searchResults) {
            // Add the search results (MapIcons) to the map
            if (mMapView != null) {
                MapElementLayer mapElementLayer = new MapElementLayer();

                for (MapElement mapElement : searchResults) {
                    mapElementLayer.getElements().add(mapElement);
                }

                mMapView.getLayers().add(mapElementLayer);
            }
        }
    }
}
