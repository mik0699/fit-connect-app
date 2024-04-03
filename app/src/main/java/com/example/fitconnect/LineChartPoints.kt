package com.example.fitconnect

data class LineChartPoints(
    var lineChartPointsId: String? = null,
    var exerciseName: String? = null,
    var pesiMassimiMap: HashMap<String,Int>? = null,
    var ripetizioniComplessiveMassimeMap: HashMap<String,Int>? = null
    )
