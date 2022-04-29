/**
 * Adapter for Attendance History Recyclerview
 * Author: Wong Jun Hao
 */
package com.example.a2007_hr_app.ui.attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a2007_hr_app.R
import com.example.a2007_hr_app.data.AttendanceData

class AttendanceAdapter(private val dataSet: ArrayList<AttendanceData>) :
    RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewLocation: TextView = view.findViewById(R.id.textViewLocationRecycler)
        val textViewDateIn: TextView = view.findViewById(R.id.textViewDateIn)
        val textViewDateOut: TextView = view.findViewById(R.id.textViewDateOut)

        init {
            //Nothing to see here
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attendance_recyclerview, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: AttendanceData = dataSet[position]
        holder.textViewLocation.text = data.location
        holder.textViewDateIn.text = data.datetime_in
        holder.textViewDateOut.text = data.datetime_out
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}