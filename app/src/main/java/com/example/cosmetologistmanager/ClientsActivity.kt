package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.example.cosmetologistmanager.databinding.ActivityClientsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClientsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var dataArrayList = ArrayList<ListClientData>()
    var listAdapter: ListClientsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val items = mutableListOf<String>()

        val user = firebaseAuth.currentUser
        user?.let {
            var uid = it.uid

            FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val client: Client? = snapshot.getValue(Client::class.java)
                            Log.d("hash", snapshot?.key+"")
                            var listData = ListClientData(
                                client?.name.toString(), client?.surname.toString(), client?.patronymic.toString(), snapshot?.key.toString()
                            )
                            Log.d("name", client?.surname.toString())

                            dataArrayList.add(listData)
                            listAdapter = ListClientsAdapter(this@ClientsActivity, dataArrayList)
                            binding.listClients.setAdapter(listAdapter)
                            binding.listClients.setClickable(true)
                            Log.d("list of appointments", items.toString())
                        }

                        if (dataArrayList.size == 0) {
                            binding.noClients.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }

        binding.listClients.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this@ClientsActivity, ClientView::class.java)
            intent.putExtra("hash", dataArrayList[i].hash)
            startActivity(intent)
        })
    }
}