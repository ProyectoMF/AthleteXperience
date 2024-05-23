package com.example.athletexperience

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EjerciciosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ejercicios)

        val routineName = intent.getStringExtra("ROUTINE_NAME") ?: ""

        findViewById<LinearLayout>(R.id.exercise_1).setOnClickListener {
            addExerciseToRoutine("Press de Banca", routineName)
        }

        findViewById<LinearLayout>(R.id.exercise_2).setOnClickListener {
            addExerciseToRoutine("Press de Banca Inclinado", routineName)
        }

        findViewById<LinearLayout>(R.id.exercise_3).setOnClickListener {
            addExerciseToRoutine("Press de Banca Declinado", routineName)
        }

        // Repetir para cada ejercicio...
        findViewById<LinearLayout>(R.id.exercise_4).setOnClickListener {
            addExerciseToRoutine("Aperturas en máquina", routineName)
        }

        findViewById<LinearLayout>(R.id.exercise_5).setOnClickListener {
            addExerciseToRoutine("Fondos", routineName)
        }

        findViewById<LinearLayout>(R.id.exercise_6).setOnClickListener {
            addExerciseToRoutine("Triceps en Polea", routineName)
        }
    }

    private fun addExerciseToRoutine(exerciseName: String, routineName: String) {
        // Aquí puedes añadir la lógica para agregar el ejercicio a la rutina seleccionada
        Toast.makeText(this, "Añadido $exerciseName a $routineName", Toast.LENGTH_SHORT).show()
    }
}
