package com.example.cabapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleAPICLient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private final int LOCATION_REQUEST_CODE = 1;
    SupportMapFragment mapFragment;
    private Button driverLogout,driverSettings;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private boolean currentLogOutDriverStatus=false;
    private DatabaseReference assignedCustomerRef,assignedCustomerPickupRef;
    private String driverID,customerID="";
    private Marker customerMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);

        driverLogout=findViewById(R.id.driver_logout_btn);
        driverSettings=findViewById(R.id.driver_setting_btn);
        mAuth=FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();
        driverID=mAuth.getCurrentUser().getUid();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        driverLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogOutDriverStatus=true;
                discconectDriver();
                mAuth.signOut();
                logOutDriver();
            }
        });
        
         getAssignedCustomerRequest();
    }

    private void getAssignedCustomerRequest() {
        assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(driverID).child("customerRideID");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    customerID=snapshot.getValue().toString();
                    getAssignedCustomerPickUpLocation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAssignedCustomerPickUpLocation() {
        assignedCustomerPickupRef=FirebaseDatabase.getInstance().getReference().child("Customers Request").child(customerID)
                .child("l");
        assignedCustomerPickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    List<Object> customerLocationMap= (List<Object>) snapshot.getValue();
                    double locationLat=0;
                    double locationLong=0;
                    if(customerLocationMap.get(0)!=null)
                    {
                        locationLat=Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if(customerLocationMap.get(1)!=null)
                    {
                        locationLong=Double.parseDouble(customerLocationMap.get(1).toString());
                    }
                    LatLng driverLatLng=new LatLng(locationLat,locationLong);
                    customerMarker =mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Pickup Location"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void logOutDriver() {
        Intent welcomeIntent=new Intent(DriverMapActivity.this,WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleAPIClient();
        mMap.setMyLocationEnabled(true);
    }

    private synchronized void buildGoogleAPIClient() {
        mGoogleAPICLient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleAPICLient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPICLient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null)
        {
            mLastLocation = location;
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            if(FirebaseAuth.getInstance().getCurrentUser()!=null)
            {
                String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference driverAvailablityRef= FirebaseDatabase.getInstance().getReference().child("driversAvailable");
                DatabaseReference driverWorkingRef= FirebaseDatabase.getInstance().getReference().child("driversWorking");
                GeoFire geoFireAvaialable=new GeoFire(driverAvailablityRef);
                GeoFire geoFireWorking=new GeoFire(driverWorkingRef);

                //when the customer is not assigned then avaiable should be there otherwise working
                switch (customerID){
                    case "":
                        geoFireWorking.removeLocation(userID);
                        geoFireAvaialable.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                        break;
                    default:
                        geoFireAvaialable.removeLocation(userID);
                        geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                        break;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mapFragment.getMapAsync(this);
            } else {
                Toast.makeText(this, "Please provide the perm", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!currentLogOutDriverStatus){
            discconectDriver();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!currentLogOutDriverStatus){
            discconectDriver();
        }
    }

    private void discconectDriver() {
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            String userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverAvailablityRef= FirebaseDatabase.getInstance().getReference().child("Drivers Available");
            GeoFire geoFire=new GeoFire(driverAvailablityRef);
            geoFire.removeLocation(userID);
        }
    }
}