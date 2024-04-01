package com.chores.keepchores

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Hides Status Bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Delay using Handler
        Handler().postDelayed({
            val homeScreenIntent = Intent(this, HomeScreen::class.java)
            startActivity(homeScreenIntent)
            //Custom Activity Transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()    //Destroy SplashScreen from Activity List to prevent returning back on exit.
        }, 500)
    }
}