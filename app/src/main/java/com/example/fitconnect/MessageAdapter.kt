package com.example.fitconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val MESSAGE_SENT = 1
    val MESSAGE_RECEIVED = 2

    class sentMessageViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val sentMessage = itemView.findViewById<TextView>(R.id.tv_sent_message)
    }
    class receivedMessageViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        val receivedMessage = itemView.findViewById<TextView>(R.id.tv_received_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == MESSAGE_SENT){
            val view = LayoutInflater.from(context).inflate(R.layout.sent_message,parent,false)
            sentMessageViewHolder(view)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.received_message,parent,false)
            receivedMessageViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if(currentMessage.senderId == Firebase.auth.currentUser?.uid){
            MESSAGE_SENT
        }else{
            MESSAGE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if(holder.javaClass == sentMessageViewHolder::class.java){
            var viewHolder = holder as sentMessageViewHolder
            viewHolder.sentMessage.text = currentMessage.text
        }else{
            var viewHolder = holder as receivedMessageViewHolder
            viewHolder.receivedMessage.text = currentMessage.text
        }
    }
}