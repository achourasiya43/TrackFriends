package com.trackfriends.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.trackfriends.locationService.AlarmReceiver

/**
 * Created by Anil on 13-06-2018.
 */
object Util {

    /*.........start Alarm service......*/
    fun startAlarmService(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val interval = 30 * 1000
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval.toLong(), pendingIntent)
        // Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show();

    }

    fun stopAlarmService(context: Context) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0)

        manager.cancel(pendingIntent)
        // Toast.makeText(context, "Alarm Canceled", Toast.LENGTH_SHORT).show();

    }
}