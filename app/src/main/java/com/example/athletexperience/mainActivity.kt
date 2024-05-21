package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.ActivityMainBinding
import com.example.athletexperience.loggin.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class mainActivity : AppCompatActivity() {

    // Variable de enlace de vistas
    private lateinit var binding: ActivityMainBinding

    // Toggle para el botón de navegación
    lateinit var toggle : ActionBarDrawerToggle

    // Cliente de inicio de sesión de Google
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // Instancia de FirebaseAuth para autenticación
    private lateinit var mAuth: FirebaseAuth

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
        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)

        // Configurar el toggle para abrir y cerrar el NavigationDrawer
        toggle = ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Obtener la vista de navegación
        val navView : NavigationView = findViewById(R.id.nav_view)

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
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}