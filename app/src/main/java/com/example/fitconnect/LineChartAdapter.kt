package com.example.fitconnect

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LineChartAdapter (val context: Context, val lineChartPointsList: ArrayList<LineChartPoints>): RecyclerView.Adapter<LineChartAdapter.LineChartViewHolder>(){
    class LineChartViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvTitolo = itemView.findViewById<TextView>(R.id.tv_titolo)
        val gvLineChart = itemView.findViewById<GraphView>(R.id.line_chart)
        val btnPesoReps = itemView.findViewById<Button>(R.id.btn_peso_reps)
        val tvPesoReps = itemView.findViewById<TextView>(R.id.tv_peso_reps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineChartViewHolder {
        return LineChartViewHolder(LayoutInflater.from(context).inflate(R.layout.linechart_layout,parent,false))
    }

    override fun getItemCount(): Int {
        return lineChartPointsList.size
    }

    override fun onBindViewHolder(holder: LineChartViewHolder, position: Int) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val lineChartPoint = lineChartPointsList[position]
        holder.tvTitolo.text = lineChartPoint.exerciseName

        val pesiMassimiMap = lineChartPoint.pesiMassimiMap!!
        val sortedPesiMassimiMap = pesiMassimiMap.toSortedMap(compareBy { dateFormat.parse(it) })
        val ripetizioniComplessiveMap = lineChartPoint.ripetizioniComplessiveMassimeMap!!

        val xData = ArrayList<Double>() // Conterrà gli indici dell'array
        val xDataString = ArrayList<String>()
        val yWeightsData = ArrayList<Int>()
        val yRepsData = ArrayList<Int>()

        var counter = 1

        for (chiave in sortedPesiMassimiMap.keys){
            xData.add(counter.toDouble())
            counter++
            xDataString.add(chiave)
            yWeightsData.add(pesiMassimiMap[chiave]!!)
            yRepsData.add(ripetizioniComplessiveMap[chiave]!!)
        }

        val dataPoints: Array<DataPoint?> = arrayOfNulls(xData.size)

        Log.d("mytag","Es: ${lineChartPoint.exerciseName}")
        for(i in xData.indices){
            dataPoints[i] = DataPoint(xData[i],yWeightsData[i].toDouble())
        }

        val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataPoints)
        series.isDrawDataPoints = true

        val maxItemsOnX = 10 // Numero massimo di item visualizzabili nel grafico

        holder.gvLineChart.gridLabelRenderer.numHorizontalLabels = if(xData.size < maxItemsOnX) xData.size else maxItemsOnX

        holder.gvLineChart.gridLabelRenderer.textSize = 28F

        val firstIndex = if(xData.size > maxItemsOnX) xData.size-maxItemsOnX else 0
        holder.gvLineChart.viewport.isXAxisBoundsManual = true
        holder.gvLineChart.viewport.setMinX(xData[firstIndex])
        holder.gvLineChart.viewport.setMaxX(xData[xData.size-1])

        holder.gvLineChart.gridLabelRenderer.padding = 80
        holder.gvLineChart.gridLabelRenderer.setHorizontalLabelsAngle(135)

        val maxValue = yWeightsData.max()

        setYIntegerLabels(maxValue, holder)

        holder.gvLineChart.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    xDataString[value.toInt()-1]
                } else {
                    value.toInt().toString()
                }
            }
        }

        holder.gvLineChart.addSeries(series)

        // Per cambiare dati visualizzati
        holder.btnPesoReps.setOnClickListener {
            holder.gvLineChart.removeAllSeries()
            if(holder.btnPesoReps.text == "Visualizza ripetizioni"){
                holder.btnPesoReps.text = "Visualizza pesi"
                holder.tvPesoReps.text = "Ripetizioni totali"

                for(i in xData.indices){
                    dataPoints[i] = DataPoint(xData[i],yRepsData[i].toDouble())
                }

                setYIntegerLabels(yRepsData.max(),holder)

                val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataPoints)
                series.isDrawDataPoints = true

                holder.gvLineChart.addSeries(series)
            }else{
                holder.btnPesoReps.text = "Visualizza ripetizioni"
                holder.tvPesoReps.text = "Peso in Kg"

                for(i in xData.indices){
                    dataPoints[i] = DataPoint(xData[i],yWeightsData[i].toDouble())
                }

                setYIntegerLabels(yWeightsData.max(),holder)

                val series: LineGraphSeries<DataPoint> = LineGraphSeries(dataPoints)
                series.isDrawDataPoints = true

                holder.gvLineChart.addSeries(series)
            }
        }
    }

    private fun setYIntegerLabels(maxValue: Int, holder: LineChartViewHolder) {
        // L'intervallo varia in base al valore massimo
        val interval = if (maxValue <= 55) {
            5
        } else if (maxValue <= 110) {
            10
        } else {
            20
        }
        // Il massimo del grafico deve essere un multiplo di interval
        var maxLabel = maxValue
        while (maxLabel % interval != 0) {
            maxLabel++
        }

        holder.gvLineChart.viewport.isYAxisBoundsManual = true
        holder.gvLineChart.viewport.setMinY(0.0)
        holder.gvLineChart.viewport.setMaxY(maxLabel.toDouble())
        holder.gvLineChart.gridLabelRenderer.numVerticalLabels = maxLabel / interval + 1
    }
}