package com.trackfriends.activity

import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.View
import com.google.firebase.database.*
import com.trackfriends.R
import com.trackfriends.adapter.SearchFriendAdapter
import com.trackfriends.beanClass.UserInfo
import com.trackfriends.utils.Constant
import kotlinx.android.synthetic.main.activity_search_friend.*

class SearchFriendActivity : AppCompatActivity() {
    private var searchview: SearchView? = null
    var map:HashMap<String,UserInfo> ?= null
    var adapter : SearchFriendAdapter ?= null
    var friendList:ArrayList<UserInfo> ?= null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_friend)
        var database = FirebaseDatabase.getInstance();
        var myRef = database.getReference(Constant.ARG_USERS);

        map = HashMap();
        friendList = ArrayList();
        searchview = findViewById(R.id.searchview)

        adapter = SearchFriendAdapter(friendList,this@SearchFriendActivity,this@SearchFriendActivity)
        recyclerView.adapter = adapter

        searchview?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                loading_view.visibility = View.GONE
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                var str = newText
                getUserByName(myRef,str)
                loading_view.visibility = View.VISIBLE

                if(str.equals("")){
                    map?.clear()
                    friendList?.clear()
                    loading_view.visibility = View.GONE
                }
                adapter?.notifyDataSetChanged()
                //

                return false
            }
        })


    }

    private fun getUserByName(myRef: DatabaseReference, newText: String) {
        myRef.orderByChild("name").equalTo(newText.toLowerCase()).addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                var userInfo = p0?.getValue(UserInfo::class.java)!!
                map?.put(p0.key!!,userInfo)

                friendList?.clear()
                var demoValues : Collection<UserInfo> = map!!.values
                friendList?.addAll(demoValues)

                loading_view.visibility = View.GONE
                adapter?.notifyDataSetChanged()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }
}
