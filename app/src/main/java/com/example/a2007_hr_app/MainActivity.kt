package com.example.a2007_hr_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //splash screen
        supportActionBar?.hide()
        Handler().postDelayed({
             val intent = Intent(this@MainActivity, LoginScreenActivity::class.java)
            startActivity(intent)
            //Remove from Stack to back click from entering this screen.
            finish()
        }, 1000,)
    }
}