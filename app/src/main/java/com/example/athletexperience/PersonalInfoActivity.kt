package com.example.athletexperience

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.ChipGroup
import com.google.android.material.internal.ViewUtils.hideKeyboard
import java.util.Calendar

class PersonalInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)


        val editTextFechaNacimiento = findViewById<EditText>(R.id.editTextDateFechaNacimiento)
        val numberPickerDia = findViewById<NumberPicker>(R.id.numberPickerDia)
        val numberPickerMes = findViewById<NumberPicker>(R.id.numberPickerMes)
        val numberPickerAnio = findViewById<NumberPicker>(R.id.numberPickerAnio)

        // Configurar NumberPicker para el día, el mes y el año
        // Configurar NumberPicker para el día
        numberPickerDia.minValue = 1
        numberPickerDia.maxValue = 31

        // Configurar NumberPicker para el mes
        numberPickerMes.minValue = 1
        numberPickerMes.maxValue = 12
        val meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        numberPickerMes.displayedValues = meses

        // Configurar NumberPicker para el año
        val anioActual = 2024 // Puedes obtener el año actual de forma dinámica si lo necesitas
        numberPickerAnio.minValue = 1900
        numberPickerAnio.maxValue = anioActual

        editTextFechaNacimiento.setOnClickListener {
            // Mostrar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.numberPickerLayout).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.buttonAceptarFechaNacimiento).setOnClickListener {
            // Manejar el clic en el botón de aceptar para la fecha de nacimiento
            val diaSeleccionado = numberPickerDia.value
            val mesSeleccionado = numberPickerMes.value
            val anioSeleccionado = numberPickerAnio.value

            // Construir la fecha seleccionada en el formato deseado
            val fechaSeleccionada = "$diaSeleccionado/$mesSeleccionado/$anioSeleccionado"

            // Mostrar la fecha seleccionada en el EditText
            editTextFechaNacimiento.setText(fechaSeleccionada)

            // Ocultar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.numberPickerLayout).visibility = View.GONE
        }

        findViewById<Button>(R.id.buttonCancelarFechaNacimiento).setOnClickListener {
            // Manejar el clic en el botón de cancelar para la fecha de nacimiento
            findViewById<LinearLayout>(R.id.numberPickerLayout).visibility = View.GONE
        }




        // Configuración de los EditText y NumberPickers para la altura y el peso

        val editTextAltura = findViewById<EditText>(R.id.editTextNumberSignedAltura)
        val editTextPeso = findViewById<EditText>(R.id.editTextNumberDecimalPeso)


        editTextAltura.setOnClickListener {
            // Mostrar el layout con los NumberPicker para la altura
            findViewById<LinearLayout>(R.id.numberPickerAlturaLayout).visibility = View.VISIBLE
            hideKeyboard()
        }

        val numberPickerAltura = findViewById<NumberPicker>(R.id.numberPickerAltura)
        numberPickerAltura.minValue = 60
        numberPickerAltura.maxValue = 260

        findViewById<Button>(R.id.buttonAceptarAltura).setOnClickListener {
            // Manejar el clic en el botón de aceptar para la altura
            val alturaSeleccionada = numberPickerAltura.value

            // Mostrar la altura seleccionada en el EditText
            editTextAltura.setText(alturaSeleccionada.toString() + " cm")

            // Ocultar el layout con los NumberPicker para la altura
            findViewById<LinearLayout>(R.id.numberPickerAlturaLayout).visibility = View.GONE
        }

        findViewById<Button>(R.id.buttonCancelarAltura).setOnClickListener {
            // Manejar el clic en el botón de cancelar para la altura
            findViewById<LinearLayout>(R.id.numberPickerAlturaLayout).visibility = View.GONE
        }

        editTextPeso.setOnClickListener {
            // Mostrar el layout con los NumberPicker para el peso
            findViewById<LinearLayout>(R.id.numberPickerPesoLayout).visibility = View.VISIBLE
            hideKeyboard()
        }


        val numberPickerPeso = findViewById<NumberPicker>(R.id.numberPickerPeso)
        numberPickerPeso.minValue = 30
        numberPickerPeso.maxValue = 200
        numberPickerPeso.setFormatter { "%.1f".format(it.toFloat()) }

        findViewById<Button>(R.id.buttonAceptarPeso).setOnClickListener {
            // Manejar el clic en el botón de aceptar para el peso
            val pesoSeleccionado = numberPickerPeso.value

            // Mostrar el peso seleccionado en el EditText
            editTextPeso.setText(pesoSeleccionado.toString() + " Kg")

            // Ocultar el layout con los NumberPicker para el peso
            findViewById<LinearLayout>(R.id.numberPickerPesoLayout).visibility = View.GONE
        }

        findViewById<Button>(R.id.buttonCancelarPeso).setOnClickListener {
            // Manejar el clic en el botón de cancelar para el peso
            findViewById<LinearLayout>(R.id.numberPickerPesoLayout).visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}