package com.example.athletexperience

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
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
import de.hdodenhof.circleimageview.CircleImageView

class PerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHeaderProfileImage: CircleImageView
    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderUserEmail: TextView
    private lateinit var database: DatabaseReference
    private lateinit var mAuth: FirebaseAuth

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }

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

        // Obtener referencias a las vistas del nav_header
        val headerView: View = navView.getHeaderView(0)
        navHeaderProfileImage = headerView.findViewById(R.id.profile_image)
        navHeaderUserName = headerView.findViewById(R.id.user_name)
        navHeaderUserEmail = headerView.findViewById(R.id.usermail)

        // Cargar datos del perfil desde Firebase
        loadUserProfile()

        setupInputFilters()
        binding.btnUpdate.setOnClickListener {
            updateProfile()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun onProfileImageClick(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = data?.data
            val imageView: ImageView = findViewById(R.id.image_profile)
            imageView.setImageURI(selectedImage)

            // Actualizar la imagen en el nav_header
            navHeaderProfileImage.setImageURI(selectedImage)
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

    private fun updateProfile() {
        val nameEditText: EditText = findViewById(R.id.edittext_name)
        val phoneEditText: EditText = findViewById(R.id.edittext_phone)

        val name = nameEditText.text.toString()
        val phone = phoneEditText.text.toString()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (!phone.matches(Regex("^[0-9]{9}$"))) {
            Toast.makeText(this, "Número de teléfono inválido", Toast.LENGTH_SHORT).show()
            return
        }

        // Guardar los datos en Firebase
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val email = mAuth.currentUser?.email ?: ""
            val userProfile = UserProfile(name, email, phone)
            database.child("users").child(userId).setValue(userProfile)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        binding.textviewFullname.text = name

                        // Actualizar los campos del nav_header
                        navHeaderUserName.text = name
                        navHeaderUserEmail.text = email

                        // Actualizar los datos en mainActivity
                        val resultIntent = Intent()
                        resultIntent.putExtra("USER_PROFILE", userProfile)
                        setResult(Activity.RESULT_OK, resultIntent)

                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al actualizar el perfil", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loadUserProfile() {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userProfile = snapshot.getValue(UserProfile::class.java)
                        if (userProfile != null) {
                            findViewById<EditText>(R.id.edittext_name).setText(userProfile.name)
                            findViewById<EditText>(R.id.edittext_phone).setText(userProfile.phone)
                            binding.textviewFullname.text = userProfile.name

                            navHeaderUserName.text = userProfile.name
                            navHeaderUserEmail.text = userProfile.email

                            // Si tienes la URL de la imagen de perfil guardada en Firebase, puedes cargarla usando una biblioteca de carga de imágenes como Glide o Picasso
                            // Glide.with(this@PerfilActivity).load(userProfile.profileImageUri).into(navHeaderProfileImage)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@PerfilActivity, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
