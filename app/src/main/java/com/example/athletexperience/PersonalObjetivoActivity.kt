package com.example.athletexperience


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.athletexperience.loggin.SingInActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PersonalObjetivoActivity : AppCompatActivity(){

    companion object {
        var objetivo = ""
        var actividad = ""
        var sexo = "hombre"
        var fecha = ""
        var altura = 0.0
        var peso = 0.0
    }

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

        // Redirige a SingInActivity al presionar el botón de atrás
        bt_back_objetive.setOnClickListener {
            // Iniciar la anterior actividad
            val intent = Intent(this, SingInActivity::class.java)
            startActivity(intent)
            //finish()
        }

        // Agregar OnClickListener a cv_perdergrasa
        cv_perdergrasa.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalActividadFisicaActivity::class.java)
            objetivo= "perder_grasa"
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_mantenerpeso
        cv_mantenerpeso.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalActividadFisicaActivity::class.java)
            objetivo= "mantener_peso"
            startActivity(intent)
        }

        // Agregar OnClickListener a cv_ganarmusculo
        cv_ganarmusculo.setOnClickListener {
            // Iniciar la siguiente actividad
            val intent = Intent(this, PersonalActividadFisicaActivity::class.java)
            objetivo= "ganar_musculo"
            startActivity(intent)
        }
    }


}
