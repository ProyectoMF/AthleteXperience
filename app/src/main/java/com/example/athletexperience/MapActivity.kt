package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Spinner
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

        // Configure Spinner
        val citySelector: Spinner = findViewById(R.id.citySelector)
        citySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> zoomToCity("Madrid")
                    1 -> zoomToCity("Barcelona")
                    2 -> zoomToCity("Valencia")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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
        zoomToCity("Madrid") // Default city on map ready
    }

    private fun zoomToCity(city: String) {
        val gyms: List<Gym> = when (city) {
            "Madrid" -> getMadridGyms()
            "Barcelona" -> getBarcelonaGyms()
            "Valencia" -> getValenciaGyms()
            else -> emptyList()
        }

        mGoogleMap?.clear()
        for (gym in gyms) {
            mGoogleMap?.addMarker(
                MarkerOptions()
                    .position(gym.location)
                    .title(gym.name)
            )
        }

        val cityLocation = when (city) {
            "Madrid" -> LatLng(40.416775, -3.703790)
            "Barcelona" -> LatLng(41.385064, 2.173404)
            "Valencia" -> LatLng(39.469907, -0.376288)
            else -> LatLng(0.0, 0.0)
        }
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(cityLocation, 12f))
    }
    private fun getMadridGyms(): List<Gym> {
        return listOf(
            Gym("David Lloyds Sports Club", LatLng(40.465845, -3.615453)),
            Gym("B3B Woman Studio", LatLng(40.426210, -3.681383)),
            Gym("Holmes Place", LatLng(40.476570, -3.676029)),
            Gym("Sergio Ramos by John Reed", LatLng(40.459619, -3.689530)),
            Gym("Reto 48", LatLng(40.438220, -3.688370)),
            Gym("Basic Fit", LatLng(40.416775, -3.703790)),
            Gym("Viva Gym", LatLng(40.429315, -3.678640)),
            Gym("Siclo", LatLng(40.424815, -3.694441)),
            Gym("Crys Dyaz & Co", LatLng(40.518567, -3.648158)),
            Gym("Yoofit", LatLng(40.472505, -3.688073)),
            Gym("Tru Cycle", LatLng(40.426800, -3.704074)),
            Gym("Hiit Studio", LatLng(40.420000, -3.703716)),
            Gym("McFit", LatLng(40.394110, -3.693196)),
            Gym("Anytime Fitness", LatLng(40.426081, -3.676694)),
            Gym("Opera Gym", LatLng(40.421342, -3.711044)),
            Gym("Urban Monkey", LatLng(40.438310, -3.693550)),
            Gym("Fightland", LatLng(40.437688, -3.703382)),
            Gym("Madison Boxing Gym", LatLng(40.428243, -3.702218)),
            Gym("47 MMA Studio", LatLng(40.432389, -3.702671)),
            Gym("Tatamisfera", LatLng(40.426700, -3.704800)),
            Gym("Brooklyn Fitboxing", LatLng(40.419879, -3.704269)),
            Gym("Bikram Yoga Spain", LatLng(40.437000, -3.685400)),
            Gym("Zentro Urban Yoga", LatLng(40.426280, -3.684060)),
            Gym("Madrid City Yoga", LatLng(40.437080, -3.702900)),
            Gym("The Class Yoga", LatLng(40.434490, -3.678220)),
            Gym("Studio 34 Pilates & Yoga", LatLng(40.440780, -3.705300)),
            Gym("AltaFit", LatLng(40.403190, -3.684230)),
            Gym("Holiday Gym Princesa", LatLng(40.431635, -3.714312)),
            Gym("Club Metropolitan Abascal", LatLng(40.440782, -3.699536)),
            Gym("Trib3 Cuzco", LatLng(40.450000, -3.694000))
        )
    }
    private fun getBarcelonaGyms(): List<Gym> {
        return listOf(
            Gym("Holmes Place", LatLng(41.387018, 2.169599)),
            Gym("DiR", LatLng(41.394827, 2.148064)),
            Gym("Metropolitan Club", LatLng(41.400510, 2.191007)),
            Gym("Anytime Fitness", LatLng(41.387917, 2.170127)),
            Gym("VivaGym", LatLng(41.403383, 2.173680)),
            Gym("Arsenal Masculí", LatLng(41.394230, 2.169250)),
            Gym("BCN Fitness", LatLng(41.387098, 2.170734)),
            Gym("Sharma Climbing", LatLng(41.409889, 2.202716)),
            Gym("Climbat La Foixarda", LatLng(41.366363, 2.158958)),
            Gym("XFit Eixample Fitness", LatLng(41.385218, 2.160181)),
            Gym("LoveCycle", LatLng(41.399183, 2.148896)),
            Gym("El Club de la Lucha", LatLng(41.393317, 2.182826)),
            Gym("Entrena en Barcelona", LatLng(41.409964, 2.189324)),
            Gym("Muay Thai Barcelona", LatLng(41.401637, 2.175014)),
            Gym("Brooklyn Fitboxing Gracia", LatLng(41.398358, 2.156188)),
            Gym("MMA Barcelona Team", LatLng(41.402914, 2.180627)),
            Gym("Academia Jiu-Jitsu", LatLng(41.403453, 2.196736)),
            Gym("Gimnasio Puro Impacto", LatLng(41.415442, 2.187229)),
            Gym("Templum BCN", LatLng(41.380867, 2.140692)),
            Gym("Yoga Studio Barcelona", LatLng(41.385063, 2.166796)),
            Gym("Hot Yoga", LatLng(41.405204, 2.169418)),
            Gym("Jivamukti Yoga", LatLng(41.388711, 2.173764)),
            Gym("Yogaia", LatLng(41.404414, 2.177754)),
            Gym("Bikram Yoga", LatLng(41.389512, 2.167659)),
            Gym("Espai Yoga", LatLng(41.393218, 2.163421)),
            Gym("Studio Australia", LatLng(41.402484, 2.161608)),
            Gym("Outdoor Circuits Bootcamp", LatLng(41.385941, 2.139329)),
            Gym("Barcelona Bootcamp", LatLng(41.383530, 2.149951)),
            Gym("Trib3", LatLng(41.386970, 2.164263)),
            Gym("Cem Joan Miró", LatLng(41.380000, 2.154000)),
            Gym("Gracia Fitness", LatLng(41.403871, 2.155983))
        )
    }

    private fun getValenciaGyms(): List<Gym> {
        return listOf(
            Gym("Metropolitan Valencia", LatLng(39.469907, -0.376288)),
            Gym("Dir Valencia", LatLng(39.470207, -0.382351)),
            Gym("VivaGym Valencia", LatLng(39.468217, -0.373537)),
            Gym("Anytime Fitness Valencia", LatLng(39.475502, -0.375379)),
            Gym("Activa Club Valencia", LatLng(39.474600, -0.372547)),
            Gym("Gymage Valencia", LatLng(39.477569, -0.380654)),
            Gym("Sano Valencia", LatLng(39.466643, -0.377452)),
            Gym("McFit Valencia", LatLng(39.463531, -0.377249)),
            Gym("Holmes Place Valencia", LatLng(39.470473, -0.383099)),
            Gym("Basic-Fit Valencia", LatLng(39.472324, -0.378869)),
            Gym("CrossFit Runa", LatLng(39.475200, -0.379569)),
            Gym("Reebok Sports Club Valencia", LatLng(39.476256, -0.374152)),
            Gym("Salud y Forma", LatLng(39.472905, -0.378342)),
            Gym("Fivestars Fitness Club", LatLng(39.465220, -0.379854)),
            Gym("Brooklyn Fitboxing Valencia", LatLng(39.470998, -0.379125)),
            Gym("Metropolitan Aqua", LatLng(39.469200, -0.378300)),
            Gym("VivaGym Aqua", LatLng(39.472260, -0.376350)),
            Gym("Gimnasio Gran Turia", LatLng(39.466814, -0.379150)),
            Gym("Wellness & Fitness", LatLng(39.468506, -0.377704)),
            Gym("Arena Alicante", LatLng(39.465500, -0.376600)),
            Gym("AltaFit Valencia", LatLng(39.471000, -0.377000)),
            Gym("Dreamfit Valencia", LatLng(39.468000, -0.376000)),
            Gym("VivaGym El Saler", LatLng(39.470000, -0.374000)),
            Gym("Atalanta Sport Club", LatLng(39.472000, -0.373000)),
            Gym("CrossFit Valencia", LatLng(39.474000, -0.372000)),
            Gym("Gimnasio Fitness Place", LatLng(39.469000, -0.375000)),
            Gym("Sano Center", LatLng(39.467000, -0.374000)),
            Gym("CrossFit Hummer", LatLng(39.468000, -0.373000)),
            Gym("Holiday Gym Valencia", LatLng(39.466000, -0.372000)),
            Gym("O2 Centro Wellness Valencia", LatLng(39.471000, -0.371000))
        )
    }
}

