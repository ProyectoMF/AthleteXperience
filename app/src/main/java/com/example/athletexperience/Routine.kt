package com.example.athletexperience

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.athletexperience.databinding.ItemRoutineBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class Routine(var name: String = "", val exercises: MutableList<Exercise> = mutableListOf())

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
        holder.binding.tvExercises.text = routine.exercises.joinToString("\n") { it.name }
        holder.binding.btnAddExercise.setOnClickListener { onRoutineClick(routine) }

        // Mantener en el nombre de la rutina para cambiar el nombre
        holder.binding.tvRoutineName.setOnLongClickListener {
            showEditRoutineDialog(holder.itemView, routine)
            true
        }

        // Mantener en el ejercicio para borrarlo
        holder.binding.tvExercises.setOnLongClickListener {
            showDeleteExerciseDialog(holder.itemView, routine)
            true
        }

        // Mantener para borrar la rutina
        holder.itemView.setOnLongClickListener {
            showDeleteRoutineDialog(holder.itemView, routine)
            true
        }

        // Ajustar tamaño
        holder.itemView.viewTreeObserver.addOnGlobalLayoutListener {
            adjustViewPagerHeight(holder.itemView)
        }
    }

    override fun getItemCount(): Int = routines.size

    private fun adjustViewPagerHeight(view: View) {
        val parentView = view.parent
        if (parentView is ViewGroup) {
            val viewPager = parentView.parent
            if (viewPager is ViewPager2) {
                val currentHeight = view.height
                val layoutParams = viewPager.layoutParams
                if (currentHeight > layoutParams.height) {
                    layoutParams.height = currentHeight
                    viewPager.layoutParams = layoutParams
                }
            }
        }
    }

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

    private fun showDeleteExerciseDialog(view: View, routine: Routine) {
        val exercises = routine.exercises.map { it.name }.toTypedArray()
        val builder = MaterialAlertDialogBuilder(view.context)
        builder.setTitle("Eliminar Ejercicio")
        builder.setItems(exercises) { _, which ->
            val exercise = routine.exercises[which]
            routine.exercises.removeAt(which)
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
