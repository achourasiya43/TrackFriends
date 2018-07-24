package com.trackfriends.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.trackfriends.R
import com.trackfriends.activity.FriendsDetailsActivity
import com.trackfriends.activity.LocationActivity
import com.trackfriends.activity.MapsActivity
import com.trackfriends.beanClass.FriendRequest
import com.trackfriends.beanClass.UserInfo
import com.trackfriends.utils.Constant
import kotlinx.android.synthetic.main.friend_item_layout.view.*
import kotlinx.android.synthetic.main.friend_request_item_layout.view.*

/**
 * Created by Anil on 10-06-2018.
 */
class FriendAdapter(var customViewType: Int, friendlist: Map<String, FriendRequest>?, context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context = context
    var friendlist = getArrayListFromMap(friendlist)

    fun getArrayListFromMap(friendlist: Map<String, FriendRequest>?): ArrayList<FriendRequest> {
        var demoValues: Collection<FriendRequest> = friendlist!!.values
        var friendList: ArrayList<FriendRequest> = ArrayList<FriendRequest>(demoValues)
        return friendList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (customViewType == Constant.FRIEND_REQUEST_VIEW) {
            var v: View = LayoutInflater.from(parent?.context).inflate(R.layout.friend_request_item_layout, parent, false)
            return ViewHolderRequest(v)
        } else if (customViewType == Constant.FRIEND_VIEW) {
            var v: View = LayoutInflater.from(parent?.context).inflate(R.layout.friend_item_layout, parent, false)
            return ViewHoder(v)
        } else {
            var v: View = LayoutInflater.from(parent?.context).inflate(R.layout.friend_item_layout, parent, false)
            return ViewHoder(v)
        }


    }

    override fun getItemCount(): Int {
        return friendlist!!.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (customViewType == Constant.FRIEND_REQUEST_VIEW) {
            val viewHolderRequest = holder as ViewHolderRequest
            viewHolderRequest.bind(friendlist, position)
        } else if (customViewType == Constant.FRIEND_VIEW) {
            val viewHolder = holder as ViewHoder
            viewHolder.bind(friendlist, position)
        }
    }

    class ViewHoder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(friendlist: ArrayList<FriendRequest>?, position: Int) = with(itemView) {

            itemView.tv_user_name.text = friendlist?.get(position)?.name
            Glide.with(context).load(friendlist?.get(position)?.profileImage).placeholder(R.drawable.place_holder_img).into(itemView.profileImage)

            itemView.track_btn.setOnClickListener {
                var intent = Intent(context, LocationActivity::class.java)
                intent.putExtra("userId", friendlist?.get(position)?.friend_firebase_uid)
                context.startActivity(intent)
            }

            itemView.folding_cell.setOnClickListener {
               /* var intent = Intent(context, FriendsDetailsActivity::class.java)
                intent.putExtra("friend_firebase_uid", friendlist?.get(position)?.friend_firebase_uid)
                context.startActivity(intent)*/

                itemView.folding_cell.toggle(false)
            }
        }
    }

    class ViewHolderRequest(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(friendlist: ArrayList<FriendRequest>?, position: Int) = with(itemView) {
            itemView.tv_name.text = friendlist?.get(position)?.name
            Glide.with(context).load(friendlist?.get(position)?.profileImage).placeholder(R.drawable.place_holder_img).into(itemView.iv_profileImage)

            itemView.accept_request.setOnClickListener {
                acceptFriendRequest(friendlist?.get(position)?.friend_firebase_uid!!)
            }

            itemView.reject_request.setOnClickListener {
                unFriend(friendlist?.get(position)?.friend_firebase_uid!!)
            }

            itemView.main_view.setOnClickListener {
                var intent = Intent(context, FriendsDetailsActivity::class.java)
                intent.putExtra("friend_firebase_uid", friendlist?.get(position)?.friend_firebase_uid)
                context.startActivity(intent)
            }
        }


        private fun acceptFriendRequest(friend_firebase_uid: String) {
            val database = FirebaseDatabase.getInstance().reference
            var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()

            database.child(Constant.FRIEND_REQUEST)
                    .child(myFirebase_uid).child(friend_firebase_uid).child("request_status")
                    .setValue(Constant.confirm_friend)

            database.child(Constant.FRIEND_REQUEST)
                    .child(friend_firebase_uid).child(myFirebase_uid).child("request_status")
                    .setValue(Constant.confirm_friend)
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

        }
    }
}