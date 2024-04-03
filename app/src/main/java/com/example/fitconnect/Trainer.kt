package com.example.fitconnect

import java.util.Date

data class Trainer (var uid: String? = null,
                    var username: String? = null,
                    var email: String? = null,
                    var age: String? = null,
                    var birthDate: String? = null,
                    var weight: String? = null,
                    var height: String? = null){
    fun toMap(): Map<String,Any?>{
        // Metto solo gli attributi modificabili
        return mapOf(
            "age" to age,
            "birthDate" to birthDate,
            "weight" to weight,
            "height" to height
        )
    }
}