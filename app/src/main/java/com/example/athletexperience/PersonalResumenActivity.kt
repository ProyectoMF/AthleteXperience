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
import org.checkerframework.common.subtyping.qual.Bottom
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.properties.Delegates


/*Mujeres: [65 + (9,6 × peso en kg)] + [(1,8 × altura en cm) - (4,7 × edad)] × Factor actividad. Hombres: [66 + (13,7 × peso en kg)] + [(5 × altura en cm) - (6,8 × edad)] × Factor actividad*/
/*
1,2 para personas sedentarias
1,375 para personas con poca actividad física (ejercicio de 1 a 3 veces por semana).
1,55 para individuos que realizan actividad moderada (ejercicio de 3 a 5 veces por semana).
1,725 para personas que hacen actividad intensa (ejercicio de 6 a 7 veces por semana).
1,9 para atletas profesionales (entrenamientos de más de 4 horas diarias).*/

class PersonalResumenActivity : AppCompatActivity() {

    private lateinit var tv_imc: TextView
    private lateinit var tv_calorias: TextView
    private lateinit var tv_macronutrientes: TextView
    private lateinit var bt_back_resumen: FloatingActionButton
    private lateinit var bt_crearplan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_resumen)

        // Recuperar el tipo de objetivo seleccionado desde la actividad anterior


        initComponent()
        initListeners()
        initUI()

        val edad = calcularEdad(fecha)
        calcularIMC()
        val sexoLowerCase = sexo?.toLowerCase(Locale.getDefault())
        val calorias = calcularCalorias(sexoLowerCase, edad,  actividad)

        // Calcular y mostrar las calorías según el objetivo seleccionado
        val caloriasObjetivo = calcularCaloriasObjetivo(calorias, objetivo)
        tv_calorias.text = "Calorías diarias: $caloriasObjetivo"

        tv_macronutrientes.text=sexoLowerCase

    }

    private fun initComponent() {
        //Boton para volver atras
        bt_back_resumen = findViewById(R.id.bt_back_resumen)
        bt_crearplan = findViewById(R.id.bt_crearplan)

        // Textviews
        tv_imc = findViewById(R.id.tv_imc)
        tv_calorias = findViewById(R.id.tv_calorias)
        tv_macronutrientes = findViewById(R.id.tv_macronutrientes)

    }

    private fun initListeners() {
        // Agregar OnClickListener para volver atras
        bt_back_resumen.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }

        bt_crearplan.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, mainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initUI() {

    }

    private fun calcularIMC() {


            if (altura != 0.0 && peso != 0.0) {
                val imc = peso / ((altura / 100) * (altura / 100))

                // Determinar el mensaje del IMC según el rango
                val mensajeIMC = when {
                    imc <= 18.5 -> "Bajo peso"
                    imc <= 24.9 -> "Normal"
                    imc <= 29.9 -> "Sobrepeso"
                    else -> "Obeso"
                }

                // Agregar un salto de línea al mensajeIMC
                val mensajeConSaltoDeLinea = "\n" + mensajeIMC

                // Mostrar el IMC y el mensaje con el salto de línea
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
                    val calendarNacimiento = Calendar.getInstance().apply {
                        time = fechaNacimiento
                    }
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
    private fun calcularCalorias(
        sexo: String?,
        edad: Int?,
        actividad: String?
    ): Double {
        if (altura != null && peso != null) {
            if (sexo != null && edad != null && peso != null && altura != null && actividad != null) {
                val factorActividad = when (actividad) {
                    "sedentario" -> 1.2
                    "ligera" -> 1.375
                    "moderado" -> 1.55
                    "alta" -> 1.725
                    "atletaProfesional" -> 1.9
                    else -> 1.0 // Valor predeterminado
                }

                val mb = if (sexo == "hombre") {
                    (66 + (13.7 * peso) + (5 * altura) - (6.8 * edad))
                } else {
                    (655 + (9.6 * peso) + (1.8 * altura) - (4.7 * edad))
                }

                return mb * factorActividad
            }
        }
        return 0.0 // Si falta algún dato, retornamos 0.0
    }

    private fun calcularCaloriasObjetivo(calorias: Double, objetivo: String?): String {
        // Ajustar las calorías según el objetivo seleccionado
        val caloriasAjustadas = when (objetivo) {
            "perder_grasa" -> calorias * 0.8 // Reducir un 20% para perder grasa
            "mantener_peso" -> calorias // Mantener el mismo valor
            "ganar_musculo" -> calorias * 1.2 // Aumentar un 20% para ganar músculo
            else -> calorias // Por defecto, mantener el mismo valor
        }
        return String.format("%.2f", caloriasAjustadas).replace(",", ".")
    }

}