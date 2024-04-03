package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class ProgressesActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private var currentClientUid: String? = null
    private lateinit var rvCharts: RecyclerView
    private lateinit var adapter: LineChartAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var lineChartPointsList: ArrayList<LineChartPoints>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progresses)

        navView = findViewById(R.id.client_progresses_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Progressi"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        dbRef = Firebase.database.reference

        rvCharts = findViewById(R.id.rv_charts)
        lineChartPointsList = ArrayList()
        adapter = LineChartAdapter(this,lineChartPointsList)
        rvCharts.adapter = adapter
        rvCharts.layoutManager = LinearLayoutManager(this)

        currentClientUid = intent.getStringExtra("client_uid")

        dbRef.child("progresses").child(currentClientUid!!).get().addOnSuccessListener {snapshot ->
            for(snap in snapshot.children){
                val curChartPoint = snap.getValue(LineChartPoints::class.java)
                curChartPoint?.let {
                    if(it.pesiMassimiMap!!.size > 1) // Aggiungo solo i grafici con più di un dato
                        lineChartPointsList.add(it)
                }
            }
            adapter.notifyDataSetChanged()
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            startActivity(intent)
            finish()
        }
    }
}