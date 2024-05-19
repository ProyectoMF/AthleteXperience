package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.athletexperience.PersonalObjetivoActivity.Companion.actividad
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PersonalActividadFisicaActivity : AppCompatActivity() {

    private lateinit var bt_back_actividad : FloatingActionButton
    private lateinit var cv_sedentario : CardView
    private lateinit var cv_ligera : CardView
    private lateinit var cv_moderado : CardView
    private lateinit var cv_alta : CardView
    private lateinit var cv_atletaProfesional : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_actividad_fisica)

        initComponent()
        initListeners()
    }


    private fun initComponent() {
        //Vaariable para volver atras
        bt_back_actividad = findViewById(R.id.bt_back_actividad)

        //Variables para los CardView
        cv_sedentario = findViewById(R.id.cv_sedentario)
        cv_ligera = findViewById(R.id.cv_ligera)
        cv_moderado = findViewById(R.id.cv_moderado)
        cv_alta = findViewById(R.id.cv_alta)
        cv_atletaProfesional = findViewById(R.id.cv_atletaProfesional)
    }

    private fun initListeners() {


        // Agregar OnClickListener para volver atras
        bt_back_actividad.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, PersonalObjetivoActivity::class.java)
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_perdergrasa
        cv_sedentario.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalInfoActivity::class.java)
            actividad= "sedentario"
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_mantenerpeso
        cv_ligera.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalInfoActivity::class.java)
            actividad= "ligera"
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_ganarmusculo
        cv_moderado.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalInfoActivity::class.java)
            actividad= "moderado"
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_mantenerpeso
        cv_alta.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalInfoActivity::class.java)
            actividad= "alta"
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_ganarmusculo
        cv_atletaProfesional.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalInfoActivity::class.java)
            actividad= "atletaProfesional"
            startActivity(intent)
        }
    }


}