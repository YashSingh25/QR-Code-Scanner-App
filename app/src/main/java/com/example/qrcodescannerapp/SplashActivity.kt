package com.example.qrcodescannerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DURATION = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        GlobalScope.launch(Dispatchers.Main) {
            delay(SPLASH_DURATION)

            withContext(Dispatchers.IO){
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            finish()
        }

    }
}