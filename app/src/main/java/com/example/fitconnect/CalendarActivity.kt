package com.example.fitconnect

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import me.relex.circleindicator.CircleIndicator3
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CalendarActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var addEventIcon: ImageView
    private lateinit var calendar: CompactCalendarView
    private lateinit var tvMonth: TextView
    private lateinit var currentSelectedDate: Date
    private lateinit var dbRef: DatabaseReference
    private lateinit var myEventsList: ArrayList<MyEvent>
    private lateinit var viewPager: ViewPager2
    private lateinit var dailyEventsList: ArrayList<MyEvent>
    private lateinit var circleIndicator: CircleIndicator3

    private var isPt = false
    private var currentClientUid: String? = null

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val monthsNumbersToString = mapOf(
        1 to "Gennaio",
        2 to "Febbraio",
        3 to "Marzo",
        4 to "Aprile",
        5 to "Maggio",
        6 to "Giugno",
        7 to "Luglio",
        8 to "Agosto",
        9 to "Settembre",
        10 to "Ottobre",
        11 to "Novembre",
        12 to "Dicembre"
    )

    private val colorTypesMap = mapOf(
        "Allenamento" to Color.RED,
        "Appuntamento" to Color.BLUE,
        "Check-Up" to Color.GREEN
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        navView = findViewById(R.id.client_calendar_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Calendario"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        addEventIcon = headView.findViewById(R.id.add_icon)
        dbRef = Firebase.database.reference
        myEventsList = ArrayList()
        dailyEventsList = ArrayList()
        circleIndicator = findViewById(R.id.circle_indicator)

        isPt = intent.getBooleanExtra("isPt",false)
        // Se isPt è true è il client uid, altrimenti è l'uid del cliente attuale
        currentClientUid = intent.getStringExtra("client_uid")

        if(!isPt)
            addEventIcon.isVisible = false

        calendar = findViewById(R.id.compactcalendar_view)
        tvMonth = findViewById(R.id.tv_month)
        viewPager = findViewById(R.id.view_pager)

        val current = LocalDate.now()
        calendar.setLocale(TimeZone.getDefault(),Locale.ITALIAN)
        calendar.setUseThreeLetterAbbreviation(true)

        currentClientUid?.let {
            dbRef.child("events").child(it).addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    calendar.removeAllEvents()
                    myEventsList.clear()
                    for (snap in snapshot.children){
                        val currentEvent = snap.getValue(MyEvent::class.java)
                        val eventDateInMillis = dateFormat.parse(currentEvent?.data).time
                        val newEvent = Event(colorTypesMap[currentEvent?.tipologia]!!,eventDateInMillis)
                        calendar.addEvent(newEvent)
                        currentEvent?.let {
                            myEventsList.add(it)
                        }
                    }
                    // All'inizio la data selezionata è quella corrente
                    currentSelectedDate = Date(current.year-1900,current.monthValue-1,current.dayOfMonth)
                    calendar.setCurrentDate(currentSelectedDate)
                    showCurrentDateEvents(currentSelectedDate)
                    tvMonth.text = "${monthsNumbersToString[current.monthValue]} ${current.year}"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        calendar.setListener(object: CompactCalendarView.CompactCalendarViewListener{
            override fun onDayClick(dateClicked: Date?) {
                dateClicked?.let {
                    showCurrentDateEvents(it)
                }

            }

            override fun onMonthScroll(firstDayOfNewMonth: Date?) {
                firstDayOfNewMonth?.let {
                    currentSelectedDate = it
                    showCurrentDateEvents(it)
                }
                tvMonth.text = "${monthsNumbersToString[firstDayOfNewMonth?.month!!+1]} ${firstDayOfNewMonth?.year!! + 1900}"
            }
        })

        addEventIcon.setOnClickListener {
            val intent = Intent(this,TrainerAddEventActivity::class.java)
            intent.putExtra("current_date",currentSelectedDate.time)
            intent.putExtra("client_uid",currentClientUid)
            startActivity(intent)
            finish()
        }

        arrowIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            startActivity(intent)
            finish()
        }
    }

    private fun showCurrentDateEvents(currentDate: Date){
        var found = false
        dailyEventsList.clear()
        Log.d("mytag","${dateFormat.format(currentDate)}")

        currentSelectedDate = currentDate
        for (event in myEventsList) {
            if (event.data == dateFormat.format(currentDate)) {
                dailyEventsList.add(event)
                found = true
            }
        }

        if (found) {
            val adapter = EventSlideAdapter(this@CalendarActivity, dailyEventsList, dailyEventsList.size)
            viewPager.adapter = adapter
            circleIndicator.setViewPager(viewPager)
            adapter.registerAdapterDataObserver(circleIndicator.adapterDataObserver)
        } else {
            val adapter = EventSlideAdapter(this@CalendarActivity, ArrayList<MyEvent>(), 0)
            viewPager.adapter = adapter
            circleIndicator.setViewPager(viewPager)
            adapter.registerAdapterDataObserver(circleIndicator.adapterDataObserver)
        }
    }

    private inner class EventSlideAdapter(fa: FragmentActivity, var eventsList: ArrayList<MyEvent>,var numPages: Int): FragmentStateAdapter(fa){
        override fun getItemCount(): Int {
            return numPages
        }

        override fun createFragment(position: Int): Fragment {
            Log.d("mytag", "$position")
            val currEv = eventsList[position]
            return EventSlideFragment(currEv, currentClientUid!!,isPt)
        }
    }
}

