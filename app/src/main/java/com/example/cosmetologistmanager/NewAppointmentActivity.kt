package com.example.cosmetologistmanager

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cosmetologistmanager.databinding.ActivityNewAppointmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import java.util.Calendar
import java.util.Date
import java.util.Locale


class NewAppointmentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityNewAppointmentBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Locale("UA")
        Locale.setDefault(locale)
        binding = ActivityNewAppointmentBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        setContentView(binding.root)
        val items = mutableListOf<Pair<String, String>>()

        val user = firebaseAuth.currentUser
                user?.let {
                    var uid = it.uid

                    FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (snapshot in dataSnapshot.children) {
                                    val user: Client? = snapshot.getValue(Client::class.java)
                                    Log.d("client hash", snapshot?.key.toString())
                                    items.add(Pair(user?.name + " " + user?.surname+ " " + user?.patronymic, snapshot?.key.toString()))

                                    Log.d("items",  "$items")

                                    val clients: MutableList<String> = items.map { it.first }.toMutableList()
                                    Log.d("clients hash all",  "$clients")

                                    val ad: ArrayAdapter<String> = ArrayAdapter<String>(
                                        this@NewAppointmentActivity,
                                        android.R.layout.simple_spinner_item,
                                        clients)
                                    ad.setDropDownViewResource(
                                        android.R.layout.simple_spinner_dropdown_item)
                                    binding.clientsListSpinner.adapter = ad
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                }

        binding.timePickerButton.setIs24HourView(true);

        binding.addNewAppointmentBtn.setOnClickListener {
            val procedure_name = binding.newProcedureName.text.toString()
            val date_day = binding.datePickerButton.dayOfMonth.toString()
            val date_month = binding.datePickerButton.month.toString()
            val date_year = binding.datePickerButton.year.toString()
            val time_hour = binding.timePickerButton.hour.toString()
            val time_minute = binding.timePickerButton.minute.toString()
            val additional_information = binding.newAdditionalInformation.text.toString()

            user?.let {

                var uid = it.uid
                var is_unique = true
                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val appointment: Appointment? = snapshot.getValue(Appointment::class.java)
                                if (appointment!!.year.equals(date_year) && appointment.month.equals(date_month) && appointment.day.equals(
                                        date_day
                                    )&& appointment.hour.equals(time_hour)&& appointment.minute.equals(time_minute)
                                ) {
                                    is_unique = false
                                    break
                                }
                            }

                            if (is_unique) {
                                var uid = it.uid
                                val md5 = MessageDigest.getInstance("md5")
                                md5.update((procedure_name + date_day + date_month + date_year + time_hour + time_minute).toByteArray())

                                val digest: ByteArray = md5.digest()
                                val appointments_hex =  digest.toHex()
                                database.child("appointments").child(uid).child(appointments_hex).child("procedure").setValue(procedure_name)
                                database.child("appointments").child(uid).child(appointments_hex).child("day").setValue(date_day)
                                database.child("appointments").child(uid).child(appointments_hex).child("month").setValue(date_month)
                                database.child("appointments").child(uid).child(appointments_hex).child("year").setValue(date_year)
                                database.child("appointments").child(uid).child(appointments_hex).child("hour").setValue(time_hour)
                                database.child("appointments").child(uid).child(appointments_hex).child("minute").setValue(time_minute)
                                database.child("appointments").child(uid).child(appointments_hex).child("additional_information").setValue(additional_information)
                                val client: String = binding.clientsListSpinner.getSelectedItem().toString()
                                for (pair in items) {
                                    if (pair.first == client) {
                                        database.child("appointments").child(uid).child(appointments_hex).child("client").setValue(pair.second)
                                    }
                                }
                                val intent = Intent(this@NewAppointmentActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(this@NewAppointmentActivity, "Запис на той час і дату вже існує!", Toast.LENGTH_LONG).show()
                            }

                            }
                        override fun onCancelled(databaseError: DatabaseError) {}

                    })
                    }
            }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast(message = "Nothing selected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (view?.id) {
            1 -> showToast(message = "Spinner 2 Position:${position} and language: fwfewfew")
            else -> {
                showToast(message = "Spinner 1 Position:${position} and language:----")
            }
        }
    }

    private fun showToast(context: Context = applicationContext, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration).show()
    }

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(year: Int, month: Int, day: Int, hour: Int, minute:Int)
    {
        val intent = Intent(applicationContext, Notification::class.java)
        val title = "allert"
        val message = "fewfrewfewef"
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime(year, month, day, hour, minute)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }
    private fun getTime(year: Int, month: Int, day: Int, hour: Int, minute:Int): Long
    {
        val calendar = Calendar.getInstance()
        calendar.set(year, month+1, day, hour, minute)
        return calendar.timeInMillis
    }
}