package com.example.geocontext

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.hardware.SensorManager.SENSOR_DELAY_UI
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.*

class MainActivity : AppCompatActivity(), SensorEventListener {

    // User interface
    lateinit var locationClient: FusedLocationProviderClient

    lateinit var locationButton: Button
    lateinit var distanceButton: Button
    lateinit var useCurrentLocation: Button

    lateinit var locationResult: TextView
    lateinit var distanceResult: TextView
    lateinit var altitudeResult: TextView

    lateinit var latitudeInput: EditText
    lateinit var longitudeInput: EditText

    lateinit var distanceTrackingButton: Button
    lateinit var distanceTrackingResult: TextView
    lateinit var velocityResult: TextView



    // Location and distance tracking
    var currentLocation: Location? = null
    var locationTimestamp = System.currentTimeMillis()

    var isTracking = false
    var distanceTravelled = 0.0

    // Direction tracking
    var manager: SensorManager? = null
    var magnetometer: Sensor? = null
    var accelerometer: Sensor? = null

    var magneticField = FloatArray(3)
    var acceleration = FloatArray(3)

    var directionTracking = false

    // Intermediate times
    var initialTimestamp: Long = 0
    var mostRecentTimestamp: Long = 0
    var kilometerTimes = mutableListOf<Pair<Double, Long>>()
    val timesAdapter = TimesAdapter(kilometerTimes)

    // Altitude tracking
    var initialAltitude = 0.0
    var maxAltitude = 0.0
    var minAltitude  = 0.0
    var currentAltitude = 0.0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* UI elements */
        locationButton = findViewById(R.id.location_button)
        distanceButton = findViewById(R.id.distance_button)

        locationResult = findViewById(R.id.location_result)
        distanceResult = findViewById(R.id.distance_result)
        altitudeResult = findViewById(R.id.altitude)

        latitudeInput = findViewById(R.id.latitude_input)
        longitudeInput = findViewById(R.id.longitude_input)
        useCurrentLocation = findViewById(R.id.this_location)

        distanceTrackingButton = findViewById(R.id.distance_tracking)
        distanceTrackingResult = findViewById(R.id.distance_tracking_result)
        distanceTrackingResult.text = "0 km"

        velocityResult = findViewById(R.id.velocity)


        /* Check if previous location info was found */
        if(savedInstanceState?.keySet()?.contains("location") == true) locationResult.text = savedInstanceState?.getString("location")
        if(savedInstanceState?.keySet()?.contains("altitude") == true) altitudeResult.text = savedInstanceState?.getDouble("altitude").toString() + " m"
        if(savedInstanceState?.keySet()?.contains("distanceTravelled") == true) distanceTrackingResult.text = savedInstanceState?.getString("distanceTravelled")


        /* Location  service client*/
        locationClient = LocationServices.getFusedLocationProviderClient(this)



