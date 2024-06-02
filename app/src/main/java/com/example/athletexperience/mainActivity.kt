package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class mainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var routineAdapter: RoutineAdapter
    private val ADD_EXERCISE_REQUEST_CODE = 1
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
                R.id.nav_profile ->{
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_map -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Obtener el nombre del usuario
        getUserName()

        routineAdapter = RoutineAdapter(mutableListOf(), { routine ->
            selectedRoutineName = routine.name
            val intent = Intent(this, EjerciciosActivity::class.java)
            intent.putExtra("ROUTINE_NAME", routine.name)
            startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE)
        }, { routine ->
            updateRoutineInDatabase(routine)
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

        loadUserRoutines()
    }

    private fun getUserName() {
        val user = mAuth.currentUser
        user?.let {
            // Obtener el nombre del usuario desde el perfil de Google
            userName = it.displayName?.replace(".", "_")?.replace("#", "_")?.replace("$", "_")?.replace("[", "_")?.replace("]", "_")
            if (userName.isNullOrEmpty()) {
                userName = "Unknown"
            }
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
        if (userName == null) getUserName() // Ensure we have the userName
        val routineRef = database.child("users").child(userName!!).child("routines").child(routine.name)
        val routineData = routine.exercises.map { exercise ->
            exercise.name to exercise.sets
        }.toMap()
        routineRef.setValue(routineData)
    }

    private fun updateRoutineInDatabase(routine: Routine) {
        if (userName == null) getUserName() // Ensure we have the userName
        val routineRef = database.child("users").child(userName!!).child("routines").child(routine.name)
        val routineData = routine.exercises.map { exercise ->
            exercise.name to exercise.sets
        }.toMap()
        routineRef.setValue(routineData)
    }

    private fun deleteRoutineFromDatabase(routine: Routine) {
        if (userName == null) getUserName() // Ensure we have the userName
        database.child("users").child(userName!!).child("routines").child(routine.name).removeValue()
    }

    private fun deleteExerciseFromRoutineInDatabase(routine: Routine, exercise: Exercise) {
        if (userName == null) getUserName() // Ensure we have the userName
        database.child("users").child(userName!!).child("routines").child(routine.name).child("exercises").child(exercise.name).removeValue()
    }

    private fun loadUserRoutines() {
        if (userName == null) getUserName() // Ensure we have the userName
        database.child("users").child(userName!!).child("routines")
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
            if (exerciseName != null && selectedRoutineName != null) {
                routineAdapter.addExerciseToRoutine(selectedRoutineName!!, Exercise(exerciseName))
                saveExerciseToDatabase(selectedRoutineName!!, Exercise(exerciseName))
            }
        }
    }

    private fun saveExerciseToDatabase(routineName: String, exercise: Exercise) {
        if (userName == null) getUserName() // Ensure we have the userName
        val exerciseRef = database.child("users").child(userName!!).child("routines")
            .child(routineName).child("exercises").child(exercise.name)
        exerciseRef.setValue(exercise.sets)
    }

    private fun saveSetToDatabase(routineName: String, exerciseName: String, set: Set) {
        if (userName == null) getUserName() // Ensure we have the userName
        val setRef = database.child("users").child(userName!!).child("routines")
            .child(routineName).child("exercises").child(exerciseName).child("sets").child(set.number.toString())
        setRef.child("weight").setValue(set.weight)
        setRef.child("reps").setValue(set.reps)
    }
}
