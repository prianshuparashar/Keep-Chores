package com.chores.keepchores

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmService : Service() {
    var context: Context? = null
    lateinit var mediaPlayer: MediaPlayer
    lateinit var vibrator: Vibrator
    private val TAG = "MyTag"

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer.isLooping = true
        vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        Log.i(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intentRing = Intent(this, RingingActivity::class.java)
        val ringPendingIntent = PendingIntent.getActivity(this, 0, intentRing, 0)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_todo)
                .setContentTitle("Time Up!")
                .setContentText("xyz")
                .setContentIntent(ringPendingIntent)
                .build()
        mediaPlayer.start()
        vibrator.vibrate(500)
        startForeground(1, notification)
        Log.i(TAG, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        mediaPlayer.stop()
        vibrator.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}