        var locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if(p0 != null) {
                    for(location in p0.locations) {
                        //Log.i("GeoContext", "Location update received: ${location.latitude}, ${location.longitude}")

                        val latitudeSuffix = if(location.latitude < 0) "S" else "N"
                        val longitudeSuffix = if(location.longitude < 0) "W" else "E"

                        // Update shown location
                        locationResult.text = "${abs(location.latitude)} °$latitudeSuffix, ${abs(location.longitude)} °$longitudeSuffix"
                        altitudeResult.text = "${"%.2f m".format(location.altitude)}"

                        // Altitude difference
                        if(location.altitude > maxAltitude) maxAltitude = location.altitude
                        else if(location.altitude < minAltitude) minAltitude = location.altitude

                        currentAltitude = location.altitude

                        if(isTracking) {
                            // Compute and update the distance travelled
                            val dist = distance(location, currentLocation)
                            distanceTravelled += dist
                            distanceTrackingResult.text = "${"%.3f".format(distanceTravelled)} km"

                            val currentTime = System.currentTimeMillis()
                            val timeDelta = (currentTime - locationTimestamp) / (3600 * 1000.0)
                            velocityResult.text = "${"%.2f".format(dist / timeDelta)} km/h"
                            locationTimestamp = currentTime

                            // Update intermediate times if necessary
                            if( distanceTravelled >= kilometerTimes.size) {

                                kilometerTimes.add(Pair(distanceTravelled, (currentTime - mostRecentTimestamp) / 1000))
                                timesAdapter.notifyDataSetChanged()
                                mostRecentTimestamp = currentTime

                            }
                        }
                        currentLocation = location

                        if(directionTracking) {
                            var location = Location(currentLocation)
                            location.longitude = longitudeInput.text.toString().toDouble()
                            location.latitude = latitudeInput.text.toString().toDouble()
                            if(currentLocation != null) {
                            distanceResult.text = "%.2f km".format(currentLocation!!.distanceTo(location) / 1000)
                            }
                        }



                    }
                }
            }

        }


        /* Event listeners and handlers */

        useCurrentLocation.setOnClickListener{
            run {
                latitudeInput.setText(currentLocation?.latitude.toString())
                longitudeInput.setText(currentLocation?.longitude.toString())
            }
        }

        distanceTrackingButton.setOnClickListener { run{
            if(isTracking) {

                kilometerTimes.add(Pair(distanceTravelled, (System.currentTimeMillis() - mostRecentTimestamp) / 1000))


                kilometerTimes = mutableListOf()
                timesAdapter.notifyDataSetChanged()
                val view = LayoutInflater.from(this).inflate(R.layout.times_layout, null, false)
                val recycler = view.findViewById<RecyclerView>(R.id.times_list)
                recycler.layoutManager = LinearLayoutManager(this)
                recycler.adapter = timesAdapter
                view.findViewById<TextView>(R.id.max_altitude_difference).text = (maxAltitude - minAltitude).toString() + " m"
                view.findViewById<TextView>(R.id.net_altitude_diffence).text = (currentAltitude - initialAltitude).toString() + " m"



                Log.i("GeoContext", "Times:" + timesAdapter.data.toString())
                AlertDialog.Builder(this).setTitle("Kilometer times").setView(view).show()
                distanceTrackingButton.setBackgroundColor(Color.rgb(0x00, 0x77, 0x00))
                distanceTrackingButton.text = "Start tracking"
                distanceTravelled = 0.0
                initialTimestamp = 0
            }
            else {
                distanceTrackingButton.text = "Stop tracking"

                // Intermediate times
                initialTimestamp = System.currentTimeMillis()
                mostRecentTimestamp = initialTimestamp

                // Altitudes
                initialAltitude = currentAltitude
                maxAltitude = initialAltitude
                minAltitude = initialAltitude

                kilometerTimes.add(Pair(0.0, 0))
                timesAdapter.notifyDataSetChanged()
                distanceTrackingButton.setBackgroundColor(Color.rgb(0xBC, 0x00, 0x01))

            }
            isTracking = !isTracking

        }}
        locationButton.setOnClickListener { run{
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(
                    listOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        .toTypedArray(), 0)
            }
            locationClient.requestLocationUpdates(LocationRequest.create().setInterval(5 *1000).setFastestInterval(5*1000).setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY), locationCallback, Looper.getMainLooper())
            locationClient.lastLocation.addOnSuccessListener {
                    location: Location? ->
                run {
                    if (location != null) {
                        currentLocation = location
                        val latitudeSuffix = if(location.latitude < 0) "S" else "N"
                        val longitudeSuffix = if(location.longitude < 0) "W" else "E"

                        locationResult.text = "${abs(location.latitude)} °$latitudeSuffix, ${abs(location.longitude)} °$longitudeSuffix"
                        altitudeResult.text = "${"%.2f m".format(location.altitude)}"
                    }
                }
                }
            }

            distanceButton.setOnClickListener{ run{
                    val radians = PI / 180
                    val lat = latitudeInput.text.toString().toDouble()
                    val long = longitudeInput.text.toString().toDouble()

                    locationClient.lastLocation.addOnSuccessListener { location -> run {

                        val l = Location(currentLocation)
                        l.latitude = lat
                        l.longitude  = long
                        val distance = "%.2f".format(distance(currentLocation, l))
                        distanceResult.text = "$distance km"

                        }

                        if(!directionTracking) {
                            distanceButton.setBackgroundColor(Color.rgb(0xBC, 0x00, 0x00))
                            manager?.registerListener(this, magnetometer, SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI)
                            manager?.registerListener(this, accelerometer, SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI)
                        }
                        else {
                            manager?.unregisterListener(this)
                            distanceButton.setBackgroundColor(Color.rgb(0x00, 0x77, 0x00))
                        }


                        directionTracking = !directionTracking
                    }
                }
            }
        }


    }

    fun haversine(alpha: Double): Double {
        val sine = sin(alpha / 2)
        return sine * sine
    }

    /**
     * Computes the great-circle distance between two points
     * @param departure The starting location
     * @param destination The ending location
     * @return The distance between the starting and ending location along a great circle path
     */
    fun distance(departure: Location?, destination: Location?, radius: Int = 6371): Double {
        if(departure == null || destination == null) {
            return 0.0
        }

            val departureLat = departure.latitude
            val departureLong = departure.longitude

            val destinationLat = destination.latitude
            val destinationLong = destination.longitude

            val radian = PI / 180
            val latAngle = (destinationLat - departureLat) * radian
            val longAngle = (destinationLong - departureLong) * radian

            val havLat = haversine(latAngle)
            val havLong = haversine(longAngle)
            val cos = cos(departureLat *radian ) * cos(destinationLat*radian)

            return 2 * radius *  asin(sqrt(havLat + cos * havLong))


    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i("GeoContext", "Sensor accuracy changed")
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if(!directionTracking)
        {
            //Log.i("GeoContext", "Not tracking direction")
            return
        }
        //Log.i("GeoContext", "Sensor Event")
        var location = Location(currentLocation)
        location.longitude = longitudeInput.text.toString().toDouble()
        location.latitude = latitudeInput.text.toString().toDouble()

        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            acceleration = event.values
        }
        else if(event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticField = event.values

        }

        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, acceleration, magneticField)
        val bearing = SensorManager.getOrientation(rotationMatrix, orientationAngles)[0]

        //Log.i("GeoContext", "North: ${"%.2f".format(orientationAngles[0]* 180 / Math.PI)}, Bearing: ${"%.2f".format(currentLocation?.bearingTo(location) ?: 0.0f)}")
        val angle = 90.0f + (-bearing ) * 180 / Math.PI + (currentLocation?.bearingTo(location) ?: 0.0f)
        //Log.i("GeoContext", "${"%.2f".format(-bearing * 180 / Math.PI )} + ${"%.2f".format((currentLocation?.bearingTo(location) ?: 0.0f))} = ${"%.2f".format(angle - 90f)}")
        imageView.rotation = angle.toFloat()



    }

    override fun onPause() {
        super.onPause()
        Log.i("GeoContext", "onPause")
        manager?.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        Log.i("GeoContext", "onResume")
        distanceTrackingResult.text = "${"%.3f".format(distanceTravelled)} km"

        /* Register sensors */
        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = manager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        manager?.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        //manager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putDouble("distanceTravelled", distanceTravelled)
        outState.putString("location", locationResult.text.toString())
        //Log.i("GeoContext", "onSaveInstanceState")
        outState.putDouble("altitude", currentAltitude)
    }

}