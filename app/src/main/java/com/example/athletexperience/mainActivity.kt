package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class mainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fireba0seAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fireba0seAuth = Firebase.auth
        binding.logoutButton.setOnClickListener{
            Firebase.auth.signOut()
            startActivity(Intent(this,SingInActivity::class.java ))
            finish()
        }

    }

}