package com.example.athletexperience


import com.example.athletexperience.loggin.SingInActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PersonalObjetivoActivity : AppCompatActivity(){

    private lateinit var bt_back_objetive : FloatingActionButton
    private lateinit var cv_perdergrasa : CardView
    private lateinit var cv_mantenerpeso : CardView
    private lateinit var cv_ganarmusculo : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_objetivo)

        initComponent()
        initListeners()

    }

    private fun initComponent(){
        //Vaariable para volver atras
        bt_back_objetive = findViewById(R.id.bt_back_objetive)

        //Variables para los CardView
        cv_perdergrasa = findViewById(R.id.cv_perdergrasa)
        cv_mantenerpeso = findViewById(R.id.cv_mantenerpeso)
        cv_ganarmusculo = findViewById(R.id.cv_ganarmusculo)

    }

    private fun initListeners(){

        // Agregar OnClickListener a cv_perdergrasa
        bt_back_objetive.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
        }


        // Agregar OnClickListener a cv_perdergrasa
        cv_perdergrasa.setOnClickListener {startActivity(intent)
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalActividadActivity::class.java)
            intent.putExtra("OBJETIVO", "perder_grasa")
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_mantenerpeso
        cv_mantenerpeso.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalActividadActivity::class.java)
            intent.putExtra("OBJETIVO", "mantener_peso")
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_ganarmusculo
        cv_ganarmusculo.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalActividadActivity::class.java)
            intent.putExtra("OBJETIVO", "ganar_musculo")
            startActivity(intent)
        }
    }


}