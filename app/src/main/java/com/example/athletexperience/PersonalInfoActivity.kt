package com.example.athletexperience

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.athletexperience.loggin.SingInActivity
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.internal.ViewUtils.hideKeyboard
import java.util.Calendar

class PersonalInfoActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        //Variable para el boton de volver
        val bt_back_personal_info = findViewById<FloatingActionButton>(R.id.bt_back_personal_info)

        // Agregar OnClickListener al boton de bolver
        bt_back_personal_info.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, PersonalObjetivoActivity::class.java)
            startActivity(intent)
        }

        // Obtener las referencias de las cards de hombre y mujer
        val cardHombre = findViewById<CardView>(R.id.bt_hombre)
        val cardMujer = findViewById<CardView>(R.id.bt_mujer)

        // Agregar el listener de clic para la card de hombre
        cardHombre.setOnClickListener {
            // Cambiar el color de fondo de la card de hombre a naranja
            cardHombre.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orange))

            // Cambiar el color de fondo de la card de mujer a orangeDark
            cardMujer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orangeDark))
        }

        // Agregar el listener de clic para la card de mujer
        cardMujer.setOnClickListener {
            // Cambiar el color de fondo de la card de mujer a naranja
            cardMujer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orange))

            // Cambiar el color de fondo de la card de hombre a orangeDark
            cardHombre.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orangeDark))
        }

        // Configuración de los EditText y NumberPickers para la fecha
        val et_fechaNacimiento = findViewById<EditText>(R.id.et_fechaNacimiento)
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
        val meses = arrayOf(
            "Enero",
            "Febrero",
            "Marzo",
            "Abril",
            "Mayo",
            "Junio",
            "Julio",
            "Agosto",
            "Septiembre",
            "Octubre",
            "Noviembre",
            "Diciembre"
        )
        numberPickerMes.displayedValues = meses

        // Obtener el año actual de forma dinámica
        val calendar = Calendar.getInstance()
        val anioActual = calendar.get(Calendar.YEAR)

        numberPickerAnio.minValue = 1924
        numberPickerAnio.maxValue = anioActual

        // Establecer el valor por defecto del año en el 2000
        numberPickerAnio.value = 2000


        //Ocultar el teclado para el EditText
        et_fechaNacimiento.isFocusable = false

        //Evento para que muestre el NumberPicker
        et_fechaNacimiento.setOnClickListener {
            // Ocultar todos los NumberPicker antes de mostrar el de fecha de nacimiento
            hideAllNumberPickers()
            // Mostrar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.LayoutNumberPickerFecha).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.buttonAceptarFechaNacimiento).setOnClickListener {
            // Manejar el clic en el botón de aceptar para la fecha de nacimiento
            val diaSeleccionado = numberPickerDia.value
            val mesSeleccionado = numberPickerMes.value
            val anioSeleccionado = numberPickerAnio.value

            // Construir la fecha seleccionada en el formato deseado
            val fechaSeleccionada = "$diaSeleccionado/$mesSeleccionado/$anioSeleccionado"

            // Mostrar la fecha seleccionada en el EditText
            et_fechaNacimiento.setText(fechaSeleccionada)

            // Ocultar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.LayoutNumberPickerFecha).visibility = View.GONE
        }

        findViewById<Button>(R.id.buttonCancelarFechaNacimiento).setOnClickListener {
            // Manejar el clic en el botón de cancelar para la fecha de nacimiento
            findViewById<LinearLayout>(R.id.LayoutNumberPickerFecha).visibility = View.GONE
        }


        // Configuración de los EditText y NumberPickers para la altura
        val et_altura = findViewById<EditText>(R.id.et_altura)
        val numberPickerAltura = findViewById<NumberPicker>(R.id.numberPickerAltura)

        //Maximo y minimo de altura
        numberPickerAltura.minValue = 60
        numberPickerAltura.maxValue = 260

        // Establecer el valor por defecto de la altura en 170
        numberPickerAltura.value = 170

        //Ocultar el teclado para el EditText
        et_altura.isFocusable = false

        //Evento para que muestre el NumberPicker
        et_altura.setOnClickListener {
            // Ocultar todos los NumberPicker antes de mostrar el de fecha de nacimiento
            hideAllNumberPickers()
            // Mostrar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.buttonAceptarAltura).setOnClickListener {
            // Manejar el clic en el botón de aceptar para la altura
            val alturaSeleccionada = numberPickerAltura.value

            // Mostrar la altura seleccionada en el EditText
            et_altura.setText(alturaSeleccionada.toString() + " cm")

            // Ocultar el layout con los NumberPicker para la altura
            findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.GONE
        }

        findViewById<Button>(R.id.buttonCancelarAltura).setOnClickListener {
            // Manejar el clic en el botón de cancelar para la altura
            findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.GONE
        }

        //Configuración de los EditText y NumberPickers para el peso
        val et_peso = findViewById<EditText>(R.id.et_peso)
        val numberPickerPeso = findViewById<NumberPicker>(R.id.numberPickerPeso)
        val numberPickerPesoDecimal = findViewById<NumberPicker>(R.id.numberPickerPesoDecimal)

        // Configurar el mínimo y máximo de NumberPickers del peso
        numberPickerPeso.minValue = 30
        numberPickerPeso.maxValue = 200

        // Establecer el valor por defecto del peso en 70
        numberPickerPeso.value = 70

        // Configurar el rango del NumberPicker del peso decimal
        numberPickerPesoDecimal.minValue = 0
        numberPickerPesoDecimal.maxValue = 9

        //Ocultar el teclado para el EditText
        et_peso.isFocusable = false

        //Evento para mostrar el NumberPicker del peso
        et_peso.setOnClickListener {
            // Ocultar todos los NumberPicker antes de mostrar el de fecha de nacimiento
            hideAllNumberPickers()
            // Mostrar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.buttonAceptarPeso).setOnClickListener {
            // Manejar el clic en el botón de aceptar para el peso
            val pesoSeleccionado = numberPickerPeso.value
            val pesoDecimalSeleccionado = numberPickerPesoDecimal.value

            val pesoTotalSeleccionado = "$pesoSeleccionado.$pesoDecimalSeleccionado"

            // Mostrar el peso seleccionado en el EditText
            et_peso.setText("$pesoTotalSeleccionado Kg")

            // Ocultar el layout con los NumberPicker para el peso
            findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.GONE
        }

        findViewById<Button>(R.id.buttonCancelarPeso).setOnClickListener {
            // Manejar el clic en el botón de cancelar para el peso
            findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.GONE
        }


    }

    private fun hideAllNumberPickers() {
        findViewById<LinearLayout>(R.id.LayoutNumberPickerFecha).visibility = View.GONE
        findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.GONE
        findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.GONE
    }
}