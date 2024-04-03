package com.example.fitconnect

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClientAdapter(val context: Context,var clientsList: ArrayList<Client>): RecyclerView.Adapter<ClientAdapter.ClientViewHolder> (){
    class ClientViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvClientName = itemView.findViewById<TextView>(R.id.tv_client_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.client_list_elem_layout,parent,false)
        return ClientViewHolder(view)
    }

    override fun getItemCount(): Int {
        return clientsList.size
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val currentClient = clientsList[position]
        holder.tvClientName.text = currentClient.username
        holder.itemView.setOnClickListener {
            val intent = Intent(context,HomeActivity::class.java)
            intent.putExtra("client_uid",currentClient.uid)
            context.startActivity(intent)
        }
    }
}