package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class TrainerClientInformationsActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var storageRef: StorageReference
    private lateinit var dbRef: DatabaseReference
    private lateinit var tvUsername: TextView
    private lateinit var profileImage: ImageView
    private lateinit var etAge: TextView
    private lateinit var etBirth: TextView
    private lateinit var etHeight: TextView
    private lateinit var etWeight: TextView
    private var currentCLientUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_client_informations)

        navView = findViewById(R.id.client_infos_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Informazioni"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        tvUsername = findViewById(R.id.tv_username)
        profileImage = findViewById(R.id.profile_image)
        etAge = findViewById(R.id.et_age)
        etBirth = findViewById(R.id.et_birth)
        etHeight = findViewById(R.id.et_height)
        etWeight = findViewById(R.id.et_weight)
        currentCLientUid = intent.getStringExtra("client_uid")

        dbRef = Firebase.database.reference
        storageRef = Firebase.storage.reference

        dbRef.child("clients").child(currentCLientUid!!).get().addOnSuccessListener {snapshot->
            val currentClient = snapshot.getValue(Client::class.java)
            etAge.text = currentClient?.age ?: "Non presente"
            etBirth.text = currentClient?.birthDate ?: "Non presente"
            etHeight.text = currentClient?.height?.let { it+"cm" } ?: "Non presente"
            etWeight.text = currentClient?.weight?.let { it+"Kg" } ?: "Non presente"
            tvUsername.text = currentClient?.username
        }

        storageRef.child("images/profile_$currentCLientUid").downloadUrl.addOnSuccessListener {
            Glide.with(this@TrainerClientInformationsActivity).load(it).into(profileImage)
            Log.d("Firebase","Immagine trovata!")
        }.addOnFailureListener {
            Log.d("Firebase","Immagine del profilo non trovata")
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this,HomeActivity::class.java)
            intent.putExtra("client_uid",currentCLientUid)
            startActivity(intent)
            finish()
        }
    }
}