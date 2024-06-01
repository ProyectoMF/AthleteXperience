package com.example.athletexperience

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Set(var number: Int, var weight: Double, var reps: Int, var isSelected: Boolean = false)

class RepsActivity : AppCompatActivity() {
    private lateinit var lyBackMainActivity: LinearLayout
    private lateinit var btnDecreaseWeight: ImageButton
    private lateinit var btnIncreaseWeight: ImageButton
    private lateinit var btnDecreaseReps: ImageButton
    private lateinit var btnIncreaseReps: ImageButton
    private lateinit var tvWeight: TextView
    private lateinit var tvReps: TextView
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button
    private lateinit var recyclerViewSets: RecyclerView
    private lateinit var setsAdapter: SetsAdapter

    private var weight: Double = 2.5
    private var reps: Int = 1
    private val sets = mutableListOf<Set>()

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
        tvWeight = findViewById(R.id.tvWeight)
        tvReps = findViewById(R.id.tvReps)
        btnSave = findViewById(R.id.btn_guardar)
        btnDelete = findViewById(R.id.btn_borrar)
        recyclerViewSets = findViewById(R.id.recyclerViewSets)

        recyclerViewSets.layoutManager = LinearLayoutManager(this)
        setsAdapter = SetsAdapter(sets)
        recyclerViewSets.adapter = setsAdapter

        tvWeight.text = String.format("%.1f", weight)
        tvReps.text = reps.toString()
    }

    private fun initListeners() {
        lyBackMainActivity.setOnClickListener {
            finish()
        }

        btnDecreaseWeight.setOnClickListener {
            if (weight > 0) {
                weight -= 2.5
                if (weight < 0) weight = 0.0
                tvWeight.text = String.format("%.1f", weight)
            }
        }

        btnIncreaseWeight.setOnClickListener {
            weight += 2.5
            tvWeight.text = String.format("%.1f", weight)
        }

        btnDecreaseReps.setOnClickListener {
            if (reps > 1) {
                reps -= 1
                tvReps.text = reps.toString()
            }
        }

        btnIncreaseReps.setOnClickListener {
            reps += 1
            tvReps.text = reps.toString()
        }

        btnSave.setOnClickListener {
            addSet()
        }

        btnDelete.setOnClickListener {
            deleteSelectedSets()
        }
    }

    private fun addSet() {
        val setNumber = sets.size + 1
        val newSet = Set(setNumber, weight, reps)
        sets.add(newSet)
        setsAdapter.notifyItemInserted(sets.size - 1)
    }

    private fun deleteSelectedSets() {
        val setsToRemove = sets.filter { it.isSelected }
        sets.removeAll(setsToRemove)
        sets.forEachIndexed { index, set -> set.number = index + 1 } // Update set numbers
        setsAdapter.notifyDataSetChanged()
    }
}
