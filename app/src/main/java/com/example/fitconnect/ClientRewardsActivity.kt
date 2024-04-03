package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text

class ClientRewardsActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_rewards)

        navView = findViewById(R.id.client_rewards_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Premi"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        dbRef = Firebase.database.reference
        auth = Firebase.auth
        val currentClientUid = auth.currentUser?.uid!!
        val rewardsList = ArrayList<Reward>()
        val textViewsList = listOf<TextView>(
            findViewById(R.id.tv_1),
            findViewById(R.id.tv_2),
            findViewById(R.id.tv_3),
            findViewById(R.id.tv_4),
            findViewById(R.id.tv_5),
            findViewById(R.id.tv_6),
        )
        val progressTextViewsList = listOf<TextView>(
            findViewById(R.id.tv_progress_1),
            findViewById(R.id.tv_progress_2),
            findViewById(R.id.tv_progress_3),
            findViewById(R.id.tv_progress_4),
            findViewById(R.id.tv_progress_5),
            findViewById(R.id.tv_progress_6),
        )
        val imageViewsList = listOf<ImageView>(
            findViewById(R.id.iv_1),
            findViewById(R.id.iv_2),
            findViewById(R.id.iv_3),
            findViewById(R.id.iv_4),
            findViewById(R.id.iv_5),
            findViewById(R.id.iv_6)
        )

        dbRef.child("rewards").child(currentClientUid).get().addOnSuccessListener {snapshot ->
            for (snap in snapshot.children){
                rewardsList.add(snap.getValue(Reward::class.java)!!)
            }

            for(i in 0 until rewardsList.size){
                val curRew = rewardsList[i]
                val curTv = textViewsList[i]
                val curIv = imageViewsList[i]
                val curPTv = progressTextViewsList[i]

                curTv.text = curRew.description
                curPTv.text = "${curRew.currentCount}/${curRew.totalCount}"

                if(curRew.earned)
                    curIv.setImageResource(R.drawable.medal)
                else
                    curIv.setImageResource(R.drawable.medal_locekd)
            }
        }


        arrowIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}