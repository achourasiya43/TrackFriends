package com.trackfriends.beanClass

import java.io.Serializable

/**
 * Created by anil on 9/11/17.
 */

class UserInfo : Serializable{

    var firebaseid: String? = null
    var name: String? = null
    var email: String? = null
    var profileImage: String? = ""
    var firebaseToken: String? = null
}
