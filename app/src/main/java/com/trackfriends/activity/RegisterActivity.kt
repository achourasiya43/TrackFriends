package com.trackfriends.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.trackfriends.ImagePickerPackge.ImagePicker
import kotlinx.android.synthetic.main.activity_register.*
import com.trackfriends.R
import com.trackfriends.beanClass.UserInfo
import com.trackfriends.cropper.CropImage
import com.trackfriends.cropper.CropImageView
import com.trackfriends.session.SessionManager
import com.trackfriends.utils.CompressImage
import com.trackfriends.utils.Constant
import com.trackfriends.utils.ValidData
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    private var bitmap: Bitmap? = null
    private var storageRef: StorageReference? = null
    private var storage: FirebaseStorage? = null
    private var app: FirebaseApp? = null
    private var imageUrl :String = ""

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        app = FirebaseApp.getInstance()
        storage = FirebaseStorage.getInstance(app!!)

        signUpBtn.setOnClickListener {
            if(isValid()){
                firebaseChatRegister()
            }
        }

        signInLay.setOnClickListener {
            var intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()

        }

        ly_image.setOnClickListener {
            getPermissionAndPicImage()
        }
    }


    fun firebaseChatRegister() {
        loading_view?.visibility = View.VISIBLE
        var username = username.text.toString().toLowerCase()
        var email = emailTxt.text.toString()
        var password = pwdTxt.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, { task ->
                    loading_view?.visibility = View.VISIBLE
                    Log.e("TAG", "performFirebaseRegistration:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        var f_id = FirebaseAuth.getInstance().uid
                        addUserFirebaseDatabase(email,username,imageUrl)
                    } else {
                        loginFirebaseDataBase()
                    }
                })
    }

    private fun loginFirebaseDataBase() {
        val username = username.text.toString()
        val email = emailTxt.text.toString()
        var password = pwdTxt.text.toString()
        //var userinfo:UserInfo = session?.user!!

        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@RegisterActivity) { task ->
                    Log.d("TAG", "performFirebaseLogin:onComplete:" + task.isSuccessful)
                    if (task.isSuccessful) {
                        var f_id = FirebaseAuth.getInstance().uid
                        addUserFirebaseDatabase(email,username,imageUrl)
                    } else {
                        firebaseChatRegister()
                    }
                }
    }

    private fun addUserFirebaseDatabase(email: String, name: String, image: String) {
        val database = FirebaseDatabase.getInstance().reference
        var session = SessionManager(this)
        var user  = UserInfo()

        user.firebaseid = FirebaseAuth.getInstance().uid.toString()
        user.email = email
        user.name = name
        user.firebaseToken = FirebaseInstanceId.getInstance().token.toString()
        user.profileImage = image

        database.child(Constant.ARG_USERS)
                .child(user.firebaseid!!)
                .setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        session?.createSession(user)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this,"data not store at firebase server", Toast.LENGTH_SHORT).show()
                    }
                }
    }


    private fun isValid(): Boolean {
        val v = ValidData()

        if (!v.isNullValue(username)) {
            username.setError("Please enter fullname")
            username.requestFocus()
            return false;

        } else if (!v.isNullValue(emailTxt)) {
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


    fun getPermissionAndPicImage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY)
            } else {
                ImagePicker.pickImage(this@RegisterActivity)
            }
        } else {
            ImagePicker.pickImage(this@RegisterActivity)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {

            Constant.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.SELECT_FILE)
                } else {
                    Toast.makeText(this@RegisterActivity, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show()
                }
            }

            Constant.MY_PERMISSIONS_REQUEST_CEMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, Constant.REQUEST_CAMERA)
                } else {
                    Toast.makeText(this@RegisterActivity , "YOUR  PERMISSION DENIED ", Toast.LENGTH_LONG).show()
                }
            }

            Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this@RegisterActivity )
                } else {
                    Toast.makeText(this@RegisterActivity, "YOUR  PERMISSION DENIED ", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 234) {
                val imageUri = ImagePicker.getImageURIFromResult(this@RegisterActivity, requestCode, resultCode, data)
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.OVAL)
                            .setAspectRatio(4, 4)
                            .start(this)
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                val result = CropImage.getActivityResult(data)
                try {
                    if (result != null)
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result!!.getUri())
                    if (bitmap != null) {
                        bitmap = ImagePicker.getImageResized(this, result!!.getUri())
                        user_profile_image.setImageBitmap(bitmap)
                        creatFirebaseProfilePicUrl(result!!.getUri())
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun creatFirebaseProfilePicUrl(selectedImageUri: Uri) {
        storageRef = storage?.getReference("chat_photos_sys" + getString(R.string.app_name))
        val photoRef = storageRef?.child(selectedImageUri.lastPathSegment)

        photoRef?.putFile(selectedImageUri)?.addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot> {
            override fun onComplete(p0: Task<UploadTask.TaskSnapshot>) {

                photoRef?.downloadUrl?.addOnSuccessListener(object : OnSuccessListener<Uri> {
                    override fun onSuccess(p0: Uri?) {
                        imageUrl = p0.toString()

                    }
                })
            }
        })
    }



}
