package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
    private val filteredList: ArrayList<ListClientData> = ArrayList()

    var listAdapter: ListClientsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val items = mutableListOf<String>()

        val user = firebaseAuth.currentUser
        user?.let {
            val uid = it.uid

            FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val client: Client? = snapshot.getValue(Client::class.java)
                            var listData = ListClientData(
                                client?.name.toString(), client?.surname.toString(), client?.patronymic.toString(), snapshot?.key.toString()
                            )

                            dataArrayList.add(listData)
                            listAdapter = ListClientsAdapter(this@ClientsActivity, dataArrayList)
                            binding.listClients.adapter = listAdapter
                            binding.listClients.isClickable = true
                        }

                        if (dataArrayList.size == 0) {
                            binding.noClients.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }

        binding.listClients.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val intent = Intent(this@ClientsActivity, ClientView::class.java)
                intent.putExtra("hash", dataArrayList[i].hash)
                startActivity(intent)
            }

        binding.searchClient.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateFilteredList(s.toString())
                binding.noSearch.visibility = View.INVISIBLE

                listAdapter = ListClientsAdapter(this@ClientsActivity, filteredList)
                binding.listClients.adapter = listAdapter
                binding.listClients.isClickable = true
                binding.listClients.onItemClickListener =
                    AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val intent = Intent(this@ClientsActivity, ClientView::class.java)
                        intent.putExtra("hash", filteredList[i].hash)
                        startActivity(intent)
                    }
                if (filteredList.size == 0) {
                binding.noSearch.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    private fun updateFilteredList(text: String) {
        filteredList.clear()
        dataArrayList.forEach { clientData ->
            if (clientData.name.contains(text, ignoreCase = true) ||
                clientData.surname.contains(text, ignoreCase = true) ||
                clientData.patronymic.contains(text, ignoreCase = true)
            ) {
                filteredList.add(clientData)
            }
        }
    }
}