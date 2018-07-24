package com.trackfriends.activity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.animation.LinearInterpolator
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.trackfriends.R
import com.trackfriends.beanClass.LocationInfo
import com.google.android.gms.maps.model.Marker
import com.trackfriends.beanClass.UserInfo
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.trackfriends.utils.Constant
import com.trackfriends.utils.GoogleDirection


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
        LocationListener,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private var mMap: GoogleMap? = null
    internal var marker: Marker? = null
    internal var locationInfo: LocationInfo? = null
    internal var userInfo: UserInfo? = null
    internal var userId = ""
    internal var mapFragment: SupportMapFragment? = null
    private var gd: GoogleDirection? = null
    private var start: LatLng? = null
    private var end: LatLng? = null

    //for location update
    private val TAG = "TOGO"
    private var INTERVAL = 1
    private var FASTEST_INTERVAL = 300
    internal var mLocationRequest: LocationRequest = LocationRequest()
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var lat: Double? = null
    internal var lng:Double? = null
    internal var lmgr: LocationManager?=null
    private var isGPSEnable: Boolean = false

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL.toLong()
        mLocationRequest.fastestInterval = FASTEST_INTERVAL.toLong()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (intent.getStringExtra("userId") != null) {
            userId = intent.getStringExtra("userId")
            getUserInfo(userId)
        }

        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment

        gd = GoogleDirection(this)
        //location update............
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap?.setMyLocationEnabled(false)

        if (locationInfo != null && locationInfo != null) {
            val loc = LatLng(locationInfo?.latitude!!, locationInfo?.longitude!!)
            //mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,18f))
            pointToPosition(loc)
            makeMarker(userInfo?.name.toString(), loc)
            drowRouteAnimated(GoogleDirection.MODE_WALKING, R.drawable.ico_current_red, R.color.colorPrimary)

        }
    }

    private fun drowRouteAnimated(MODE: String, ico_current_red: Int, colorPrimary: Int) {
        if (gd != null) {

            gd?.setOnDirectionResponseListener { status, doc, gd ->

                gd.animateDirection(mMap, gd.getDirection(doc), 0, false, false, false, false, null, false, false, PolylineOptions().width(6f).color(resources.getColor(R.color.colorPrimary)))

                mMap?.addMarker(MarkerOptions().position(start!!)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_current_red)))

                mMap?.addMarker(MarkerOptions().position(end!!)
                        .icon(BitmapDescriptorFactory.fromResource(ico_current_red)))

                val options = PolylineOptions().width(8f).color(resources.getColor(colorPrimary)).geodesic(true)
                val arr_pos = gd.getDirection(doc)

                for (z in arr_pos.indices) {
                    val point = arr_pos[z]
                    options.add(point)
                }
                mMap?.addPolyline(options)


            }
        }
    }

    private fun pointToPosition(position: LatLng) {
        //Build camera position
        val cameraPosition = CameraPosition.Builder()
                .target(position)
                .zoom(19f).build()
        //Zoom in and animate the camera.
        mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun makeMarker(title: String, latLng: LatLng) {
        marker = mMap?.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.placeholder_marker))
                .title(title)
                .position(latLng))


    }

    internal fun getLocationFromFCM(userId: String) {
        FirebaseDatabase.getInstance().reference.child("location").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.getValue(LocationInfo::class.java) != null) {

                    locationInfo = dataSnapshot.getValue(LocationInfo::class.java)

                    if (marker == null)
                        mapFragment?.getMapAsync(this@MapsActivity)

                    end = LatLng(locationInfo?.latitude!!, locationInfo?.longitude!!)
                    animateMarker(end!!, marker)

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    internal fun getUserInfo(userId: String) {
        FirebaseDatabase.getInstance().reference.child("users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.getValue(UserInfo::class.java) != null) {
                    userInfo = dataSnapshot.getValue(UserInfo::class.java)
                    getLocationFromFCM(userId)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun animateMarker(destination: LatLng, marker: Marker?) {
        if (marker != null) {
            val startPosition = marker.position
            val endPosition = destination

            val startRotation = marker.rotation

            val latLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 1000 // duration 1 second
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                    marker.setPosition(newPosition)
                    //marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                } catch (ex: Exception) {
                    // I don't care atm..
                }
            }

            valueAnimator.start()
        }
    }

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }


    // location update.....................................................

    override fun onLocationChanged(p0: Location?) {
        lat = p0?.getLatitude()
        lng = p0?.getLongitude()
        if (lat != null && lng != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }

    override fun onConnected(p0: Bundle?) {
        if(lat == null && lng == null){
            startLocationUpdates() }
    }
    override fun onConnectionSuspended(p0: Int) {}
    override fun onConnectionFailed(p0: ConnectionResult) {}
    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect() }
    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect() }
    override fun onPause() {
        super.onPause()
        stopLocationUpdates() }
    protected fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        Constant.ACCESS_FINE_LOCATION)
            } else {
                val pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        } else {
            val pendingResult = LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this) }
        Log.d(TAG, "Location update started ..............: ")
    }
    protected fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        Log.d(TAG, "Location update stopped .......................")
    }
    //....................................................................................................

    fun isGpsEnable(): Boolean {
        isGPSEnable = lmgr!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGPSEnable) {
            val ab = android.support.v7.app.AlertDialog.Builder(this)
            ab.setTitle(R.string.gps_not_enable)
            ab.setMessage(R.string.do_you_want_to_enable)
            ab.setCancelable(false)
            ab.setPositiveButton(R.string.settings, DialogInterface.OnClickListener { dialog, which ->
                isGPSEnable = true
                val `in` = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(`in`)
            })
            ab.show() }
        return isGPSEnable
    }



}
