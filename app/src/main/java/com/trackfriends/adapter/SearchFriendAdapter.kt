package com.trackfriends.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.trackfriends.R
import com.trackfriends.activity.FriendsDetailsActivity
import com.trackfriends.beanClass.UserInfo
import kotlinx.android.synthetic.main.search_friend_layout.view.*

/**
 * Created by anil on 25/11/17.
 **/

class SearchFriendAdapter (friendList: ArrayList<UserInfo>?, activity: FragmentActivity, context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var activity = activity
    var context = context
    var reqList = friendList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var v:View = LayoutInflater.from(parent?.context).inflate(R.layout.search_friend_layout,parent,false)
        return ViewHoder(v,activity)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder= holder as ViewHoder
        viewHolder.bind(reqList,position)
    }


    override fun getItemCount(): Int {
        return reqList!!.size
    }

    fun deleteItem(position:Int){
        reqList?.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHoder (itemView: View,activity: FragmentActivity):RecyclerView.ViewHolder(itemView){
        var myFirebaseId = FirebaseAuth.getInstance().uid.toString()
        var activity = activity

        fun bind(reqList: ArrayList<UserInfo>?, position: Int) = with (itemView){
            var userInfo = reqList?.get(position)
            tv_user_name.text = userInfo?.name
            Glide.with(context).load(userInfo?.profileImage).placeholder(R.drawable.place_holder_img).into(itemView.iv_profileImage)

            if(userInfo?.firebaseid?.equals(myFirebaseId)!!){
                tv_you.visibility = View.VISIBLE
            }else tv_you.visibility = View.GONE

            itemView.setOnClickListener {
                if(!userInfo?.firebaseid?.equals(myFirebaseId)!!){
                    var intent = Intent(activity,FriendsDetailsActivity::class.java)
                    intent.putExtra("friend_firebase_uid",userInfo.firebaseid)
                    activity.startActivity(intent)
                }
            }
        }
    }



}