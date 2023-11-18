package com.example.cosmetologistmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivityNewAppointmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class NewAppointmentActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityNewAppointmentBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewAppointmentBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        //Toast.makeText(this, "fewfewfewfew".toString(), Toast.LENGTH_LONG).show();

        setContentView(binding.root)
        val courses = mutableListOf<String>(
            "C", "Data structures",
            "Interview prep", "Algorithms",
            "DSA with java", "OS"
        )
        val items = mutableListOf<String>("No Users")

        val user = firebaseAuth.currentUser
                user?.let {
                    var uid = it.uid

                    FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (snapshot in dataSnapshot.children) {
                                    val user: Client? = snapshot.getValue(Client::class.java)
                                    items.add(user?.name + " " + user?.surname+ " " + user?.patronymic)

                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })
                    /*var aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    with(binding.clientsListSpinner)
                    {
                        adapter = aa
                        setSelection(0, false)
                        onItemSelectedListener = this@NewAppointmentActivity
                        prompt = "Select your favourite language"
                        gravity = Gravity.CENTER

                    }
*/
                    //var ref = FirebaseDatabase.getInstance().getReference("clients").child("client").child("name")
                    //val items = mutableListOf<String>()

                    /*val menuListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            items.add(dataSnapshot.getValue() as String)
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            // handle error
                        }
                    }
                    ref.addListenerForSingleValueEvent(menuListener)*/
                    /*val adapter = ArrayAdapter(this,         android.R.layout.simple_spinner_item
                        , items)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.clientsListSpinner?.onItemClickListener = dapterView.OnItemClickListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            (parent!!.getChildAt(0) as TextView).setTextColor(0x77273047)
                            Toast.makeText(this@NewAppointmentActivity, "fewfewfewfew".toString(), Toast.LENGTH_LONG).show();


                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            Toast.makeText(this@NewAppointmentActivity, parent?.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();
                            (parent!!.getChildAt(0) as TextView).setTextColor(0x77273047)

                        }

                    }
                    binding.clientsListSpinner.setAdapter(adapter)*/
                    //binding.clientsListSpinner.setSelection(0)

                }
        val ad: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            items)
        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        binding.clientsListSpinner.adapter = ad
        binding.addNewAppointmentBtn.setOnClickListener {
            val name = binding.newClientName.text.toString()
            val surname = binding.newClientSurname.text.toString()
            val patronymic = binding.newClientPatronymic.text.toString()
            database.child("appointments").child("appointment").child("name").setValue(name)
            database.child("appointments").child("appointment").child("surname").setValue(surname)
            database.child("appointments").child("appointment").child("patronymic").setValue(patronymic)
            val user = firebaseAuth.currentUser
            user?.let {
                var uid = it.uid
                database.child("clients").child("client").child("costemologist_id").setValue(uid)
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
        showToast(message = "Nothing selected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        when (view?.id) {
            1 -> showToast(message = "Spinner 2 Position:${position} and language: fwfewfew")
            else -> {
                showToast(message = "Spinner 1 Position:${position} and language:----")
            }
        }
    }

    private fun showToast(context: Context = applicationContext, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration).show()
    }


}