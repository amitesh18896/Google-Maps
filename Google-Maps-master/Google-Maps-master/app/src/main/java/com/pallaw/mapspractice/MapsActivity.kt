package com.pallaw.mapspractice

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private var mLocationPermissionGranted: Boolean = false
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 678
    private val ERROR_DIALOG_REQUEST: Int = 123
    private lateinit var mMap: GoogleMap
    private val TAG = "MapsActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        searchView.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event?.action == KeyEvent.ACTION_DOWN ||
                    event?.action == KeyEvent.KEYCODE_ENTER
                ) {
                    geoLocate()
                }

                return true
            }
        })

        btnCurrentLocation.setOnClickListener(this)
        btnMap.setOnClickListener(this)
        btnInfo.setOnClickListener(this)

        if (isServiceAvailable()) {
            getLocationPermission()
        } else {
            Toast.makeText(
                this,
                "Your device doesn't have google play service, so you can not use google maps",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun geoLocate() {
        val searchTexh = searchView.text.toString().trim()
        val geocoder = Geocoder(this)
        val fromLocationName = geocoder.getFromLocationName(searchTexh, 1)

        if (fromLocationName.size > 0) {
            val address = fromLocationName[0]
            Log.d(TAG, "geoLocate: Location found $address")
            moveCameraTo(LatLng(address.latitude, address.longitude), address.getAddressLine(0))
        }

    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (mLocationPermissionGranted) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "getDeviceLocation: current location fetched ")
                    val result = it.result
                    result?.let {
                        moveCameraTo(LatLng(it.latitude, it.longitude), "current Location")
                    }
                } else
                    Log.d(TAG, "getDeviceLocation: current location is not fetched ")
            }
        }
    }

    private fun moveCameraTo(result: LatLng, title: String) {
        result.let {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12.0f))

            val marker = MarkerOptions().apply {
                position(result)
                title(title)
            }
            mMap.addMarker(marker)

        }
    }

    private fun initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission() {
        val permissionList = arrayOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(
                    this.applicationContext,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionGranted = false
                ActivityCompat.requestPermissions(
                    this,
                    permissionList,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                mLocationPermissionGranted = true
                initMap()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                    initMap()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isServiceAvailable(): Boolean {
        val googlePlayServicesAvailable =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (googlePlayServicesAvailable == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServiceAvailable: Google play service is working")
            return true
        } else if (GoogleApiAvailability.getInstance()
                .isUserResolvableError(googlePlayServicesAvailable)
        ) {
            Log.d(TAG, "isServiceAvailable: Error but can be tackeled")
            val errorDialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this, googlePlayServicesAvailable, ERROR_DIALOG_REQUEST)
            errorDialog.show()
        } else {
            Log.d(TAG, "isServiceAvailable: You can't make map request")
        }
        return false
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getDeviceLocation()
    }

    override fun onClick(v: View?) {
        when (v) {
            btnCurrentLocation -> {
                getDeviceLocation()
            }
            btnMap -> {
            }
            btnInfo -> {
            }
        }
    }
}