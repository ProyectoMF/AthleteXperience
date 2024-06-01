package com.example.athletexperience

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

data class Set(var number: Int, var weight: Double, var reps: Int, var isSelected: Boolean = false)

class RepsActivity : AppCompatActivity() {
    private lateinit var lyBackMainActivity: LinearLayout
    private lateinit var btnDecreaseWeight: ImageButton
    private lateinit var btnIncreaseWeight: ImageButton
    private lateinit var btnDecreaseReps: ImageButton
    private lateinit var btnIncreaseReps: ImageButton
    private lateinit var etWeight: EditText
    private lateinit var etReps: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var recyclerViewSets: RecyclerView
    private lateinit var setsAdapter: SetsAdapter

    private var weight: Double = 2.5
    private var reps: Int = 1
    private val sets = mutableListOf<Set>()
    private var selectedSet: Set? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reps)

        initComponents()
        initListeners()
    }

    private fun initComponents() {
        lyBackMainActivity = findViewById(R.id.ly_back_mainActivity)
        btnDecreaseWeight = findViewById(R.id.btnDecreaseWeight)
        btnIncreaseWeight = findViewById(R.id.btnIncreaseWeight)
        btnDecreaseReps = findViewById(R.id.btnDecreaseReps)
        btnIncreaseReps = findViewById(R.id.btnIncreaseReps)
        etWeight = findViewById(R.id.etWeight)
        etReps = findViewById(R.id.etReps)
        btnSave = findViewById(R.id.btn_guardar)
        btnDelete = findViewById(R.id.btn_borrar)
        recyclerViewSets = findViewById(R.id.recyclerViewSets)

        recyclerViewSets.layoutManager = LinearLayoutManager(this)
        setsAdapter = SetsAdapter(sets, ::onSetClicked)
        recyclerViewSets.adapter = setsAdapter

        etWeight.setText(String.format(Locale.US, "%.1f", weight))
        etReps.setText(reps.toString())
    }

    private fun initListeners() {
        lyBackMainActivity.setOnClickListener {
            finish()
        }

        btnDecreaseWeight.setOnClickListener {
            if (weight > 0) {
                weight -= 2.5
                if (weight < 0) weight = 0.0
                etWeight.setText(String.format(Locale.US, "%.1f", weight))
            }
        }

        btnIncreaseWeight.setOnClickListener {
            weight += 2.5
            etWeight.setText(String.format(Locale.US, "%.1f", weight))
        }

        btnDecreaseReps.setOnClickListener {
            if (reps > 1) {
                reps -= 1
                etReps.setText(reps.toString())
            }
        }

        btnIncreaseReps.setOnClickListener {
            reps += 1
            etReps.setText(reps.toString())
        }

        btnSave.setOnClickListener {
            weight = etWeight.text.toString().replace(",", ".").toDoubleOrNull() ?: weight
            reps = etReps.text.toString().toIntOrNull() ?: reps
            if (selectedSet != null) {
                updateSet(selectedSet!!)
            } else {
                addSet()
            }
            clearSelection()
        }

        btnDelete.setOnClickListener {
            deleteSelectedSet()
            clearSelection()
        }
    }

    private fun onSetClicked(set: Set) {
        if (set == selectedSet) {
            set.isSelected = false
            selectedSet = null
        } else {
            selectedSet?.isSelected = false
            set.isSelected = true
            selectedSet = set
            weight = set.weight
            reps = set.reps
            etWeight.setText(String.format(Locale.US, "%.1f", weight))
            etReps.setText(reps.toString())
        }
        setsAdapter.notifyDataSetChanged()
    }

    private fun addSet() {
        val setNumber = sets.size + 1
        val newSet = Set(setNumber, weight, reps)
        sets.add(newSet)
        setsAdapter.notifyItemInserted(sets.size - 1)
    }

    private fun updateSet(set: Set) {
        set.weight = weight
        set.reps = reps
        setsAdapter.notifyDataSetChanged()
        selectedSet = null
    }

    private fun deleteSelectedSet() {
        selectedSet?.let {
            sets.remove(it)
            sets.forEachIndexed { index, set -> set.number = index + 1 }
            setsAdapter.notifyDataSetChanged()
            selectedSet = null
        }
    }

    private fun clearSelection() {
        selectedSet?.isSelected = false
        selectedSet = null
        sets.forEach { it.isSelected = false }
        setsAdapter.notifyDataSetChanged()
    }
}
