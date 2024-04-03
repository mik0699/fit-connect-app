package com.example.fitconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TrainerClientsListActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var accountIcon: ImageView
    private lateinit var clientsRecyclerView: RecyclerView
    private lateinit var clientsList: ArrayList<Client>
    private lateinit var adapter: ClientAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer_clients_list)

        navView = findViewById(R.id.clients_list_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Lista clienti"
        accountIcon = headView.findViewById(R.id.account_icon)

        clientsList = ArrayList()
        adapter = ClientAdapter(this,clientsList)
        clientsRecyclerView = findViewById(R.id.clients_list_rv)
        clientsRecyclerView.layoutManager = LinearLayoutManager(this)
        clientsRecyclerView.adapter = adapter

        dbRef = Firebase.database.reference
        auth = Firebase.auth

        dbRef.child("clients").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                clientsList.clear()
                for(clientSnapshot in snapshot.children){
                    val currentClient = clientSnapshot.getValue(Client::class.java)
                    if(currentClient?.ptCode == auth.currentUser?.uid!!){
                        clientsList.add(currentClient)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        accountIcon.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}