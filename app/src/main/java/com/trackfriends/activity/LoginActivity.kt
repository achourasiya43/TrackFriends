package com.trackfriends.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.trackfriends.R
import com.trackfriends.beanClass.UserInfo
import com.trackfriends.session.SessionManager
import com.trackfriends.utils.Constant
import com.trackfriends.utils.ValidData
import com.trackfriends.utils.InsLoadingView
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    var session : SessionManager ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        session = SessionManager(this)

        signInBtn.setOnClickListener {
            if(isValid()){
                loginFirebaseDataBase();
            }
        }

        if (session?.getIschecked!!){
            emailTxt.setText(session?.getemail)
            pwdTxt.setText(session?.getpass)
            checkbox_remember_me.isChecked = session?.getIschecked!!
        }


        signUpLay.setOnClickListener {
            var intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
            finish()

        }
    }


    private fun loginFirebaseDataBase() {
        loading_view?.visibility = View.VISIBLE
        var email = emailTxt.text.toString()
        var password = pwdTxt.text.toString()
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->

                    if (task.isSuccessful) {
                        //updateFirebaseToken(task.getResult().getUser().getUid(), token);
                        if(checkbox_remember_me.isChecked){
                            session?.saveEmailPass(email,password,checkbox_remember_me.isChecked)
                        }else{
                            session?.uncheck()
                        }

                        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                        gettingDataFromUserTable(currentFirebaseUser!!.uid)

                    } else {
                        loading_view?.visibility = View.GONE
                        Toast.makeText(this,"Somthing went wrong",Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun gettingDataFromUserTable(firebaseUid : String){
        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(firebaseUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                loading_view?.visibility = View.GONE
                var userInfo = p0?.getValue(UserInfo :: class.java)!!
                session?.createSession(userInfo)
                addUserFirebaseDatabase(userInfo.email!!, userInfo.name!!,"")

                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        })
    }

    private fun addUserFirebaseDatabase(email: String, name: String, image: String) {
        val database = FirebaseDatabase.getInstance().reference
        var user  = UserInfo()
        user.firebaseid = FirebaseAuth.getInstance().uid.toString()
        user.email = email
        user.name = name.toLowerCase()
        user.firebaseToken = FirebaseInstanceId.getInstance().token.toString()
        user.profileImage = image

        database.child(Constant.ARG_USERS)
                .child(user.firebaseid!!)
                .setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    } else {
                        Toast.makeText(this,"data not store at firebase server", Toast.LENGTH_SHORT).show()
                    }
                }
    }


    private fun isValid(): Boolean {
        val v = ValidData()

        if (!v.isNullValue(emailTxt)) {
            emailTxt.setError("Enter email address")
            emailTxt.requestFocus()
            return false;

        } else if (!v.isEmailValid(emailTxt)) {
            emailTxt.setError("Enter valid email address")
            emailTxt.requestFocus()
            return false;

        } else if (!v.isNullValue(pwdTxt)) {
            pwdTxt.setError("Enter password")
            pwdTxt.requestFocus()
            return false;

        } else if (!v.isPasswordValid(pwdTxt)) {
            pwdTxt.setError("Password atleast 6 characters required")
            pwdTxt.requestFocus()
            return false;
        }
        return true
    }




}
