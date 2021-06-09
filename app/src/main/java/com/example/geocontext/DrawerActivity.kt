package com.example.geocontext

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_drawer.*
import java.io.File

lateinit var toggle: ActionBarDrawerToggle

class DrawerActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)


        toggle = ActionBarDrawerToggle(this, drawer_layout, 0, 0 )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        SavedLocationsManager.loadSavedLocations(this)
        Log.i("GeoContext", "SavedLocationsManager: " + SavedLocationsManager.getSavedLocations().toString())
        val preferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if(!preferences.contains("fast_interval") || !preferences.contains("max_interval")) {
            preferences.edit().putInt("fast_interval", 1000).putInt("max_interval", 5000)
        }
        if(!preferences.contains("distance_unit")) {
            preferences.edit().putString("distance_unit", "kilometer")
        }

        val settingsFragment = SettingsFragment()
        val mainFragment = MainFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, mainFragment).commit()

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.location_view -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, mainFragment).commit()

                R.id.settings ->
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, settingsFragment).commit()

            }
            true
        }

        /*
        val file = File(applicationContext.filesDir, "saved_locations")

        if(!file.exists()) {
            file.createNewFile()
        }
        */
    }
}