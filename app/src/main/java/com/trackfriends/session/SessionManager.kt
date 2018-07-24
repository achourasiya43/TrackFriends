package com.trackfriends.session

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.gson.Gson
import com.trackfriends.activity.LoginActivity
import com.trackfriends.beanClass.UserInfo

/**
 * Created by anil on 30-05-18.
 */
class SessionManager(private val _context: Context){

    private val mypref: SharedPreferences
    private val mypref2: SharedPreferences
    private val editor: SharedPreferences.Editor
    private val editor2: SharedPreferences.Editor


    var isUpdateUid: Boolean
        get() = mypref.getBoolean(IS_UPDATE_UID, false)
        set(isUpdate) {
            editor.putBoolean(IS_UPDATE_UID, isUpdate)
            editor.commit()
        }


    val user: UserInfo?
        get() {
            val gson = Gson()
            val string = mypref.getString("user", "")
            return if (!string!!.isEmpty())
                gson.fromJson<UserInfo>(string, UserInfo::class.java)
            else
                null
        }


    val isLoggedIn: Boolean
        get() = mypref.getBoolean(IS_LOGGEDIN, false)

    init {
        mypref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        mypref2 = _context.getSharedPreferences(PREF_NAME2, Context.MODE_PRIVATE)
        editor = mypref.edit()
        editor2 = mypref2.edit()
        editor.apply()
        editor2.apply()
    }

    fun createSession(user: UserInfo) {
        var gson = Gson()
        var json = gson.toJson(user) // myObject - instance of MyObject
        editor.putString("user", json)
        editor.putBoolean(IS_LOGGEDIN, true)
        //editor.putString("authToken", user.data.authToken)
        editor.commit()
    }
    /*.....................................................................*/
    fun saveEmailPass(email:String,password:String,ischecked:Boolean){
        editor2.putString(EMAIL, email)
        editor2.putString(PASSWORD, password)
        editor2.putBoolean(IsChecked, ischecked)
        editor2.commit()
    }

    fun saveOldPassword(oldPassword:String){
        editor2.putString(OldPassword,oldPassword)
        editor2.commit()
    }

    fun saveIsNotificationActive(isNotifyActive :String){
        editor2.putString(IsNotificationActive,isNotifyActive)
        editor2.commit()
    }


    val getemail: String
        get() = mypref2.getString(EMAIL,"")
    val getpass: String
        get() = mypref2.getString(PASSWORD,"")
    val getIschecked:Boolean
    get() = mypref2.getBoolean(IsChecked,false)

    fun uncheck(){
        editor2.clear()
        editor2.apply()
    }


    fun logout() {
        editor.clear()
        editor.apply()
        //FirebaseAuth.getInstance().signOut();
        /* try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        val showLogin = Intent(_context, LoginActivity::class.java)
        showLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        showLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        _context.startActivity(showLogin)
    }

    companion object {
        private val PREF_NAME = "DNG"
        private val PREF_NAME2 = "appSession"

        private val IS_LOGGEDIN = "isLoggedIn"
        private val IS_FIrebaseLogin = "isFirebaseLogin"
        private val IS_UPDATE_UID = "isUpdateUid"

        private val EMAIL  = "email"
        private val PASSWORD = "password"
        private val IsChecked = "ischecked"
        private val OldPassword = "oldpassword"
        private val IsNotificationActive = "isnotificationactive"
    }


}


