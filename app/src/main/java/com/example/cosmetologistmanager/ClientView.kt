package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cosmetologistmanager.databinding.ActivityAppointmentViewBinding
import com.example.cosmetologistmanager.databinding.ActivityClientViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ClientView : AppCompatActivity() {
    private lateinit var binding: ActivityClientViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityClientViewBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        var hash: String? = "";

        val intent = this.intent
        if (intent != null) {
            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid
                hash = intent.getStringExtra("hash")
                FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                    .child(hash.toString()).get().addOnSuccessListener {
                        var appointment: Client? = it.getValue(Client::class.java)
                        binding.clientName.setText(appointment?.name)
                        binding.clientSurname.setText(appointment?.surname)
                        binding.clientPatronymic.setText(appointment?.patronymic)
                        binding.additionalInformationTextView.setText(appointment?.additional_information)

                    }
            }
        }

        binding.editClient.setOnClickListener {
            val intent = Intent(this, EditClientActivity::class.java)
            intent.putExtra("hash", hash)
            startActivity(intent)
        }
        binding.deleteClient.setOnClickListener {
            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid
                FirebaseDatabase.getInstance().reference.child("clients").child(uid).child(hash.toString()).removeValue()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

    }
}