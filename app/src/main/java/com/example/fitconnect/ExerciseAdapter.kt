package com.example.fitconnect

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ExerciseAdapter (var context: Context, var exerciseList: ArrayList<WorkoutExercise>, var currentClientUid: String, var currentTemplate: String): RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>(){
    class ExerciseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvExName = itemView.findViewById<TextView>(R.id.tv_ex_name)
        val tvSet = itemView.findViewById<TextView>(R.id.tv_set)
        val tvReps = itemView.findViewById<TextView>(R.id.tv_reps)
        val tvWeight = itemView.findViewById<TextView>(R.id.tv_weight)
        val tvRest = itemView.findViewById<TextView>(R.id.tv_rest)
        val btnModify = itemView.findViewById<Button>(R.id.btn_modify)
        val btnNotes = itemView.findViewById<Button>(R.id.btn_notes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.exercise_layout,parent,false)
        return ExerciseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return exerciseList.size
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val currentWorkoutExercise = exerciseList[position]
        var currentExercise: Exercise? = null
        Firebase.database.reference.child("exercises").child(currentWorkoutExercise.exerciseId!!).get().addOnSuccessListener {
            currentExercise = it.getValue(Exercise::class.java)
            holder.tvExName.text = currentExercise?.tipologia.toString()
        }

        holder.tvSet.text = currentWorkoutExercise.set
        holder.tvReps.text = currentWorkoutExercise.ripetizioni
        holder.tvWeight.text = currentWorkoutExercise.peso+"Kg"
        holder.tvRest.text = currentWorkoutExercise.riposo

        val dialog = Dialog(context)
        holder.btnNotes.setOnClickListener {
            dialog.setContentView(R.layout.notes_dialog)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)
            val tvNote = dialog.findViewById<TextView>(R.id.tv_note)
            val rbDifficulty = dialog.findViewById<RatingBar>(R.id.rb_difficulty)
            val tvClose = dialog.findViewById<TextView>(R.id.tv_close)
            // Se sono null non faccio niente, lascio i default
            currentWorkoutExercise.note?.let {
                tvNote.text = it
            }
            currentWorkoutExercise.diffRate?.let {
                rbDifficulty.rating = it
            }
            tvClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        holder.btnModify.setOnClickListener {
            val intent = Intent(context,TrainerAddModExerciseActivity::class.java)
            intent.putExtra("client_uid",currentClientUid)
            intent.putExtra("current_template",currentTemplate)
            intent.putExtra("isAdd",false)
            intent.putExtra("workout_id",currentWorkoutExercise.workoutExerciseId)
            intent.putExtra("isLast", exerciseList.size==1)

            intent.putExtra("fascia",currentExercise?.fascia.toString())
            intent.putExtra("tipologia",currentExercise?.tipologia.toString())
            intent.putExtra("set",currentWorkoutExercise.set)
            intent.putExtra("ripetizioni",currentWorkoutExercise.ripetizioni)
            intent.putExtra("peso",currentWorkoutExercise.peso)
            intent.putExtra("riposo",currentWorkoutExercise.riposo)
            context.startActivity(intent)
        }
    }
}