package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TrainerAddModExerciseActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var dbRef: DatabaseReference
    private var isAdd: Boolean = false
    private var currentClientUid: String? = null
    private lateinit var acFascia: AutoCompleteTextView
    private lateinit var fasciaAdapter: ArrayAdapter<String>
    private lateinit var acTipo: AutoCompleteTextView
    private lateinit var tipoAdapter: ArrayAdapter<String>
    private lateinit var currentTipiList: ArrayList<String>
    private var fasceList = listOf<String>("Gambe","Pettorali","Dorsali","Spalle","Bicipiti","Tricipiti","Addominali")
    private var currentFascia: String? = null
    private lateinit var etSet: EditText
    private lateinit var etRipetizioni: EditText
    private lateinit var etPeso: EditText
    private lateinit var etRiposo: EditText
    private lateinit var btnAddMod: Button
    private lateinit var imgDelete: ImageView
    private var currentTipologia: String? = null
    private lateinit var currentExCode: String
    private lateinit var currentTemplate: String
    private lateinit var currentWorkoutId: String
    private var isLast = false

    private lateinit var fascia: String
    private lateinit var tipologia: String
    private lateinit var serie: String
    private lateinit var ripetizioni: String
    private lateinit var peso: String
    private lateinit var riposo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_add_mod_exercise)

        navView = findViewById(R.id.trainer_add_ex_nv)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        arrowIcon = headView.findViewById(R.id.arrow_back)
        currentClientUid = intent.getStringExtra("client_uid")
        isAdd = intent.getBooleanExtra("isAdd",false)
        fascia = intent.getStringExtra("fascia")!!
        currentTemplate = intent.getStringExtra("current_template")!!

        dbRef = Firebase.database.reference
        acFascia = findViewById(R.id.ac_fascia)
        acTipo = findViewById(R.id.ac_tipo)
        etSet = findViewById(R.id.et_set)
        etRipetizioni = findViewById(R.id.et_ripetizioni)
        etPeso = findViewById(R.id.et_peso)
        etRiposo = findViewById(R.id.et_riposo)
        btnAddMod = findViewById(R.id.btn_add_mod)
        imgDelete = findViewById(R.id.img_delete)

        currentTipiList = ArrayList()

        fasciaAdapter = ArrayAdapter<String>(this,R.layout.dropdown_list_item,fasceList)
        acFascia.setAdapter(fasciaAdapter)
        tipoAdapter = ArrayAdapter<String>(this,R.layout.dropdown_list_item,currentTipiList)
        acTipo.setAdapter(tipoAdapter)

        if(!isAdd){
            // Pagina di modifica, inserisco i dati nei campi
            tvTitle.text = "Modifica esercizio"
            btnAddMod.text = "Modifica esercizio"
            tipologia = intent.getStringExtra("tipologia")!!
            serie = intent.getStringExtra("set")!!
            ripetizioni = intent.getStringExtra("ripetizioni")!!
            peso = intent.getStringExtra("peso")!!
            riposo = intent.getStringExtra("riposo")!!
            currentWorkoutId = intent.getStringExtra("workout_id")!!

            isLast = intent.getBooleanExtra("isLast",true)
            imgDelete.isVisible = !isLast
            acTipo.isEnabled = true
            currentFascia = fascia
            currentTipologia = tipologia

            acFascia.setText(fascia,false)
            acTipo.setText(tipologia,false)
            etSet.setText(serie)
            etRipetizioni.setText(ripetizioni)
            etPeso.setText(peso)
            etRiposo.setText(riposo)
            populateTypesList(fascia)
            setCurrentExCode(fascia,tipologia)
        }else{
            tvTitle.text = "Aggiungi esercizio"
            btnAddMod.text = "Aggiungi esercizio"
            imgDelete.isVisible = false
            acTipo.isEnabled = false
        }

        acFascia.onItemClickListener = object: AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                acTipo.isEnabled = true
                val item = parent?.getItemAtPosition(position).toString()
                currentFascia = item
                populateTypesList(currentFascia!!)
                acTipo.setText("",false)
            }
        }

        acTipo.onItemClickListener = object: AdapterView.OnItemClickListener{
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position).toString()
                currentTipologia = item
                setCurrentExCode(currentFascia!!,currentTipologia!!)
            }
        }

        btnAddMod.setOnClickListener {
            val regexTime = "([0-9]):[0-5][0-9]".toRegex()
            if(currentFascia == null || currentTipologia == null || etPeso.text.toString().isBlank() || etSet.text.toString().isBlank() || etRipetizioni.text.toString().isBlank()|| etRiposo.text.toString().isBlank()){
                Toast.makeText(this,"Compilare tutti i campi",Toast.LENGTH_SHORT).show()
            }else if(!regexTime.matches(etRiposo.text.toString())){
                Toast.makeText(this,"Il formato del campo riposo non è corretto",Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this,InformationConfirmExerciseActivity::class.java)
                intent.putExtra("client_uid",currentClientUid)
                intent.putExtra("ex_code",currentExCode)
                intent.putExtra("isAdd",isAdd)
                intent.putExtra("template_name",currentTemplate)
                if(!isAdd)
                    intent.putExtra("workout_id",currentWorkoutId)
                intent.putExtra("isLast",isLast)

                intent.putExtra("peso",etPeso.text.toString())
                intent.putExtra("set",etSet.text.toString())
                intent.putExtra("ripetizioni",etRipetizioni.text.toString())
                intent.putExtra("riposo",etRiposo.text.toString())
                startActivity(intent)
                finish()
            }
        }

        imgDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Sei sicuro di voler eliminare l'esercizio?")
                .setCancelable(false)
                .setPositiveButton("Sì") { dialog, id ->
                    dbRef.child("workouts").child(currentClientUid+currentTemplate).child("exercises").child(currentWorkoutId).setValue(null)
                    val intent = Intent(this, TrainerWorkoutActivity::class.java)
                    intent.putExtra("client_uid", currentClientUid)
                    intent.putExtra("template_name",currentTemplate)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this, TrainerWorkoutActivity::class.java)
            intent.putExtra("client_uid", currentClientUid)
            intent.putExtra("template_name",currentTemplate)
            startActivity(intent)
            finish()
        }
    }

    private fun setCurrentExCode(compareFascia: String, compareTipologia: String){
        dbRef.child("exercises").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children){
                    val currEx = snap.getValue(Exercise::class.java)
                    val currKey = snap.key
                    Log.d("mytag","fascia: ${currEx?.fascia}, tipo: ${currEx?.tipologia}, key: ${snap.key}")
                    if(currEx?.fascia == compareFascia && currEx.tipologia == compareTipologia){
                        currentExCode = currKey!!
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun populateTypesList(compareFascia: String){
        dbRef.child("exercises").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentTipiList.clear()
                for(snap in snapshot.children){
                    val currentExercise = snap.getValue(Exercise::class.java)
                    if(currentExercise?.fascia == compareFascia){
                        Log.d("mytag","fascia: ${currentExercise.fascia!!},tipo: ${currentExercise.tipologia!!}")
                        currentTipiList.add(currentExercise.tipologia!!)
                        Log.d("mytag", "lista dopo: ${currentTipiList[0]}")
                    }
                }
                tipoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}