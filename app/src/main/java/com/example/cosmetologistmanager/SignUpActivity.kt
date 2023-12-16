package com.example.cosmetologistmanager

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {

                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            database = Firebase.database.reference
                            val user = firebaseAuth.currentUser
                            user?.let {
                                var uid = it.uid
                                Log.d("uid", uid)
                                database.child(uid).child("email").setValue(email)
                                database.child(uid).child("password").setValue(pass)
                            }
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                        } else {
                            try {
                                throw it.getException()!!
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                Toast.makeText(this, "Слабкий пароль", Toast.LENGTH_SHORT).show()
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(this, "Невірна електронна пошта", Toast.LENGTH_SHORT).show()
                            } catch (e: FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "Користувач з такою поштою вже існує", Toast.LENGTH_SHORT).show()

                            } catch (e: Exception) {
                                Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_SHORT).show()
                            }
                  }
                    }
                } else {
                    Toast.makeText(this, "Паролі не співпадають", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Пароль не введений", Toast.LENGTH_SHORT).show()

            }
        }
    }
    override fun onStart() {
        super.onStart()

        if(firebaseAuth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}