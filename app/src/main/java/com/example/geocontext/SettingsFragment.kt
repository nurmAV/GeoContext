package com.example.geocontext

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    lateinit var saveButton: Button
    lateinit var fastestIntervalInput: EditText
    lateinit var maxIntervalInput: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        val preferences = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if(!preferences!!.contains("fast_interval") || !preferences?.contains("max_interval"))
        preferences?.edit()?.putInt("fast_interval", 5000)?.putInt("max_interval", 5000)?.commit()

         */


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Shared preferences for persisting settings
        val preferences = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        fastestIntervalInput = view!!.findViewById(R.id.fast_interval)
        maxIntervalInput = view!!.findViewById(R.id.max_interval)

        val fastInterval = preferences?.getInt("fast_interval", 5).toString()
        val maxInterval = preferences?.getInt("max_interval", 5).toString()

        fastestIntervalInput.setText(fastInterval)
        maxIntervalInput.setText(maxInterval)

        // Save button and event listener
        saveButton = view!!.findViewById(R.id.save_settings)
        saveButton.setOnClickListener {

            preferences?.edit()
                    ?.putInt("fast_interval", fastestIntervalInput.text.toString().toInt() * 1000)
                    ?.putInt("max_interval", max_interval.text.toString().toInt() * 1000)
                    ?.apply()
            Toast.makeText(context, "Saved settings", Toast.LENGTH_SHORT).show()

        }

        return view

    }



}