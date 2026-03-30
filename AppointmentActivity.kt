package com.smarthealth.cm.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smarthealth.cm.R
import com.smarthealth.cm.data.Appointment
import com.smarthealth.cm.data.HealthDatabase
import kotlinx.coroutines.launch
import java.util.*

class AppointmentActivity : AppCompatActivity() {
    private lateinit var db: HealthDatabase
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment)

        db = HealthDatabase.getDatabase(this)
        
        val rv = findViewById<RecyclerView>(R.id.rvAppointments)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AppointmentAdapter(emptyList())
        rv.adapter = adapter

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnBookAppointment).setOnClickListener {
            showAddAppointmentDialog()
        }

        loadAppointments()
    }

    private fun loadAppointments() {
        lifecycleScope.launch {
            val appointments = db.healthDao().getAllAppointments()
            adapter.updateData(appointments)
        }
    }

    private fun showAddAppointmentDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_appointment, null)
        val etDoctor = dialogView.findViewById<TextView>(R.id.etDoctorName)
        val btnPickDate = dialogView.findViewById<Button>(R.id.btnPickDate)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tvSelectedDate)

        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                tvSelectedDate.text = date
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle("New Appointment")
            .setView(dialogView)
            .setPositiveButton("Book") { _, _ ->
                val doctor = etDoctor.text.toString()
                val date = tvSelectedDate.text.toString()
                if (doctor.isNotEmpty() && date != "No date selected") {
                    lifecycleScope.launch {
                        db.healthDao().insertAppointment(Appointment(doctorName = doctor, date = date))
                        loadAppointments()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class AppointmentAdapter(private var list: List<Appointment>) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvTitle)
            val subtitle: TextView = view.findViewById(R.id.tvSubtitle)
            val detail: TextView = view.findViewById(R.id.tvDetail)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_generic, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.title.text = "Appointment"
            holder.subtitle.text = "Dr. ${item.doctorName}"
            holder.detail.text = "Scheduled: ${item.date}"
        }

        override fun getItemCount() = list.size

        fun updateData(newList: List<Appointment>) {
            list = newList
            notifyDataSetChanged()
        }
    }
}
