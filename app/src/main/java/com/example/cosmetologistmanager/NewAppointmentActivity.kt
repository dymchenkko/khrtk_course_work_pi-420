package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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


class NewAppointmentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityNewAppointmentBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewAppointmentBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        setContentView(binding.root)

        val courses = mutableListOf<String>(
            "C", "Data structures",
            "Interview prep", "Algorithms",
            "DSA with java", "OS"
        )
        val items = mutableListOf<String>("No Users")

        val user = firebaseAuth.currentUser
                user?.let {
                    var uid = it.uid

                    FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (snapshot in dataSnapshot.children) {
                                    val user: Client? = snapshot.getValue(Client::class.java)
                                    items.add(user?.name + " " + user?.surname+ " " + user?.patronymic)

                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                }
        val ad: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            items)

        ad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)


        binding.clientsListSpinner.adapter = ad
        binding.addNewAppointmentBtn.setOnClickListener {
            val procedure_name = binding.newProcedureName.text.toString()
            val date = binding.datePickerButton.text.toString()
            val time_hour = binding.timePickerButton.hour.toString()
            val time_minute = binding.timePickerButton.minute.toString()


            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid
                val md5 = MessageDigest.getInstance("md5")
                md5.update((procedure_name + date + time_hour + time_minute).toByteArray())

                val digest: ByteArray = md5.digest()
                val appointments_hex =  digest.toHex()
                database.child("appointments").child(uid).child(appointments_hex).child("procedure").setValue(procedure_name)
                database.child("appointments").child(uid).child(appointments_hex).child("date").setValue(date)
                database.child("appointments").child(uid).child(appointments_hex).child("hour").setValue(time_hour)
                database.child("appointments").child(uid).child(appointments_hex).child("minute").setValue(time_minute)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

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


}