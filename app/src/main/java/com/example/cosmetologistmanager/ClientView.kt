package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.cosmetologistmanager.databinding.ActivityAppointmentViewBinding
import com.example.cosmetologistmanager.databinding.ActivityClientViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientView : AppCompatActivity() {
    private lateinit var binding: ActivityClientViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientViewBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        var dataArrayList = ArrayList<ListAppointmentData>()
        var listAdapter: ListAppointmentsAdapter? = null

        setContentView(binding.root)
        var hash: String? = "";

        val intent = this.intent
        val user = firebaseAuth.currentUser

        if (intent != null) {
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
        user?.let {
            var uid = it.uid

            FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val appointment: Appointment? = snapshot.getValue(Appointment::class.java)
                            Log.d("hash", snapshot?.key+"")

                            var client_id: String = hash.toString();

                            if (appointment?.client.toString().equals(client_id)) {
                                Log.d("hash", snapshot?.key+"")
                                var listData = ListAppointmentData(
                                    appointment?.procedure.toString(), appointment?.hour+"", appointment?.minute+"", snapshot?.key+""
                                )
                                dataArrayList.add(listData)
                                dataArrayList.sortWith(compareBy({ it.hour?.toIntOrNull() }, { it.minute?.toIntOrNull() }))
                                listAdapter = ListAppointmentsAdapter(this@ClientView, dataArrayList)
                                binding.listAppointments.setAdapter(listAdapter)
                                binding.listAppointments.setClickable(true)

                            }

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
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
                FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                    .child(hash.toString()).removeValue()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

    }
}