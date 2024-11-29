package com.example.firebaseauth.activity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.view.View
import com.example.firebaseauth.R



class GeofenceAdapter(private val geofences: MutableList<String>) : RecyclerView.Adapter<GeofenceAdapter.GeofenceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeofenceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_geofence, parent, false)
        return GeofenceViewHolder(view)
    }

    override fun onBindViewHolder(holder: GeofenceViewHolder, position: Int) {
        holder.bind(geofences[position])
    }

    override fun getItemCount(): Int = geofences.size

    fun addGeofence(geofence: String) {
        geofences.add(geofence)
        notifyDataSetChanged()
    }

    class GeofenceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val geofenceText: TextView = itemView.findViewById(R.id.geofenceText)

        fun bind(geofence: String) {
            geofenceText.text = geofence
        }
    }
}
