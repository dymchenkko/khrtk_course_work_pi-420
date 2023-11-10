package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.cosmetologistmanager.R
import com.example.cosmetologistmanager.databinding.ActivityNewClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class NewClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewClientBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = Firebase.database.reference
        firebaseAuth = FirebaseAuth.getInstance()

        binding.addNewClientBtn.setOnClickListener {
            val name = binding.newClientName.text.toString()
            val surname = binding.newClientSurname.text.toString()
            val patronymic = binding.newClientPatronymic.text.toString()
            database.child("clients").child("client").child("name").setValue(name)
            database.child("clients").child("client").child("surname").setValue(surname)
            database.child("clients").child("client").child("patronymic").setValue(patronymic)
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