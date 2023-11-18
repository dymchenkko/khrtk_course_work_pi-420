package com.example.cosmetologistmanager

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivityMainBinding
import com.example.cosmetologistmanager.databinding.ActivityNewAppointmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class NewAppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewAppointmentBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewAppointmentBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        var ref = FirebaseDatabase.getInstance().getReference("clients").child("client").child("name")
        val items = mutableListOf<String>()

        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                items.add(dataSnapshot.getValue() as String)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        ref.addListenerForSingleValueEvent(menuListener)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        binding.clientsListSpinner.setAdapter(adapter)

        binding.addNewAppointmentBtn.setOnClickListener {
            val name = binding.newClientName.text.toString()
            val surname = binding.newClientSurname.text.toString()
            val patronymic = binding.newClientPatronymic.text.toString()
            database.child("appointments").child("appointment").child("name").setValue(name)
            database.child("appointments").child("appointment").child("surname").setValue(surname)
            database.child("appointments").child("appointment").child("patronymic").setValue(patronymic)
            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid
                database.child("clients").child("client").child("costemologist_id").setValue(uid)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }
}