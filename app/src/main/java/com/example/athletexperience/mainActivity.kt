package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.athletexperience.databinding.ActivityMainBinding
import com.example.athletexperience.loggin.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class mainActivity : AppCompatActivity() {

    // Variable de enlace de vistas
    private lateinit var binding: ActivityMainBinding

    // Toggle para el botón de navegación
    lateinit var toggle: ActionBarDrawerToggle

    // Cliente de inicio de sesión de Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // Instancia de FirebaseAuth para autenticación
    private lateinit var mAuth: FirebaseAuth

    private lateinit var routineAdapter: RoutineAdapter
    private val ADD_EXERCISE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance()

        // Configuración de opciones de inicio de sesión de Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Inicializar el cliente de inicio de sesión de Google
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

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
                R.id.nav_notes -> {
                    // Iniciar la actividad NotesActivity
                    val intent = Intent(this, NotesActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_logout -> {
                    signOutAndStartSignInActivity()
                    true
                }
                else -> false
            }
        }

        routineAdapter = RoutineAdapter(mutableListOf()) { routine ->
            val intent = Intent(this, EjerciciosActivity::class.java)
            intent.putExtra("ROUTINE_NAME", routine.name)
            startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = routineAdapter

        binding.btAddRutina.setOnClickListener {
            showAddRoutineDialog()
        }
    }

    private fun showAddRoutineDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        val input = EditText(this)
        input.hint = "Nombre de la rutina"
        builder.setTitle("Añadir Rutina")
        builder.setView(input)
        builder.setPositiveButton("Añadir") { _, _ ->
            val routineName = input.text.toString()
            if (routineName.isNotEmpty()) {
                val newRoutine = Routine(routineName)
                routineAdapter.addRoutine(newRoutine)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // Método para cerrar sesión y iniciar la actividad de inicio de sesión
    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            val intent = Intent(this@mainActivity, SignInActivity::class.java)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EXERCISE_REQUEST_CODE && resultCode == RESULT_OK) {
            val exerciseName = data?.getStringExtra("EXERCISE_NAME")
            val routineName = data?.getStringExtra("ROUTINE_NAME")

            if (exerciseName != null && routineName != null) {
                routineAdapter.addExerciseToRoutine(routineName, Exercise(exerciseName))
            }
        }
    }
}