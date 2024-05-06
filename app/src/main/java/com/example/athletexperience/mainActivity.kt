package com.example.athletexperience

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.athletexperience.databinding.ActivityMainBinding
import com.example.athletexperience.loggin.SingInActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class mainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fireba0seAuth: FirebaseAuth
    lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this,drawerLayout,binding.toolbar,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> Toast.makeText(applicationContext,"Clicked Home",Toast.LENGTH_SHORT).show()
                R.id.nav_message -> Toast.makeText(applicationContext,"Clicked message",Toast.LENGTH_SHORT).show()
                R.id.nav_sync -> Toast.makeText(applicationContext,"Clicked sync",Toast.LENGTH_SHORT).show()
                R.id.nav_trash -> Toast.makeText(applicationContext,"Clicked trash",Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(applicationContext,"Clicked setting",Toast.LENGTH_SHORT).show()
                R.id.nav_login -> Toast.makeText(applicationContext,"Clicked loggin",Toast.LENGTH_SHORT).show()
                R.id.nav_share -> Toast.makeText(applicationContext,"Clicked share",Toast.LENGTH_SHORT).show()
                R.id.nav_rate_us -> Toast.makeText(applicationContext,"Clicked rate us",Toast.LENGTH_SHORT).show()
            }
            true
        }

        fireba0seAuth = Firebase.auth
        binding.logoutButton.setOnClickListener{
            Firebase.auth.signOut()
            startActivity(Intent(this, SingInActivity::class.java ))
            finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}