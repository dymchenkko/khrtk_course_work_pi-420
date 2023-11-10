package com.example.cosmetologistmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.example.cosmetologistmanager.databinding.ActivityUserAccountBinding

class UserAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.logOutBtn.setOnClickListener {
            if(firebaseAuth.currentUser != null)
            {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
        }
    }
}