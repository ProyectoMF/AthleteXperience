package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EjerciciosActivity : AppCompatActivity() {

    private lateinit var exerciseLayouts: List<Pair<LinearLayout, String>>
    private lateinit var routineName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ejercicios)

        routineName = intent.getStringExtra("ROUTINE_NAME") ?: ""

// Inicializar la lista de ejercicios
        exerciseLayouts = listOf(
            Pair(findViewById(R.id.exercise_1), "Press de Banca"),
            Pair(findViewById(R.id.exercise_2), "Press de Banca Inclinado"),
            Pair(findViewById(R.id.exercise_3), "Press de Banca Declinado"),
            Pair(findViewById(R.id.exercise_4), "Aperturas en máquina"),
            Pair(findViewById(R.id.exercise_5), "Fondos"),
            Pair(findViewById(R.id.exercise_6), "Triceps en Polea"),
            Pair(findViewById(R.id.exercise_7), "Triceps en Polea tras Nuca"),
            Pair(findViewById(R.id.exercise_8), "Crunch Abdominal"),
            Pair(findViewById(R.id.exercise_9), "Crunch laterales"),
            Pair(findViewById(R.id.exercise_10), "Biceps en Banco Scott"),
            Pair(findViewById(R.id.exercise_11), "Curl de Biceps en Banco Inclinado"),
            Pair(findViewById(R.id.exercise_12), "Curl de Martillo"),
            Pair(findViewById(R.id.exercise_13), "Encogimiento de Hombros"),
            Pair(findViewById(R.id.exercise_14), "Pull Over"),
            Pair(findViewById(R.id.exercise_15), "Remo en Barra T"),
            Pair(findViewById(R.id.exercise_16), "Remo Dorsal"),
            Pair(findViewById(R.id.exercise_17), "Jalón al Pecho"),
            Pair(findViewById(R.id.exercise_18), "Peso Muerto"),
            Pair(findViewById(R.id.exercise_19), "Sentadilla con Barra"),
            Pair(findViewById(R.id.exercise_20), "Sentadilla Hacka"),
            Pair(findViewById(R.id.exercise_21), "Prensa inclinada"),
            Pair(findViewById(R.id.exercise_22), "Extensión de Gemelos"),
            Pair(findViewById(R.id.exercise_23), "Máquina de Aductores"),
            Pair(findViewById(R.id.exercise_24), "Máquina de Abductores"),
            Pair(findViewById(R.id.exercise_25), "Elevaciones Laterales Hombro"),
            Pair(findViewById(R.id.exercise_26), "Extensiones de Cuádriceps"),
            Pair(findViewById(R.id.exercise_27), "Curl de Femoral"),
            Pair(findViewById(R.id.exercise_28), "Press Militar"),
            Pair(findViewById(R.id.exercise_29), "Hombro Posterior en Máquina"),
            Pair(findViewById(R.id.exercise_30), "Patada de Glúteo")
        )


        // Configurar listeners para todos los ejercicios
        exerciseLayouts.forEach { pair ->
            pair.first.setOnClickListener {
                addExerciseToRoutine(pair.second, routineName)
            }
        }

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

    private fun filterExercises(query: String) {
        val lowerCaseQuery = query.toLowerCase()
        exerciseLayouts.forEach { pair ->
            if (pair.second.toLowerCase().contains(lowerCaseQuery)) {
                pair.first.visibility = View.VISIBLE
            } else {
                pair.first.visibility = View.GONE
            }
        }
    }

    private fun addExerciseToRoutine(exerciseName: String, routineName: String) {
        Toast.makeText(this, "Añadido $exerciseName a $routineName", Toast.LENGTH_SHORT).show()

        val returnIntent = Intent()
        returnIntent.putExtra("EXERCISE_NAME", exerciseName)
        returnIntent.putExtra("ROUTINE_NAME", routineName)
        setResult(RESULT_OK, returnIntent)
        finish()
    }
}