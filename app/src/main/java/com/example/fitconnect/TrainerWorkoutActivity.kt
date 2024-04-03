package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TrainerWorkoutActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var addIcon: ImageView
    private var currentClientUid: String? = null
    private lateinit var workoutRv: RecyclerView
    private lateinit var adapter: ExerciseAdapter
    private lateinit var exerciseList: ArrayList<WorkoutExercise>
    private var templateName: String? = null
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_workout)

        navView = findViewById(R.id.trainer_workout_navigation_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        arrowIcon = headView.findViewById(R.id.arrow_back)
        addIcon = headView.findViewById(R.id.add_icon)
        currentClientUid = intent.getStringExtra("client_uid")
        templateName = intent.getStringExtra("template_name")
        tvTitle.text = templateName
        dbRef = Firebase.database.reference

        exerciseList = ArrayList()
        adapter = ExerciseAdapter(this,exerciseList,currentClientUid!!,templateName!!)
        workoutRv = findViewById(R.id.trainer_workout_rv)
        workoutRv.adapter = adapter
        workoutRv.layoutManager = LinearLayoutManager(this)

        dbRef.child("workouts").child(currentClientUid+templateName).child("exercises").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                exerciseList.clear()
                for(exSnap in snapshot.children){
                    val currentWorkEx = exSnap.getValue(WorkoutExercise::class.java)
                    exerciseList.add(currentWorkEx!!)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        addIcon.setOnClickListener {
            val intent = Intent(this, TrainerAddModExerciseActivity::class.java)
            intent.putExtra("client_uid", currentClientUid)
            intent.putExtra("isAdd",true)
            intent.putExtra("fascia",templateName)
            intent.putExtra("current_template",templateName)
            startActivity(intent)
            finish()
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this,WorkoutsActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("isPt",true)
            startActivity(intent)
            finish()
        }
    }
}