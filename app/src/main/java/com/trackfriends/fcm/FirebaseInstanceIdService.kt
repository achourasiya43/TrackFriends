package com.elite.fcm

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by anil on 8/12/17.
 */
class FirebaseInstanceIdService : FirebaseInstanceIdService() {

    private val TAG = "MyAndroidFCMIIDService"

    override fun onTokenRefresh() {
        //Get hold of the registration token
        try {
            val refreshedToken = FirebaseInstanceId.getInstance().token
            //Log the token
            Log.d(TAG, "Refreshed token: " + refreshedToken!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}