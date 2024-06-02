package com.example.athletexperience

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Patterns
import android.view.MenuItem
import android.view.View
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
import de.hdodenhof.circleimageview.CircleImageView

data class UserProfile(val name: String = "", val email: String = "", val phone: String = "", val profileImageUri: String = "")

class PerfilActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navHeaderProfileImage: CircleImageView
    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderUserEmail: TextView

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
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

    fun onProfileImageClick(view: android.view.View) {
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
        FirebaseAuth.getInstance().signOut()
        GoogleSignIn.getClient(this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut().addOnCompleteListener(this) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupInputFilters() {
        val emailEditText = binding.edittextEmail.editText
        val phoneEditText = binding.edittextPhone.editText

        emailEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.toString().endsWith("@gmail.com")) {
                    emailEditText.error = "Email must be @gmail.com"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        phoneEditText?.filters = arrayOf(InputFilter.LengthFilter(9))
        phoneEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.toString().matches(Regex("^[0-9]*$"))) {
                    phoneEditText.error = "Only numbers are allowed"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun updateProfile() {
        val name = binding.edittextName.editText?.text.toString()
        val email = binding.edittextEmail.editText?.text.toString()
        val phone = binding.edittextPhone.editText?.text.toString()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!email.endsWith("@gmail.com") || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return
        }

        if (!phone.matches(Regex("^[0-9]{9}$"))) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        binding.textviewFullname.text = name

        // Actualizar los campos del nav_header
        navHeaderUserName.text = name
        navHeaderUserEmail.text = email

        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
    }
}