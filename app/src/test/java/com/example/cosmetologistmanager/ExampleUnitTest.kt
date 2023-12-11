package com.example.cosmetologistmanager

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun addition_isCorrec() {
        assertEquals(4, 2 + 5)
    }
    @Test
    fun test_writing_data_to_firebase() {
        //val database = FirebaseDatabase.getInstance()
        lateinit var database: DatabaseReference
        database = Firebase.database.reference
        val data = "*^#^&$@#*@$^#&!("
        /*val test_ref = database.getReference("test")
        test_ref.setValue(data)

        test_ref.get().addOnSuccessListener {
                if (!it.toString().equals(data)){
                    throw RuntimeException("Failed to read data")
                }
                throw RuntimeException(it.toString())

                Log.d("READDATA", it.toString())
            }.addOnFailureListener{
                throw RuntimeException("Failed to read data")
            Log.d("READDATA", it.toString())

            }*/
        database.child("test").child("test_data").setValue(data)
            .addOnSuccessListener {
                Log.d("READDATA", "successfull")
                throw RuntimeException("Failed to read data")

            }
            .addOnFailureListener {
                throw RuntimeException("Failed to read data")

            }

    }
}