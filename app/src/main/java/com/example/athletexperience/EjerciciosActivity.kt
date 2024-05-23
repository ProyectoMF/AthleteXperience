package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
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
            Pair(findViewById(R.id.exercise_6), "Triceps en Polea")
            // Agrega más ejercicios aquí si es necesario
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
