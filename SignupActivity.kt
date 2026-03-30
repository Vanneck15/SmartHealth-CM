package com.smarthealth.cm.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smarthealth.cm.R

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Sign Up (Register) -> Dashboard (or Login)
        findViewById<Button>(R.id.btnCreate).setOnClickListener {
            // Ici on pourrait ajouter la logique de création de compte
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        // Déjà un compte -> Login
        findViewById<TextView>(R.id.btnBackToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
