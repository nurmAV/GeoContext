package com.example.geocontext

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlin.NumberFormatException


class SettingsFragment : Fragment(R.layout.fragment_settings) {


    // UI elements
    lateinit var saveButton: Button
    lateinit var fastestIntervalInput: EditText
    lateinit var maxIntervalInput: EditText
    lateinit var distanceUnitsSpinner: Spinner
    lateinit var savedLocationsRecycler: RecyclerView
    lateinit var addLocationButton: Button

    // Saved locations
    lateinit var savedLocations: List<Location>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Shared preferences for persisting settings
        val preferences = activity?.getSharedPreferences("settings", Context.MODE_PRIVATE)

        val view = super.onCreateView(inflater, container, savedInstanceState)

        distanceUnitsSpinner = view!!.findViewById(R.id.distance_units)
        fastestIntervalInput = view!!.findViewById(R.id.fast_interval)
        maxIntervalInput = view!!.findViewById(R.id.max_interval)
        savedLocationsRecycler = view!!.findViewById(R.id.saved_locations_recycler)
        savedLocationsRecycler.layoutManager = LinearLayoutManager(context)
        addLocationButton = view.findViewById(R.id.add_location_button)
        //val data = mutableListOf(Location("Home", 60.2486 ,24.7634))
        //savedLocations = mutableListOf<Location>()

        savedLocations = SavedLocationsManager.getSavedLocations()
        savedLocationsRecycler.adapter = LocationAdapter(savedLocations)

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

        addLocationButton.setOnClickListener {
            val view = LayoutInflater.from(activity as Context).inflate(R.layout.save_location_input, null, false)
            val nameInput = view.findViewById<EditText>(R.id.location_name_input)
            val latitudeInput = view.findViewById<EditText>(R.id.save_location_latitude_input)
            val longitudeInput = view.findViewById<EditText>(R.id.save_location_longitude_input)

            val builder = AlertDialog.Builder(activity as Context)

            builder.setPositiveButton("Add location", DialogInterface.OnClickListener { _,i ->
                try {
                    val name = nameInput.text.toString()
                    val latitude = latitudeInput.text.toString().toDouble()
                    val longitude = longitudeInput.text.toString().toDouble()
                    if(latitude >= -90 && latitude <= 90
                        && longitude >= -180 && longitude <= 180
                        && name != "") {


                        SavedLocationsManager.addLocation(
                            Location(name, latitude, longitude),
                            context!!
                        )
                        Toast.makeText(
                            context,
                            "Added a new location \"$name\"",
                            Toast.LENGTH_SHORT
                        ).show()
                        savedLocationsRecycler.adapter?.notifyDataSetChanged()
                    }else {
                        Toast.makeText(context, "Invalid latitude and/or longitude", Toast.LENGTH_SHORT).show()
                    }
                }catch (e: NumberFormatException) {
                    Toast.makeText(context, "Invalid latitude and/or longitude", Toast.LENGTH_SHORT ).show()
                }
            })
            builder.setTitle("Add a new location").setView(view).create().show()

        }




        return view

    }

    override fun onResume() {
        super.onResume()
        SavedLocationsManager.loadSavedLocations(context!!)
        savedLocations = SavedLocationsManager.getSavedLocations()
        savedLocationsRecycler.adapter!!.notifyDataSetChanged()


    }




}