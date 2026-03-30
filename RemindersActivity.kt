package com.smarthealth.cm.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import com.smarthealth.cm.data.HealthDatabase
import com.smarthealth.cm.data.MedicationReminder
import com.smarthealth.cm.receiver.ReminderReceiver
import kotlinx.coroutines.launch
import java.util.*

class RemindersActivity : AppCompatActivity() {
    private lateinit var db: HealthDatabase
    private lateinit var adapter: RemindersAdapter
    private var selectedHour: Int = -1
    private var selectedMinute: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        db = HealthDatabase.getDatabase(this)
        
        val rv = findViewById<RecyclerView>(R.id.rvReminders)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = RemindersAdapter(emptyList())
        rv.adapter = adapter

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnAddReminder).setOnClickListener {
            showAddReminderDialog()
        }

        loadReminders()
    }

    private fun loadReminders() {
        lifecycleScope.launch {
            val reminders = db.healthDao().getAllReminders()
            adapter.updateData(reminders)
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null)
        val etName = dialogView.findViewById<TextView>(R.id.etMedicationName)
        val btnPickTime = dialogView.findViewById<Button>(R.id.btnPickTime)
        val tvSelectedTime = dialogView.findViewById<TextView>(R.id.tvSelectedTime)

        btnPickTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                tvSelectedTime.text = String.format("%02d:%02d", hourOfDay, minute)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add Medication Reminder")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val time = tvSelectedTime.text.toString()
                if (name.isNotEmpty() && selectedHour != -1) {
                    val reminder = MedicationReminder(medicationName = name, time = time)
                    lifecycleScope.launch {
                        db.healthDao().insertReminder(reminder)
                        scheduleNotification(reminder, selectedHour, selectedMinute)
                        loadReminders()
                    }
                } else {
                    Toast.makeText(this, "Please enter name and time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleNotification(reminder: MedicationReminder, hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("MEDICATION_NAME", reminder.medicationName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this, reminder.id, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    class RemindersAdapter(private var reminders: List<MedicationReminder>) : RecyclerView.Adapter<RemindersAdapter.ViewHolder>() {
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
            val reminder = reminders[position]
            holder.title.text = reminder.medicationName
            holder.subtitle.text = "Time: ${reminder.time}"
            holder.detail.text = "Notification enabled"
        }

        override fun getItemCount() = reminders.size

        fun updateData(newReminders: List<MedicationReminder>) {
            reminders = newReminders
            notifyDataSetChanged()
        }
    }
}
