package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView

class WorkoutsActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private var currentClientUid: String? = null
    private lateinit var tvGambe: TextView
    private lateinit var tvSpalle: TextView
    private lateinit var tvAddominali: TextView
    private lateinit var tvDorsali: TextView
    private lateinit var tvPettorali: TextView
    private lateinit var tvBicipiti: TextView
    private lateinit var tvTricipiti: TextView
    private var isPt = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workouts)

        navView = findViewById(R.id.trainer_templates_navigation_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Workouts"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        // Contiene l'uid del cliente se sono un PT o l'uid dell'utente stesso se sono un cliente
        currentClientUid = intent.getStringExtra("client_uid")
        isPt = intent.getBooleanExtra("isPt",false)

        tvGambe = findViewById(R.id.tv_gambe)
        tvBicipiti = findViewById(R.id.tv_bicipiti)
        tvSpalle = findViewById(R.id.tv_spalle)
        tvAddominali = findViewById(R.id.tv_addominali)
        tvPettorali = findViewById(R.id.tv_pettorali)
        tvDorsali = findViewById(R.id.tv_dorsali)
        tvTricipiti = findViewById(R.id.tv_tricipiti)

        tvGambe.setOnClickListener {
            val intent = if(isPt) Intent(this, TrainerWorkoutActivity::class.java) else Intent(this,ClientInteractivePlanActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name","Gambe")
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        tvBicipiti.setOnClickListener {
            val intent = if(isPt) Intent(this, TrainerWorkoutActivity::class.java) else Intent(this,ClientInteractivePlanActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name","Bicipiti")
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        tvSpalle.setOnClickListener {
            val intent = if(isPt) Intent(this, TrainerWorkoutActivity::class.java) else Intent(this,ClientInteractivePlanActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name","Spalle")
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        tvAddominali.setOnClickListener {
            val intent = if(isPt) Intent(this, TrainerWorkoutActivity::class.java) else Intent(this,ClientInteractivePlanActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name","Addominali")
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        tvPettorali.setOnClickListener {
            val intent = if(isPt) Intent(this, TrainerWorkoutActivity::class.java) else Intent(this,ClientInteractivePlanActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name","Pettorali")
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        tvDorsali.setOnClickListener {
            val intent = if(isPt) Intent(this, TrainerWorkoutActivity::class.java) else Intent(this,ClientInteractivePlanActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name","Dorsali")
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        tvTricipiti.setOnClickListener {
            val intent = if(isPt) Intent(this, TrainerWorkoutActivity::class.java) else Intent(this,ClientInteractivePlanActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name","Tricipiti")
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            startActivity(intent)
            finish()
        }
    }
}