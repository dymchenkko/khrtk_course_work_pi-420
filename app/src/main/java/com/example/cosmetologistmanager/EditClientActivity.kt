package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.cosmetologistmanager.databinding.ActivityEditAppointmentBinding
import com.example.cosmetologistmanager.databinding.ActivityEditClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class EditClientActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityEditClientBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = this.intent
        if (intent != null) {
            var hash = intent.getStringExtra("hash")
            firebaseAuth = FirebaseAuth.getInstance()
            database = Firebase.database.reference

            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid

                FirebaseDatabase.getInstance().reference.child("clients").child(uid).child(hash.toString()).get().addOnSuccessListener {
                    var client: Client? = it.getValue(Client::class.java)

                    binding.newClientName.setText(client?.name)
                    binding.newClientSurname.setText(client?.surname)
                    binding.newClientPatronymic.setText(client?.patronymic)
                    binding.newAdditionalInformation.setText(client?.additional_information)

                    binding.editClientBtn.setOnClickListener {
                        FirebaseDatabase.getInstance().reference.child("clients").child(uid).child(hash.toString()).removeValue();

                        val name = binding.newClientName.text.toString()
                        val surname = binding.newClientSurname.text.toString()
                        val patronymic = binding.newClientPatronymic.text.toString()
                        val additional_information = binding.newAdditionalInformation.text.toString()


                        val md5 = MessageDigest.getInstance("md5")
                        md5.update((name + surname + patronymic + additional_information).toByteArray())

                        val digest: ByteArray = md5.digest()
                        val client_hex =  digest.toHex()
                        database.child("clients").child(uid).child(client_hex).child("name").setValue(name)
                        database.child("clients").child(uid).child(client_hex).child("surname").setValue(surname)
                        database.child("clients").child(uid).child(client_hex).child("patronymic").setValue(patronymic)
                        database.child("clients").child(uid).child(client_hex).child("additional_information").setValue(additional_information)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }

                }.addOnFailureListener{
                    Log.e("firebase", "Error getting data", it)
                }
            }
        }

    }
    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }


}