package com.example.cosmetologistmanager

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.cosmetologistmanager.databinding.ActivityNewExpenseBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import java.time.LocalDate
import java.util.Locale

class NewExpense : AppCompatActivity() {
    private lateinit var binding: ActivityNewExpenseBinding
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Locale("UA")
        Locale.setDefault(locale)
        binding = ActivityNewExpenseBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        setContentView(binding.root)

        binding.addExpense.setOnClickListener {
            val expense_name = binding.newExpenseName.text.toString()
            val date_day = binding.datePickerButton.dayOfMonth.toString()
            var date_month = binding.datePickerButton.month.toString()
            date_month = (date_month.toInt() + 1).toString()
            val date_year = binding.datePickerButton.year.toString()
            val procedure_price = binding.newExpensePrice.text.toString()
            val user = firebaseAuth.currentUser
            user?.let {

                val uid = it.uid
                FirebaseDatabase.getInstance().reference.child("expenses").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onDataChange(dataSnapshot: DataSnapshot) {


                            if (validate()) {

                                val uid = it.uid
                                val md5 = MessageDigest.getInstance("md5")
                                md5.update((expense_name + date_day + date_month + date_year).toByteArray())

                                val digest: ByteArray = md5.digest()
                                val appointments_hex = digest.toHex()
                                database.child("expenses").child(uid)
                                    .child(appointments_hex)
                                    .child("name").setValue(expense_name)
                                database.child("expenses").child(uid)
                                    .child(appointments_hex)
                                    .child("day").setValue(date_day)
                                database.child("expenses").child(uid)
                                    .child(appointments_hex)
                                    .child("month").setValue(date_month)
                                database.child("expenses").child(uid)
                                    .child(appointments_hex)
                                    .child("year").setValue(date_year)
                                database.child("expenses").child(uid)
                                    .child(appointments_hex)
                                    .child("price").setValue(procedure_price)
                                val intent =
                                    Intent(
                                        this@NewExpense,
                                        MainActivity::class.java
                                    )
                                startActivity(intent)
                            }


                        }

                        override fun onCancelled(databaseError: DatabaseError) {}

                    })
            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validate(): Boolean {
        val date_day = binding.datePickerButton.dayOfMonth.toString()
        var date_month = binding.datePickerButton.month.toString()
        date_month = (date_month.toInt() + 1).toString()
        val date_year = binding.datePickerButton.year.toString()
        val expense_name = binding.newExpenseName.text.toString()
        val expense_price = binding.newExpensePrice.text.toString()

        if (expense_name.equals("")){
            val text = "Поле назви витрати не може бути пустим"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@NewExpense, text, duration)
            toast.show()
            return false
        }
        if (expense_price.equals("")){
            val text = "Поле ціни витрати не може бути пустим"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@NewExpense, text, duration)
            toast.show()
            return false
        }
        if (isFutureDate(date_day?.toInt()!!, date_month?.toInt()!!, date_year?.toInt()!!)){
            val text = "Дата витрати не може бути у майбутньому"
            val duration = Toast.LENGTH_LONG

            val toast = Toast.makeText(this@NewExpense, text, duration)
            toast.show()
            return false
        }

        return true

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun isFutureDate(day: Int, month: Int, year: Int): Boolean {
        val currentDate = LocalDate.now()
        val inputDate = LocalDate.of(year, month, day)
        return inputDate.isAfter(currentDate)
    }
}

fun ByteArray.toHex(): String =
    joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
