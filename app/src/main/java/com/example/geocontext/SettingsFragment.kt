package com.example.geocontext

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    lateinit var saveButton: Button
    lateinit var fastestIntervalInput: EditText
    lateinit var maxIntervalInput: EditText
    lateinit var distanceUnitsSpinner: Spinner



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Shared preferences for persisting settings
        val preferences = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        distanceUnitsSpinner = view!!.findViewById(R.id.distance_units)
        fastestIntervalInput = view!!.findViewById(R.id.fast_interval)
        maxIntervalInput = view!!.findViewById(R.id.max_interval)

        var selectedDistanceUnit = preferences?.getString("distance_unit", "kilometer")
        val fastInterval = preferences?.getInt("fast_interval", 5000)?.div(1000.0)
        val maxInterval = preferences?.getInt("max_interval", 5000)?.div(1000.0)


        val spinnerAdapater = ArrayAdapter.createFromResource(activity!!, R.array.distance_units_array, R.layout.support_simple_spinner_dropdown_item)
                .also {
                    arrayAdapter ->
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                }
        distanceUnitsSpinner.adapter = spinnerAdapater

        selectedDistanceUnit = when(selectedDistanceUnit) {
            "kilometer" -> "Kilometers"
            "mile" -> "Miles"
            "nautical mile" -> "Nautical miles"
            else -> "Kilometers"
        }
        distanceUnitsSpinner.setSelection( spinnerAdapater.getPosition(selectedDistanceUnit))

        fastestIntervalInput.setText("%.2f".format(fastInterval))
        maxIntervalInput.setText("%.2f".format(maxInterval))

        // Save button and event listener
        saveButton = view!!.findViewById(R.id.save_settings)
        saveButton.setOnClickListener {

            val distanceUnit = when(distanceUnitsSpinner.selectedItem) {
                "Kilometers" -> "kilometer"
                "Miles" -> "mile"
                "Nautical miles" -> "nautical mile"
                else -> "kilometer"
            }
            preferences?.edit()
                    ?.putInt("fast_interval", (fastestIntervalInput.text.toString().toDouble() * 1000).toInt())
                    ?.putInt("max_interval", (max_interval.text.toString().toDouble() * 1000).toInt())

                    ?.putString("distance_unit", distanceUnit)
                    ?.apply()
            Toast.makeText(context, "Saved settings", Toast.LENGTH_SHORT).show()

        }




        return view

    }



}