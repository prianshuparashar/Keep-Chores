package com.chores.keepchores

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chores.keepchores.databinding.ActivityRingingBinding

class RingingActivity : AppCompatActivity() {
    lateinit var binding: ActivityRingingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dismiss.setOnClickListener {
            val intentService = Intent(applicationContext, AlarmService::class.java)
            applicationContext.stopService(intentService)
            finishAffinity()
            finish()
        }
    }
}