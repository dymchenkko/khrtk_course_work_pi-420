package com.example.cosmetologistmanager

import android.R
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.example.cosmetologistmanager.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var dataArrayList = ArrayList<ListAppointmentData>()
    var listAdapter: ListAppointmentsAdapter? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Locale("UA")
        Locale.setDefault(locale)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        val items = mutableListOf<String>()
        dataArrayList = ArrayList<ListAppointmentData>()
        binding.bottomNavigation.setItemIconTintList(null);

        binding.bottomNavigation.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            Log.d("itemid", item.itemId.toString())

            if (item.itemId == com.example.cosmetologistmanager.R.id.new_client) {
                if (firebaseAuth.currentUser != null) {
                    val intent = Intent(this, NewClientActivity::class.java)
                    startActivity(intent)
                }
            } else if (item.itemId == com.example.cosmetologistmanager.R.id.new_apointment) {
                if (firebaseAuth.currentUser != null) {
                    val intent = Intent(this, NewAppointmentActivity::class.java)
                    startActivity(intent)
                }
            } else if (item.itemId == com.example.cosmetologistmanager.R.id.all_clients) {
                if (firebaseAuth.currentUser != null) {
                    val intent = Intent(this, ClientsActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        })

        val user = firebaseAuth.currentUser
        user?.let {
            var uid = it.uid

            FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val new_appointment: Appointment? =
                                snapshot.getValue(Appointment::class.java)
                            Log.d("hash", snapshot?.key + "")

                            if (binding.datePicker.dayOfMonth.toString()
                                    .equals(new_appointment!!.day) && binding.datePicker.month.toString()
                                    .equals(new_appointment!!.month) && binding.datePicker.year.toString()
                                    .equals(new_appointment!!.year)
                            ) {
                                var listData = ListAppointmentData(
                                    new_appointment?.procedure.toString(),
                                    new_appointment?.hour + "",
                                    new_appointment?.minute + "",
                                    snapshot?.key + ""
                                )
                                dataArrayList.add(listData)
                                dataArrayList.sortWith(compareBy({ it.hour?.toIntOrNull() },
                                    { it.minute?.toIntOrNull() })
                                )

                            }
                            listAdapter = ListAppointmentsAdapter(this@MainActivity, dataArrayList)
                            binding.listAppointments.setAdapter(listAdapter)
                            binding.listAppointments.setClickable(true)
                            Log.d("list of appointments", items.toString())
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
        binding.datePicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            dataArrayList = ArrayList<ListAppointmentData>()
            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid

                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val new_appointment: Appointment? =
                                    snapshot.getValue(Appointment::class.java)
                                Log.d("hash", snapshot?.key + "")

                                if (binding.datePicker.dayOfMonth.toString()
                                        .equals(new_appointment!!.day) && binding.datePicker.month.toString()
                                        .equals(new_appointment!!.month) && binding.datePicker.year.toString()
                                        .equals(new_appointment!!.year)
                                ) {
                                    val age_group =
                                        if (new_appointment?.minute.toString().length == 1) "0" + new_appointment?.minute.toString() else new_appointment?.minute.toString()

                                    var listData = ListAppointmentData(
                                        new_appointment?.procedure.toString(),
                                        new_appointment?.hour + "",
                                        new_appointment?.minute + "",
                                        snapshot?.key + ""
                                    )
                                    dataArrayList.add(listData)
                                    dataArrayList.sortWith(compareBy({ it.hour?.toIntOrNull() },
                                        { it.minute?.toIntOrNull() })
                                    )

                                }
                                listAdapter =
                                    ListAppointmentsAdapter(this@MainActivity, dataArrayList)
                                binding.listAppointments.setAdapter(listAdapter)
                                binding.listAppointments.setClickable(true)
                                Log.d("list of appointments", items.toString())
                                Log.d(
                                    "appointments",
                                    new_appointment?.procedure + " " + new_appointment?.hour + " " + new_appointment?.minute
                                )
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }

        }


        binding.listAppointments.setOnItemClickListener(OnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this@MainActivity, AppointmentView::class.java)
            intent.putExtra("name", dataArrayList[i].name)
            intent.putExtra("hash", dataArrayList[i].hash)
            startActivity(intent)
        })
        binding.logOutBtn.setOnClickListener {
            val intent = Intent(this, UserAccountActivity::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun initializeCamera() {
        firebaseAuth = FirebaseAuth.getInstance()
        val items = mutableListOf<String>()
        dataArrayList = ArrayList<ListAppointmentData>()
        val user = firebaseAuth.currentUser
        user?.let {
            var uid = it.uid

            FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val new_appointment: Appointment? =
                                snapshot.getValue(Appointment::class.java)
                            Log.d("hash", snapshot?.key + "")

                            if (binding.datePicker.dayOfMonth.toString()
                                    .equals(new_appointment!!.day) && binding.datePicker.month.toString()
                                    .equals(new_appointment!!.month) && binding.datePicker.year.toString()
                                    .equals(new_appointment!!.year)
                            ) {
                                var listData = ListAppointmentData(
                                    new_appointment?.procedure.toString(),
                                    new_appointment?.hour + "",
                                    new_appointment?.minute + "",
                                    snapshot?.key + ""
                                )
                                dataArrayList.add(listData)
                                dataArrayList.sortWith(compareBy({ it.hour?.toIntOrNull() },
                                    { it.minute?.toIntOrNull() })
                                )

                            }
                            listAdapter = ListAppointmentsAdapter(this@MainActivity, dataArrayList)
                            binding.listAppointments.setAdapter(listAdapter)
                            binding.listAppointments.setClickable(true)
                            Log.d("list of appointments", items.toString())
                            Log.d(
                                "appointments",
                                new_appointment?.procedure + " " + new_appointment?.hour + " " + new_appointment?.minute
                            )
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
        binding.datePicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            dataArrayList = ArrayList<ListAppointmentData>()
            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid

                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val new_appointment: Appointment? =
                                    snapshot.getValue(Appointment::class.java)
                                Log.d("hash", snapshot?.key + "")

                                if (binding.datePicker.dayOfMonth.toString()
                                        .equals(new_appointment!!.day) && binding.datePicker.month.toString()
                                        .equals(new_appointment!!.month) && binding.datePicker.year.toString()
                                        .equals(new_appointment!!.year)
                                ) {
                                    var listData = ListAppointmentData(
                                        new_appointment?.procedure.toString(),
                                        new_appointment?.hour + "",
                                        new_appointment?.minute + "",
                                        snapshot?.key + ""
                                    )
                                    dataArrayList.add(listData)
                                    dataArrayList.sortWith(compareBy({ it.hour?.toIntOrNull() },
                                        { it.minute?.toIntOrNull() })
                                    )

                                }
                                listAdapter =
                                    ListAppointmentsAdapter(this@MainActivity, dataArrayList)
                                binding.listAppointments.setAdapter(listAdapter)
                                binding.listAppointments.setClickable(true)
                                Log.d("list of appointments", items.toString())
                                Log.d(
                                    "appointments",
                                    new_appointment?.procedure + " " + new_appointment?.hour + " " + new_appointment?.minute
                                )
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }

        }
    }

}