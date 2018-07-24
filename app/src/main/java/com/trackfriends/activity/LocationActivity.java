package com.trackfriends.activity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trackfriends.R;
import com.trackfriends.beanClass.LocationInfo;
import com.trackfriends.beanClass.UserInfo;
import com.trackfriends.utils.GoogleDirection;


import java.util.ArrayList;

/**
 * Created by Anil on 16-06-2018.
 */

public class LocationActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private static final String TAG = "MapsActivity";
    private static final long INTERVAL = 1;
    private static final long FASTEST_INTERVAL = 3000;
    Marker marker;
    LocationInfo locationInfo;
    UserInfo userInfo;
    String userId = "";
    SupportMapFragment mapFragment;
    private GoogleDirection gd;
    private LatLng start;
    private LatLng end;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (getIntent().getStringExtra("userId") != null) {
            userId = getIntent().getStringExtra("userId");
            getUserInfo(userId);
        }
        gd = new GoogleDirection(this);
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);

    }

    void getLocationFromFCM(String userId) {
        FirebaseDatabase.getInstance().getReference().child("location").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(LocationInfo.class) != null)
                    locationInfo = dataSnapshot.getValue(LocationInfo.class);

                end = new LatLng(locationInfo.getLatitude(),locationInfo.getLongitude());
                if (marker == null)
                    mapFragment.getMapAsync(LocationActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void getUserInfo(String userId) {
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(UserInfo.class) != null) {
                    userInfo = dataSnapshot.getValue(UserInfo.class);
                    getLocationFromFCM(userId);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop fired ..............");

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();

    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) LocationActivity.this);
        Log.e(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == 1) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        start = latLng;
        animateMarker(location, marker);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "On Pause call...............................");

    }

    protected void stopLocationUpdates() {

       /* LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.e(TAG, "Location update stopped .......................");*/
    }

    @Override
    public void onResume() {
        super.onResume();

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

    }


    public void animateMarker(final Location destination, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.getLatitude(), destination.getLongitude());

            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
                        //marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                    } catch (Exception ex) {
                        // I don't care atm..
                    }
                }
            });

            valueAnimator.start();
        }
    }


    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }


    public void makeMarker(String title, LatLng latLng) {
        marker = mMap.addMarker(new MarkerOptions()
                .icon((BitmapDescriptorFactory
                        .fromResource(R.drawable.placeholder_marker)))
                .title(title)
                .position(latLng));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(false);

        if (locationInfo != null && locationInfo != null) {
            LatLng loc = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());
            pointToPosition(loc);
            makeMarker("Anil", loc);
        }
    }

    private void pointToPosition( LatLng position) {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(19f).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void drowRouteAnimated(String MODE, final int ico_current_red, final int colorPrimary) {
        if (gd != null) {

            gd.setOnDirectionResponseListener((GoogleDirection.OnDirectionResponseListener) (status, doc, gd) -> {

                gd.animateDirection(mMap, gd.getDirection(doc),0
                        , false, false, false, false, null, false, false, new PolylineOptions().width(6).color(getResources().getColor(R.color.colorPrimary)));

            /*    mMap.addMarker(new MarkerOptions().position(start)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_current_red)));*/

                mMap.addMarker(new MarkerOptions().position(end)
                        .icon(BitmapDescriptorFactory.fromResource(ico_current_red)));

                PolylineOptions options = new PolylineOptions().width(8).color(getResources().getColor(colorPrimary)).geodesic(true);
                ArrayList<LatLng> arr_pos = gd.getDirection(doc);

                for (int z = 0; z < arr_pos.size(); z++) {
                    LatLng point = arr_pos.get(z);
                    options.add(point);
                }
                mMap.addPolyline(options);
            });
            if(start != null && end != null)
                gd.request(start, end, MODE);
        }

    }


}
