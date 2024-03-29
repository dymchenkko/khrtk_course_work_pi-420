package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.cosmetologistmanager.databinding.ActivityEditClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class EditClientActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityEditClientBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditClientBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ArrayAdapter.createFromResource(
            this,
            R.array.skin_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.skinTypeSpinner.adapter = adapter
        }
        val intent = this.intent
        if (intent != null) {
            val hash = intent.getStringExtra("hash")
            firebaseAuth = FirebaseAuth.getInstance()
            database = Firebase.database.reference

            val user = firebaseAuth.currentUser

                user?.let {
                    var uid = it.uid

                    FirebaseDatabase.getInstance().reference.child("clients").child(uid)
                        .child(hash.toString()).get().addOnSuccessListener {
                            var client: Client? = it.getValue(Client::class.java)

                            binding.newClientName.setText(client?.name)
                            binding.newAllergy.setText(client?.allergy)
                            binding.skinCondition.setText(client?.skin_condition)
                            binding.newPhoneNumber.setText(client?.phone_number)
                            binding.newClientSurname.setText(client?.surname)
                            binding.newClientPatronymic.setText(client?.patronymic)
                            binding.newAdditionalInformation.setText(client?.additional_information)
                            when (client?.skin_type) {
                                "Суха" -> binding.skinTypeSpinner.setSelection(1)
                                "Комбінована" -> binding.skinTypeSpinner.setSelection(2)
                                "Жирна" -> binding.skinTypeSpinner.setSelection(3)
                                else -> {
                                    binding.skinTypeSpinner.setSelection(0)
                                }
                            }

                                binding.editClientBtn.setOnClickListener {
                                    if (validate()) {FirebaseDatabase.getInstance().reference.child("clients")
                                        .child(uid)
                                        .child(hash.toString()).removeValue();

                                    val name = binding.newClientName.text.toString().trim()
                                    val surname = binding.newClientSurname.text.toString().trim()
                                    val patronymic =
                                        binding.newClientPatronymic.text.toString().trim()
                                    val phone_number = binding.newPhoneNumber.text.toString().trim()
                                    val allergy = binding.newAllergy.text.toString().trim()
                                    val skin_condition =
                                        binding.skinCondition.text.toString().trim()
                                    val skin_type: String =
                                        binding.skinTypeSpinner.getSelectedItem().toString().trim()
                                    val additional_information =
                                        binding.newAdditionalInformation.text.toString().trim()


                                    val md5 = MessageDigest.getInstance("md5")
                                    md5.update((name + surname + patronymic + additional_information).toByteArray())

                                    val digest: ByteArray = md5.digest()
                                    val client_hex = hash.toString()
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("name")
                                        .setValue(name)
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("surname")
                                        .setValue(surname)
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("patronymic")
                                        .setValue(patronymic)
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("additional_information")
                                        .setValue(additional_information)
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("phone_number").setValue(phone_number)
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("skin_condition").setValue(skin_condition)
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("skin_type").setValue(skin_type)
                                    database.child("clients").child(uid).child(client_hex)
                                        .child("allergy").setValue(allergy)
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }

                        }.addOnFailureListener {
                            Log.e("firebase", "Error getting data", it)
                        }
                }

        }

    }

    fun validate(): Boolean {
        val regex = Regex("[^\\p{L}]")

        var name = binding.newClientName.text.toString().trim()
        var surname = binding.newClientSurname.text.toString().trim()
        var patronymic = binding.newClientPatronymic.text.toString().trim()
        var phone_number = binding.newPhoneNumber.text.toString()

        if (regex.containsMatchIn(name) || regex.containsMatchIn(surname) || regex.containsMatchIn(
                patronymic
            )
        ) {
            val text = "Ім'я, прізвище та по-батькові можуть містити тільки літери"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this@EditClientActivity, text, duration)
            toast.show()
            return false
        }
        else if (name == ""){
            val text = "Ім'я не може бути пустим"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this@EditClientActivity, text, duration)
            toast.show()
            return false
        }
        else if (surname == ""){
            val text = "Прізвище не може бути пустим"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(this@EditClientActivity, text, duration)
            toast.show()
            return false
        }
        else if (phone_number.length != 9){
            val text = "Номер телефону не може бути коротше 9"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@EditClientActivity, text, duration)
            toast.show()
            return false
        }
        return true
    }

    fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
}