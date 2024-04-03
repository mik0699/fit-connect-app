package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrainerAddEventActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var etTitolo: EditText
    private lateinit var etData: EditText
    private lateinit var acTipologia: AutoCompleteTextView
    private lateinit var etDesc: EditText
    private var currentSelectedDate = Date()
    private var currentClientUid: String? = null
    private lateinit var tipologiaAdapter: ArrayAdapter<String>
    private lateinit var dateFormatter: SimpleDateFormat
    private val eventTypesList = listOf(
        "Allenamento",
        "Appuntamento",
        "Check-Up"
    )
    private var currentTipologiaEvento: String? = null
    private lateinit var btnAddEvent: Button
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_add_event)

        navView = findViewById(R.id.trainer_add_calendar_event_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Aggiungi evento"
        arrowIcon = headView.findViewById(R.id.arrow_back)

        etTitolo = findViewById(R.id.et_titolo)
        etData = findViewById(R.id.et_data)
        acTipologia = findViewById(R.id.ac_tipologia)
        etDesc = findViewById(R.id.et_desc)
        btnAddEvent = findViewById(R.id.btn_add_event)
        dbRef = Firebase.database.reference

        tipologiaAdapter = ArrayAdapter(this,R.layout.dropdown_list_item,eventTypesList)
        acTipologia.setAdapter(tipologiaAdapter)

        currentSelectedDate.time = intent.getLongExtra("current_date",-1)
        currentClientUid = intent.getStringExtra("client_uid")

        dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etData.setText(dateFormatter.format(currentSelectedDate).toString())

        acTipologia.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val item = parent?.getItemAtPosition(position).toString()
            currentTipologiaEvento = item
        }

        btnAddEvent.setOnClickListener {
            if(currentTipologiaEvento == null || etTitolo.text.isBlank() || etData.text.isBlank() || etDesc.text.isBlank()) {
                Toast.makeText(this, "Compilare tutti i campi", Toast.LENGTH_SHORT).show()
            }
            else{
                currentClientUid?.let {
                    Log.d("mytag","${dateFormatter.parse(etData.text.toString())}")
                    val eventKey = dbRef.child("events").child(it).push().key
                    val newEvent = MyEvent(eventKey,etTitolo.text.toString(),etData.text.toString(),currentTipologiaEvento,etDesc.text.toString())
                    dbRef.child("events").child(it).child(eventKey!!).setValue(newEvent).addOnSuccessListener {
                        val intent = Intent(this,CalendarActivity::class.java)
                        intent.putExtra("isPt",true)
                        intent.putExtra("client_uid",currentClientUid)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this,CalendarActivity::class.java)
            intent.putExtra("isPt",true)
            intent.putExtra("client_uid",currentClientUid)
            startActivity(intent)
            finish()
        }
    }
}