package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.ActivityNotesBinding
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesActivity : AppCompatActivity() {


    private lateinit var binding: ActivityNotesBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var calendarView: CalendarView
    private lateinit var noteEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var notesContainer: LinearLayout
    private val notesList = mutableListOf<TextView>()
    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderUserEmail: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


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
                    // Ya estamos en NotesActivity
                    true
                }
                R.id.nav_logout -> {
                    signOutAndStartSignInActivity()
                    true
                }
                R.id.nav_map -> {
                    val intent = Intent(this, MapActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_rate_us -> {
                    val intent = Intent(this, RateActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_share -> {
                    shareLink()
                    true
                }
                else -> false
            }
        }

        val headerView: View = navView.getHeaderView(0)
        navHeaderUserName = headerView.findViewById(R.id.user_name)
        navHeaderUserEmail = headerView.findViewById(R.id.usermail)

        loadUserProfile()

        calendarView = findViewById(R.id.calendarView)
        noteEditText = findViewById(R.id.noteEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        notesContainer = findViewById(R.id.notesContainer)

        saveButton.setOnClickListener {
            val note = noteEditText.text.toString()
            if (note.isNotEmpty()) {
                val date = getCurrentDate()
                val noteWithDate = "[$date] $note"
                addNoteToContainer(noteWithDate)
                saveNoteToDatabase(noteWithDate)
                noteEditText.text.clear()
            }
        }

        deleteButton.setOnClickListener {
            ActivarModoBorrado()
        }

        loadUserNotes()
    }

    private fun loadUserProfile() {
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId).child("profile")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userProfile = snapshot.getValue(UserProfile::class.java)
                        if (userProfile != null) {
                            navHeaderUserName.text = userProfile.name
                            navHeaderUserEmail.text = userProfile.email
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("NotesActivity", "Error al cargar el perfil", error.toException())
                    }
                })
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun addNoteToContainer(note: String) {
        val textView = TextView(this).apply {
            text = note
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@NotesActivity, android.R.color.holo_orange_light))
            setPadding(16, 16, 16, 16)
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 16)
        }
        textView.layoutParams = layoutParams

        textView.setOnClickListener {
            if (deleteButton.isSelected) {
                notesContainer.removeView(textView)
                notesList.remove(textView)
                deleteNoteFromDatabase(note)
            }
        }

        notesList.add(textView)
        notesContainer.addView(textView)
    }

    private fun ActivarModoBorrado() {
        deleteButton.isSelected = !deleteButton.isSelected
        if (deleteButton.isSelected) {
            deleteButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
        } else {
            deleteButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
        }
    }

    private fun saveNoteToDatabase(note: String) {
        val userId = mAuth.currentUser?.uid ?: return
        val noteId = database.child("users").child(userId).child("notes").push().key ?: return
        database.child("users").child(userId).child("notes").child(noteId).setValue(note)
    }

    private fun deleteNoteFromDatabase(note: String) {
        val userId = mAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("notes")
            .orderByValue().equalTo(note)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (noteSnapshot in snapshot.children) {
                        noteSnapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotesActivity", "Error al borrar la nota", error.toException())
                }
            })
    }

    private fun loadUserNotes() {
        val userId = mAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("notes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(String::class.java)
                        if (note != null) {
                            addNoteToContainer(note)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotesActivity", "Error al cargar las notas", error.toException())
                }
            })
    }
    private fun shareLink() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Echa un vistazo a este link: https://github.com/ProyectoMF/AthleteXperience")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
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