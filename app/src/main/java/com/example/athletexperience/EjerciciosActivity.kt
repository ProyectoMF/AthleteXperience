package com.example.athletexperience

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream

class EjerciciosActivity : AppCompatActivity() {

    private lateinit var exerciseAdapter: ExerciseAdapter
    private lateinit var exerciseList: List<Exercise>
    private lateinit var routineName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ejercicios)

        routineName = intent.getStringExtra("ROUTINE_NAME") ?: ""

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        exerciseList = loadExercisesFromJson()

        exerciseAdapter = ExerciseAdapter(exerciseList) { exercise ->
            Toast.makeText(this, "Añadido ${exercise.name} a $routineName", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = exerciseAdapter
    }

    private fun loadExercisesFromJson(): List<Exercise> {
        val jsonFileString = getJsonDataFromAsset("gymdatabase.json")
        val gson = Gson()
        val exerciseResponseType = object : TypeToken<ExerciseResponse>() {}.type
        val exerciseResponse: ExerciseResponse = gson.fromJson(jsonFileString, exerciseResponseType)
        return exerciseResponse.item_array
    }

    private fun getJsonDataFromAsset(fileName: String): String? {
        val jsonString: String
        try {
            val inputStream: InputStream = assets.open(fileName)
            jsonString = inputStream.bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}
