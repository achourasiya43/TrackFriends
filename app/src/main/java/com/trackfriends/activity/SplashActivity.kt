package com.trackfriends.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.trackfriends.R
import com.trackfriends.session.SessionManager

class SplashActivity : AppCompatActivity() {
    var session:SessionManager ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        session = SessionManager(this)

        Handler().postDelayed({
            /* Create an Intent that will start the Menu-Activity. */
           if (session!!.isLoggedIn){
               val mainIntent = Intent(this, MainActivity::class.java)
               startActivity(mainIntent)
               finish()
           }else{
               val mainIntent = Intent(this, LoginActivity::class.java)
               startActivity(mainIntent)
               finish()
           }


        }, 3000)
    }
}
