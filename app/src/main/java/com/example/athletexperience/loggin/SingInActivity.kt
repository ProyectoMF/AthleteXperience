package com.example.athletexperience.loggin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.PersonalObjetivoActivity
import com.example.athletexperience.R
import com.example.athletexperience.databinding.ActivitySingInBinding
import com.example.athletexperience.mainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SingInActivity : AppCompatActivity() {

    // Variable de enlace de vistas
    private lateinit var binding: ActivitySingInBinding

    // Instancia de FirebaseAuth para autenticación
    private lateinit var firebaseAuth: FirebaseAuth

    // URI de la imagen seleccionada
    private lateinit var imageUri: String

    companion object {
        // Códigos de solicitud
        private const val PICK_IMAGE_REQUEST = 1002
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_in)

        binding = ActivitySingInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Listener para el botón de iniciar sesión con correo electrónico y contraseña
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            // Verificar si los campos de correo electrónico y contraseña no están vacíos
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Iniciar sesión con correo electrónico y contraseña
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Redirigir a la actividad principal si la autenticación es exitosa
                        val intent = Intent(this, PersonalObjetivoActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Mostrar mensaje de error si la autenticación falla
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Mostrar mensaje si hay campos vacíos\
                Toast.makeText(this, "No se admiten campos vacios", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para el botón de abrir galería
        binding.floatingActionButton.setOnClickListener {
            openGallery()
        }

        // Listener para el botón de iniciar sesión con Google
        binding.ButtonGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Agrega el OnClickListener al TextView deseado
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // Método para iniciar sesión con Google
    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Manejar los resultados de las actividades
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                // Obtener la URI de la imagen seleccionada y mostrarla
                val uri = data.data
                uri?.let {
                    imageUri = it.toString()
                    binding.imageView.setImageURI(uri)
                }
            } else {
                // Mostrar mensaje de error si la selección de imagen falla
                Log.e("SignInActivity", "Error al seleccionar imagen de la galería")
                Toast.makeText(this, "Error al seleccionar imagen de la galería", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == RC_SIGN_IN) {
            // Manejar el resultado de iniciar sesión con Google
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Autenticar con Firebase utilizando el token de ID de Google
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Mostrar mensaje de error si el inicio de sesión con Google falla
                Toast.makeText(this, "Inicio de sesion con Google fallido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Método para autenticar con Firebase utilizando el token de ID de Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Has iniciado sesion como: ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, PersonalObjetivoActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Autenticacion fallida", Toast.LENGTH_SHORT).show()
                }
            }
    }
    // Método para abrir la galería de imágenes
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onStart() {
        super.onStart()
        // Verificar si ya hay un usuario autenticado al iniciar la actividad
        if (firebaseAuth.currentUser != null) {
            // Redirigir a la actividad principal si hay un usuario autenticado
            val intent = Intent(this, mainActivity::class.java)
            startActivity(intent)
        }
    }
}