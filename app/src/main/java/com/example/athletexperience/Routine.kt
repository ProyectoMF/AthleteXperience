package com.example.athletexperience

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.athletexperience.databinding.ItemRoutineBinding

data class Routine(val name: String = "", val exercises: MutableList<Exercise> = mutableListOf())

class RoutineAdapter(private val routines: MutableList<Routine>, private val onRoutineClick: (Routine) -> Unit) :
    RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    class RoutineViewHolder(val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val binding = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]
        holder.binding.tvRoutineName.text = routine.name
        holder.binding.tvExercises.text = routine.exercises.joinToString("\n") { it.name }
        holder.binding.btnAddExercise.setOnClickListener { onRoutineClick(routine) }
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

    fun updateRoutines(newRoutines: List<Routine>) {
        routines.clear()
        routines.addAll(newRoutines)
        notifyDataSetChanged()
    }
}
