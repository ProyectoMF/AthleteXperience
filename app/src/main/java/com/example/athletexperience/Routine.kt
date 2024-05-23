package com.example.athletexperience

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

data class Routine(val name: String, val exercises: MutableList<Exercise> = mutableListOf())

class RoutineAdapter(
    private val routines: MutableList<Routine>,
    private val onAddExerciseClick: (Routine) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    class RoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val routineName: TextView = itemView.findViewById(R.id.tvRoutineName)
        val exercises: TextView = itemView.findViewById(R.id.tvExercises)
        val btnAddExercise: FloatingActionButton = itemView.findViewById(R.id.btnAddExercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]
        holder.routineName.text = routine.name
        holder.exercises.text = routine.exercises.joinToString("\n") { it.name }
        holder.btnAddExercise.setOnClickListener {
            onAddExerciseClick(routine)
        }
    }

    override fun getItemCount(): Int = routines.size

    fun addRoutine(routine: Routine) {
        routines.add(routine)
        notifyItemInserted(routines.size - 1)
    }

    fun addExerciseToRoutine(routineName: String, exercise: Exercise) {
        val routine = routines.find { it.name == routineName }
        routine?.exercises?.add(exercise)
        notifyDataSetChanged()
    }
}