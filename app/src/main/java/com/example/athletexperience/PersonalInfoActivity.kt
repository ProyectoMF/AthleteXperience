package com.example.athletexperience

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.ChipGroup
import java.util.Calendar

class PersonalInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        // Set up the ChipGroup for gender
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group)
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chip_hombre -> {
                    // Handle the user selecting "Hombre"
                }
                R.id.chip_mujer -> {
                    // Handle the user selecting "Mujer"
                }
            }
        }

        // Set up the EditText for date of birth
        val dateEditText = findViewById<EditText>(R.id.editTextDateFechaNacimiento)
        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                    dateEditText.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Set up the NumberPickers for height and weight
        val heightPicker = findViewById<NumberPicker>(R.id.heightPicker)
        val weightPicker = findViewById<NumberPicker>(R.id.weightPicker)
        val heightEditText = findViewById<EditText>(R.id.heightEditText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)

        // Set the text color of the NumberPickers to white
        val textPaintComponents = heightPicker.dividerPaint.textSize
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = textPaintComponents
        heightPicker.dividerPaint = textPaint
        weightPicker.dividerPaint = textPaint

        // Set the format for the Weight NumberPicker to include a decimal point
        weightPicker.format = "%.1f"

        // Set up the labels for the NumberPickers
        val heightLabel = findViewById<TextView>(R.id.heightLabel)
        val weightLabel = findViewById<TextView>(R.id.weightLabel)

        heightLabel.setOnClickListener {
            heightPicker.visibility = View.VISIBLE
            weightPicker.visibility = View.GONE
        }

        weightLabel.setOnClickListener {
            heightPicker.visibility = View.GONE
            weightPicker.visibility = View.VISIBLE
        }

        // Set up the ValueChangeListeners for the NumberPickers
        heightPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            heightEditText.setText(newVal.toString())
        }

        weightPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            weightEditText.setText(newVal.toString())
        }
    }
}