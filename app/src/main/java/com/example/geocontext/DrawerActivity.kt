package com.example.geocontext

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.activity_drawer.*

lateinit var toggle: ActionBarDrawerToggle

class DrawerActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawer)


        toggle = ActionBarDrawerToggle(this, drawer_layout, 0, 0 )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val settingsFragment = SettingsFragment()
        val mainFragment = MainFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, mainFragment).commit()

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.location_view -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, mainFragment).commit()
                    // Toast.makeText(this, "Item 1", Toast.LENGTH_SHORT).show()
                R.id.settings ->
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container_view, settingsFragment).commit()
            //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
            }
            true
        }


    }
}