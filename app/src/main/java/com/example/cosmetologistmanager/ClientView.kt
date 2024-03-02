package com.example.cosmetologistmanager

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
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
        var dataArrayList = ArrayList<ListClientsAppointmentData>()
        var listAdapter: ListClientsAppointmentsAdapter? = null

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
                        var client: Client? = it.getValue(Client::class.java)
                        binding.clientName.setText("Ім'я: " + client?.name)
                        binding.clientSurname.setText("Прізвище: " + client?.surname)
                        binding.clientPatronymic.setText("По-батькові: " + client?.patronymic)
                        binding.clientPhoneNumber.setText("Номер телефону: +380" + client?.phone_number)
                        binding.clientSkinCondition.setText("Стан шкіри: " + client?.skin_condition)
                        binding.clientSkinType.setText("Тип шкіри: " + client?.skin_type)
                        binding.clientAllergy.setText("Алергія: " + client?.allergy)
                        binding.additionalInformationTextView.setText("Додаткова інформація: " + client?.additional_information)

                    }
            }
        }
        user?.let { it ->
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

                                var listData = ListClientsAppointmentData(
                                    appointment?.procedure.toString(), addLeadingZero(appointment?.hour.toString()), addLeadingZero(appointment?.minute.toString()), addLeadingZero(appointment?.day.toString()), addLeadingZero(appointment?.month.toString()), appointment?.year.toString(), snapshot?.key.toString()
                                )
                                dataArrayList.add(listData)
                                dataArrayList.sortWith(compareBy(
                                    { it.year.toIntOrNull() },
                                    { it.month.toIntOrNull() },
                                    { it.day.toIntOrNull() },
                                    { it.hour.toIntOrNull() },
                                    { it.minute.toIntOrNull() }
                                ))
                                listAdapter = ListClientsAppointmentsAdapter(this@ClientView, dataArrayList)
                                binding.listAppointments.setAdapter(listAdapter)
                                binding.listAppointments.setClickable(true)

                            }

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }

        binding.listAppointments.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this@ClientView, AppointmentView::class.java)
            intent.putExtra("name", dataArrayList[i].name)
            intent.putExtra("hash", dataArrayList[i].hash)
            startActivity(intent)
        })
        binding.editClient.setOnClickListener {
            val intent = Intent(this, EditClientActivity::class.java)
            intent.putExtra("hash", hash)
            startActivity(intent)
        }
        binding.deleteClient.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@ClientView)
            builder
                .setMessage("Ви точно хочете видалити клієнта?")
                .setTitle("Видалення клієнта")
                .setPositiveButton("Так") { dialog, which ->
                    val user = firebaseAuth.currentUser
                    user?.let {
                        var uid = it.uid
                        FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                            .child(hash.toString()).removeValue()

                        FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot in dataSnapshot.children) {
                                        val appointment: Appointment? = snapshot.getValue(Appointment::class.java)
                                        Log.d("hash", snapshot?.key+"")

                                        var client_id: String = hash.toString();

                                        if (appointment?.client.toString().equals(client_id)) {
                                            Log.d("hash", snapshot?.key+"")
                                            FirebaseDatabase.getInstance().reference.child("appointments").child(uid).child(snapshot?.key.toString()).removeValue()
                                            val intent = Intent(this@ClientView, MainActivity::class.java)
                                            startActivity(intent)
                                        }

                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {}
                            })

                    }
                }
                .setNegativeButton("Ні") { dialog, which ->
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

    }
}
fun addLeadingZero(input: String): String {
    return if (input.length == 1) {
        "0$input"
    } else {
        input
    }
}