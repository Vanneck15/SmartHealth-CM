package com.smarthealth.cm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smarthealth.cm.R
import com.smarthealth.cm.data.HealthDatabase
import com.smarthealth.cm.data.MedicalRecord
import kotlinx.coroutines.launch

class RecordsActivity : AppCompatActivity() {
    private lateinit var db: HealthDatabase
    private lateinit var adapter: RecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        db = HealthDatabase.getDatabase(this)
        
        val rv = findViewById<RecyclerView>(R.id.rvRecords)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = RecordsAdapter(emptyList())
        rv.adapter = adapter

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnAddRecord).setOnClickListener {
            showAddRecordDialog()
        }

        loadRecords()
    }

    private fun loadRecords() {
        lifecycleScope.launch {
            val records = db.healthDao().getAllRecords()
            adapter.updateData(records)
        }
    }

    private fun showAddRecordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null)
        val etType = dialogView.findViewById<TextView>(R.id.etConsultationType)
        val etDoctor = dialogView.findViewById<TextView>(R.id.etDoctorName)
        val etDate = dialogView.findViewById<TextView>(R.id.etDate)

        AlertDialog.Builder(this)
            .setTitle("Add Medical Record")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val type = etType.text.toString()
                val doctor = etDoctor.text.toString()
                val date = etDate.text.toString()
                if (type.isNotEmpty() && doctor.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.healthDao().insertRecord(MedicalRecord(consultationType = type, doctorName = doctor, date = date))
                        loadRecords()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class RecordsAdapter(private var records: List<MedicalRecord>) : RecyclerView.Adapter<RecordsAdapter.ViewHolder>() {
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
            val record = records[position]
            holder.title.text = record.consultationType
            holder.subtitle.text = record.doctorName
            holder.detail.text = "Date: ${record.date}"
        }

        override fun getItemCount() = records.size

        fun updateData(newRecords: List<MedicalRecord>) {
            records = newRecords
            notifyDataSetChanged()
        }
    }
}
