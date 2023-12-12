package com.example.cosmetologistmanager

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.cosmetologistmanager.databinding.ActivityAppointmentViewBinding
import com.example.cosmetologistmanager.databinding.ActivityEditAppointmentBinding
import com.example.cosmetologistmanager.databinding.ActivityMainBinding
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

class EditAppointmentActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityEditAppointmentBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = mutableListOf<Pair<String, String>>()

        val intent = this.intent
        if (intent != null) {
            var hash = intent.getStringExtra("hash")
            firebaseAuth = FirebaseAuth.getInstance()
            database = Firebase.database.reference


            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid



                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .child(hash.toString()).get().addOnSuccessListener {
                    var appointment: Appointment? = it.getValue(Appointment::class.java)
                    FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (snapshot in dataSnapshot.children) {
                                    val user: Client? = snapshot.getValue(Client::class.java)
                                    Log.d("client hash", snapshot?.key.toString())
                                    items.add(
                                        Pair(
                                            user?.name + " " + user?.surname + " " + user?.patronymic,
                                            snapshot?.key.toString()
                                        )
                                    )
                                    val clients: MutableList<String> =
                                        items.map { it.first }.toMutableList()
                                    val ad: ArrayAdapter<String> = ArrayAdapter<String>(
                                        this@EditAppointmentActivity,
                                        R.layout.simple_spinner_item,
                                        clients
                                    )
                                    ad.setDropDownViewResource(
                                        android.R.layout.simple_spinner_dropdown_item
                                    )
                                    binding.clientsListSpinner.adapter = ad

                                }
                                for (i in 0..items.size) {
                                    if (items[i].second == appointment?.client) {
                                        binding.clientsListSpinner.setSelection(i)
                                        break
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    Log.i("firebase", "Got value ${appointment}")
                    binding.newProcedureName.setText(appointment?.procedure)
                    var hour = appointment?.hour?.toInt()
                    var minute = appointment?.minute?.toInt()
                    var year = appointment?.year?.toInt()!!
                    var month = appointment?.month?.toInt()!!
                    var day = appointment?.day?.toInt()!!
                    Log.i("firebase", "hour ${hour} minute ${minute}")
                    Log.i("firebase", "year ${year} month ${month} day ${day}")

                    binding.timePickerButton.hour = hour!!
                    binding.timePickerButton.minute = minute!!
                    binding.datePickerButton.init(year, month, day) { view, year, month, day ->

                    }
                    binding.newAdditionalInformation.setText(appointment.additional_information)

                    binding.editAppointmentBtn.setOnClickListener {
                        FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                            .child(hash.toString()).removeValue();

                        val procedure_name = binding.newProcedureName.text.toString()
                        val date_day = binding.datePickerButton.dayOfMonth.toString()
                        val date_month = binding.datePickerButton.month.toString()
                        val date_year = binding.datePickerButton.year.toString()
                        val time_hour = binding.timePickerButton.hour.toString()
                        val time_minute = binding.timePickerButton.minute.toString()
                        val additional_information =
                            binding.newAdditionalInformation.text.toString()


                        val md5 = MessageDigest.getInstance("md5")
                        md5.update((procedure_name + date_day + date_month + date_year + time_hour + time_minute).toByteArray())

                        val digest: ByteArray = md5.digest()
                        val appointments_hex = digest.toHex()
                        database.child("appointments").child(uid).child(appointments_hex)
                            .child("procedure").setValue(procedure_name)
                        database.child("appointments").child(uid).child(appointments_hex)
                            .child("day").setValue(date_day)
                        database.child("appointments").child(uid).child(appointments_hex)
                            .child("month").setValue(date_month)
                        database.child("appointments").child(uid).child(appointments_hex)
                            .child("year").setValue(date_year)
                        database.child("appointments").child(uid).child(appointments_hex)
                            .child("hour").setValue(time_hour)
                        database.child("appointments").child(uid).child(appointments_hex)
                            .child("minute").setValue(time_minute)
                        database.child("appointments").child(uid).child(appointments_hex)
                            .child("additional_information").setValue(additional_information)
                        val client: String = binding.clientsListSpinner.getSelectedItem().toString()
                        for (pair in items) {
                            if (pair.first == client) {
                                database.child("appointments").child(uid).child(appointments_hex).child("client").setValue(pair.second)
                            }
                        }
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }

                }.addOnFailureListener {
                    Log.e("firebase", "Error getting data", it)
                }
            }
        }

    }

    fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

}