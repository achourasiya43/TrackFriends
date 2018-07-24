package com.trackfriends.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.trackfriends.R
import com.trackfriends.adapter.FriendAdapter
import com.trackfriends.beanClass.FriendRequest
import com.trackfriends.utils.Constant
import kotlinx.android.synthetic.main.fragment_friend.view.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.*


class FriendsFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var friendList:HashMap<String,FriendRequest> ?= null
    private var adapter:FriendAdapter ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view =  inflater.inflate(R.layout.fragment_friend, container, false)

        friendList = HashMap()
        adapter = FriendAdapter(Constant.FRIEND_VIEW,friendList,context!!)
        view.recyclerViewHome.adapter = adapter

        getAllFriend()
        return view
    }

    fun getAllFriend(){
        val database = FirebaseDatabase.getInstance().reference
        var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()
        database.child(Constant.FRIEND_REQUEST)
                .child(myFirebase_uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.getValue(FriendRequest::class.java) == null){
                            view?.shimmer_view_container?.stopShimmerAnimation()
                            view?.shimmer_view_container?.visibility = View.GONE
                            if(friendList?.size == 0){
                                view?.ly_no_friend_found?.visibility = View.VISIBLE
                            }else  view?.ly_no_friend_found?.visibility = View.GONE
                        }
                    }
                    override fun onCancelled(p0: DatabaseError) {}
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
                        if(p0?.getValue(FriendRequest :: class.java)!! != null){
                            var friendRequest = p0?.getValue(FriendRequest :: class.java)!!
                            if(friendRequest?.request_status.equals(Constant.confirm_friend)){
                                friendList?.remove(friendRequest.friend_firebase_uid)
                            }
                            if(adapter != null && context != null && view != null){
                                adapter = FriendAdapter(Constant.FRIEND_VIEW,friendList,context!!)
                                view?.recyclerViewHome?.adapter = adapter

                                if(friendList?.size == 0){
                                    view?.ly_no_friend_found?.visibility = View.VISIBLE
                                }else  view?.ly_no_friend_found?.visibility = View.GONE
                            }



                        }

                    }

                })
    }

    private fun friendData(p0: DataSnapshot) {
        if(p0?.getValue(FriendRequest::class.java)!! != null){
            var friendRequest = p0?.getValue(FriendRequest::class.java)!!
            if (friendRequest?.request_status.equals(Constant.confirm_friend)) {
                friendList?.put(friendRequest.friend_firebase_uid!!, friendRequest)
            }
            if(context != null && adapter != null){
                adapter = FriendAdapter(Constant.FRIEND_VIEW,friendList,context!!)
                view?.recyclerViewHome?.adapter = adapter

                view?.shimmer_view_container?.stopShimmerAnimation()
                view?.shimmer_view_container?.visibility = View.GONE
                if(friendList?.size == 0){
                    view?.ly_no_friend_found?.visibility = View.VISIBLE
                }else  view?.ly_no_friend_found?.visibility = View.GONE
            }
        }
    }
    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): FriendsFragment {
            val fragment = FriendsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
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
}// Required empty public constructor

