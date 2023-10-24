package com.example.maps

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.maps.databinding.ActivityMainBinding
import android.Manifest
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : AppCompatActivity() {
    // binding
    private lateinit var binding: ActivityMainBinding
    // maps
    private lateinit var mapController: MapController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        mapView = binding.mapView
        mapView.setMultiTouchControls(true)
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        mapController = mapView.controller as MapController

        // FusedLocationProviderClient initialization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        getLocationMarker()

        // Add onClickListener to icon button
        binding.iconPlus.setOnClickListener { zoomIn() }
        binding.iconMinus.setOnClickListener { zoomOut() }
        binding.iconRecenter.setOnClickListener { recenterMap() }
    }

    private fun getLocationMarker() {
        mapView = binding.mapView
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        locationOverlay.enableMyLocation()

        var userLocation: GeoPoint?  // Define userLocation here

        // Checking the location permit
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationOverlay.runOnFirstFix {
                val myLocation = locationOverlay.myLocation
                if (myLocation != null) {
                    userLocation = GeoPoint(myLocation)

                    // Set map center to user location
                    runOnUiThread {
                        mapController.setCenter(userLocation)
                        mapView.controller.zoomTo(20)
                    }

                    // Add markers after getting the location
                    val userMarker = Marker(mapView)
                    userMarker.position = userLocation
                    userMarker.title = "My Location"
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    userMarker.icon = ResourcesCompat.getDrawable(resources, R.drawable.icon_location, null)

                    runOnUiThread {
                        mapView.overlays.add(userMarker)
                        mapView.invalidate()
                    }
                }
            }
        } else {
            // If a location permit is not granted, you will need to request a permit first.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        mapView.overlays.add(locationOverlay)
    }

    private fun zoomIn() {
        mapController.zoomIn()
    }

    private fun zoomOut() {
        mapController.zoomOut()
    }

    private fun recenterMap() {
        getLocationMarker()
    }
}
