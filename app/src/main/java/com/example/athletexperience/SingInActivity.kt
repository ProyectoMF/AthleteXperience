package com.example.athletexperience

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.databinding.ActivitySingInBinding
import com.example.athletexperience.databinding.ActivitySingUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SingInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var textView: TextView
    private lateinit var client: GoogleSignInClient
    companion object {
        private const val RC_SIGN_IN = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Configurar el cliente de inicio de sesión con Google
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        client = GoogleSignIn.getClient(this, googleSignInOptions)

        // Manejar clic en el botón de inicio de sesión con Google
        binding.ButtonGoogle.setOnClickListener {
            val signInIntent = client.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, mainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "No se admiten campos vacios", Toast.LENGTH_SHORT).show()

            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión con éxito, navegar a la actividad principal
                    val intent = Intent(this, mainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Error al autenticar con Firebase
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Error al autenticar con Firebase", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Inicio de sesión exitoso con Google, autenticar con Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Error en el inicio de sesión con Google
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onStart() {
        super.onStart()

        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, mainActivity::class.java)
            startActivity(intent)
        }
    }
}
