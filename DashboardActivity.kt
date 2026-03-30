package com.smarthealth.cm.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smarthealth.cm.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        try {
            // Liaison sécurisée de chaque fonctionnalité
            findViewById<View>(R.id.btnHospitals).setOnClickListener {
                startActivity(Intent(this, MapsActivity::class.java))
            }

            findViewById<View>(R.id.btnAppointments).setOnClickListener {
                startActivity(Intent(this, AppointmentActivity::class.java))
            }

            findViewById<View>(R.id.btnRecords).setOnClickListener {
                startActivity(Intent(this, RecordsActivity::class.java))
            }

            findViewById<View>(R.id.btnReminders).setOnClickListener {
                startActivity(Intent(this, RemindersActivity::class.java))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading features: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
