package com.example.athletexperience.loggin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.PersonalInfoActivity
import com.example.athletexperience.PersonalObjetivoActivity
import com.example.athletexperience.R
import com.example.athletexperience.databinding.ActivitySingInBinding
import com.example.athletexperience.mainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class SingInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)

                        userRef.get().addOnSuccessListener { dataSnapshot ->
                            val userHasCompletedData = dataSnapshot.child("completedData").getValue(Boolean::class.java) ?: false
                            if (userHasCompletedData) {
                                startActivity(Intent(this, mainActivity::class.java))
                            } else {
                                startActivity(Intent(this, PersonalObjetivoActivity::class.java))
                            }
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "No se admiten campos vacios", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ButtonGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.textView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

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
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Inicio de sesion con Google fallido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val userRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)

                userRef.get().addOnSuccessListener { dataSnapshot ->
                    val userHasCompletedData = dataSnapshot.child("completedData").getValue(Boolean::class.java) ?: false
                    if (userHasCompletedData) {
                        startActivity(Intent(this, mainActivity::class.java))
                    } else {
                        startActivity(Intent(this, PersonalObjetivoActivity::class.java))
                    }
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Autenticacion fallida", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val user = firebaseAuth.currentUser
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)

            userRef.get().addOnSuccessListener { dataSnapshot ->
                val userHasCompletedData = dataSnapshot.child("completedData").getValue(Boolean::class.java) ?: false
                if (userHasCompletedData) {
                    startActivity(Intent(this, mainActivity::class.java))
                } else {
                    startActivity(Intent(this, PersonalObjetivoActivity::class.java))
                }
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
