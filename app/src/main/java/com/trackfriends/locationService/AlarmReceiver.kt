package com.trackfriends.locationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.trackfriends.beanClass.LocationInfo
import com.trackfriends.session.SessionManager


class AlarmReceiver : BroadcastReceiver(),
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private var sessionManager: SessionManager? = null
    var f_id:String = String()
    /*   private static final long INTERVAL = 10000 * 30;
    private static final long FASTEST_INTERVAL = 3000 * 30;*/


    internal var mLocationRequest: LocationRequest ?= null
    internal var mGoogleApiClient: GoogleApiClient ?= null
    internal var mCurrentLocation: Location? = null

    internal var mContext: Context ?= null

    internal var current_lat: Double = 0.toDouble()
    internal var current_long: Double = 0.toDouble()

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = INTERVAL
        mLocationRequest?.fastestInterval = FASTEST_INTERVAL
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        sessionManager = SessionManager(context)
        f_id = sessionManager?.user?.firebaseid.toString()
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        // For our recurring task, we'll just display a message
        // Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        mGoogleApiClient?.connect()
    }


    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient?.isConnected)
        startLocationUpdates()
    }

    protected fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        val pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this)
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "Firing onLocationChanged..............................................")
        mCurrentLocation = location
        if (null != mCurrentLocation) {
            current_lat = mCurrentLocation!!.latitude
            current_long = mCurrentLocation!!.longitude
            // Toast.makeText(mContext, "" + current_lat + "\n" + current_long, Toast.LENGTH_SHORT).show();
            startTrackLocationApi()
            val startPoint = Location("locationA")
            startPoint.latitude = location.latitude
            startPoint.longitude = location.longitude

            current_lat = location.latitude
            current_long = location.longitude

            var locationInfo = LocationInfo()
            if(sessionManager?.user != null){
                locationInfo.firebaseid = f_id
                locationInfo.latitude = current_lat
                locationInfo.longitude = current_long

                val database = FirebaseDatabase.getInstance().reference
                database.child("location").child(sessionManager?.user?.firebaseid!!).setValue(locationInfo)
            }



        }

    }


    private fun startTrackLocationApi() {

    }

    companion object {

        private val TAG = "MainActivity"
        /*private val INTERVAL = (1000 * 20).toLong()
        private val FASTEST_INTERVAL = (1000 * 10).toLong()*/

        private val INTERVAL: Long = 1
        private val FASTEST_INTERVAL: Long = 5
    }
}