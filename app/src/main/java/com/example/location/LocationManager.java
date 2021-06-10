package com.example.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationManager {

    private static LocationManager instance = null;
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder locationSettingsRequestBuilder;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationSettingsCallBack locationSettingsCallBack;
    private LocationCallback locationCallback;
    private Location lastLocation;

    private LocationManager() {
    }

    public static LocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationManager();
        }
        instance.init(context);
        return instance;
    }

    private void init(Context context) {
        this.context = context;
        this.locationSettingsCallBack = (LocationSettingsCallBack) context;
        setupLocationService();
    }

    //call in init
    void setupLocationService() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback() {
            @Override public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    Log.d("Location Update:",
                            "Lat: " + location.getLatitude() + "Long: " + location.getLongitude());
                    // Update UI with location data
                    // ...
                }
            }
        };
    }

    //this function actually gets the location
    //this function is called from onLocationSettingsSuccessful()
    Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override public void onSuccess(Location location) {
                if (location == null) {
                    Toast.makeText(context, "Could not fetch the location, Please try again!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    lastLocation = location;
                    Log.d("Last Location",
                            "Lat: " + location.getLatitude() + "Long: " + location.getLongitude());
                    //Toast.makeText(context,"Lat: " + location.getLatitude() + "Long: " +
                    // location.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return lastLocation;
    }

    //call this function on button click when location permissions are granted.
    void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        getCurrentLocationSettings(locationRequest);
    }

    //this function is called in chain with createLocationRequest()
    private void getCurrentLocationSettings(LocationRequest locationRequest) {
        locationSettingsRequestBuilder =
                new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        checkIfLocationSettingsAreOK();
    }

    //this function is called in chain with createLocationRequest() and getCurrentLocationSettings()
    private void checkIfLocationSettingsAreOK() {
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task =
                client.checkLocationSettings(locationSettingsRequestBuilder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                locationSettingsCallBack.onLocationSettingsSuccessful();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override public void onFailure(@NonNull Exception e) {
                locationSettingsCallBack.onLocationSettingsFailure(e);
            }
        });
    }

    //call this function in onResume()
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper());
    }

    //call this function in onPause()
    public void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //implement this in the activity/fragment
    interface LocationSettingsCallBack {
        //this function is called from checkIfLocationSettingsAreOK() and implemented in
        // activity/fragment
        void onLocationSettingsSuccessful();

        //this function is called from checkIfLocationSettingsAreOK() and implemented in
        // activity/fragment
        void onLocationSettingsFailure(Exception e);
    }
}
