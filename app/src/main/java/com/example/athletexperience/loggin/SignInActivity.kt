package com.example.athletexperience.loggin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.PersonalObjetivoActivity
import com.example.athletexperience.R
import com.example.athletexperience.databinding.ActivitySingInBinding
import com.example.athletexperience.mainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignInActivity : AppCompatActivity() {
    // Variable de enlace de vistas
    private lateinit var binding: ActivitySingInBinding

    // Instancia de FirebaseAuth para autenticación
    private lateinit var firebaseAuth: FirebaseAuth

    // Cliente de Google SignIn
    private lateinit var googleSignInClient: GoogleSignInClient




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

        // Verificar si el usuario ya está autenticado
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Usuario ya autenticado, redirigir a mainActivity
            val intent = Intent(this, mainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Configurar opciones de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Listener para el botón de iniciar sesión con correo electrónico y contraseña
        binding.buttonSignIn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Verificar si el usuario es nuevo o ya existente
                        val user = firebaseAuth.currentUser
                        if (user != null) {
                            val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false
                            val intent = if (isNewUser) {
                                Intent(this, PersonalObjetivoActivity::class.java)
                            } else {
                                Intent(this, mainActivity::class.java)
                            }
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Correo o contraseña erroneas", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No se admiten campos vacíos", Toast.LENGTH_SHORT).show()
            }
        }



        // Listener para el botón de iniciar sesión con Google
        binding.ButtonGoogle.setOnClickListener {
            signOutGoogle() // Cerrar sesión de Google primero
        }

        // Agrega el OnClickListener al TextView deseado
        binding.buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // Método para cerrar sesión de Google y luego iniciar sesión nuevamente
    private fun signOutGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            signInWithGoogle() // Iniciar sesión con Google después de cerrar sesión
        }
    }

    // Método para iniciar sesión con Google
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
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
                    val newUser = task.result.additionalUserInfo?.isNewUser ?: false
                    val intent = if (newUser) {
                        Intent(this, PersonalObjetivoActivity::class.java)
                    } else {
                        Intent(this, mainActivity::class.java)
                    }
                    Toast.makeText(this, "Has iniciado sesión como: ${task.result.user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}