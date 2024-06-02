package com.example.athletexperience

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

data class Set(var number: Int, var weight: Double, var reps: Int, var isSelected: Boolean = false)

class RepsActivity : AppCompatActivity() {
    private lateinit var lyBackMainActivity: LinearLayout
    private lateinit var btnDecreaseWeight: ImageButton
    private lateinit var btnIncreaseWeight: ImageButton
    private lateinit var btnDecreaseReps: ImageButton
    private lateinit var btnIncreaseReps: ImageButton
    private lateinit var etWeight: EditText
    private lateinit var etReps: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var recyclerViewSets: RecyclerView
    private lateinit var setsAdapter: SetsAdapter

    private var weight: Double = 2.5
    private var reps: Int = 1
    private val sets = mutableListOf<Set>()
    private var selectedSet: Set? = null
    private var routineName: String? = null
    private var exerciseName: String? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reps)

        routineName = intent.getStringExtra("ROUTINE_NAME")
        exerciseName = intent.getStringExtra("EXERCISE_NAME")
        getUserName()

        initComponents()
        initListeners()
        loadSetsFromDatabase() // Cargar series desde la base de datos
    }

    private fun getUserName() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Obtener el nombre del usuario desde el perfil de Google
            userName = it.displayName?.replace(".", "_")?.replace("#", "_")?.replace("$", "_")?.replace("[", "_")?.replace("]", "_")
            if (userName.isNullOrEmpty()) {
                userName = "Unknown"
            }
        }
    }

    private fun initComponents() {
        lyBackMainActivity = findViewById(R.id.ly_back_mainActivity)
        btnDecreaseWeight = findViewById(R.id.btnDecreaseWeight)
        btnIncreaseWeight = findViewById(R.id.btnIncreaseWeight)
        btnDecreaseReps = findViewById(R.id.btnDecreaseReps)
        btnIncreaseReps = findViewById(R.id.btnIncreaseReps)
        etWeight = findViewById(R.id.etWeight)
        etReps = findViewById(R.id.etReps)
        btnSave = findViewById(R.id.btn_guardar)
        btnDelete = findViewById(R.id.btn_borrar)
        recyclerViewSets = findViewById(R.id.recyclerViewSets)

        recyclerViewSets.layoutManager = LinearLayoutManager(this)
        setsAdapter = SetsAdapter(sets, ::onSetClicked)
        recyclerViewSets.adapter = setsAdapter

        etWeight.setText(String.format(Locale.US, "%.1f", weight))
        etReps.setText(reps.toString())
    }

    private fun initListeners() {
        lyBackMainActivity.setOnClickListener {
            finish()
        }

        btnDecreaseWeight.setOnClickListener {
            if (weight > 0) {
                weight -= 2.5
                if (weight < 0) weight = 0.0
                etWeight.setText(String.format(Locale.US, "%.1f", weight))
            }
        }

        btnIncreaseWeight.setOnClickListener {
            weight += 2.5
            etWeight.setText(String.format(Locale.US, "%.1f", weight))
        }

        btnDecreaseReps.setOnClickListener {
            if (reps > 1) {
                reps -= 1
                etReps.setText(reps.toString())
            }
        }

        btnIncreaseReps.setOnClickListener {
            reps += 1
            etReps.setText(reps.toString())
        }

        btnSave.setOnClickListener {
            weight = etWeight.text.toString().replace(",", ".").toDoubleOrNull() ?: weight
            reps = etReps.text.toString().toIntOrNull() ?: reps
            if (selectedSet != null) {
                updateSet(selectedSet!!)
            } else {
                addSet()
            }
            clearSelection()
        }

        btnDelete.setOnClickListener {
            deleteSelectedSet()
            clearSelection()
        }
    }

    private fun onSetClicked(set: Set) {
        if (set == selectedSet) {
            set.isSelected = false
            selectedSet = null
        } else {
            selectedSet?.isSelected = false
            set.isSelected = true
            selectedSet = set
            weight = set.weight
            reps = set.reps
            etWeight.setText(String.format(Locale.US, "%.1f", weight))
            etReps.setText(reps.toString())
        }
        setsAdapter.notifyDataSetChanged()
    }

    private fun addSet() {
        val setNumber = sets.size + 1
        val newSet = Set(setNumber, weight, reps)
        sets.add(newSet)
        setsAdapter.notifyItemInserted(sets.size - 1)
        saveSetToDatabase(newSet)
    }

    private fun updateSet(set: Set) {
        set.weight = weight
        set.reps = reps
        setsAdapter.notifyDataSetChanged()
        saveSetToDatabase(set)
        selectedSet = null
    }

    private fun deleteSelectedSet() {
        selectedSet?.let {
            sets.remove(it)
            sets.forEachIndexed { index, set -> set.number = index + 1 }
            setsAdapter.notifyDataSetChanged()
            deleteSetFromDatabase(it)
            selectedSet = null
        }
    }

    private fun clearSelection() {
        selectedSet?.isSelected = false
        selectedSet = null
        sets.forEach { it.isSelected = false }
        setsAdapter.notifyDataSetChanged()
    }

    private fun saveSetToDatabase(set: Set) {
        val setRef = FirebaseDatabase.getInstance().reference.child("users").child(userName!!).child("routines")
            .child(routineName!!).child("exercises").child(exerciseName!!).child("sets").child(set.number.toString())
        setRef.child("weight").setValue(set.weight)
        setRef.child("reps").setValue(set.reps)
    }

    private fun deleteSetFromDatabase(set: Set) {
        val setRef = FirebaseDatabase.getInstance().reference.child("users").child(userName!!).child("routines")
            .child(routineName!!).child("exercises").child(exerciseName!!).child("sets").child(set.number.toString())
        setRef.removeValue()
    }

    private fun loadSetsFromDatabase() {
        val setsRef = FirebaseDatabase.getInstance().reference.child("users").child(userName!!).child("routines")
            .child(routineName!!).child("exercises").child(exerciseName!!).child("sets")

        setsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sets.clear()
                for (setSnapshot in snapshot.children) {
                    val setNumber = setSnapshot.key?.toIntOrNull() ?: continue
                    val weight = setSnapshot.child("weight").getValue(Double::class.java) ?: 0.0
                    val reps = setSnapshot.child("reps").getValue(Int::class.java) ?: 0
                    sets.add(Set(setNumber, weight, reps))
                }
                setsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RepsActivity, "Error al cargar sets: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
