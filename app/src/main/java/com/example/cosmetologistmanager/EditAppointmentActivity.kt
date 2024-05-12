package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivityEditAppointmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Locale

class EditAppointmentActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityEditAppointmentBinding
    private lateinit var database: DatabaseReference
    private var hash = ""
    private var items = mutableListOf<Pair<String, String>>()

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Locale("UA")
        Locale.setDefault(locale)

        binding = ActivityEditAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.timePickerButton.setIs24HourView(true);

        val intent = this.intent
        if (intent != null) {
            hash = intent.getStringExtra("hash")!!
            firebaseAuth = FirebaseAuth.getInstance()
            database = Firebase.database.reference


            val user = firebaseAuth.currentUser
            user?.let {
                val uid = it.uid

                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .child(hash).get().addOnSuccessListener {
                        val appointment: Appointment? = it.getValue(Appointment::class.java)
                        FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot in dataSnapshot.children) {
                                        val user: Client? = snapshot.getValue(Client::class.java)
                                        Log.d("client hash", snapshot?.key.toString())
                                        items.add(
                                            Pair(
                                                user?.surname + " " + user?.name + " " + user?.patronymic,
                                                snapshot?.key.toString()
                                            )
                                        )
                                        val clients: MutableList<String> =
                                            items.map { it.first }.toMutableList()
                                        val ad: ArrayAdapter<String> = ArrayAdapter<String>(
                                            this@EditAppointmentActivity,
                                            android.R.layout.simple_spinner_item,
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
                        val hour = appointment?.hour?.toInt()
                        val minute = appointment?.minute?.toInt()
                        val year = appointment?.year?.toInt()!!
                        val month = appointment.month?.toInt()!!
                        val day = appointment.day?.toInt()!!
                        Log.i("firebase", "hour ${hour} minute ${minute}")
                        Log.i("firebase", "year ${year} month ${month} day ${day}")

                        binding.timePickerButton.hour = hour!!
                        binding.timePickerButton.minute = minute!!
                        binding.datePickerButton.init(year, month-1, day) { view, year, month, day ->

                        }
                        binding.newAdditionalInformation.setText(appointment.additional_information)
                        binding.editAppointmentPrice.setText(appointment.price)

                        binding.editAppointmentBtn.setOnClickListener {
                            if (validate()) {

                                FirebaseDatabase.getInstance().reference.child("appointments")
                                    .child(uid).child(hash.toString()).removeValue();

                                val procedure_name = binding.newProcedureName.text.toString()
                                val date_day = binding.datePickerButton.dayOfMonth.toString()
                                var date_month = binding.datePickerButton.month.toString()
                                date_month = (date_month.toInt() + 1).toString()
                                val date_year = binding.datePickerButton.year.toString()
                                val time_hour = binding.timePickerButton.hour.toString()
                                val time_minute = binding.timePickerButton.minute.toString()
                                val additional_information =
                                    binding.newAdditionalInformation.text.toString()
                                val procedure_price = binding.editAppointmentPrice.text.toString()


                                val md5 = MessageDigest.getInstance("md5")
                                md5.update((procedure_name + date_day + date_month + date_year + time_hour + time_minute).toByteArray())

                                val digest: ByteArray = md5.digest()
                                val appointments_hex = hash.toString()
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
                                    .child("price").setValue(procedure_price)
                                database.child("appointments").child(uid).child(appointments_hex)
                                    .child("additional_information")
                                    .setValue(additional_information)
                                val client: String =
                                    binding.clientsListSpinner.getSelectedItem().toString()
                                for (pair in items) {
                                    if (pair.first == client) {
                                        database.child("appointments").child(uid)
                                            .child(appointments_hex).child("client")
                                            .setValue(pair.second)
                                    }
                                }
                                this.finish();
                            }
                        }

                    }.addOnFailureListener {
                        Log.e("firebase", "Error getting data", it)
                    }
            }
        }

    }
    override fun onBackPressed() {
        val user = firebaseAuth.currentUser
        val uid = user?.uid.toString()
       FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
            .child(hash).get().addOnSuccessListener {
                var appointment: Appointment = it.getValue(Appointment::class.java)!!
        if (wasTheInformationChanged(appointment)) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@EditAppointmentActivity)
            builder
                .setMessage("Ви точно хочете закінчити редагування?")
                .setTitle("Дані не збережені і інформація не буде оновлена.")
                .setPositiveButton("Так") { dialog, which ->
                    this.finish()
                }
                .setNegativeButton("Ні") { dialog, which ->
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
        else {
            this.finish()
        }
        }.addOnFailureListener {
            Log.e("firebase", "Error getting data", it)
        }

    }

    fun wasTheInformationChanged(appointment: Appointment): Boolean {
        var changed = false
        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        val procedure_name = binding.newProcedureName.text.toString()
                    val date_day = binding.datePickerButton.dayOfMonth.toString()
                    var date_month = binding.datePickerButton.month.toString()
                    date_month = (date_month.toInt() + 1).toString()
                    val date_year = binding.datePickerButton.year.toString()
                    val time_hour = binding.timePickerButton.hour.toString()
                    val time_minute = binding.timePickerButton.minute.toString()
                    val additional_information =
                        binding.newAdditionalInformation.text.toString()
                    val procedure_price = binding.editAppointmentPrice.text.toString()
                    val client: String =
                        binding.clientsListSpinner.getSelectedItem().toString()
                    Log.e("client", client)
                    Log.e("items len", items.size.toString())

                    for (pair in items) {
                        if (pair.first == client) {
                            if (appointment.procedure != procedure_name ||
                                appointment.day != date_day ||
                                appointment.month != date_month ||
                                appointment.year != date_year ||
                                appointment.hour != time_hour ||
                                appointment.minute != time_minute ||
                                appointment.additional_information != additional_information ||
                                appointment.price != procedure_price ||
                                appointment.client != pair.second) {
                                Log.e("not equeal", "not equal")
                                changed = true
                            }
                        }
                    }
            Log.e("changed", changed.toString())

            return changed
    }
        @RequiresApi(Build.VERSION_CODES.O)
    fun validate(): Boolean {

        val date_day = binding.datePickerButton.dayOfMonth.toString()
        var date_month = binding.datePickerButton.month.toString()
        date_month = (date_month.toInt() + 1).toString()
        val date_year = binding.datePickerButton.year.toString()
        val time_hour = binding.timePickerButton.hour.toString()
        val time_minute = binding.timePickerButton.minute.toString()
        val procedure_name = binding.newProcedureName.text.toString()
        val procedure_price = binding.editAppointmentPrice.text.toString()

        if (procedure_name == "") {
            val text = "Поле назви процедури не може бути пустим"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@EditAppointmentActivity, text, duration)
            toast.show()
            return false
        }
        if (procedure_price == ""){
            val text = "Поле ціни не може бути пустим"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@EditAppointmentActivity, text, duration)
            toast.show()
            return false
        }
        if (procedure_price.toInt() < 0){
            val text = "Ціна процедури не може бути від'ємною"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@EditAppointmentActivity, text, duration)
            toast.show()
            return false
        }
        if (isDateTimeInThePast(
                date_day.toInt(),
                date_month.toInt(),
                date_year.toInt(),
                time_hour.toInt(),
                time_minute.toInt()
            )
        ) {
            val text = "Зверніть увагу, що ви додали дату і час у минулому!"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@EditAppointmentActivity, text, duration)
            toast.show()
        }
        return true
    }

    fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

}