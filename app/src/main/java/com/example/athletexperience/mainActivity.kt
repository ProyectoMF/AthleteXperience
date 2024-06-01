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
                else -> false
            }
        }

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

    private fun showAddRoutineDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        val input = EditText(this)
        input.hint = "Nombre de la rutina"
        builder.setTitle("Añadir Rutina")
        builder.setView(input)
        builder.setPositiveButton("Añadir") { _, _ ->
            val routineName = input.text.toString()
            if (routineName.isNotEmpty()) {
                // Supongamos que utilizamos una imagen predeterminada para todas las rutinas nuevas
                val defaultImageResId = R.drawable.ic_arrownext // Reemplaza con tu recurso de imagen
                val newRoutine = Routine(routineName, mutableListOf(), defaultImageResId)
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
        val userId = mAuth.currentUser?.uid ?: return
        val routineId = database.child("users").child(userId).child("routines").push().key ?: return
        database.child("users").child(userId).child("routines").child(routineId).setValue(routine)
    }

    private fun updateRoutineInDatabase(routine: Routine) {
        val userId = mAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("routines")
            .orderByChild("name").equalTo(routine.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (routineSnapshot in snapshot.children) {
                        val routineKey = routineSnapshot.key ?: continue
                        database.child("users").child(userId).child("routines").child(routineKey)
                            .setValue(routine)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to update routine", error.toException())
                }
            })
    }

    private fun deleteRoutineFromDatabase(routine: Routine) {
        val userId = mAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("routines")
            .orderByChild("name").equalTo(routine.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (routineSnapshot in snapshot.children) {
                        val routineKey = routineSnapshot.key ?: continue
                        database.child("users").child(userId).child("routines").child(routineKey)
                            .removeValue()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to delete routine", error.toException())
                }
            })
    }

    private fun deleteExerciseFromRoutineInDatabase(routine: Routine, exercise: Exercise) {
        val userId = mAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("routines")
            .orderByChild("name").equalTo(routine.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (routineSnapshot in snapshot.children) {
                        val routineKey = routineSnapshot.key ?: continue
                        val exercisesRef = database.child("users").child(userId).child("routines")
                            .child(routineKey).child("exercises")
                        exercisesRef.orderByChild("name").equalTo(exercise.name)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(exerciseSnapshot: DataSnapshot) {
                                    for (exerciseChild in exerciseSnapshot.children) {
                                        exerciseChild.ref.removeValue()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("MainActivity", "Failed to delete exercise", error.toException())
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to delete exercise from routine", error.toException())
                }
            })
    }

    private fun loadUserRoutines() {
        val userId = mAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("routines")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val routines = mutableListOf<Routine>()
                    for (routineSnapshot in snapshot.children) {
                        val routineName = routineSnapshot.child("name").getValue(String::class.java) ?: continue
                        val exercisesSnapshot = routineSnapshot.child("exercises")
                        val exercises = mutableListOf<Exercise>()
                        for (exerciseSnapshot in exercisesSnapshot.children) {
                            val exerciseName = exerciseSnapshot.child("name").getValue(String::class.java)
                            if (exerciseName != null) {
                                exercises.add(Exercise(exerciseName))
                            }
                        }
                        // Obtén el recurso de imagen, aquí se usa una imagen predeterminada si no hay ninguna específica en la base de datos
                        val imageResId = routineSnapshot.child("image").getValue(Int::class.java) ?: R.drawable.ic_arrownext
                        routines.add(Routine(routineName, exercises, imageResId))
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
            val exerciseImage = data?.getIntExtra("EXERCISE_IMAGE", R.drawable.ic_arrownext)
            if (exerciseName != null && selectedRoutineName != null) {
                routineAdapter.addExerciseToRoutine(selectedRoutineName!!, Exercise(exerciseName, exerciseImage!!))
                updateRoutineInDatabase(selectedRoutineName!!, exerciseName)
            }
        }
    }

    private fun updateRoutineInDatabase(routineName: String, exerciseName: String) {
        val userId = mAuth.currentUser?.uid ?: return
        database.child("users").child(userId).child("routines")
            .orderByChild("name").equalTo(routineName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (routineSnapshot in snapshot.children) {
                        val routineKey = routineSnapshot.key ?: continue
                        val exercisesRef = database.child("users").child(userId).child("routines")
                            .child(routineKey).child("exercises")
                        exercisesRef.push().setValue(Exercise(exerciseName))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainActivity", "Failed to update routine", error.toException())
                }
            })
    }
}