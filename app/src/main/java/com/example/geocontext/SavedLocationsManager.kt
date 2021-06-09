package com.example.geocontext

import android.content.Context
import android.util.Log
import java.io.File

/** A singleton object for managing operations related to saved locations */
object SavedLocationsManager {

    private val savedFileName = "saved_locations"
    private val savedLocations = mutableListOf<Location>()


    fun loadSavedLocations(context: Context) {


        savedLocations.clear()


        val saveFile = File(context.filesDir, savedFileName)
        if(saveFile.exists()) {
            saveFile.readLines().forEach {
                val parts = it.split("|")
                val name = parts[0]
                val latitude = parts[1].toString().toDouble()
                val longitude = parts[2].toString().toDouble()
                savedLocations.add(Location(name, latitude, longitude))
            }
        }else saveFile.createNewFile()
    }

    /** Returns a list containing the currently known saved locations */
    fun getSavedLocations(): List<Location> {
        return savedLocations.toList()
    }
    /** Deletes a location from saved locations */
    fun deleteLocation(index: Int) {
        savedLocations.removeAt(index)
    }

    /** Adds a new location */
    fun addLocation(location: Location, context: Context) {
        savedLocations.add(location)
        saveLocations(context)
    }

    /** Saves all currently known saved locations */
    fun saveLocations(context: Context) {
        val locsAsStrings = savedLocations.map { "${it.name}|${it.latitude}|${it.longitude}" }
        val fileContents = locsAsStrings.joinToString(separator="\n")
        Log.i("GeoContext", "Locations to save ${fileContents}")
        val stream = context.openFileOutput(savedFileName, Context.MODE_PRIVATE)
        stream.write(fileContents.toByteArray())
        stream.close()
    }
}

