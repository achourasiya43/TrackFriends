package com.trackfriends.app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth






/**
 * Created by Anil on 31-05-2018.
 */
class TrackFraiends : Application() {

    private var INSTANCE: TrackFraiends? = null

    private fun Singleton() {};


    fun getInstance(): TrackFraiends {
        if (INSTANCE == null) {
            INSTANCE = TrackFraiends()
        }
        return INSTANCE!!
    }

    override fun onCreate() {
        super.onCreate()
    }
}