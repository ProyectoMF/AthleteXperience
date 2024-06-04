package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.ActivityRateusBinding
import com.example.athletexperience.loggin.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RateActivity: AppCompatActivity()  {

    private lateinit var binding: ActivityRateusBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderUserEmail: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var ratingBar: RatingBar
    private lateinit var saveRatingButton: Button
    private lateinit var averageRatingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRateusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, mainActivity::class.java))
                    true
                }
                R.id.nav_notes -> {
                    startActivity(Intent(this, NotesActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.nav_logout -> {
                    signOutAndStartSignInActivity()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_rate_us -> {
                    // Ya estamos en RateActivity
                    true
                }
                else -> false
            }
        }

        // Obtener referencias a las vistas del nav_header
        val headerView: View = navView.getHeaderView(0)
        navHeaderUserName = headerView.findViewById(R.id.user_name)
        navHeaderUserEmail = headerView.findViewById(R.id.usermail)


        loadUserProfile()

        // Inicializar vistas de la actividad de valoración
        ratingBar = findViewById(R.id.ratingBar)
        saveRatingButton = findViewById(R.id.saveRatingButton)
        averageRatingTextView = findViewById(R.id.averageRatingTextView)

        saveRatingButton.setOnClickListener {
            saveUserRating()
        }

        loadUserRating()
        loadAverageRating()
    }

    private fun loadUserProfile() {
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userProfile = snapshot.getValue(UserProfile::class.java)
                        if (userProfile != null) {
                            navHeaderUserName.text = userProfile.name
                            navHeaderUserEmail.text = userProfile.email
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("RateActivity", "Error al cargar el perfil", error.toException())
                    }
                })
        }
    }

    private fun saveUserRating() {
        val rating = ratingBar.rating
        val userId = mAuth.currentUser?.uid

        if (userId != null) {
            database.child("ratings").child(userId).setValue(rating).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RateActivity", "Rating saved successfully")
                    loadAverageRating()
                } else {
                    Log.e("RateActivity", "Error saving rating", task.exception)
                }
            }
        }
    }

    private fun loadUserRating() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            database.child("ratings").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rating = snapshot.getValue(Float::class.java)
                    if (rating != null) {
                        ratingBar.rating = rating
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RateActivity", "Error loading user rating", error.toException())
                }
            })
        }
    }

    private fun loadAverageRating() {
        database.child("ratings").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRating = 0f
                var ratingCount = 0

                for (ratingSnapshot in snapshot.children) {
                    val rating = ratingSnapshot.getValue(Float::class.java)
                    if (rating != null) {
                        totalRating += rating
                        ratingCount++
                    }
                }

                val averageRating = if (ratingCount > 0) totalRating / ratingCount else 0f
                averageRatingTextView.text = String.format("Valoración media: %.2f", averageRating)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RateActivity", "Error loading average rating", error.toException())
            }
        })
    }

    private fun signOutAndStartSignInActivity() {
        FirebaseAuth.getInstance().signOut()
        GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut().addOnCompleteListener(this) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}