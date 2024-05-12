package com.example.athletexperience


import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberUtils.formatNumber
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var bt_back_personal_info : FloatingActionButton
    private lateinit var bt_next_personal_info : FloatingActionButton
    private lateinit var cv_hombre : CardView
    private lateinit var cv_mujer : CardView
    private lateinit var et_fechaNacimiento : EditText
    private lateinit var numberPickerDia : NumberPicker
    private lateinit var numberPickerMes : NumberPicker
    private lateinit var numberPickerAnio : NumberPicker
    private lateinit var et_altura : EditText
    private lateinit var numberPickerAltura : NumberPicker
    private lateinit var et_peso : EditText
    private lateinit var numberPickerPeso : NumberPicker
    private lateinit var numberPickerPesoDecimal : NumberPicker
    private lateinit var buttonAceptarFechaNacimiento : Button
    private lateinit var buttonCancelarFechaNacimiento : Button
    private lateinit var buttonAceptarAltura : Button
    private lateinit var buttonCancelarAltura : Button
    private lateinit var buttonAceptarPeso : Button
    private lateinit var buttonCancelarPeso : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        initComponent()
        initListeners()
        initUI()
    }

    private fun initComponent() {
        //Variable para el boton de volver y siguiente
        bt_back_personal_info = findViewById(R.id.bt_back_personal_info)
        bt_next_personal_info = findViewById(R.id.bt_next_personal_info)


        // Obtener las referencias de las cards de hombre y mujer
        cv_hombre = findViewById(R.id.cv_hombre)
        cv_mujer = findViewById(R.id.cv_mujer)

        //EditText y NumberPickers para la fecha
        et_fechaNacimiento = findViewById(R.id.et_fechaNacimiento)
        numberPickerDia = findViewById(R.id.numberPickerDia)
        numberPickerMes = findViewById(R.id.numberPickerMes)
        numberPickerAnio = findViewById(R.id.numberPickerAnio)

        //EditText y NumberPickers para la altura
        et_altura = findViewById(R.id.et_altura)
        numberPickerAltura = findViewById(R.id.numberPickerAltura)

        //EditText y NumberPickers para el peso
        et_peso = findViewById(R.id.et_peso)
        numberPickerPeso = findViewById(R.id.numberPickerPeso)
        numberPickerPesoDecimal = findViewById(R.id.numberPickerPesoDecimal)

        // inicialización de botón aceptar y cancelar para la fecha de nacimiento
        buttonAceptarFechaNacimiento = findViewById(R.id.buttonAceptarFechaNacimiento)
        buttonCancelarFechaNacimiento = findViewById(R.id.buttonCancelarFechaNacimiento)

        // inicialización de botón aceptar y cancelar para la altura
        buttonAceptarAltura = findViewById(R.id.buttonAceptarAltura)
        buttonCancelarAltura = findViewById(R.id.buttonCancelarAltura)

        // inicialización de botón aceptar y cancelar para el peso
        buttonAceptarPeso = findViewById(R.id.buttonAceptarPeso)
        buttonCancelarPeso = findViewById(R.id.buttonCancelarPeso)
    }

    private fun initListeners() {
        // Agregar OnClickListener al boton de volver
        bt_back_personal_info.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, PersonalObjetivoActivity::class.java)
            startActivity(intent)
        }

        // Agregar OnClickListener al boton de siguiente
        bt_next_personal_info.setOnClickListener {
            //Obtiene el género,peso altura, fecha seleccionada
            val selectedCardView = findViewById<CardView>(R.id.cv_hombre).isSelected
            var genero = findViewById<CardView>(R.id.cv_hombre).tag as String
            if (!selectedCardView) {
                genero = findViewById<CardView>(R.id.cv_mujer).tag as String
            }
            val fechaNacimiento = et_fechaNacimiento.text.toString()
            val peso = et_peso.text.toString()
            val altura = et_altura.text.toString()


            // Create an intent to start PersonalResumenActivity
            val intent = Intent(this, PersonalResumenActivity::class.java)

            // Put the data as extras
            intent.putExtra("FECHA_NACIMIENTO", fechaNacimiento)
            intent.putExtra("GENERO", genero)
            intent.putExtra("PESO", peso.toFloat())
            intent.putExtra("ALTURA", altura.toFloat())

            startActivity(intent)
        }

        // Agregar el listener de clic para la card de hombre
        cv_hombre.setOnClickListener {
            // Cambiar el color de fondo de la card de hombre a naranja
            cv_hombre.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orange))

            // Cambiar el color de fondo de la card de mujer a orangeDark
            cv_hombre.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orangeDark))
        }

        // Agregar el listener de clic para la card de mujer
        cv_mujer.setOnClickListener {
            // Cambiar el color de fondo de la card de mujer a naranja
            cv_mujer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orange))

            // Cambiar el color de fondo de la card de hombre a orangeDark
            cv_mujer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.orangeDark))
        }

        //Evento para que muestre el NumberPicker
        et_fechaNacimiento.setOnClickListener {
            // Ocultar todos los NumberPicker antes de mostrar el de fecha de nacimiento
            hideAllNumberPickers()
            // Mostrar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.LayoutNumberPickerFecha).visibility = View.VISIBLE
        }


        // Manejar el clic en el botón de aceptar para la fecha de nacimiento
        buttonAceptarFechaNacimiento.setOnClickListener {

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

        // Manejar el clic en el botón de cancelar para la fecha de nacimiento
        buttonCancelarFechaNacimiento.setOnClickListener {
            findViewById<LinearLayout>(R.id.LayoutNumberPickerFecha).visibility = View.GONE
        }


        //Evento para que muestre el NumberPicker
        et_altura.setOnClickListener {
            // Ocultar todos los NumberPicker antes de mostrar el de fecha de nacimiento
            hideAllNumberPickers()
            // Mostrar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.VISIBLE
        }

        // Manejar el clic en el botón de aceptar para la altura
        buttonAceptarAltura.setOnClickListener {

            val alturaSeleccionada = numberPickerAltura.value

            // Mostrar la altura seleccionada en el EditText
            et_altura.setText(alturaSeleccionada.toString() + " cm")

            // Ocultar el layout con los NumberPicker para la altura
            findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.GONE
        }

        // Manejar el clic en el botón de cancelar para la altura
        buttonCancelarAltura.setOnClickListener {
            findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.GONE
        }


        //Evento para mostrar el NumberPicker del peso
        et_peso.setOnClickListener {
            // Ocultar todos los NumberPicker antes de mostrar el de fecha de nacimiento
            hideAllNumberPickers()
            // Mostrar el layout con los NumberPicker
            findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.VISIBLE
        }

        // Manejar el clic en el botón de aceptar para el peso
        buttonAceptarPeso.setOnClickListener {
            val pesoSeleccionado = numberPickerPeso.value
            val pesoDecimalSeleccionado = numberPickerPesoDecimal.value

            val pesoTotalSeleccionado = "$pesoSeleccionado.$pesoDecimalSeleccionado"

            // Mostrar el peso seleccionado en el EditText
            et_peso.setText("$pesoTotalSeleccionado Kg")

            // Ocultar el layout con los NumberPicker para el peso
            findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.GONE
        }

        // Manejar el clic en el botón de cancelar para el peso
        buttonCancelarPeso.setOnClickListener {
            findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.GONE
        }
    }

    private fun initUI() {
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

        //Ocultar el teclado para el EditText de la fecha
        et_fechaNacimiento.isFocusable = false


        //Maximo y minimo de altura para la altura
        numberPickerAltura.minValue = 60
        numberPickerAltura.maxValue = 260

        // Establecer el valor por defecto de la altura en 170
        numberPickerAltura.value = 170

        //Ocultar el teclado para el EditText
        et_altura.isFocusable = false



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
    }


    private fun hideAllNumberPickers() {
        findViewById<LinearLayout>(R.id.LayoutNumberPickerFecha).visibility = View.GONE
        findViewById<LinearLayout>(R.id.LayoutNumberPickerAltura).visibility = View.GONE
        findViewById<LinearLayout>(R.id.LayoutNumberPickerPeso).visibility = View.GONE
    }
}