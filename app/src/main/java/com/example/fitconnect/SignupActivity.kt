package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class SignupActivity : AppCompatActivity() {
    private lateinit var etUser: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var switchPT: SwitchMaterial
    private lateinit var etCode: EditText
    private lateinit var btnSignup: Button
    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        etUser = findViewById(R.id.et_user)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        switchPT = findViewById(R.id.pt_toggle)
        etCode = findViewById(R.id.et_code)
        btnSignup = findViewById(R.id.btn_signup)
        btnLogin = findViewById(R.id.btn_login)
        auth = Firebase.auth
        dbRef = Firebase.database.reference

        switchPT.setOnCheckedChangeListener { buttonView, isChecked ->
            etCode.isEnabled = !isChecked
        }

        btnSignup.setOnClickListener {
            val email = etEmail.text.toString()
            val pwd = etPassword.text.toString()
            val username = etUser.text.toString()
            if(email.isBlank() || pwd.isBlank() || username.isBlank())
                Toast.makeText(this, "Compilare tutti i campi",Toast.LENGTH_SHORT).show()
            else{
                val isPT = switchPT.isChecked // Se true è un PT
                val ptCode = etCode.text.toString()
                var isPresent: Boolean = false

                // Controllo sul codice PT
                if(!isPT){
                    // Se è false devo controllare che esista già il codice perchè sono un cliente
                    Log.d("Signup","PT Code: $ptCode")
                    dbRef.child("trainers").addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for(trainer in snapshot.children){
                                // Usiamo l'uid del pt come codice
                                val codicePt = trainer.getValue(Trainer::class.java)?.uid
                                Log.d("Signup","codicePT $codicePt")
                                if (ptCode == codicePt){
                                    isPresent = true
                                    signup(email,pwd,username,isPT,ptCode)
                                    break
                                }
                            }
                            if(!isPresent)
                                Toast.makeText(this@SignupActivity,"Il codice PT non esiste",Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }else{
                    // Assegna codice del PT che è pari all'uid
                    signup(email,pwd,username,isPT,ptCode)
                }
            }
        }

        btnLogin.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signup(email: String, password: String, username: String, isPT: Boolean, ptCode: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Se va a buon fine aggiungo un nuovo utente al db
                    val uid = auth.currentUser?.uid!!
                    var intent: Intent

                    if(isPT){
                        val user = Trainer(uid,username,email)
                        dbRef.child("trainers").child(uid).setValue(user)
                        intent = Intent(this@SignupActivity,TrainerClientsListActivity::class.java)
                    }else{
                        val user = Client(uid,username,email,ptCode)
                        // Quando creo un nuovo client creo anche i workout e premi associati ad esso
                        createWorkouts(uid)
                        createBasicRewards(uid)
                        dbRef.child("clients").child(uid).setValue(user)
                        intent = Intent(this@SignupActivity,HomeActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } else {
                    // Altrimenti scrivo che c'è un problema
                    Toast.makeText(this, "Errore di autenticazione", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createWorkouts(currentUid: String) {
        val basicExercisesNames = listOf<String>("Squat","Distensione bilanciere panca piana","Trazioni prone","Lento avanti bilanciere","Curl in piedi con manubri","Push down","Crunch a terra")
        dbRef.child("exercises").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(exerciseSnap in snapshot.children){
                    val currentExercise = exerciseSnap.getValue(Exercise::class.java)
                    val currentExerciseKey = exerciseSnap.key
                    Log.d("mytag","key: $currentExercise")
                    if(currentExercise?.tipologia in basicExercisesNames){
                        val currentWorkoutKey = dbRef.child("workouts").child(currentUid+currentExercise?.fascia).child("exercises").push().key
                        val currentWorkoutExercise = WorkoutExercise(currentWorkoutKey,currentExerciseKey,"3","5","30","1:00")
                        dbRef.child("workouts").child(currentUid+currentExercise?.fascia).child("exercises").child(currentWorkoutKey!!).setValue(currentWorkoutExercise)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun createBasicRewards(currentUid: String){
        val rewardsList = listOf<Reward>(
            Reward(null,"workout","Hai completato il tuo primo workout",1),
            Reward(null,"workout","Hai completato 5 workouts", 5),
            Reward(null,"esercizio","Hai completato il tuo primo esercizio",1),
            Reward(null,"esercizio","Hai completato 10 esercizi",10),
            Reward(null,"squat","Hai eseguito 10 volte l'esercizio squat",10),
            Reward(null,"distensione bilanciere panca piana","Hai eseguito 10 volte l'esercizio distensione bilanciere panca piana",10),
        )
        for(reward in rewardsList){
            val curKey = dbRef.child("rewards").child(currentUid).push().key
            reward.rewardId = curKey
            dbRef.child("rewards").child(currentUid).child(curKey!!).setValue(reward)
        }
    }
}