package com.trackfriends.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.trackfriends.R
import com.trackfriends.adapter.FriendAdapter
import com.trackfriends.beanClass.FriendRequest
import com.trackfriends.utils.Constant
import kotlinx.android.synthetic.main.fragment_friend_req.view.*

class FriendRequestFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var friendList:HashMap<String,FriendRequest> ?= null
    private var adapter:FriendAdapter ?= null
    private var mContext:Context ?= null


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        fun newInstance(param1: String, param2: String): FriendRequestFragment {
            val fragment = FriendRequestFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_friend_req, container, false)
        friendList = HashMap()
        adapter = FriendAdapter(Constant.FRIEND_REQUEST_VIEW,friendList,mContext!!)
        view.recyclerView.adapter = adapter
        getAllFriendRequest()
        return view
    }

    fun getAllFriendRequest(){
        val database = FirebaseDatabase.getInstance().reference
        var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()
        database.child(Constant.FRIEND_REQUEST)
                .child(myFirebase_uid).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.getValue(FriendRequest::class.java) == null){
                            view?.shimmer_view_container?.stopShimmerAnimation()
                            view?.shimmer_view_container?.visibility = View.GONE
                            if(friendList?.size == 0){
                                view?.ly_no_friend_request?.visibility = View.VISIBLE
                            }else  view?.ly_no_friend_request?.visibility = View.GONE
                        }
                    }

                })

        database.child(Constant.FRIEND_REQUEST)
                .child(myFirebase_uid).addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                        friendData(p0)
                    }

                    override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                        friendData(p0)
                    }

                    override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                        friendData(p0)
                    }

                    override fun onChildRemoved(p0: DataSnapshot) {
                        var friendRequest = p0?.getValue(FriendRequest :: class.java)!!
                        if(friendRequest?.request_status.equals(Constant.receive)){
                            friendList?.remove(friendRequest.friend_firebase_uid)
                        }
                        adapter = FriendAdapter(Constant.FRIEND_REQUEST_VIEW,friendList,mContext!!)
                        view?.recyclerView?.adapter = adapter

                        if(friendList?.size == 0){
                            view?.ly_no_friend_request?.visibility = View.VISIBLE
                        }else  view?.ly_no_friend_request?.visibility = View.GONE
                    }

                })
    }

    private fun friendData(p0: DataSnapshot) {
        if(p0?.getValue(FriendRequest::class.java)!! != null){
            var friendRequest = p0?.getValue(FriendRequest::class.java)!!
            if (friendRequest?.request_status.equals(Constant.receive)) {
                friendList?.put(friendRequest.friend_firebase_uid!!, friendRequest)
            }
            else if(friendRequest?.request_status.equals(Constant.confirm_friend)){
                friendList?.remove(friendRequest.friend_firebase_uid!!)
            }
            if(context != null && adapter != null){

            adapter = FriendAdapter(Constant.FRIEND_REQUEST_VIEW,friendList,mContext!!)
            view?.recyclerView?.adapter = adapter

            view?.shimmer_view_container?.stopShimmerAnimation()
            view?.shimmer_view_container?.visibility = View.GONE
            if(friendList?.size == 0){
                view?.ly_no_friend_request?.visibility = View.VISIBLE
            }else  view?.ly_no_friend_request?.visibility = View.GONE
        }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.shimmer_view_container?.startShimmerAnimation()
    }

    override fun onPause() {
        view?.shimmer_view_container?.stopShimmerAnimation()
        super.onPause()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mContext = context;
    }

}
