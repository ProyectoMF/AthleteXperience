package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale
import kotlin.properties.Delegates

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
        val fecha = intent.getStringExtra("FECHA_NACIMIENTO")
        val altura = intent.getDoubleExtra("ALTURA", 0.0)
        val peso = intent.getDoubleExtra("PESO", 0.0)


        initComponent()
        initListeners()
        initUI()

        // Calcular y mostrar el IMC
        calcularIMC(altura, peso)

        // Mostrar el mensaje personalizado según el objetivo
        calcularCalorias(objetivo)
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
    private fun calcularIMC(altura: Double, peso: Double) {
        if (altura > 0 && peso > 0) {
            val imc = peso / ((altura / 100) * (altura / 100))
            tv_imc.text = String.format(Locale.getDefault(), "Tu IMC actual es: %.2f", imc)
        } else {
            // Manejar el caso de valores no válidos
            tv_imc.text = "Altura o peso no válidos"
        }
    }



    private fun calcularCalorias(objetivo: String?) {

        // Utilizar un when para establecer el mensaje en tv_calorias según el objetivo
        val mensaje = when (objetivo) {
            "perder_grasa" -> "Para perder grasa, es importante mantener un déficit calórico."
            "mantener_peso" -> "Para mantener tu peso, asegúrate de consumir la misma cantidad de calorías que gastas."
            "ganar_musculo" -> "Para ganar músculo, es necesario consumir un exceso calórico y entrenar con pesas."
            else -> "Objetivo no especificado"
        }
        // Mostrar el mensaje en tv_calorias
        tv_calorias.text = mensaje

    }


}