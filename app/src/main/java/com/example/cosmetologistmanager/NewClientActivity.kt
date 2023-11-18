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
import java.security.MessageDigest

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
            val additional_information = binding.newAdditionalInformation.text.toString()

            val md5 = MessageDigest.getInstance("md5")
            md5.update((name + surname + patronymic).toByteArray())

            val digest: ByteArray = md5.digest()
            val client_hex =  digest.toHex()
            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid
                database.child("clients").child(uid).child(client_hex).child("name").setValue(name)
                database.child("clients").child(uid).child(client_hex).child("surname").setValue(surname)
                database.child("clients").child(uid).child(client_hex).child("patronymic").setValue(patronymic)
                database.child("clients").child(uid).child(client_hex).child("additional_information").setValue(additional_information)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }
    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }


}