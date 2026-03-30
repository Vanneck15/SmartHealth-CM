package com.smarthealth.cm.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smarthealth.cm.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val logo = findViewById<ImageView>(R.id.ivLogo)
        val title = findViewById<TextView>(R.id.tvAppName)
        val tagline = findViewById<TextView>(R.id.tvTagline)
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        val btnLogin = findViewById<TextView>(R.id.btnLoginWelcome)

        // Load Animations
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Apply Animations
        logo.startAnimation(fadeIn)
        title.startAnimation(slideUp)
        tagline.startAnimation(slideUp)
        btnGetStarted.startAnimation(slideUp)
        btnLogin.startAnimation(fadeIn)

        // Bouton Get Started -> Sign Up
        btnGetStarted.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // Bouton "Already have an account? Sign In" -> Login
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
