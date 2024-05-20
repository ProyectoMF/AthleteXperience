package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
        //initUI()

        val edad = calcularEdad(fecha)
        calcularIMC()
        val sexoLowerCase = sexo?.toLowerCase(Locale.getDefault())
        val calorias = calcularCalorias(sexoLowerCase, edad, actividad)

        // Calcular y mostrar las calorías según el objetivo seleccionado
        val caloriasObjetivo = calcularCaloriasObjetivo(calorias, objetivo)
        tv_calorias.text = "Calorías diarias: $caloriasObjetivo"

        // Calcular y mostrar los macronutrientes
        val macronutrientes = calcularMacronutrientes(caloriasObjetivo.toDouble())
        tv_macronutrientes.text = "Proteínas: ${macronutrientes["proteinas"]} g\nCarbohidratos: ${macronutrientes["carbohidratos"]} g\nGrasas: ${macronutrientes["grasas"]} g"
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
            // Guardar los datos en la base de datos Firebase
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val imcString = tv_imc.text.toString().split(" ")[2].toDoubleOrNull()?.toInt() ?: 0.0
                val caloriasString = tv_calorias.text.toString().split(":")[1].trim().toDoubleOrNull()?.toInt() ?: 0.0
                val macronutrientesText = tv_macronutrientes.text.toString().split("\n")
                val proteinas = macronutrientesText[0].split(":")[1].trim().split(" ")[0].toDoubleOrNull()?.toInt() ?: 0.0
                val carbohidratos = macronutrientesText[1].split(":")[1].trim().split(" ")[0].toDoubleOrNull()?.toInt() ?: 0.0
                val grasas = macronutrientesText[2].split(":")[1].trim().split(" ")[0].toDoubleOrNull()?.toInt() ?: 0.0
                val userMap = mapOf(
                    "imc" to imcString,
                    "calorias" to caloriasString,
                    "macronutrientes" to mapOf(
                        "proteinas" to proteinas,
                        "carbohidratos" to carbohidratos,
                        "grasas" to grasas
                    )
                )

                FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .setValue(userMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Plan creado exitosamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Error al crear el plan: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            val intent = Intent(this, mainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }



    private fun calcularIMC() {
        if (altura != 0.0 && peso != 0.0) {
            val imc = peso / ((altura / 100) * (altura / 100))
            val mensajeIMC = when {
                imc < 18.5 -> "\nBajo peso"
                imc <= 24.9 -> "\nNormal"
                imc <= 29.9 -> "\nSobrepeso"
                else -> "\nObeso"
            }
            tv_imc.text = String.format(
                Locale.US, "IMC actual: %.2f %s", imc, mensajeIMC
            )
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
        if (altura != null && peso != null && sexo != null && edad != null && actividad != null) {
            val factorActividad = when (actividad) {
                "sedentario" -> 1.2
                "ligera" -> 1.375
                "moderado" -> 1.55
                "alta" -> 1.725
                "atletaProfesional" -> 1.9
                else -> 1.0
            }

            val mb = if (sexo == "hombre") {
                66.5 + (13.75 * peso) + (5.003 * altura) - (6.75 * edad)
            } else {
                655 + (9.563 * peso) + (1.85 * altura) - (4.676 * edad)
            }

            return mb * factorActividad
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
        return String.format(Locale.US, "%.2f", caloriasAjustadas)
    }

    private fun calcularMacronutrientes(calorias: Double): Map<String, String> {
        // Establece los porcentajes de macronutrientes
        val porcentajeProteinas = 0.20  // 20% de las calorías totales provendrán de proteínas
        val porcentajeCarbohidratos = 0.50  // 50% de las calorías totales provendrán de carbohidratos
        val porcentajeGrasas = 0.30  // 30% de las calorías totales provendrán de grasas

        // Calcula las calorías asignadas a cada macronutriente
        val caloriasProteinas = calorias * porcentajeProteinas
        val caloriasCarbohidratos = calorias * porcentajeCarbohidratos
        val caloriasGrasas = calorias * porcentajeGrasas

        // Convierte las calorías de cada macronutriente en gramos
        val gramosProteinas = caloriasProteinas / 4  // Cada gramo de proteínas proporciona 4 calorías
        val gramosCarbohidratos = caloriasCarbohidratos / 4  // Cada gramo de carbohidratos proporciona 4 calorías
        val gramosGrasas = caloriasGrasas / 9  // Cada gramo de grasas proporciona 9 calorías

        // Crea un mapa para devolver los valores calculados, formateados a dos decimales
        return mapOf(
            "proteinas" to String.format(Locale.US, "%.2f", gramosProteinas),
            "carbohidratos" to String.format(Locale.US, "%.2f", gramosCarbohidratos),
            "grasas" to String.format(Locale.US, "%.2f", gramosGrasas)
        )
    }

}
