package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
                else -> false
            }
        }

        routineAdapter = RoutineAdapter(mutableListOf()) { routine ->
            selectedRoutineName = routine.name
            val intent = Intent(this, EjerciciosActivity::class.java)
            intent.putExtra("ROUTINE_NAME", routine.name)
            startActivityForResult(intent, ADD_EXERCISE_REQUEST_CODE)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = routineAdapter

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
                val newRoutine = Routine(routineName, mutableListOf())
                routineAdapter.addRoutine(newRoutine)
                saveRoutineToDatabase(newRoutine)
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
                        routines.add(Routine(routineName, exercises))
                    }
                    routineAdapter.updateRoutines(routines)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("mainActivity", "Failed to load routines", error.toException())
                }
            })
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
                    Log.e("mainActivity", "Failed to update routine", error.toException())
                }
            })
    }
}