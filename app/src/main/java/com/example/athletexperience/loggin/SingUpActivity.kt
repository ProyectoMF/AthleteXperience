// SignUpActivity.kt
package com.example.athletexperience.loggin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.PersonalInfoActivity
import com.example.athletexperience.databinding.ActivitySingUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            startActivity(Intent(this, SingInActivity::class.java))
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            val userId = user!!.uid
                            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                            val userData = mapOf(
                                "email" to email,
                                "completedData" to false
                            )

                            userRef.setValue(userData).addOnCompleteListener {
                                startActivity(Intent(this, PersonalInfoActivity::class.java))
                                finish()
                            }
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "La contraseña no coincide", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se permiten campos vacíos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
