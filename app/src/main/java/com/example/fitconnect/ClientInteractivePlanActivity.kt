package com.example.fitconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClientInteractivePlanActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var currentUid: String
    private lateinit var currentTemplate: String
    private var isPt = false
    private lateinit var btnTimer: Button
    private lateinit var tvTimer: TextView
    private lateinit var dbRef: DatabaseReference
    private lateinit var workExList: ArrayList<WorkoutExercise>
    private lateinit var tvExName: TextView
    private lateinit var pbEx: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvSet: TextView
    private lateinit var tvReps: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvRest: TextView
    private lateinit var btnInfo: Button
    private lateinit var btnNext: Button
    private var currentWorkoutExercise: WorkoutExercise? = null
    private var currentExercise: Exercise? = null
    private lateinit var currentRest: String
    private var timer: CountDownTimer? = null
    private var timerStart = true
    private lateinit var btnBack: Button
    private var currentTotalSet: Int = 1
    private lateinit var rbDiff: RatingBar
    private lateinit var btnDiff: Button
    private lateinit var etNote: EditText
    private lateinit var notificationManager: NotificationManager
    private var notificationID: Int = 99
    private var isLast = false
    private var nCompletedExercises = 0

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private var currentSet = 1
    private var currentPosition: Int = 1

    private val CHANNEL_ID = "com.example.fitconnect.rewards"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_interactive_plan)

        navView = findViewById(R.id.client_interactive_plan_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Scheda interattiva"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        btnTimer = findViewById(R.id.btn_timer)
        tvTimer = findViewById(R.id.tv_timer)
        tvExName = findViewById(R.id.tv_ex_name)
        pbEx = findViewById(R.id.pb_ex)
        tvProgress = findViewById(R.id.tv_progress)
        tvSet = findViewById(R.id.tv_set)
        tvReps = findViewById(R.id.tv_reps)
        tvWeight = findViewById(R.id.tv_weight)
        tvRest = findViewById(R.id.tv_rest)
        btnInfo = findViewById(R.id.btn_info)
        btnNext = findViewById(R.id.btn_next)
        btnBack = findViewById(R.id.btn_back)
        rbDiff = findViewById(R.id.rb_diff)
        etNote = findViewById(R.id.et_note)

        // Se c'è l'extra vuol dire che arrivo da informazioni, altrimenti sono all'inizio metto la posizione corrente a 1
        currentPosition = intent.getIntExtra("current_position",1)
        currentSet = intent.getIntExtra("current_set",1)

        dbRef = Firebase.database.reference
        workExList = ArrayList()

        currentUid = intent.getStringExtra("client_uid")!!
        currentTemplate = intent.getStringExtra("template_name")!!
        isPt = intent.getBooleanExtra("isPt",false)

        // Gestione delle notifiche
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID,"Rewards Notification",importance).apply {
            this.description = "New reward notification"
        }
        // Attivo la vibrazione
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)

        dbRef.child("workouts").child(currentUid+currentTemplate).child("exercises").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                workExList.clear()
                for(snap in snapshot.children){
                    val currentWorkEx = snap.getValue(WorkoutExercise::class.java)
                    workExList.add(currentWorkEx!!)
                }
                pbEx.max = workExList.size
                setExercise()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        Log.d("mytag","${workExList.size}")

        btnInfo.setOnClickListener {
            etNote.clearFocus()
            setNotes()
            setDifficultyLevel()
            val intent = Intent(this,InformationConfirmExerciseActivity::class.java)
            intent.putExtra("isInfo",true)
            intent.putExtra("ex_code",currentWorkoutExercise?.exerciseId)
            intent.putExtra("current_position",currentPosition)
            intent.putExtra("client_uid",currentUid)
            intent.putExtra("template_name",currentTemplate)
            Log.d("mytag","curr_set: $currentSet")
            intent.putExtra("current_set",currentSet)
            startActivity(intent)
            finish()
        }

        btnTimer.setOnClickListener {
            // timerStart è true se nel timer c'è scritto Avvia Rest Timer
            if(timerStart){
                var milliseconds = (currentRest.split(":")[0].toLong() * 60 + currentRest.split(":")[1].toLong()) * 1000
                // Sono all'ultimo set dell'esercizio
                if(currentSet == currentTotalSet){
                    // Aggiorno i feedback dell'utente
                    setNotes()
                    setDifficultyLevel()

                    // Aggiorno Premi e Progressi
                    currentExercise?.tipologia?.let {
                        updateRewards(it.lowercase())
                    }
                    updateRewards("esercizio")
                    addProgressPoint()

                    nCompletedExercises++
                    // Sono all'ultimo esercizio della scheda
                    if(currentPosition == workExList.size){
                        isLast = true
                        val intent = Intent(this,WorkoutsActivity::class.java)
                        intent.putExtra("client_uid",currentUid)
                        intent.putExtra("isPt",isPt)
                        // Il workout è completato solo se ho eseguito tutti gli esercizi senza saltarne nessuno
                        if(nCompletedExercises >= workExList.size)
                            updateRewards("workout")
                        startActivity(intent)
                        finish()
                    }else{
                        currentPosition++
                        currentSet = 1
                        // Il completamento dell'esercizio lo aggiorno solo se l'utente compie tutti i set tramite il timer
                        etNote.clearFocus()
                        setExercise()
                        milliseconds = 120000
                    }

                } else {
                    currentSet++
                    tvSet.text = "$currentSet/$currentTotalSet"
                }
                if(!isLast){
                    timer = object : CountDownTimer(milliseconds, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val seconds = (millisUntilFinished / 1000) % 60
                            val minutes = (millisUntilFinished / 1000) / 60
                            val time =
                                String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds)
                            tvTimer.text = time
                        }

                        override fun onFinish() {
                            timerStart = true
                            tvTimer.text = currentWorkoutExercise?.riposo
                            if(currentSet == currentTotalSet){
                                if(currentPosition == workExList.size) {
                                    btnTimer.text = "Finisci Workout"
                                }
                                else
                                    btnTimer.text = "Avvia Rest Timer\n e Prosegui"
                                tvTimer.text = "2:00"
                            }
                            else
                                btnTimer.text = "Avvia\nRest Timer"
                            timer?.cancel()
                        }
                    }.start()
                    btnTimer.text = "Reset\nRest Timer"
                    timerStart = false
                }
            }else{
                timerStart = true
                tvTimer.text = currentWorkoutExercise?.riposo
                if(currentSet == currentTotalSet){
                    if(currentPosition == workExList.size) {
                        btnTimer.text = "Finisci Workout"
                    }
                    else
                        btnTimer.text = "Avvia Rest Timer\n e Prosegui"
                    tvTimer.text = "2:00"
                }
                else
                    btnTimer.text = "Avvia\nRest Timer"
                timer?.cancel()
            }
        }

        btnNext.setOnClickListener {
            setNotes()
            setDifficultyLevel()
            etNote.clearFocus()
            currentSet = 1
            timerStart = true
            btnTimer.text = "Avvia\nRest Timer"
            tvTimer.text = currentWorkoutExercise?.riposo
            timer?.cancel()
            if(currentPosition < workExList.size){
                currentPosition++
                setExercise()
            }else{
                val intent = Intent(this,WorkoutsActivity::class.java)
                intent.putExtra("client_uid",currentUid)
                intent.putExtra("isPt",isPt)
                startActivity(intent)
                finish()
            }
        }

        btnBack.setOnClickListener {
            setNotes()
            setDifficultyLevel()
            etNote.clearFocus()
            currentSet = 1
            timerStart = true
            btnTimer.text = "Avvia\nRest Timer"
            tvTimer.text = currentWorkoutExercise?.riposo
            timer?.cancel()
            if(currentPosition == 1){
                val intent = Intent(this,WorkoutsActivity::class.java)
                intent.putExtra("client_uid",currentUid)
                intent.putExtra("isPt",isPt)
                startActivity(intent)
                finish()
            }else{
                currentPosition--
                setExercise()
            }
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this, WorkoutsActivity::class.java)
            intent.putExtra("client_uid",currentUid)
            intent.putExtra("isPt",isPt)
            startActivity(intent)
            finish()
        }
    }

    private fun setDifficultyLevel() {
        val diffLevel = rbDiff.rating
        if(diffLevel == 0.0F) { // Se è vuoto cancello il campo dal db
            val update = mapOf ("diffRate" to null)
            dbRef.child("workouts").child(currentUid+currentTemplate).child("exercises").child(currentWorkoutExercise?.workoutExerciseId!!).updateChildren(update)
        }else{
            val update = mapOf ("diffRate" to diffLevel)
            dbRef.child("workouts").child(currentUid+currentTemplate).child("exercises").child(currentWorkoutExercise?.workoutExerciseId!!).updateChildren(update)
        }
    }

    private fun setNotes(){
        if(etNote.text.toString().trim() == "") { // Se è vuoto cancello il campo dal db
            val update = mapOf ("note" to null)
            dbRef.child("workouts").child(currentUid+currentTemplate).child("exercises").child(currentWorkoutExercise?.workoutExerciseId!!).updateChildren(update)
        }else{
            val update = mapOf ("note" to etNote.text.toString())
            dbRef.child("workouts").child(currentUid+currentTemplate).child("exercises").child(currentWorkoutExercise?.workoutExerciseId!!).updateChildren(update)
        }
    }

    private fun addProgressPoint() {
        //calendar.add(Calendar.DATE,3)
        val curDate = dateFormat.format(calendar.time)
        Log.d("mytag","Data: $curDate")
        val curPeso = currentWorkoutExercise?.peso!!.toInt()
        val curTotReps = currentWorkoutExercise?.ripetizioni!!.toInt() * currentWorkoutExercise?.set!!.toInt()
        var found = false
        val curExName = currentExercise?.tipologia!!
        var curPesiMap: HashMap<String,Int>
        var curTotRepsMap: HashMap<String,Int>

        dbRef.child("progresses").child(currentUid).get().addOnSuccessListener {snapshot ->
            for(snap in snapshot.children){
                val curLineChartPoints = snap.getValue(LineChartPoints::class.java)
                curLineChartPoints?.let {
                    // Aggiorno solamente la voce con il nome uguale a quello dell'esercizio corrente
                    if(it.exerciseName!! == curExName){
                        found = true
                        curPesiMap = it.pesiMassimiMap!!
                        curTotRepsMap = it.ripetizioniComplessiveMassimeMap!!
                        // Se l'esercizio è già stato svolto in questa data aggiorno solo se i valori sono maggiori
                        if(curDate in curPesiMap.keys){
                            if(curPeso > curPesiMap[curDate]!!){
                                curPesiMap[curDate] = curPeso
                                dbRef.child("progresses").child(currentUid).child(it.lineChartPointsId!!).updateChildren(
                                    mapOf("pesiMassimiMap" to curPesiMap)
                                )
                            }
                            if(curTotReps > curTotRepsMap[curDate]!!){
                                curTotRepsMap[curDate] = curTotReps
                                dbRef.child("progresses").child(currentUid).child(it.lineChartPointsId!!).updateChildren(
                                    mapOf("ripetizioniComplessiveMassimeMap" to curTotRepsMap)
                                )
                            }
                        }else{
                            curPesiMap[curDate] = curPeso
                            curTotRepsMap[curDate] = curTotReps
                            dbRef.child("progresses").child(currentUid).child(it.lineChartPointsId!!).updateChildren(
                                mapOf("pesiMassimiMap" to curPesiMap,
                                    "ripetizioniComplessiveMassimeMap" to curTotRepsMap)
                            )
                        }
                    }
                }
            }
            // È la prima volta che si esegue l'esercizio
            if(!found){
                val key = dbRef.child("progresses").child(currentUid).push().key

                dbRef.child("progresses")
                    .child(currentUid)
                    .child(key!!).setValue(LineChartPoints(
                        key,
                        curExName,
                        hashMapOf(curDate to curPeso),
                        hashMapOf(curDate to curTotReps)
                    ))
            }
        }
    }

    private fun updateRewards(titolo: String){
        Log.d("mytag","Entrato in update rewards")
        dbRef.child("rewards").child(currentUid).get().addOnSuccessListener {snapshot ->
            for(snap in snapshot.children){
                val currReward = snap.getValue(Reward::class.java)
                currReward?.let {
                    var currCount = it.currentCount

                    // Eseguo le operazioni di aggiornamento solo se il premio non è ancora stato guadagnato
                    if(!it.earned){
                        if(it.title == titolo){
                            Log.d("mytag","$currCount")
                            currCount += 1
                            dbRef.child("rewards").child(currentUid).child(it.rewardId!!).updateChildren(
                                mapOf("currentCount" to currCount)
                            )
                        }
                        if(currCount == it.totalCount){
                            dbRef.child("rewards").child(currentUid).child(it.rewardId!!).updateChildren(
                                mapOf("earned" to true)
                            )
                            notifyAchievedReward(it.title!!)
                        }
                    }
                }
            }
        }
    }

    private fun notifyAchievedReward(titolo: String){
        val intent = Intent(this, ClientRewardsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        notificationID++
        val builder = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("Premio Guadagnato")
            .setContentText("Hai guadagnato un premio, complimenti! Clicca qui per visualizzarlo")
            .setSmallIcon(R.drawable.medal)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationID,builder.build())
    }

    private fun readRatingAndNotes() {
        dbRef.child("workouts").child(currentUid + currentTemplate).child("exercises")
            .child(currentWorkoutExercise?.workoutExerciseId!!).get().addOnSuccessListener {
                val workEx = it.getValue(WorkoutExercise::class.java)
                rbDiff.rating = workEx?.diffRate ?: 0.0F
                etNote.setText(workEx?.note ?: "")
            }
    }

    private fun setExercise(){
        if(currentPosition == workExList.size) // Ultimo esercizio
            btnNext.text = "Termina allenamento"
        else
            btnNext.text = "Prossimo"

        if(currentPosition == 1)
            btnBack.text = "Torna ai Workouts"
        else
            btnBack.text = "Precedente"

        currentWorkoutExercise = workExList[currentPosition-1]
        dbRef.child("exercises").child(currentWorkoutExercise?.exerciseId!!).get().addOnSuccessListener {
            currentExercise = it.getValue(Exercise::class.java)
            tvExName.text = currentExercise?.tipologia
        }
        pbEx.progress = currentPosition
        tvProgress.text = "$currentPosition/${pbEx.max}"
        tvSet.text = "$currentSet/${currentWorkoutExercise?.set}"
        tvReps.text = currentWorkoutExercise?.ripetizioni
        tvWeight.text = currentWorkoutExercise?.peso+"Kg"
        tvRest.text = currentWorkoutExercise?.riposo
        tvTimer.text = currentWorkoutExercise?.riposo
        currentRest = currentWorkoutExercise?.riposo!!
        currentTotalSet = currentWorkoutExercise?.set!!.toInt()

        if(currentSet == currentTotalSet){
            btnTimer.text = "Avvia Rest Timer\n e Prosegui"
            tvTimer.text = "2:00"
        }
        readRatingAndNotes()
    }
}