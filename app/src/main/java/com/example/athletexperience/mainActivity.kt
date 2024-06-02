package com.example.athletexperience

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.ActivityMainBinding
import com.example.athletexperience.loggin.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class mainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var routineAdapter: RoutineAdapter
    private lateinit var navHeaderProfileImage: CircleImageView
    private lateinit var navHeaderUserName: TextView
    private lateinit var navHeaderUserEmail: TextView
    private val ADD_EXERCISE_REQUEST_CODE = 1
    private val UPDATE_PROFILE_REQUEST_CODE = 2
    private var selectedRoutineName: String? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        setSupportActionBar(binding.toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_notes -> {
                    val intent = Intent(this, NotesActivity::class.java)
                    startActivity(intent)
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
                    startActivityForResult(intent, UPDATE_PROFILE_REQUEST_CODE)
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

        // Obtener el nombre del usuario
        getUserName { userName ->
            routineAdapter = RoutineAdapter(mutableListOf(), { routine ->
                selectedRoutineName = routine.name
                val intent = Intent(this, EjerciciosActivity::class.java)
                intent.putExtra("ROUTINE_NAME", routine.name)
                startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE)
            }, { routine ->
                saveRoutineToDatabase(routine)
            }, { routine ->
                deleteRoutineFromDatabase(routine)
            }, { routine, exercise ->
                deleteExerciseFromRoutineInDatabase(routine, exercise)
            })

            binding.viewPager.adapter = routineAdapter

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.setCustomView(R.layout.custom_tab)
            }.attach()

            binding.btAddRutina.setOnClickListener {
                showAddRoutineDialog()
            }

            loadUserRoutines(userName)
        }
    }

    private fun getUserName(callback: (String) -> Unit) {
        val user = mAuth.currentUser
        user?.let {
            val userId = it.uid
            database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userProfile = snapshot.getValue(UserProfile::class.java)
                    userName = userProfile?.name?.replace(".", "_")?.replace("#", "_")?.replace("$", "_")?.replace("[", "_")?.replace("]", "_")
                    if (userName.isNullOrEmpty()) {
                        userName = "Unknown"
                    }
                    callback(userName!!)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to get user name", error.toException())
                }
            })
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
                val newRoutine = Routine(routineName, mutableListOf())
                val newPosition = routineAdapter.addRoutine(newRoutine)
                saveRoutineToDatabase(newRoutine)
                binding.viewPager.setCurrentItem(newPosition, true)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun saveRoutineToDatabase(routine: Routine) {
        getUserName { userName ->
            val routineRef = database.child("users").child(userName).child("routines").child(routine.name)
            val routineData = routine.exercises.map { exercise ->
                exercise.name to exercise.sets
            }.toMap()
            routineRef.setValue(routineData)
        }
    }

    private fun deleteRoutineFromDatabase(routine: Routine) {
        getUserName { userName ->
            database.child("users").child(userName).child("routines").child(routine.name).removeValue()
        }
    }

    private fun deleteExerciseFromRoutineInDatabase(routine: Routine, exercise: Exercise) {
        getUserName { userName ->
            database.child("users").child(userName).child("routines").child(routine.name).child("exercises").child(exercise.name).removeValue()
        }
    }

    private fun loadUserRoutines(userName: String) {
        database.child("users").child(userName).child("routines")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val routines = mutableListOf<Routine>()
                    for (routineSnapshot in snapshot.children) {
                        val routineName = routineSnapshot.key ?: continue
                        val exercisesSnapshot = routineSnapshot.child("exercises")
                        val exercises = mutableListOf<Exercise>()
                        for (exerciseSnapshot in exercisesSnapshot.children) {
                            val exerciseName = exerciseSnapshot.key ?: continue
                            val setsSnapshot = exerciseSnapshot.child("sets")
                            val sets = mutableListOf<Set>()
                            for (setSnapshot in setsSnapshot.children) {
                                val setNumber = setSnapshot.key?.toIntOrNull() ?: continue
                                val weight = setSnapshot.child("weight").getValue(Double::class.java) ?: 0.0
                                val reps = setSnapshot.child("reps").getValue(Int::class.java) ?: 0
                                sets.add(Set(setNumber, weight, reps))
                            }
                            exercises.add(Exercise(exerciseName, sets))
                        }
                        routines.add(Routine(routineName, exercises))
                    }
                    routineAdapter.updateRoutines(routines)
                    setupTabs(routines) // Configurar las pestañas después de cargar las rutinas
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to load routines", error.toException())
                }
            })
    }

    private fun setupTabs(routines: List<Routine>) {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.removeAllTabs() // Limpiar todas las pestañas existentes
        routines.forEach { routine ->
            tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.custom_tab)) // Añadir pestaña con diseño personalizado
        }
    }

    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            val intent = Intent(this@mainActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
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
                            navHeaderUserName.text = userProfile.name
                            navHeaderUserEmail.text = userProfile.email

                            // Si tienes la URL de la imagen de perfil guardada en Firebase, puedes cargarla usando una biblioteca de carga de imágenes como Glide o Picasso
                            // Glide.with(this@mainActivity).load(userProfile.profileImageUri).into(navHeaderProfileImage)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@mainActivity, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
                    }
                })
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
        if (requestCode == ADD_EXERCISE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val exerciseName = data?.getStringExtra("EXERCISE_NAME")
            if (exerciseName != null && selectedRoutineName != null) {
                routineAdapter.addExerciseToRoutine(selectedRoutineName!!, Exercise(exerciseName))
                saveExerciseToDatabase(selectedRoutineName!!, Exercise(exerciseName))
            }
        } else if (requestCode == UPDATE_PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val userProfile = data?.getParcelableExtra<UserProfile>("USER_PROFILE")
            if (userProfile != null) {
                navHeaderUserName.text = userProfile.name
                navHeaderUserEmail.text = userProfile.email
            }
        } else if (requestCode == PerfilActivity.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = data?.data
            navHeaderProfileImage.setImageURI(selectedImage)
        }
    }

    private fun saveExerciseToDatabase(routineName: String, exercise: Exercise) {
        getUserName { userName ->
            val exerciseRef = database.child("users").child(userName).child("routines")
                .child(routineName).child("exercises").child(exercise.name)
            exerciseRef.setValue(exercise.sets)
        }
    }

    private fun saveSetToDatabase(routineName: String, exerciseName: String, set: Set) {
        getUserName { userName ->
            val setRef = database.child("users").child(userName).child("routines")
                .child(routineName).child("exercises").child(exerciseName).child("sets").child(set.number.toString())
            setRef.child("weight").setValue(set.weight)
            setRef.child("reps").setValue(set.reps)
        }
    }
}
