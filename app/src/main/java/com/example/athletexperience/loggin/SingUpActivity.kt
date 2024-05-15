package com.example.athletexperience.loggin


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.PersonalInfoActivity
import com.example.athletexperience.PersonalObjetivoActivity
import com.example.athletexperience.databinding.ActivitySingUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    // Variable de enlace de vistas
    private lateinit var binding: ActivitySingUpBinding

    // Instancia de FirebaseAuth para autenticación
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar y establecer el diseño de la actividad
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Listener para el TextView que redirige a la actividad de información personal
        binding.textView.setOnClickListener {
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
            finish() // Finalizar la actividad actual para evitar que el usuario regrese utilizando el botón Atrás
        }

        // Listener para el botón de registro
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()
            val confirmPass = binding.confirmPassET.text.toString().trim()

            // Verificar si los campos de correo electrónico, contraseña y confirmación de contraseña no están vacíos
            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                // Verificar si la contraseña y la confirmación de contraseña coinciden
                if (pass == confirmPass) {
                    // Crear un nuevo usuario con correo electrónico y contraseña
                    registerUser(email, pass)
                } else {
                    // Mostrar mensaje si la contraseña y la confirmación de contraseña no coinciden
                    Toast.makeText(this, "La contraseña no coincide", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Mostrar mensaje si hay campos vacíos
                Toast.makeText(this, "No se permiten campos vacíos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Guardando el estado de registro del usuario como no nuevo
                val sharedPreferences = getSharedPreferences(" g  ", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("isNewUser", false)  // User is no longer new after registration
                editor.apply()

                // Redirigir al usuario para iniciar el proceso de recopilación de datos
                val intent = Intent(this, PersonalObjetivoActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Manejo de errores
                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}