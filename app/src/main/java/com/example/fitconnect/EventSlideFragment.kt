package com.example.fitconnect

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private val colorTypesMap = mapOf(
    "Allenamento" to Color.RED,
    "Appuntamento" to Color.BLUE,
    "Check-Up" to Color.GREEN
)

class EventSlideFragment (var event: MyEvent, var currentClientUid: String, var isPt: Boolean): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.event_fragment,container,false)
        view.findViewById<TextView>(R.id.tv_titolo).text = event.titolo
        view.findViewById<TextView>(R.id.tv_tipologia).text = event.tipologia
        view.findViewById<TextView>(R.id.tv_tipologia).setTextColor(colorTypesMap[event.tipologia]!!)
        view.findViewById<TextView>(R.id.tv_desc).text = event.descrizione

        if(!isPt)
            view.findViewById<ImageView>(R.id.btn_delete).visibility = View.GONE
        else{
            view.findViewById<ImageView>(R.id.btn_delete).setOnClickListener {
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setMessage("Sei sicuro di voler eliminare l'evento?")
                    .setCancelable(false)
                    .setPositiveButton("Sì") { dialog, id ->
                        Firebase.database.reference.child("events").child(currentClientUid).child(event.eventId!!).setValue(null)
                    }
                    .setNegativeButton("No") { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = builder.create()
                alert.show()
            }
        }
        return view
    }
}