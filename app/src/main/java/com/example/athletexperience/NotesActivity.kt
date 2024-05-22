package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la barra de herramientas como la barra de soporte
        setSupportActionBar(binding.toolbar)

        // Obtener el layout del NavigationDrawer
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)

        // Configurar el toggle para abrir y cerrar el NavigationDrawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Obtener la vista de navegación
        val navView: NavigationView = findViewById(R.id.nav_view)

        // Configurar el listener del elemento de navegación seleccionado
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, mainActivity::class.java))
                    true
                }
                R.id.nav_notes -> {
                    // Ya estamos en MessageActivity
                    true
                }
                R.id.nav_logout -> {
                    signOutAndStartSignInActivity()
                    true
                }
                else -> false
            }
        }

        // Inicializar vistas
        calendarView = findViewById(R.id.calendarView)
        noteEditText = findViewById(R.id.noteEditText)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        notesContainer = findViewById(R.id.notesContainer)

        // Configurar botón de guardar
        saveButton.setOnClickListener {
            val note = noteEditText.text.toString()
            if (note.isNotEmpty()) {
                val date = getCurrentDate()
                val noteWithDate = "[$date] $note"
                addNoteToContainer(noteWithDate)
                noteEditText.text.clear()
            }
        }

        // Configurar botón de borrar
        deleteButton.setOnClickListener {
            ActivarModoBorrado()
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


        // Añadir click listener para borrar nota
        textView.setOnClickListener {
            if (deleteButton.isSelected) {
                notesContainer.removeView(textView)
                notesList.remove(textView)
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

    // Método para cerrar sesión y iniciar la actividad de inicio de sesión
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
