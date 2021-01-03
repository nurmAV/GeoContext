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
class TimesAdapter(val data: MutableList<Pair<Double, Long>>) : RecyclerView.Adapter<TimesAdapter.ViewHolder>() {

    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val title = v.findViewById<TextView>(R.id.time_entry_title)
        val time = v.findViewById<TextView>(R.id.time_entry)


    }
    /* Create  a view holder for a list item */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.time_list_item, parent, false)
        return ViewHolder(view)
    }

    /* Set values to a single list item instance */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text =  " ${data[position].first} km"
        val minutes = data[position].second / 60
        val seconds = data[position].second % 60

        holder.time.text = minutes.toString() + ":" + String.format("%02d", seconds)
    }

    /* Return the number of elements in this adapter */
    override fun getItemCount(): Int {
        return data.size

    }
}