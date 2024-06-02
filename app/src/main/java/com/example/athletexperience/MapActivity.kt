package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.AcitivityMapBinding
import com.example.athletexperience.loggin.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MapActivity : AppCompatActivity() , OnMapReadyCallback{
    private lateinit var binding: AcitivityMapBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var mGoogleMap:GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val mapFrament = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFrament.getMapAsync(this)
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
                    startActivity(Intent(this, NotesActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    // Ya estamos en MapActivity
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

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }
}