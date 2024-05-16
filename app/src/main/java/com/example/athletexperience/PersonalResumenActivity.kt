package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.athletexperience.PersonalObjetivoActivity.Companion.actividad
import com.example.athletexperience.PersonalObjetivoActivity.Companion.altura
import com.example.athletexperience.PersonalObjetivoActivity.Companion.fecha
import com.example.athletexperience.PersonalObjetivoActivity.Companion.objetivo
import com.example.athletexperience.PersonalObjetivoActivity.Companion.peso
import com.example.athletexperience.PersonalObjetivoActivity.Companion.sexo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PersonalResumenActivity : AppCompatActivity() {

    private lateinit var tv_imc: TextView
    private lateinit var tv_calorias: TextView
    private lateinit var tv_macronutrientes: TextView
    private lateinit var bt_back_resumen: FloatingActionButton
    private lateinit var bt_crearplan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_resumen)

        initComponent()
        initListeners()
        initUI()

        val edad = calcularEdad(fecha)
        calcularIMC()
        val sexoLowerCase = sexo?.toLowerCase(Locale.getDefault())
        val calorias = calcularCalorias(sexoLowerCase, edad, actividad)

        // Calcular y mostrar las calorías según el objetivo seleccionado
        val caloriasObjetivo = calcularCaloriasObjetivo(calorias, objetivo)
        tv_calorias.text = "Calorías diarias: $caloriasObjetivo"

        // Calcular y mostrar los macronutrientes
        val macronutrientes = calcularMacronutrientes(caloriasObjetivo.toDouble())
        tv_macronutrientes.text = "Macronutrientes diarios:\nProteínas: ${macronutrientes["proteinas"]} g\nCarbohidratos: ${macronutrientes["carbohidratos"]} g\nGrasas: ${macronutrientes["grasas"]} g"
    }

    private fun initComponent() {
        bt_back_resumen = findViewById(R.id.bt_back_resumen)
        bt_crearplan = findViewById(R.id.bt_crearplan)
        tv_imc = findViewById(R.id.tv_imc)
        tv_calorias = findViewById(R.id.tv_calorias)
        tv_macronutrientes = findViewById(R.id.tv_macronutrientes)
    }

    private fun initListeners() {
        bt_back_resumen.setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        bt_crearplan.setOnClickListener {
            val intent = Intent(this, mainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initUI() {}

    private fun calcularIMC() {
        if (altura != 0.0 && peso != 0.0) {
            val imc = peso / ((altura / 100) * (altura / 100))
            val mensajeIMC = when {
                imc <= 18.5 -> "Bajo peso"
                imc <= 24.9 -> "Normal"
                imc <= 29.9 -> "Sobrepeso"
                else -> "Obeso"
            }
            val mensajeConSaltoDeLinea = "\n" + mensajeIMC
            tv_imc.text = String.format(
                Locale.getDefault(),
                "IMC actual: %.2f %s",
                imc,
                mensajeConSaltoDeLinea
            ).replace(",", ".")
        } else {
            tv_imc.text = "Altura o peso no válidos"
        }
    }

    private fun calcularEdad(fechaNacimientoStr: String?): Int {
        if (fechaNacimientoStr != null) {
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            try {
                val fechaNacimiento = formatoFecha.parse(fechaNacimientoStr)
                if (fechaNacimiento != null) {
                    val calendarNacimiento = Calendar.getInstance().apply { time = fechaNacimiento }
                    val calendarActual = Calendar.getInstance()
                    var edad = calendarActual.get(Calendar.YEAR) - calendarNacimiento.get(Calendar.YEAR)
                    if (calendarActual.get(Calendar.DAY_OF_YEAR) < calendarNacimiento.get(Calendar.DAY_OF_YEAR)) {
                        edad--
                    }
                    return edad
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return 0
    }

    private fun calcularCalorias(sexo: String?, edad: Int?, actividad: String?): Double {
        if (altura != null && peso != null) {
            if (sexo != null && edad != null && peso != null && altura != null && actividad != null) {
                val factorActividad = when (actividad) {
                    "sedentario" -> 1.2
                    "ligera" -> 1.375
                    "moderado" -> 1.55
                    "alta" -> 1.725
                    "atletaProfesional" -> 1.9
                    else -> 1.0
                }

                val mb = if (sexo == "hombre") {
                    (66 + (13.7 * peso) + (5 * altura) - (6.8 * edad))
                } else {
                    (655 + (9.6 * peso) + (1.8 * altura) - (4.7 * edad))
                }

                return mb * factorActividad
            }
        }
        return 0.0
    }

    private fun calcularCaloriasObjetivo(calorias: Double, objetivo: String?): String {
        val caloriasAjustadas = when (objetivo) {
            "perder_grasa" -> calorias * 0.8
            "mantener_peso" -> calorias
            "ganar_musculo" -> calorias * 1.2
            else -> calorias
        }
        return String.format("%.2f", caloriasAjustadas).replace(",", ".")
    }

    private fun calcularMacronutrientes(calorias: Double): Map<String, String> {
        val porcentajeProteinas = 0.20
        val porcentajeCarbohidratos = 0.50
        val porcentajeGrasas = 0.30

        val caloriasProteinas = calorias * porcentajeProteinas
        val caloriasCarbohidratos = calorias * porcentajeCarbohidratos
        val caloriasGrasas = calorias * porcentajeGrasas

        val gramosProteinas = caloriasProteinas / 4
        val gramosCarbohidratos = caloriasCarbohidratos / 4
        val gramosGrasas = caloriasGrasas / 9

        return mapOf(
            "proteinas" to String.toString().format("%.2f", gramosProteinas).replace(",", "."),
            "carbohidratos" to String.toString().format("%.2f", gramosCarbohidratos).replace(",", "."),
            "grasas" to String.toString().format("%.2f", gramosGrasas).replace(",", ".")
        )
    }
}
