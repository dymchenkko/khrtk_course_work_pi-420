package com.example.cosmetologistmanager

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var datePickerDialog: DatePickerDialog? = null
    var dataArrayList = ArrayList<ListData>()
    var listAdapter: ListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        val items = mutableListOf<String>()

        val user = firebaseAuth.currentUser
        user?.let {
            var uid = it.uid

            FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val new_appointment: Appointment? = snapshot.getValue(Appointment::class.java)
                            Log.d("hash", snapshot?.key+"")
                                var listData = ListData(
                                    new_appointment?.procedure.toString(), new_appointment?.hour+"", new_appointment?.minute+"", snapshot?.key+""
                                )
                            dataArrayList.add(listData)
                            dataArrayList.sortWith(compareBy({ it.hour?.toIntOrNull() }, { it.minute?.toIntOrNull() }))
                            listAdapter = ListAdapter(this@MainActivity, dataArrayList)
                            binding.listAppointments.setAdapter(listAdapter)
                            binding.listAppointments.setClickable(true)
                            Log.d("list of appointments", items.toString())
                            Log.d("appointments", new_appointment?.procedure + " " + new_appointment?.hour + " " + new_appointment?.minute)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
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
        binding.addNewClientBtn.setOnClickListener {
            if(firebaseAuth.currentUser != null)
            {
                val intent = Intent(this, NewClientActivity::class.java)
                startActivity(intent)
            }
        }

        binding.addNewAppointmentBtn.setOnClickListener {
            if(firebaseAuth.currentUser != null)
            {
                val intent = Intent(this, NewAppointmentActivity::class.java)
                startActivity(intent)
            }
        }
    }

}