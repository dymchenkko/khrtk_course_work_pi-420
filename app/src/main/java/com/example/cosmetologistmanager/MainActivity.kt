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
                            val user: Appointment? = snapshot.getValue(Appointment::class.java)
                            items.add(user?.procedure + " " + user?.date+ " " + user?.hour + " " + user?.minute)
                            for (i in 0 until items.size) {
                                var listData = ListData(
                                    items[i]
                                )
                                dataArrayList.add(listData)
                            }
                            listAdapter = ListAdapter(this@MainActivity, dataArrayList)
                            binding.listAppointments.setAdapter(listAdapter)
                            binding.listAppointments.setClickable(true)
                            Log.d("appointments", user?.procedure + " " + user?.date+ " " + user?.hour + " " + user?.minute)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }

        for (i in 0 until items.size) {
            var listData = ListData(
                items[i]
            )
            dataArrayList.add(listData)
        }
        listAdapter = ListAdapter(this@MainActivity, dataArrayList)
        binding.listAppointments.setAdapter(listAdapter)
        binding.listAppointments.setClickable(true)

        binding.listAppointments.setOnItemClickListener(OnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this@MainActivity, AppointmentView::class.java)
            intent.putExtra("name", items[i])
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
        initDatePicker()
        binding.datePickerButton.setText(getTodaysDate());
        binding.datePickerButton.setOnClickListener {
            datePickerDialog?.show()
        }
    }

    private fun getTodaysDate(): String? {
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        var month: Int = cal.get(Calendar.MONTH)
        month += 1
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
        return makeDateString(day, month, year)
    }

    private fun initDatePicker() {
        val dateSetListener =
            OnDateSetListener { datePicker, year, month, day ->
                var month = month
                month += 1
                val date = makeDateString(day, month, year)
                binding.datePickerButton.setText(date)
            }
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
        val style: Int = AlertDialog.THEME_HOLO_LIGHT
        datePickerDialog = DatePickerDialog(this, style, dateSetListener, year, month, day)
        //datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private fun makeDateString(day: Int, month: Int, year: Int): String? {
        return getMonthFormat(month) + " " + day + " " + year
    }

    private fun getMonthFormat(month: Int): String {
        if (month == 1) return "JAN"
        if (month == 2) return "FEB"
        if (month == 3) return "MAR"
        if (month == 4) return "APR"
        if (month == 5) return "MAY"
        if (month == 6) return "JUN"
        if (month == 7) return "JUL"
        if (month == 8) return "AUG"
        if (month == 9) return "SEP"
        if (month == 10) return "OCT"
        if (month == 11) return "NOV"
        return if (month == 12) "DEC" else "JAN"

        //default should never happen
    }
}