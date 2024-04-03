package com.example.fitconnect

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccountActivity : AppCompatActivity() {
    private lateinit var tvTitle: TextView
    private lateinit var navView: NavigationView
    private lateinit var headView: android.view.View
    private lateinit var arrowIcon: ImageView
    private lateinit var storageRef: StorageReference
    private lateinit var dbRef: DatabaseReference
    private lateinit var profileImage: ImageView
    private lateinit var btnAddImg: Button
    private lateinit var currentUserUid: String
    private lateinit var userImgName: String
    private lateinit var tvUsername: TextView
    private lateinit var btnLogout: Button
    private lateinit var etAge: TextView
    private lateinit var etBirth: TextView
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var btnSalva: Button
    private lateinit var btnShare: Button
    private lateinit var imgCalendar: ImageView
    private var isPT: Boolean = false

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        navView = findViewById(R.id.client_account_nav_view)
        headView = navView.getHeaderView(0)
        tvTitle = headView.findViewById(R.id.tv_title)
        tvTitle.text = "Profilo"
        arrowIcon = headView.findViewById(R.id.arrow_back)
        profileImage = findViewById(R.id.profile_image)
        btnAddImg = findViewById(R.id.btn_add_img)
        tvUsername = findViewById(R.id.tv_username)
        btnLogout = findViewById(R.id.btn_logout)
        etAge = findViewById(R.id.et_age)
        etBirth = findViewById(R.id.et_birth)
        etWeight = findViewById(R.id.et_weight)
        etHeight = findViewById(R.id.et_height)
        btnSalva = findViewById(R.id.btn_salva)
        btnShare = findViewById(R.id.btn_share)
        imgCalendar = findViewById(R.id.calendar_icon)

        storageRef = Firebase.storage.reference
        dbRef = Firebase.database.reference
        currentUserUid = Firebase.auth.currentUser?.uid!!
        userImgName = "profile_$currentUserUid"

        // Mettere lo username e gli altri campi precompilati
        dbRef.child("trainers").child(currentUserUid).get().addOnSuccessListener {
            if(it.exists()){
                val currentUser = it.getValue(Trainer::class.java)
                tvUsername.text = currentUser?.username
                Log.d("Firebase","Trainer trovato")
                if (currentUser?.age.toString() != "null") etAge.setText(currentUser?.age.toString()) else etAge.setHint("Età")
                if (currentUser?.height.toString() != "null") etHeight.setText(currentUser?.height.toString()) else etHeight.setHint("Altezza")
                if (currentUser?.weight.toString() != "null") etWeight.setText(currentUser?.weight.toString()) else etWeight.setHint("Peso")
                if (currentUser?.birthDate.toString() != "null") etBirth.setText(currentUser?.birthDate.toString()) else etBirth.setHint("Data")
                isPT = true
            }
        }

        dbRef.child("clients").child(currentUserUid).get().addOnSuccessListener {
            if(it.exists()){
                val currentUser = it.getValue(Client::class.java)
                tvUsername.text = currentUser?.username
                Log.d("Firebase","Client trovato")
                if (currentUser?.age.toString() != "null") etAge.setText(currentUser?.age.toString()) else etAge.setHint("Età")
                if (currentUser?.height.toString() != "null") etHeight.setText(currentUser?.height.toString()) else etHeight.setHint("Altezza")
                if (currentUser?.weight.toString() != "null") etWeight.setText(currentUser?.weight.toString()) else etWeight.setHint("Peso")
                if (currentUser?.birthDate.toString() != "null") etBirth.setText(currentUser?.birthDate.toString()) else etBirth.setHint("Data")
                btnShare.visibility = View.GONE
            }
        }

        // Carico subito l'immagine di profilo se c'è già
        storageRef.child("images/$userImgName").downloadUrl.addOnSuccessListener {
            Glide.with(this@AccountActivity).load(it).into(profileImage)
            Log.d("Firebase","Immagine trovata!")
        }.addOnFailureListener {
            Log.d("Firebase","Immagine del profilo non trovata")
        }

        arrowIcon.setOnClickListener {
            val intent = if(isPT){
                Intent(this, TrainerClientsListActivity::class.java)
            }else{
                Intent(this, HomeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        val imagePickerActivityResult: ActivityResultLauncher<Intent> =
            // Lambda expression per ricevere il risultato, qui riceviamo una singola foto della selezione
            registerForActivityResult( ActivityResultContracts.StartActivityForResult()) { result ->
                if (result != null) {
                    val imageUri: Uri? = result.data?.data

                    // Il nome dell'immagine è composto dall'uid dell'utente
                    val imageName = "profile_$currentUserUid"
                    imageUri?.let {
                        val uploadTask = storageRef.child("images/$imageName").putFile(it)

                        // Al successo, scarico l'immagine e la mostro
                        uploadTask.addOnSuccessListener {
                            storageRef.child("images/$imageName").downloadUrl.addOnSuccessListener {
                                Glide.with(this@AccountActivity)
                                    .load(it)
                                    .into(profileImage)
                                Log.d("Firebase", "Immagine scaricata")
                            }.addOnFailureListener {
                                Log.d("Firebase", "Download fallito")
                            }
                        }.addOnFailureListener {
                            Log.d("Firebase", "Upload fallito")
                        }
                    }
                }
            }

        btnAddImg.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            // Tipo dell'immagine
            galleryIntent.type = "image/*"
            imagePickerActivityResult.launch(galleryIntent)
        }

        imgCalendar.setOnClickListener {
            val myCalendar = Calendar.getInstance()

            // Imposto la data che c'è giù sul calendario
            if(etBirth.text.toString().trim().isNotBlank()){
                val splittedDate = etBirth.text.split("/")
                myCalendar.set(splittedDate[2].toInt(),splittedDate[1].toInt()-1,splittedDate[0].toInt())
            }

            val year = myCalendar.get(Calendar.YEAR)
            val month = myCalendar.get(Calendar.MONTH)
            val day = myCalendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(this, { view, year, month, dayOfMonth ->
                val selectedDate = "$dayOfMonth/${month + 1}/$year"
                etBirth.text = "$selectedDate"
                if(etBirth.text.toString().trim().isNotBlank()) {
                    val birthDate = etBirth.text.toString()
                    val calendar = Calendar.getInstance()
                    val curDate = dateFormat.format(calendar.time)
                    val splittedBirthDate = birthDate.split("/")
                    val splittedCurDate = curDate.split("/")

                    var age = splittedCurDate[2].toInt() - splittedBirthDate[2].toInt() // Come se avesse già compiuto gli anni quest'anno
                    if(splittedCurDate[1].toInt() < splittedBirthDate[1].toInt() || (splittedCurDate[1].toInt() == splittedBirthDate[1].toInt() && splittedCurDate[0].toInt() < splittedBirthDate[0].toInt()))
                        age--
                    etAge.text = age.toString()
                }
            }, year,month,day)
            // Metto la data minima impostabile a 10 anni indietro da adesso
            dialog.datePicker.maxDate = Calendar.getInstance().timeInMillis - 315576000000L
            dialog.show()
        }

        btnSalva.setOnClickListener {
            if(isPT){
                val newTrainer = Trainer(
                    age = if(etAge.text.isNotBlank()) etAge.text?.toString() else null,
                    height = if(etHeight.text.isNotBlank()) etHeight.text.toString() else null,
                    weight = if(etWeight.text.isNotBlank()) etWeight.text.toString() else null,
                    birthDate = if(etBirth.text.isNotBlank()) etBirth.text.toString() else null
                )
                dbRef.child("trainers").child(currentUserUid).updateChildren(newTrainer.toMap())
            }else{
                val newClient = Client(
                    age = if(etAge.text.isNotBlank()) etAge.text?.toString() else null,
                    height = if(etHeight.text.isNotBlank()) etHeight.text.toString() else null,
                    weight = if(etWeight.text.isNotBlank()) etWeight.text.toString() else null,
                    birthDate = if(etBirth.text.isNotBlank()) etBirth.text.toString() else null
                )
                dbRef.child("clients").child(currentUserUid).updateChildren(newClient.toMap())
            }

            val intent = if(isPT){
                Intent(this, TrainerClientsListActivity::class.java)
            }else{
                Intent(this, HomeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }

        btnShare.setOnClickListener {
            if (isPT){
                val codice = Firebase.auth.currentUser?.uid!!
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_TEXT, codice)
                intent.type = "text/plain"
                startActivity(intent)
            }
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}