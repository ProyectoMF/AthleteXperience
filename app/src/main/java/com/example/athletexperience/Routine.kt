package com.example.athletexperience

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.athletexperience.databinding.ItemRoutineBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class Routine(var name: String = "", val exercises: MutableList<Exercise> = mutableListOf(), val image: Int)

class RoutineAdapter(private val routines: MutableList<Routine>,
                     private val onRoutineClick: (Routine) -> Unit,
                     private val onRoutineUpdate: (Routine) -> Unit,
                     private val onRoutineDelete: (Routine) -> Unit,
                     private val onExerciseDelete: (Routine, Exercise) -> Unit) :
    RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    class RoutineViewHolder(val binding: ItemRoutineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val binding = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RoutineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]
        holder.binding.tvRoutineName.text = routine.name

        holder.binding.lyExercisesContainer.removeAllViews() // Elimina las vistas previas

        routine.exercises.forEach { exercise ->
            val exerciseView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.item_exercise, holder.binding.lyExercisesContainer, false)
            val exerciseNameTextView = exerciseView.findViewById<TextView>(R.id.tvExercises)
            val ly_ejercicios_to_reps = exerciseView.findViewById<LinearLayout>(R.id.ly_ejercicios_to_reps)

            exerciseNameTextView.text = exercise.name

            // Configurar el listener de click largo para eliminar el ejercicio
            exerciseView.setOnLongClickListener {
                showDeleteExerciseDialog(holder.itemView, routine, exercise)
                true
            }

            // Configurar el listener de click para navegar a RepsActivity
            ly_ejercicios_to_reps.setOnClickListener() {
                val context = holder.itemView.context
                val intent = Intent(context, RepsActivity::class.java)
                context.startActivity(intent)
            }


            holder.binding.lyExercisesContainer.addView(exerciseView)
        }

        holder.binding.btnAddExercise.setOnClickListener { onRoutineClick(routine) }

        // Configurar el listener de click para editar el nombre de la rutina
        holder.binding.tvRoutineName.setOnClickListener {
            showEditRoutineDialog(holder.itemView, routine)
        }

        // Configurar el listener de click largo para eliminar la rutina
        holder.binding.clRutina.setOnLongClickListener {
            showDeleteRoutineDialog(holder.itemView, routine)
            true
        }
    }

    override fun getItemCount(): Int = routines.size

    fun addRoutine(routine: Routine): Int {
        routines.add(routine)
        notifyItemInserted(routines.size - 1)
        return routines.size - 1
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

    private fun showEditRoutineDialog(view: View, routine: Routine) {
        val builder = MaterialAlertDialogBuilder(view.context)
        val input = EditText(view.context)
        input.setText(routine.name)
        builder.setTitle("Editar Nombre de Rutina")
        builder.setView(input)
        builder.setPositiveButton("Guardar") { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                routine.name = newName
                onRoutineUpdate(routine)
                notifyDataSetChanged()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun showDeleteExerciseDialog(view: View, routine: Routine, exercise: Exercise) {
        val builder = MaterialAlertDialogBuilder(view.context)
        builder.setTitle("Eliminar Ejercicio")
        builder.setMessage("¿Seguro que deseas eliminar este ejercicio?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            routine.exercises.remove(exercise)
            onExerciseDelete(routine, exercise)
            notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun showDeleteRoutineDialog(view: View, routine: Routine) {
        val builder = MaterialAlertDialogBuilder(view.context)
        builder.setTitle("Eliminar Rutina")
        builder.setMessage("¿Seguro que deseas eliminar esta rutina?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            onRoutineDelete(routine)
            routines.remove(routine)
            notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }
}
