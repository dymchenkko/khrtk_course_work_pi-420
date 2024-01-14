package com.example.cosmetologistmanager

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private var is_validation_passed = false
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

        binding.passET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                val pass = binding.passET.text.toString()
                is_validation_passed = true

                if (pass.length >= 6 ) {
                    binding.lenght.setTextColor(Integer.parseUnsignedInt("FF018786",16))
                } else {
                    binding.lenght.setTextColor(Integer.parseUnsignedInt("FFFF0000",16))
                    is_validation_passed = false
                }

                if (containsUpperCase(pass)) {
                    binding.bigLetter.setTextColor(Integer.parseUnsignedInt("FF018786",16))
                } else {
                    binding.bigLetter.setTextColor(Integer.parseUnsignedInt("FFFF0000",16))
                    is_validation_passed = false
                }

                if (containsDigit(pass) ) {
                    binding.oneNumber.setTextColor(Integer.parseUnsignedInt("FF018786",16))
                } else {
                    binding.oneNumber.setTextColor(Integer.parseUnsignedInt("FFFF0000",16))
                    is_validation_passed = false
                }
            }
        })

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    if (is_validation_passed) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            database = Firebase.database.reference
                            val user = firebaseAuth.currentUser
                            user?.let {
                                var uid = it.uid
                                Log.d("uid", uid)
                                database.child("cosmetologists").child(uid).child("email").setValue(email)
                                database.child("cosmetologists").child(uid).child("password").setValue(pass)
                            }
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                        } else {
                            try {
                                throw it.getException()!!
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                Toast.makeText(this, "Слабкий пароль", Toast.LENGTH_SHORT).show()
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(this, "Невірна електронна пошта", Toast.LENGTH_SHORT)
                                    .show()
                            } catch (e: FirebaseAuthUserCollisionException) {
                                Toast.makeText(
                                    this,
                                    "Користувач з такою поштою вже існує",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } catch (e: Exception) {
                                Toast.makeText(this@SignUpActivity, e.message, Toast.LENGTH_SHORT)
                                    .show()
                            }

                        }
                    }

                    }
                    else {
                        Toast.makeText(this, "Введенний пароль не задовільняє усім умовам", Toast.LENGTH_LONG).show()
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
    fun containsUpperCase(input: String): Boolean {
        for (char in input) {
            if (char.isUpperCase()) {
                return true
            }
        }
        return false
    }

    fun containsDigit(input: String): Boolean {
        for (char in input) {
            if (char.isDigit()) {
                return true
            }
        }
        return false
    }
}