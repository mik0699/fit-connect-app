package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var chatIcon: ImageView
    private lateinit var accountIcon: ImageView
    private lateinit var workoutCV: CardView
    private lateinit var calendarCV: CardView
    private lateinit var rewardsInfoCV: CardView
    private lateinit var progressesCV: CardView
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isPt = false
    private lateinit var ivPremiInfo: ImageView
    private lateinit var tvPremiInfo: TextView
    private var currentClient: Client? = null
    private var currentClientUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navView = findViewById(R.id.home_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        chatIcon = headView.findViewById(R.id.chat_icon)
        accountIcon = headView.findViewById(R.id.account_icon)
        workoutCV = findViewById(R.id.cv_workout)
        calendarCV = findViewById(R.id.cv_calendar)
        rewardsInfoCV = findViewById(R.id.cv_rewards)
        progressesCV = findViewById(R.id.cv_progresses)
        ivPremiInfo = findViewById(R.id.iv_premi_info)
        tvPremiInfo = findViewById(R.id.tv_premi_info)

        dbRef = Firebase.database.reference
        auth = Firebase.auth

        dbRef.child("trainers").child(auth.currentUser?.uid!!).get().addOnSuccessListener {
            isPt = it.exists()
            if(isPt){
                Log.d("Firebase","PT trovato")
                currentClientUid = intent.getStringExtra("client_uid")
                Log.d("Firebase","$currentClientUid")
                ivPremiInfo.setImageResource(R.drawable.info_icon)
                tvPremiInfo.text = "INFORMAZIONI"
                accountIcon.setImageResource(R.drawable.baseline_arrow_back_24)
                dbRef.child("clients").child(currentClientUid!!).get().addOnSuccessListener {it2 ->
                    if(it2.exists()){
                        currentClient = it2.getValue(Client::class.java)
                        tvTitle.text = currentClient?.username.toString()
                    }
                }
            }
        }

        chatIcon.setOnClickListener {
            val intent = Intent(this,ChatActivity::class.java)
            if(isPt)
                intent.putExtra("client_uid",currentClientUid)
            startActivity(intent)
            finish()
        }

        accountIcon.setOnClickListener {
            val intent: Intent = if(isPt){
                Intent(this,TrainerClientsListActivity::class.java)
            }else{
                Intent(this,AccountActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        workoutCV.setOnClickListener {
            var intent = Intent(this,WorkoutsActivity::class.java)
            if(isPt){
                intent.putExtra("client_uid",currentClientUid)
            }else{
                // Se non sono pt, il client uid diventa l'uid dell'utente attuale
                intent.putExtra("client_uid",auth.currentUser?.uid)
            }
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        calendarCV.setOnClickListener {
            val intent = Intent(this,CalendarActivity::class.java)
            if(isPt)
                intent.putExtra("client_uid",currentClientUid)
            else
                intent.putExtra("client_uid",auth.currentUser?.uid!!)
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }

        rewardsInfoCV.setOnClickListener {
            var intent: Intent
            Log.d("Firebase","ispt: $isPt")
            if(isPt){
                intent = Intent(this, TrainerClientInformationsActivity::class.java)
                intent.putExtra("client_uid",currentClientUid)
            }else{
                intent = Intent(this, ClientRewardsActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        progressesCV.setOnClickListener {
            val intent = Intent(this,ProgressesActivity::class.java)
            if(isPt)
                intent.putExtra("client_uid",currentClientUid)
            else
                intent.putExtra("client_uid",auth.currentUser?.uid!!)
            startActivity(intent)
            finish()
        }
    }
}