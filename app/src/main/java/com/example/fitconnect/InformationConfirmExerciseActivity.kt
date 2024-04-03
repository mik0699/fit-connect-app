package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class InformationConfirmExerciseActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var dbRef: DatabaseReference
    private var currentClientUid: String? = null
    private var currentExerciseCode: String? = null
    private var currentPeso: String? = null
    private var currentSet: String? = null
    private var currentRiposo: String? = null
    private var currentRipetizioni: String? = null
    private var currentTemplate: String? = null
    private var currentWorkoudId: String? = null
    private var currentPosition: Int? = null
    private var currentInfoSet: Int? = null
    private lateinit var wvEx: WebView
    private lateinit var currentVideo: String
    private var isAdd = false
    private lateinit var currentFascia: String
    private lateinit var currentTipologia: String
    private lateinit var currentCarico: String
    private lateinit var currentNote: String
    private lateinit var currentAttrezzo: String
    private lateinit var tvTipo: TextView
    private lateinit var tvFascia: TextView
    private lateinit var tvCarico: TextView
    private lateinit var tvNote: TextView
    private lateinit var tvPeso: TextView
    private lateinit var btnAddMod: Button
    private var isInfo = false
    private var isLast = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information_confirm_exercise)

        navView = findViewById(R.id.trainer_conf_ex_nv)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        arrowIcon = headView.findViewById(R.id.arrow_back)
        btnAddMod = findViewById(R.id.btn_add_mod)
        isInfo = intent.getBooleanExtra("isInfo",false)
        isLast = intent.getBooleanExtra("isLast",false)
        tvTitle.text = if(isInfo) "Informazioni esercizio" else "Conferma esercizio"
        if(isInfo){
            btnAddMod.isVisible = false
            currentPosition = intent.getIntExtra("current_position",1)
            currentInfoSet = intent.getIntExtra("current_set",1)
        }
        // Diversi da null quando arrivo da informazioni (isInfo true)
        currentExerciseCode = intent.getStringExtra("ex_code")
        currentClientUid = intent.getStringExtra("client_uid")
        currentTemplate = intent.getStringExtra("template_name")

        isAdd = intent.getBooleanExtra("isAdd",false)
        if(!isAdd)
            currentWorkoudId = intent.getStringExtra("workout_id")

        currentSet = intent.getStringExtra("set")
        currentRipetizioni = intent.getStringExtra("ripetizioni")
        currentRiposo = intent.getStringExtra("riposo")
        currentPeso = intent.getStringExtra("peso")

        wvEx = findViewById(R.id.ww_ex)
        dbRef = Firebase.database.reference

        tvTipo = findViewById(R.id.tv_tipo)
        tvFascia = findViewById(R.id.tv_fascia)
        tvPeso = findViewById(R.id.tv_peso)
        tvNote = findViewById(R.id.tv_note)
        tvCarico = findViewById(R.id.tv_carico)


        dbRef.child("exercises").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val currentExercise = snap.getValue(Exercise::class.java)
                    if(snap.key == currentExerciseCode){
                        currentVideo = currentExercise?.video!!
                        currentFascia = currentExercise.fascia!!
                        currentTipologia = currentExercise.tipologia!!
                        currentCarico = currentExercise.carico!!
                        currentAttrezzo = currentExercise.peso!!
                        currentNote = currentExercise.note!!
                    }
                }
                val video = "<iframe width=\"100%\" height=\"100%\" src=\"$currentVideo\" title=\"YouTube video player\" \" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" allowfullscreen></iframe>"
                wvEx.loadData(video,"text/html","utf-8")
                wvEx.settings.javaScriptEnabled = true
                wvEx.webChromeClient = WebChromeClient()

                tvTipo.text = currentTipologia
                tvFascia.text = currentFascia
                tvPeso.text = currentAttrezzo
                tvNote.text = currentNote
                tvCarico.text = currentCarico
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        btnAddMod.setOnClickListener {
            Log.d("mytag",currentExerciseCode!!)

            if(isAdd){
                // Creo un nuovo WorkoutExercise
                val workId = dbRef.child("workouts").child(currentClientUid+currentTemplate).child("exercises").push().key
                val newWorkoutExercise = WorkoutExercise(workId,currentExerciseCode,currentSet,currentRipetizioni,currentPeso,currentRiposo)
                dbRef.child("workouts").child(currentClientUid+currentTemplate).child("exercises").child(workId!!).setValue(newWorkoutExercise)
            }else{
                // Aggiorno il WorkoutExercise esistente
                val newWorkoutExercise = WorkoutExercise(currentWorkoudId,currentExerciseCode,currentSet,currentRipetizioni,currentPeso,currentRiposo)
                dbRef.child("workouts").child(currentClientUid+currentTemplate).child("exercises").child(currentWorkoudId!!).updateChildren(newWorkoutExercise.toMap()).addOnSuccessListener {
                    Log.d("mytag","Aggiornamento workout riuscito")
                }
            }
            val intent = Intent(this,TrainerWorkoutActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("template_name",currentTemplate)
            startActivity(intent)
            finish()
        }

        arrowIcon.setOnClickListener {
            if(!isInfo){
                val intent = Intent(this,TrainerAddModExerciseActivity::class.java)
                intent.putExtra("client_uid",currentClientUid)
                intent.putExtra("isAdd",isAdd)
                intent.putExtra("fascia",currentFascia)
                intent.putExtra("current_template",currentTemplate)
                intent.putExtra("tipologia",currentTipologia)
                if(!isAdd)
                    intent.putExtra("workout_id",currentWorkoudId)
                intent.putExtra("isLast",isLast)

                intent.putExtra("peso",currentPeso)
                intent.putExtra("set",currentSet)
                intent.putExtra("ripetizioni",currentRipetizioni)
                intent.putExtra("riposo",currentRiposo)

                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this,ClientInteractivePlanActivity::class.java)
                intent.putExtra("isPt",false)
                intent.putExtra("client_uid",currentClientUid)
                intent.putExtra("template_name",currentTemplate)
                intent.putExtra("current_position",currentPosition)
                Log.d("mytag","curr_set: $currentInfoSet")
                intent.putExtra("current_set",currentInfoSet)
                startActivity(intent)
                finish()
            }
        }
    }
}