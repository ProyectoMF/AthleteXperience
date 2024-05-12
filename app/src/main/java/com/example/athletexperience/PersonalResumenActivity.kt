package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var bt_back_objetive: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_resumen)

        // Recuperar el tipo de objetivo seleccionado desde la actividad anterior
        val intent = intent
        val objetivo = intent.getStringExtra("OBJETIVO")
        val actividad = intent.getStringExtra("ACTIVIDAD")
        val sexo= intent.getStringExtra("SEXO")
        val fecha = intent.getStringExtra("FECHA_NACIMIENTO")
        val altura = intent.getStringExtra("ALTURA")
        val peso = intent.getStringExtra("PESO")


        initComponent()
        initListeners()
        initUI()

        // Calcular y mostrar el IMC
        calcularIMC(altura, peso)

        // Mostrar el mensaje personalizado según el objetivo
        //calcularCalorias(sexo, edad, peso, altura,actividad)

        tv_macronutrientes.setText(sexo)
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

    private fun initUI(){

    }
    private fun calcularIMC(altura: String?, peso: String?) {

        if (altura != null && peso != null) {
            val altura = altura.substringBefore(" ").toFloatOrNull()
            val peso = peso.substringBefore(" ").toFloatOrNull()

            if (altura != null && peso != null) {
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
                tv_imc.text = String.format(Locale.getDefault(), "IMC actual: %.2f %s", imc, mensajeConSaltoDeLinea).replace(",", ".")

            } else {
                tv_imc.text = "Altura o peso no válidos"
            }
        } else {
            tv_imc.text = "Altura o peso no especificados"
        }

    }



    private fun calcularCalorias(sexo: String?, edad: Int?, peso: Float?, altura: Float?, actividad: String?) {
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

            val calorias = mb * factorActividad

            // Mostrar el resultado en la interfaz de usuario
            val mensaje = "Calorías diarias estimadas: ${calorias.toInt()}"
            tv_calorias.text = mensaje
        } else {
            tv_calorias.text = "Faltan datos para el cálculo de calorías"
        }
    }


}