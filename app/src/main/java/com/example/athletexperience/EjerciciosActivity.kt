package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class EjerciciosActivity : AppCompatActivity() {

    private lateinit var routineName: String
    private lateinit var lyBackMainActivity: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ejercicios)

        initListeners()

        routineName = intent.getStringExtra("ROUTINE_NAME") ?: ""

        // Cargar los ejercicios desde Firebase
        loadExercisesFromFirebase()

        // Configurar la búsqueda
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterExercises(newText ?: "")
                return true
            }
        })
    }

    private fun initListeners() {
        lyBackMainActivity = findViewById(R.id.ly_back_mainActivity)
        lyBackMainActivity.setOnClickListener {
            finish()
        }
    }

    private fun loadExercisesFromFirebase() {
        val database = FirebaseDatabase.getInstance().reference.child("exercises")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val exercises = mutableListOf<Pair<String, String>>() // Pair of exercise name and image resource
                    for (exerciseSnapshot in snapshot.children) {
                        val name = exerciseSnapshot.child("name").getValue(String::class.java) ?: ""
                        val imageResource = exerciseSnapshot.child("image_resource").getValue(String::class.java) ?: ""
                        exercises.add(Pair(name, imageResource))
                    }
                    createExerciseViews(exercises)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EjerciciosActivity, "Error loading exercises", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createExerciseViews(exercises: List<Pair<String, String>>) {
        val container = findViewById<LinearLayout>(R.id.exercise_container)
        container.removeAllViews()

        for ((name, imageResource) in exercises) {
            val exerciseView = layoutInflater.inflate(R.layout.item_exercices, container, false)

            val exerciseNameTextView = exerciseView.findViewById<TextView>(R.id.tvExercises)
            val exerciseImageView = exerciseView.findViewById<ImageView>(R.id.exercise_image)

            exerciseNameTextView.text = name
            // Cargar imagen de firebase
            loadExerciseImage(imageResource, exerciseImageView)

            exerciseView.setOnClickListener {
                addExerciseToRoutine(name, routineName)
            }

            container.addView(exerciseView)
        }
    }

    private fun loadExerciseImage(imageResource: String, imageView: ImageView) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageResource)
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this).load(uri).into(imageView)
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Error de carga imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterExercises(query: String) {
        val lowerCaseQuery = query.toLowerCase()
        val container = findViewById<LinearLayout>(R.id.exercise_container)
        for (i in 0 until container.childCount) {
            val exerciseView = container.getChildAt(i)
            val exerciseNameTextView = exerciseView.findViewById<TextView>(R.id.tvExercises)
            if (exerciseNameTextView.text.toString().toLowerCase().contains(lowerCaseQuery)) {
                exerciseView.visibility = View.VISIBLE
            } else {
                exerciseView.visibility = View.GONE
            }
        }
    }

    private fun addExerciseToRoutine(exerciseName: String, routineName: String) {
        Toast.makeText(this, "Añadido $exerciseName a $routineName", Toast.LENGTH_SHORT).show()

        val returnIntent = Intent()
        returnIntent.putExtra("EXERCISE_NAME", exerciseName)
        returnIntent.putExtra("ROUTINE_NAME", routineName)
        returnIntent.putExtra("EXERCISE_IMAGE", R.drawable.ic_arrownext) // Agrega la imagen del ejercicio
        setResult(RESULT_OK, returnIntent)
        finish()
    }
}
