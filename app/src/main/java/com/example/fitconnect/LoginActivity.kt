package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var btnSignup: Button
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        btnLogin = findViewById(R.id.btn_login)
        btnSignup = findViewById(R.id.btn_signup)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        auth = Firebase.auth
        dbRef = Firebase.database.reference

        btnSignup.setOnClickListener {
            val intent = Intent(this,SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pwd = etPassword.text.toString()
            if(email.isBlank() || pwd.isBlank())
                Toast.makeText(this,"Compilare tutti i campi",Toast.LENGTH_SHORT).show()
            else
                login(email,pwd)
        }
    }

    private fun createExercises() {
        val exercises = listOf<Exercise>(
            Exercise("Squat","Gambe","Medio alto","Bilanciere","Inarcare la schiena e scendere bene con le gambe","https://www.youtube.com/shorts/gslEzVggur8"),
            Exercise("Distensione bilanciere panca piana","Pettorali","Medio alto","Bilanciere","Inarcare la schiena e tenere il petto in fuori","https://www.youtube.com/shorts/6zEAxY8-8m8"),
            Exercise("Trazioni prone","Dorsali","Corpo libero","Zavorra","Superare la sbarra con il mento","https://www.youtube.com/shorts/ciCPa2knNDQ"),
            Exercise("Lento avanti bilanciere","Spalle","Medio bassso","Bilanciere","Distendere il bilanciere in maniera esplosiva","https://www.youtube.com/shorts/KF1FDif8fxo"),
            Exercise("Curl in piedi con manubri","Bicipiti","Medio basso","Manubri","Distendere il braccio fino alla massima estensione","https://www.youtube.com/shorts/zP7IMCtC9LY"),
            Exercise("Push down","Tricipiti","Medio basso","Macchinario","Tenere il petto all'infuori","https://www.youtube.com/shorts/utsHG6La7RM"),
            Exercise("Crunch a terra","Addominali","Corpo libero","Nessuno","Contrarre il muscolo addominale e non salire troppo con la schiena","https://www.youtube.com/watch?v=Hmt3utbaBqg&ab_channel=iri4fitness"),
            Exercise("Leg extention","Gambe","Medio alto","Macchinario","Salita esplosiva, tenuta e discesa lenta e controllata","https://www.youtube.com/embed/IZpKu3JyLKs?si=z5VRBA9YDtFUbMUN"),
            Exercise("Pectoral fly","Pettorali","Medio alto","Macchinario","Non piegare i gomiti e chiudere bene le braccia","https://www.youtube.com/embed/eGjt4lk6g34?si=3btbvYIXosUfka6t"),
            Exercise("Lat machine","Dorsali","Medio alto","Macchinario","Petto in fuori e scapole chiuse. Portare la barra fino a sotto il mento","https://www.youtube.com/embed/NL6Lqd6nU-g?si=2Tm0UrVw1dCioYPI"),
            Exercise("Alzate laterali manubri","Spalle","Medio","Manubri","Salite e discese controllate. Tenere il busto rigido","https://www.youtube.com/embed/7-8Qvrppe8g?si=yCVJ38LKy-zRL1rL"),
            Exercise("Curl manubri panca 60°","Bicipiti","Medio","Manubri","Tenere il braccio Perpendicolare al pavimento. Eseguire un movimento controllato","https://www.youtube.com/embed/l7d5Sbh-NvY?si=tl_YomwnlobJymDD"),
            Exercise("Dip","Tricipiti","Corpo libero","Zavorra","Contrarre l'addome e scendere fino ad avere i gomiti a 90°","https://www.youtube.com/embed/SLVwguvd6io?si=X9uIamCRpIir9EyZ"),
            Exercise("Leg rises","Addominali","Corpo libero","Nessuno","Sollevare le gambe il più possibile e non aiutarsi con le braccia")
        )
        for(exercise in exercises){
            dbRef.child("exercises").push().setValue(exercise)
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var intent: Intent
                    Log.d("Firebase","${auth.currentUser?.uid!!}")
                    dbRef.child("trainers").child(auth.currentUser?.uid!!).get().addOnSuccessListener {
                        // Se arrivo qui vuol dire che l'uid dell'utente attuale appartiene a un trainer
                        if(it.exists()){
                            Log.d("Firebase", "Trainer trovato")
                            intent = Intent(this, TrainerClientsListActivity::class.java)
                        }else{
                            Log.d("Firebase","Client trovato")
                            intent = Intent(this, HomeActivity::class.java)
                        }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this,"Autenticazione fallita",Toast.LENGTH_SHORT).show()
                }
            }
    }
}