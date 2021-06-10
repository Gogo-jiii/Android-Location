package com.example.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationManager.LocationTracker {

    Button btnGetLastLocation;
    TextView txtResult;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private PermissionManager permissionManager;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetLastLocation = findViewById(R.id.btnGetLastLocation);
        txtResult = findViewById(R.id.txtResult);

        permissionManager = PermissionManager.getInstance(this);
        locationManager = LocationManager.getInstance(this);

        btnGetLastLocation.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!permissionManager.checkPermissions(permissions)) {
                    permissionManager.askPermissions(MainActivity.this, permissions, 100);
                } else {
                    if (locationManager.isLocationEnabled()) {
                        Location location = locationManager.getLastLocation();
                        if (location != null) {
                            txtResult.setText("Last Location: \n" + "Lat: " + location.getLatitude() + "\n" +
                                    "Long: " + location.getLongitude());
                        } else {
                            Toast.makeText(MainActivity.this, "Could not fetch location.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        locationManager.createLocationRequest();
                    }
                }
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        locationManager.startLocationUpdates();
    }

    @Override protected void onPause() {
        locationManager.stopLocationUpdates();
        super.onPause();
    }

    @Override public void updateLocation(Location location) {
        if (location != null) {
            Toast.makeText(MainActivity.this,
                    "Updated Location: \n" + "Lat: " + location.getLatitude() + "\n" + "Long: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not fetch location.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode,
                                                     @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionManager.handlePermissionResult(MainActivity.this, 100, permissions,
                grantResults)) {
            locationManager.createLocationRequest();
        }
    }
}