package com.trackfriends.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.trackfriends.R
import com.trackfriends.beanClass.FriendRequest
import com.trackfriends.beanClass.UserInfo
import com.trackfriends.session.SessionManager
import com.trackfriends.utils.Constant
import kotlinx.android.synthetic.main.activity_friends_details.*

class FriendsDetailsActivity : AppCompatActivity() {
    var friend_firebase_uid = ""
    //var userInfo: UserInfo? = null
    var session: SessionManager? = null
    var userInfo:UserInfo ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends_details)

        session = SessionManager(this)

        if (intent.getStringExtra("friend_firebase_uid") != null) {
            friend_firebase_uid = intent.getStringExtra("friend_firebase_uid")
            CheckFriendRequest(friend_firebase_uid.toString())
        }

        send_request.setOnClickListener() {
            sendFriendRequest(userInfo)  // 0 staus for not your friend
        }

        accept_request.setOnClickListener {
            acceptFriendRequest(friend_firebase_uid!!)
        }

        reject_request.setOnClickListener {
            unFriend(friend_firebase_uid!!)
        }

        btn_unfriend.setOnClickListener {
            unFriend(friend_firebase_uid!!)
        }
        val database = FirebaseDatabase.getInstance().reference
        requestButtonSetting(database)
        getUserData(database)
    }

    private fun requestButtonSetting(database: DatabaseReference) {
        database.child(Constant.FRIEND_REQUEST)
                .child(friend_firebase_uid!!).orderByKey().addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                        CheckFriendRequest(friend_firebase_uid.toString())
                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                        CheckFriendRequest(friend_firebase_uid.toString())
                    }

                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                        CheckFriendRequest(friend_firebase_uid.toString())
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                        CheckFriendRequest(friend_firebase_uid.toString())
                    }

                })

        database.child(Constant.FRIEND_REQUEST)
                .child(FirebaseAuth.getInstance().uid.toString()).orderByKey().addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                        CheckFriendRequest(friend_firebase_uid.toString())
                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                        CheckFriendRequest(friend_firebase_uid.toString())

                    }

                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                        CheckFriendRequest(friend_firebase_uid.toString())
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                        CheckFriendRequest(friend_firebase_uid.toString())
                    }
                })
    }

    private fun sendFriendRequest(userInfo: UserInfo?) {
        val database = FirebaseDatabase.getInstance().reference
        var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()
        var friend_firebase_uid = userInfo?.firebaseid.toString()

        var myImage = session?.user?.profileImage
        var friend_myImage = userInfo?.profileImage

        // this is sender here we are sending request. this is me only
        var friendRequestSend = FriendRequest()
        friendRequestSend.friend_firebase_uid = myFirebase_uid
        friendRequestSend.request_status = "receive"
        friendRequestSend.email = session?.user?.email
        friendRequestSend.firebaseToken = session?.user?.firebaseToken
        friendRequestSend.name = session?.user?.name
        friendRequestSend.profileImage = myImage

        database.child(Constant.FRIEND_REQUEST)
                .child(friend_firebase_uid).child(myFirebase_uid).setValue(friendRequestSend)

/*...................................................................................................*/

        // this is receiver here we are receiving request
        var friendRequestReceive = FriendRequest()
        friendRequestReceive.friend_firebase_uid = friend_firebase_uid
        friendRequestReceive.request_status = "send"
        friendRequestReceive.email = userInfo?.email
        friendRequestReceive.firebaseToken = userInfo?.firebaseToken
        friendRequestReceive.name = userInfo?.name
        friendRequestReceive.profileImage = friend_myImage

        database.child(Constant.FRIEND_REQUEST)
                .child(myFirebase_uid).child(friend_firebase_uid)
                .setValue(friendRequestReceive)

        send_request.visibility = View.GONE
    }

    private fun unFriend(friend_firebase_uid: String) {
        val database = FirebaseDatabase.getInstance().reference
        var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()

        // this is sender here we are sending request. this is me only
        database.child(Constant.FRIEND_REQUEST)
                .child(myFirebase_uid).child(friend_firebase_uid)
                .setValue(null)

        // this is receiver here we are receiving request
        database.child(Constant.FRIEND_REQUEST)
                .child(friend_firebase_uid).child(myFirebase_uid)
                .setValue(null)

        btn_unfriend.visibility = View.GONE
        send_request.visibility = View.VISIBLE

    }

    private fun acceptFriendRequest(friend_firebase_uid: String) {
        val database = FirebaseDatabase.getInstance().reference
        var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()

        database.child(Constant.FRIEND_REQUEST)
                .child(myFirebase_uid).child(friend_firebase_uid).child("request_status")
                .setValue(Constant.confirm_friend).addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        ly_accept_reject.visibility = View.GONE
                        btn_unfriend.visibility = View.VISIBLE
                    }

                })

        database.child(Constant.FRIEND_REQUEST)
                .child(friend_firebase_uid).child(myFirebase_uid).child("request_status")
                .setValue(Constant.confirm_friend).addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        ly_accept_reject.visibility = View.GONE
                        btn_unfriend.visibility = View.VISIBLE
                    }

                })
    }

    private fun CheckFriendRequest(friend_firebase_uid: String) {

        var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()
        FirebaseDatabase.getInstance().reference.child(Constant.FRIEND_REQUEST).child(myFirebase_uid).child(friend_firebase_uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0?.getValue(FriendRequest::class.java) != null) {

                    var friendRequest = p0?.getValue(FriendRequest::class.java)!!

                    if (friendRequest.request_status?.equals(Constant.confirm_friend)!!) {
                        send_request.visibility = View.GONE
                        btn_unfriend.visibility = View.VISIBLE
                    } else if (friendRequest.request_status?.equals("send")!!) {
                        send_request.visibility = View.GONE
                        ly_accept_reject.visibility = View.GONE
                    } else if (friendRequest.request_status?.equals("receive")!!) {
                        send_request.visibility = View.GONE
                        ly_accept_reject.visibility = View.VISIBLE
                    }

                } else if(p0?.getValue(FriendRequest::class.java) == null) {
                    send_request.visibility = View.VISIBLE
                    ly_accept_reject.visibility = View.GONE
                    btn_unfriend.visibility = View.GONE
                }
            }
        })
    }

    private fun getUserData(database: DatabaseReference) {
        database.child(Constant.ARG_USERS).child(friend_firebase_uid.toString()).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.getValue(UserInfo::class.java) != null){
                    userInfo =  p0.getValue(UserInfo::class.java)
                    tv_user_name.text = userInfo?.name
                    tv_user_email.text = userInfo?.email
                }
            }

        })
    }

}
