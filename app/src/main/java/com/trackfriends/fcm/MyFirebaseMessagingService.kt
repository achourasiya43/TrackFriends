package com.elite.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trackfriends.R
import com.trackfriends.activity.MainActivity

/**
 * Created by anil on 8/12/17.
 */

 class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        //val notification = remoteMessage?.getNotification()
        val data = remoteMessage?.getData()
        Log.e("FROM", remoteMessage?.getFrom())

        var type = data?.get("type")
        if(type.equals("") || type == null){

        }else  sendNotification(data!!)


    }


    private fun sendNotification(data: Map<String, String>) {
        var reference_id = data?.get("reference_id")
        var type = data?.get("type")
        var title = data?.get("title")
        var message = data?.get("body")


        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("type",type)
        intent.putExtra("reference_id",reference_id)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(5, notificationBuilder.build())
    }


}
