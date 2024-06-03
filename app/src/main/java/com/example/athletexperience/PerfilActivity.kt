package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.ActivityPerfilBinding
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

class PerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderUserEmail: TextView
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

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
                    // Ya estamos en PerfilActivity
                    true
                }
                R.id.nav_rate_us -> {
                    val intent = Intent(this, RateActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val headerView: View = navView.getHeaderView(0)
        navHeaderUserName = headerView.findViewById(R.id.user_name)
        navHeaderUserEmail = headerView.findViewById(R.id.usermail)

        loadUserProfile()

        setupInputFilters()
        binding.btnUpdate.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        val name = binding.edittextName.text.toString()
        val phone = binding.edittextPhone.text.toString()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (!phone.matches(Regex("^[0-9]{9}$"))) {
            Toast.makeText(this, "Número de teléfono inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val email = mAuth.currentUser?.email ?: ""

            val userProfileMap = mutableMapOf<String, Any>(
                "profile/name" to name,
                "profile/email" to email,
                "profile/phone" to phone
            )

            saveUserProfile(userId, userProfileMap)
        }
    }

    private fun saveUserProfile(userId: String, userProfileMap: Map<String, Any>) {
        database.child("users").child(userId).updateChildren(userProfileMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navHeaderUserName.text = userProfileMap["profile/name"].toString()
                    navHeaderUserEmail.text = userProfileMap["profile/email"].toString()
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadUserProfile() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            binding.edittextName.setText(snapshot.child("name").value.toString())
                            binding.edittextPhone.setText(snapshot.child("phone").value.toString())
                            binding.textviewFullname.text = snapshot.child("name").value.toString()

                            navHeaderUserName.text = snapshot.child("name").value.toString()
                            navHeaderUserEmail.text = snapshot.child("email").value.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@PerfilActivity, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()
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

    private fun setupInputFilters() {
        val phoneEditText: EditText = findViewById(R.id.edittext_phone)
        phoneEditText.filters = arrayOf(InputFilter.LengthFilter(9))
        phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.toString().matches(Regex("^[0-9]*$"))) {
                    phoneEditText.error = "Solo se permiten números"
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}