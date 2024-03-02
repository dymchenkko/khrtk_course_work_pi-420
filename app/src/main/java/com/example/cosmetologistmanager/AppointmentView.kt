package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivityAppointmentViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class AppointmentView : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentViewBinding
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAppointmentViewBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        setContentView(binding.root)
        var hash: String? = "";

        val intent = this.intent
        if (intent != null) {
            val user = firebaseAuth.currentUser
            user?.let {
                val uid = it.uid
                hash = intent.getStringExtra("hash")
                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .child(hash.toString()).get().addOnSuccessListener {
                    val appointment: Appointment? = it.getValue(Appointment::class.java)
                        binding.appointmentName.setText("Процедура: " + appointment?.procedure)
                        binding.dateTextView.setText("Дата процедури: ${addLeadingZero(appointment?.day.toString())}/${addLeadingZero(appointment?.month.toString())}/${appointment?.year}")
                        binding.timeTextView.setText("Час процедури: ${addLeadingZero(appointment?.hour.toString())}:${addLeadingZero(appointment?.minute.toString())}")
                        binding.additionalInformationTextView.setText("Додаткова інформація:" + appointment?.additional_information)

                        FirebaseDatabase.getInstance().reference.child("clients").child(uid).child(appointment?.client.toString())
                            .get().addOnSuccessListener {
                                var client: Client? = it.getValue(Client::class.java)
                                binding.clientTextView.setText("Клієнт: ${client?.surname} ${client?.name} ${client?.patronymic}")
                                binding.clientTextView.setOnClickListener {
                                    val intent = Intent(this@AppointmentView, ClientView::class.java)
                                    intent.putExtra("hash", appointment?.client.toString())
                                    startActivity(intent)
                                }
                            }

                    }
                }
            }

        binding.editAppointment.setOnClickListener {
            val intent = Intent(this, EditAppointmentActivity::class.java)
            intent.putExtra("hash", hash)
            startActivity(intent)
        }
        binding.deleteAppointment.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@AppointmentView)
            builder
                .setMessage("Ви точно хочете видалити запис?")
                .setTitle("Видалення запису")
                .setPositiveButton("Так") { dialog, which ->
                    val user = firebaseAuth.currentUser
                    user?.let {
                        var uid = it.uid
                        FirebaseDatabase.getInstance().reference.child("appointments").child(uid).child(hash.toString()).removeValue()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
                .setNegativeButton("Ні") { dialog, which ->
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

    }
}