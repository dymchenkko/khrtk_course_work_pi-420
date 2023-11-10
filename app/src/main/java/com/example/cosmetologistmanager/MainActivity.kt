package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cosmetologistmanager.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

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
    }
}