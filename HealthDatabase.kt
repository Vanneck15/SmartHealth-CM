package com.smarthealth.cm.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorName: String,
    val date: String
)

@Entity(tableName = "medical_records")
data class MedicalRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val consultationType: String,
    val doctorName: String,
    val date: String
)

@Entity(tableName = "reminders")
data class MedicationReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicationName: String,
    val time: String
)

@Dao
interface HealthDao {
    @Insert suspend fun insertAppointment(appointment: Appointment)
    @Query("SELECT * FROM appointments ORDER BY id DESC") suspend fun getAllAppointments(): List<Appointment>

    @Insert suspend fun insertRecord(record: MedicalRecord)
    @Query("SELECT * FROM medical_records ORDER BY id DESC") suspend fun getAllRecords(): List<MedicalRecord>

    @Insert suspend fun insertReminder(reminder: MedicationReminder)
    @Query("SELECT * FROM reminders ORDER BY id DESC") suspend fun getAllReminders(): List<MedicationReminder>
}

@Database(entities = [Appointment::class, MedicalRecord::class, MedicationReminder::class], version = 1)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile private var INSTANCE: HealthDatabase? = null
        fun getDatabase(context: Context): HealthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HealthDatabase::class.java,
                    "health_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
