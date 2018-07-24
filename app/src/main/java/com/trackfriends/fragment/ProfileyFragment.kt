package com.trackfriends.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.trackfriends.R
import com.trackfriends.activity.EditProfileActivity
import com.trackfriends.adapter.FriendAdapter
import com.trackfriends.beanClass.FriendRequest
import com.trackfriends.beanClass.UserInfo
import com.trackfriends.utils.Constant
import kotlinx.android.synthetic.main.fragment_friend.view.*
import kotlinx.android.synthetic.main.fragment_history.view.*


class ProfileyFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): ProfileyFragment {
            val fragment = ProfileyFragment()
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

        var view = inflater.inflate(R.layout.fragment_history, container, false)
        getProfile()

        view.edit_profile.setOnClickListener {
          startActivity(Intent(context, EditProfileActivity::class.java))
            activity?.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)

        }
        return view
    }


    fun getProfile(){
        val database = FirebaseDatabase.getInstance().reference
        var myFirebase_uid = FirebaseAuth.getInstance().uid.toString()
        database.child(Constant.ARG_USERS)
                .child(myFirebase_uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.getValue(UserInfo::class.java) != null) {
                            var userInfo = p0.getValue(UserInfo::class.java)
                            view!!.name.text = userInfo?.name
                            view!!.email.text = userInfo?.email

                            Glide.with(context).load(userInfo?.profileImage).placeholder(R.drawable.place_holder_img).into(view!!.profile_img_bg)

                        }
                    }

                })
    }

}
