package com.example.fitconnect

data class WorkoutExercise(
    var workoutExerciseId: String? = null,
    var exerciseId: String? = null,
    var set: String? = null,
    var ripetizioni: String? = null,
    var peso: String? = null,
    var riposo: String? = null,
    var diffRate: Float? = null,
    var note: String? = null
){
    fun toMap(): Map<String,Any?>{
        // Metto solo gli attributi modificabili
        return mapOf(
            "workoutExerciseId" to workoutExerciseId,
            "exerciseId" to exerciseId,
            "set" to set,
            "ripetizioni" to ripetizioni,
            "peso" to peso,
            "riposo" to riposo
        )
    }
}
