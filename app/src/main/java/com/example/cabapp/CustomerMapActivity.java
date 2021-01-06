package com.example.cabapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleAPICLient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private final int LOCATION_REQUEST_CODE = 1;
    private String userID = "";
    private LatLng customerPickupLocation;
    private SupportMapFragment mapFragment;
    private Button customerLogout, customerSettings, callACab;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference customerDatabaseRef, driverAvailableDatabaseRef, driverRef, driverWorkingRef;
    private double radius = 1;
    private Boolean driverFound = false, requestType = false;
    private String driverID;
    private Marker driverMarker, PickUpMarker;
    private ValueEventListener driverLocationRefListener;
    private GeoQuery geoQuery;
    private TextView txtName, txtPhone, txtCarName;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;
    private String destination;
    private RadioGroup mRadioGroup;
    private String mService;
    private RadioButton radioButton;
    private int selectedID;
    private LatLng destinationLatLng;
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private Boolean requestBol = false;

    // Set the fields to specify which types of place data to
    // return after the user has made a selection.
    List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG, Place.Field.NAME);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        destinationLatLng = new LatLng(0.0,0.0);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyA7Be5NnYagvV0f3Fhh866N9CDTWK2kYXY");
        }

        customerLogout = findViewById(R.id.customer_logout_btn);
        customerSettings = findViewById(R.id.customer_setting_btn);
        callACab = findViewById(R.id.customer_call_a_cab);

        txtName = findViewById(R.id.name_driver);
        txtPhone = findViewById(R.id.phone_driver);
        txtCarName = findViewById(R.id.car_name_driver);
        profilePic = findViewById(R.id.profile_image_driver);
        relativeLayout = findViewById(R.id.rel1);
        mRadioGroup=findViewById(R.id.radioGroupCustomer);
        selectedID=mRadioGroup.getCheckedRadioButtonId();

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        userID = mCurrentUser.getUid();
        customerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customers Request");
        driverAvailableDatabaseRef = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        driverWorkingRef = FirebaseDatabase.getInstance().getReference().child("driversWorking");

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }

        customerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerMapActivity.this, SettingsActivity.class);
                intent.putExtra("type", "Customers");
                startActivity(intent);
            }
        });

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(fields);
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NotNull Place place) {
                    destination = place.getName();
                    destinationLatLng = place.getLatLng();
                }

                @Override
                public void onError(@NotNull Status status) {

                }
            });
        }

        customerLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                logOutCustomer();
            }
        });

        callACab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestType) {
                    requestType = false;
                    geoQuery.removeAllListeners();
                    driverWorkingRef.removeEventListener(driverLocationRefListener);
                    if (driverFound != null) {
                        driverRef = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Drivers").child(driverID).child("customerRequest");

                        driverRef.removeValue();

                        driverID = null;
                    }

                    driverFound = false;
                    radius = 1;

                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    GeoFire geoFire = new GeoFire(customerDatabaseRef);
                    geoFire.removeLocation(customerId);

                    if (PickUpMarker != null) {
                        PickUpMarker.remove();
                    }
                    if (driverMarker != null) {
                        driverMarker.remove();
                    }

                    callACab.setText("Call a Cab");
                    relativeLayout.setVisibility(View.GONE);

                } else {
                    requestType = true;

                    GeoFire geoFire = new GeoFire(customerDatabaseRef);
                    geoFire.setLocation(userID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    customerPickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    PickUpMarker = mMap.addMarker(new MarkerOptions().position(customerPickupLocation).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));

                    callACab.setText("Fetching Drivers ...");
                    getClosestDriver();
                }
            }
        });


    }


    private void getClosestDriver() {
        GeoFire geoFire = new GeoFire(driverAvailableDatabaseRef);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickupLocation.latitude, customerPickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestType) {
                    DatabaseReference mDataBaseRef=FirebaseDatabase.getInstance().getReference().child("Users")
                            .child("Drivers").child(key);
                    mDataBaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                Map<String,Object> driverMap= (Map<String, Object>) dataSnapshot.getValue();
                                if(driverFound)
                                {
                                    return;
                                }
                                selectedID = mRadioGroup.getCheckedRadioButtonId();
                                radioButton=findViewById(selectedID);
                                mService=radioButton.getText().toString();
                                if(driverMap.get("service").equals(mService))
                                {
                                    driverFound = true;
                                    driverID = dataSnapshot.getKey();
                                    driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID)
                                            .child("customerRequest");
                                    HashMap map = new HashMap();
                                    map.put("customerRideID", userID);
                                    map.put("destination",destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);
                                    gettingDriverLocation();
                                    getHasRideEnded();
                                    callACab.setText("Looking for driver location");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound) {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void gettingDriverLocation() {
        driverLocationRefListener = driverWorkingRef.child(driverID).child("l").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && requestType) {
                    List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    callACab.setText("Driver Found");

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedDriverInformation();

                    if (driverLocationMap != null) {
                        if (driverLocationMap.get(0) != null) {
                            locationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                        }
                        if (driverLocationMap.get(1) != null) {
                            locationLong = Double.parseDouble(driverLocationMap.get(1).toString());
                        }
                    }

                    Location location1 = new Location("");
                    location1.setLatitude(customerPickupLocation.latitude);
                    location1.setLongitude(customerPickupLocation.longitude);

                    Location location2 = new Location("");
                    location2.setLatitude(locationLat);
                    location2.setLongitude(locationLong);

                    float distance = location1.distanceTo(location2);
                    if (distance < 90) {
                        callACab.setText("Driver's Reached");
                    } else {
                        callACab.setText("Driver Found: " + distance + "m");
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLong);
                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your driver is here").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void logOutCustomer() {
        Intent welcomeIntent = new Intent(CustomerMapActivity.this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
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
        if (getApplicationContext() != null) {
            mLastLocation = location;
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
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

    private void getAssignedDriverInformation() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();
                    String car = dataSnapshot.child("car").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);
                    txtCarName.setText(car);

                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(profilePic);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("customerRequest").child("customerRideID");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        requestBol = false;
        geoQuery.removeAllListeners();
        driverRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if (driverID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("customerRequest");
            driverRef.removeValue();
            driverID = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(PickUpMarker != null){
            PickUpMarker.remove();
        }
        if (driverMarker != null){
            driverMarker.remove();
        }
        callACab.setText("call Uber");

        relativeLayout.setVisibility(View.GONE);
        txtName.setText("");
        txtPhone.setText("");
        txtCarName.setText("Destination: --");
        profilePic.setImageResource(R.drawable.profile);
    }
}