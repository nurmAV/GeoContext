package com.example.geocontext

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


/**
 * Represents an adapter for a intermediate times list and a RecyclerView
 *
 * @param data the intermediate times as a list
 */
class LocationAdapter(val data: MutableList<Location>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val name = v.findViewById<TextView>(R.id.location_name)
        val longitude = v.findViewById<TextView>(R.id.location_longitude)
        val latitude = v.findViewById<TextView>(R.id.location_latitude)

    }

    /* Create  a view holder for a list item */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_list_item, parent, false)
        return LocationAdapter.ViewHolder(view)
    }

    /* Set values to a single list item instance */
    override fun onBindViewHolder(holder: LocationAdapter.ViewHolder, position: Int) {
        holder.name.text = data[position].name
        holder.latitude.text = "%.5f".format(data[position].latitude)
        holder.longitude.text = "%.5f".format(data[position].longitude)

    }
    /* Return the number of elements in this adapter */
    override fun getItemCount(): Int {
        return data.size

    }
}