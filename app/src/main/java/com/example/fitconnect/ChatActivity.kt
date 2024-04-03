package com.example.fitconnect

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

class ChatActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    var isPt = false
    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var currentClientUid: String? = null
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatRv: RecyclerView
    private lateinit var sendIcon: ImageView
    private lateinit var etMessage: EditText
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        navView = findViewById(R.id.chat_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Chat"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        sendIcon = findViewById(R.id.send_icon)
        etMessage = findViewById(R.id.et_message)

        messageList = ArrayList()
        chatRv = findViewById(R.id.chat_rv)
        messageAdapter = MessageAdapter(this,messageList)
        chatRv.adapter = messageAdapter
        chatRv.layoutManager = LinearLayoutManager(this)

        auth = Firebase.auth
        dbRef = Firebase.database.reference

        dbRef.child("clients").child(auth.currentUser?.uid!!).get().addOnSuccessListener {
            if(it.exists()){
                isPt = false
                val currentClient = it.getValue(Client::class.java)
                senderRoom = auth.currentUser?.uid!! + currentClient?.ptCode
                receiverRoom = currentClient?.ptCode + auth.currentUser?.uid!!
            }else{
                isPt = true
                currentClientUid = intent.getStringExtra("client_uid")
                senderRoom = auth.currentUser?.uid!! + currentClientUid
                receiverRoom = currentClientUid + auth.currentUser?.uid!!
            }
            dbRef.child("chat").child(senderRoom).child("messages").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for(messageSnapshot in snapshot.children){
                        val currentMessage = messageSnapshot.getValue(Message::class.java)
                        messageList.add(currentMessage!!)
                    }
                    messageAdapter.notifyDataSetChanged()
                    chatRv.scrollToPosition(messageList.size-1)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        sendIcon.isEnabled = false // Di default disabilitata

        // Quando si apre la tastiera la chat scrolla all'ulitmo messaggio
        chatRv.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom){
                chatRv.post { chatRv.scrollToPosition(messageList.size - 1) }
            }
        }

        etMessage.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Prima dell'implementazione
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendIcon.isEnabled = true
                sendIcon.isClickable = s.toString().trim() != ""
                Log.d("mytag","click: ${sendIcon.isClickable}")
            }

            override fun afterTextChanged(s: Editable?) {
                // Dopo l'implementazione
            }
        })

        Log.d("mytag","isclickable: ${sendIcon.isClickable}")
        sendIcon.setOnClickListener {
            val messageText = etMessage.text.toString()
            val message = Message(messageText,auth.currentUser?.uid!!)

            dbRef.child("chat").child(senderRoom).child("messages").push().setValue(message).addOnSuccessListener {
                dbRef.child("chat").child(receiverRoom).child("messages").push().setValue(message)
            }
            etMessage.setText("")
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            if(isPt) intent.putExtra("client_uid",currentClientUid)
            startActivity(intent)
            finish()
        }
    }
}