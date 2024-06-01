package com.example.athletexperience

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SetsAdapter(private val sets: MutableList<Set>) : RecyclerView.Adapter<SetsAdapter.SetViewHolder>() {

    class SetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSetNumber: TextView = view.findViewById(R.id.tvSetNumber)
        val tvWeight: TextView = view.findViewById(R.id.tvWeight)
        val tvReps: TextView = view.findViewById(R.id.tvReps)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val chkDelete: CheckBox = view.findViewById(R.id.chkDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_set, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val set = sets[position]
        holder.tvSetNumber.text = "Series ${set.number}"
        holder.tvWeight.text = "${set.weight} kg"
        holder.tvReps.text = "${set.reps} reps"

        holder.btnEdit.setOnClickListener {
            // Handle edit functionality here
        }

        holder.chkDelete.setOnCheckedChangeListener { _, isChecked ->
            set.isSelected = isChecked
        }
    }

    override fun getItemCount(): Int = sets.size
}
