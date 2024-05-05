package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class mainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Configurar el botón de cerrar sesión
        binding.logoutButton.setOnClickListener {
            // Cerrar sesión
            firebaseAuth.signOut()

            // Redirigir al usuario a la pantalla de inicio de sesión
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
            finish() // Finalizar la actividad actual para evitar que el usuario regrese utilizando el botón Atrás
        }
    }

}