package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.R
import com.example.athletexperience.loggin.SingInActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PersonalResumenActivity : AppCompatActivity(){

    private lateinit var tv_imc : TextView
    private lateinit var tv_calorias : TextView
    private lateinit var tv_macronutrientes : TextView
    private lateinit var bt_back_objetive : FloatingActionButton
    private lateinit var genero: String
    private var peso: Float = 0.0f
    private var altura: Float = 0.0f
    private lateinit var fechaNacimiento: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_resumen)

        initComponent()
        initListeners()
        initUI()
    }
    private fun initComponent() {
        //Boton para volver atras
        bt_back_objetive = findViewById(R.id.bt_back_objetive)

        // Textviews
        tv_imc = findViewById(R.id.tv_imc)
        tv_calorias = findViewById(R.id.tv_calorias)
        tv_macronutrientes = findViewById(R.id.tv_macronutrientes)

    }
    private fun initListeners() {
        // Agregar OnClickListener para volver atras
        bt_back_objetive.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }
    }
   private fun initUI() {

        // Recuperar los datos pasados desde la actividad anterior
        genero = intent.getStringExtra("GENERO") ?: ""
        peso = intent.getFloatExtra("PESO", 0.0f)
        altura = intent.getFloatExtra("ALTURA", 0.0f)
        fechaNacimiento = intent.getStringExtra("FECHA_NACIMIENTO") ?: ""

        // Ahora puedes usar estos datos para calcular el IMC, las calorías necesarias y los macronutrientes
        // y mostrarlos en tu diseño de resumen.
        // Por ejemplo:
        val imc = calcularIMC(peso, altura)
        val calorias = calcularCalorias(genero, peso, altura, fechaNacimiento)
        val macronutrientes = calcularMacronutrientes(calorias)

        // Mostrar los datos en tu diseño de resumen
        mostrarDatosEnResumen(imc, calorias, macronutrientes)
    }

    // Funciones para calcular el IMC, las calorías necesarias y los macronutrientes
    private fun calcularIMC(peso: Float, altura: Float): Float {
        return peso / (altura * altura)
    }

    private fun calcularCalorias(genero: String, peso: Float, altura: Float, fechaNacimiento: String): Int {
        // Calcular la edad a partir de la fecha de nacimiento
        val edad = calcularEdad(fechaNacimiento)

        // Calcular el metabolismo basal (MB) utilizando la fórmula de Harris-Benedict
        val mb: Float = if (genero.equals("Hombre", ignoreCase = true)) {
            88.362f + (13.397f * peso) + (4.799f * altura * 100) - (5.677f * edad)
        } else {
            447.593f + (9.247f * peso) + (3.098f * altura * 100) - (4.330f * edad)
        }

        // Calcular las calorías necesarias basadas en el nivel de actividad física (Nivel de actividad física = 1.55 por defecto)
        val nivelActividadFisica = 1.55f // Puedes ajustar este valor según el nivel de actividad física del usuario
        val calorias = (mb * nivelActividadFisica).toInt()

        return calorias
    }

    private fun calcularEdad(fechaNacimiento: String): Int {
        // Parsear la fecha de nacimiento en un objeto Date
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaNac = sdf.parse(fechaNacimiento)

        // Calcular la diferencia en años entre la fecha de nacimiento y la fecha actual
        val calNacimiento = Calendar.getInstance().apply { time = fechaNac }
        val calActual = Calendar.getInstance()
        var edad = calActual.get(Calendar.YEAR) - calNacimiento.get(Calendar.YEAR)
        if (calActual.get(Calendar.DAY_OF_YEAR) < calNacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--
        }
        return edad
    }
    private fun calcularMacronutrientes(calorias: Int): String {
        // Calcular los macronutrientes basados en el porcentaje de calorías que provienen de cada macronutriente
        val porcentajeProteinas = 0.25f // Porcentaje de calorías que provienen de proteínas
        val porcentajeGrasas = 0.25f // Porcentaje de calorías que provienen de grasas
        val porcentajeCarbohidratos = 0.5f // Porcentaje de calorías que provienen de carbohidratos

        val caloriasProteinas = (calorias * porcentajeProteinas).toInt()
        val caloriasGrasas = (calorias * porcentajeGrasas).toInt()
        val caloriasCarbohidratos = (calorias * porcentajeCarbohidratos).toInt()

        // Convertir las calorías de cada macronutriente a gramos (1 gramo de proteína o carbohidratos = 4 calorías, 1 gramo de grasa = 9 calorías)
        val gramosProteinas = caloriasProteinas / 4
        val gramosGrasas = caloriasGrasas / 9
        val gramosCarbohidratos = caloriasCarbohidratos / 4

        // Construir una cadena para mostrar los macronutrientes
        val macronutrientes = "Proteínas: $gramosProteinas g\nGrasas: $gramosGrasas g\nCarbohidratos: $gramosCarbohidratos g"
        return macronutrientes
    }

    private fun mostrarDatosEnResumen(imc: Float, calorias: Int, macronutrientes: String) {

        // Establecer los datos calculados en los TextViews
        tv_imc.text = String.format("%.2f", imc)
        tv_calorias.text = calorias.toString()
        tv_macronutrientes.text = macronutrientes
    }
}