package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivityNewClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest


class NewClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewClientBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = Firebase.database.reference
        firebaseAuth = FirebaseAuth.getInstance()
        ArrayAdapter.createFromResource(
            this,
            R.array.skin_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.skinTypeSpinner.adapter = adapter
        }

        binding.addNewClientBtn.setOnClickListener {
            val name = binding.newClientName.text.toString()
            val surname = binding.newClientSurname.text.toString()
            val patronymic = binding.newClientPatronymic.text.toString()
            val phone_number = binding.newPhoneNumber.text.toString()
            val allergy = binding.newAllergy.text.toString()
            val skin_condition = binding.skinCondition.text.toString()
            val skin_type: String = binding.skinTypeSpinner.getSelectedItem().toString()

            val additional_information = binding.newAdditionalInformation.text.toString()

            val md5 = MessageDigest.getInstance("md5")
            md5.update((name + surname + patronymic).toByteArray())

            val digest: ByteArray = md5.digest()
            val client_hex = digest.toHex()
            val user = firebaseAuth.currentUser
            if (validate()) {
                user?.let {

                    var uid = it.uid
                    var is_unique = true
                        FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot in dataSnapshot.children) {
                                        val client: Client? = snapshot.getValue(Client::class.java)
                                        if (client!!.name.equals(name) && client.surname.equals(surname) && client.patronymic.equals(
                                                patronymic
                                            )
                                        ) {
                                            is_unique = false
                                            break
                                        }
                                    }

                                    if (is_unique)
                                    {
                                        database.child("clients").child(uid).child(client_hex).child("name")
                                            .setValue(name)
                                        database.child("clients").child(uid).child(client_hex).child("surname")
                                            .setValue(surname)
                                        database.child("clients").child(uid).child(client_hex).child("patronymic")
                                            .setValue(patronymic)
                                        database.child("clients").child(uid).child(client_hex)
                                            .child("additional_information").setValue(additional_information)
                                        database.child("clients").child(uid).child(client_hex)
                                            .child("phone_number").setValue(phone_number)
                                        database.child("clients").child(uid).child(client_hex)
                                            .child("skin_condition").setValue(skin_condition)
                                        database.child("clients").child(uid).child(client_hex)
                                            .child("skin_type").setValue(skin_type)
                                        database.child("clients").child(uid).child(client_hex)
                                            .child("allergy").setValue(allergy)
                                        val intent = Intent(this@NewClientActivity, MainActivity::class.java)
                                        startActivity(intent)
                                    }
                                    else {
                                        val text = "Такий клієнт вже існує! Змініть ім'я, прізвище або по-батькові!"
                                        val duration = Toast.LENGTH_SHORT

                                        val toast = Toast.makeText(this@NewClientActivity, text, duration)
                                        toast.show()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })

                    }
            }
        }
    }

    fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    fun validate(): Boolean {
        val regex = Regex("[^\\p{L}]")

        var name = binding.newClientName.text.toString()
        var surname = binding.newClientSurname.text.toString()
        var patronymic = binding.newClientPatronymic.text.toString()

        if (regex.containsMatchIn(name) || regex.containsMatchIn(surname) || regex.containsMatchIn(
                patronymic
            )
        ) {
            val text = "Ім'я, прізвище та по-батькові можуть містити тільки літери"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this@NewClientActivity, text, duration)
            toast.show()
            return false
        }
        else if (name.equals("")){
            val text = "Ім'я не може бути пустим"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this@NewClientActivity, text, duration)
            toast.show()
            return false
        }
        return true
    }
    fun readData(name: String, surname: String, patronymic: String, listener: OnGetDataListener) {

        val user = firebaseAuth.currentUser

        user?.let {
            var uid = it.uid

            FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val client: Client? = snapshot.getValue(Client::class.java)
                            if (client!!.name.equals(name) && client.surname.equals(surname) && client.patronymic.equals(patronymic)) {
                                listener.onSuccess(false);
                                break
                            }
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
        }
    }
}

interface OnGetDataListener {
    fun onSuccess(dataSnapshotValue: Boolean)
}

