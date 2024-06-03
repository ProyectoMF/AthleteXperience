package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.AcitivityMapBinding
import com.example.athletexperience.loggin.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapActivity : AppCompatActivity() , OnMapReadyCallback{
    private lateinit var binding: AcitivityMapBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var mGoogleMap: GoogleMap? = null
    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderUserEmail: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcitivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val mapOptionButton: ImageButton = findViewById(R.id.mapOptionsMenu)
        val popupMenu = PopupMenu(this, mapOptionButton)
        popupMenu.menuInflater.inflate(R.menu.maps_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            changeMap(menuItem.itemId)
            true
        }
        mapOptionButton.setOnClickListener {
            popupMenu.show()
        }

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
                        Log.e("MapActivity", "Error al cargar el perfil", error.toException())
                    }
                })
        }
    }

    private fun changeMap(itemId: Int) {
        when (itemId) {
            R.id.normal_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybrid_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.statellite_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_map -> mGoogleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        val gyms = getFamousGyms()
        for (gym in gyms) {
            mGoogleMap?.addMarker(
                MarkerOptions()
                    .position(gym.location)
                    .title(gym.name))
        }

        val madrid = LatLng(40.416775, -3.703790)
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 10f))
    }

    private fun getFamousGyms(): List<Gym> {
        return listOf(
            Gym("David Lloyds Sports Club", LatLng(40.465845, -3.615453)),
            Gym("B3B Woman Studio", LatLng(40.426210, -3.681383)),
            Gym("Holmes Place", LatLng(40.476570, -3.676029)),
            Gym("Sergio Ramos by John Reed", LatLng(40.459619, -3.689530)),
            Gym("Reebok Sports Club", LatLng(40.435004, -3.688347)),
            Gym("Holiday Gym Princesa", LatLng(40.431635, -3.714312)),
            Gym("Metropolitan Abascal", LatLng(40.440782, -3.699536)),
            Gym("Opera Gym", LatLng(40.421342, -3.711044)),
            Gym("McFit", LatLng(40.394110, -3.693196)),
            Gym("Anytime Fitness", LatLng(40.426081, -3.676694)),
            Gym("CrossFit Singular Box", LatLng(40.433634, -3.679927)),
            Gym("In Shape 24Seven", LatLng(40.430000, -3.709997)),
            Gym("Gymage Lounge Resort", LatLng(40.423516, -3.705416)),
            Gym("GO fit", LatLng(40.444429, -3.654167)),
            Gym("SmartClub", LatLng(40.429572, -3.709292)),
            Gym("Reto 48", LatLng(40.446578, -3.694242)),
            Gym("Basic Fit", LatLng(40.416347, -3.703828)),
            Gym("Viva Gym", LatLng(40.428516, -3.704484)),
            Gym("Siclo", LatLng(40.445308, -3.688416)),
            Gym("Crys Dyaz & Co", LatLng(40.518567, -3.648158)),
            Gym("Yoofit", LatLng(40.472505, -3.688073)),
            Gym("Tru Cycle", LatLng(40.426800, -3.704074)),
            Gym("Hiit Studio", LatLng(40.420000, -3.703716)),
            Gym("TopCycle", LatLng(40.447029, -3.705528)),
            Gym("Síclo", LatLng(40.423865, -3.693731)),
            Gym("Club Deportivo José Valenciano", LatLng(40.431224, -3.714221)),
            Gym("Fightland", LatLng(40.437688, -3.703382)),
            Gym("Boxing Club Suanzes", LatLng(40.440317, -3.704329)),
            Gym("Brooklyn Fitboxing", LatLng(40.419879, -3.704269)),
            Gym("Madison Boxing Gym", LatLng(40.428243, -3.702218)),
            Gym("47 MMA Studio", LatLng(40.432389, -3.702671)),
            Gym("Tatamisfera", LatLng(40.426700, -3.704800))
        )
    }
}