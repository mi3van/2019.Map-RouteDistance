package com.test_ebook.managers

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.test_ebook.R
import com.test_ebook.interfaces.SaveRestoreInterface

/**
 * Created by Ivan Kuzmin on 06.10.2019;
 * 3van@mail.ru;
 * Copyright © 2019 Example. All rights reserved.
 */
private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
private const val KEY_BUNDLE_START_LATLON = "KEY_BUNDLE_START_LATLON"
private const val KEY_BUNDLE_FINISH_LATLON = "KEY_BUNDLE_FINISH_LATLON"

interface MapInterface {
    fun requestMyPermissions(permissions: Array<String>, requestCode: Int)
    fun showRequestPermissRationale(permission: String): Boolean
    fun checkIsLocationAvailable(): Boolean
    fun checkIsInternetAvailable(): Boolean
    fun checkLocationPermission(listener: DialogInterface.OnClickListener)
    fun showToast(text: String)
    fun closeApp()
    fun distanceResult(float: String)
}

class MapManager(private val _context: Context,
                 mapFragm: SupportMapFragment,
                 private val _providerClient: FusedLocationProviderClient?,
                 private val _mapInterface: MapInterface
): OnMapReadyCallback, SaveRestoreInterface, DistanceInterface {

    private lateinit var _dirManager: DirectionsManager
    private val _distanceManager = DistanceManager(_context, this)
    private lateinit var _gMap: GoogleMap
    private lateinit var _locationRequest: LocationRequest
    private var _currLocationMarker: Marker? = null
    private var _endLocationMarker: Marker? = null
    private var _startLatLng: LatLng? = null
    private var _endLatLng: LatLng? = null

    init {
        mapFragm.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        _gMap = googleMap

        _locationRequest = LocationRequest()
        _locationRequest.interval = 120000 // two minute interval
        _locationRequest.fastestInterval = 120000
        _locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             if (checkSelfPermission(ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                 //Location Permission already granted
                 configureMap()
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        }
        else {
            configureMap()
        }
    }

    override fun distanceResult(float: String) {
        _mapInterface.distanceResult(float)
    }

    private fun configureMap() {
        _providerClient?.requestLocationUpdates(_locationRequest, _locationCallback, Looper.myLooper())
        _gMap.isMyLocationEnabled = true
        _dirManager = DirectionsManager(_context, _gMap)
        _gMap.setOnMapLongClickListener {
            if (_mapInterface.checkIsInternetAvailable() &&
                    _currLocationMarker != null &&
                    _mapInterface.checkIsLocationAvailable()) {
                configureEndMarker(it)
                tryFindRoute()
                updateScreen()
            }
        }
        if (_startLatLng != null) {
            configureStartMarker(_startLatLng!!)
            if (_endLatLng != null) {
                configureEndMarker(_endLatLng!!)
                tryFindRoute()
            }
        }
    }

    fun onPause() {
        _providerClient?.removeLocationUpdates(_locationCallback)
    }

    fun onResume() {
        _startLatLng?.let {
            configureStartMarker(_startLatLng!!)
        }
    }

    private var _locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                //The last location in the list is the newest
                val location = locationList[locationList.size - 1]

                val latLng = LatLng(location.latitude, location.longitude)
                configureStartMarker(latLng)

                //move map camera
                updateScreen()
            }
        }
    }

    private fun updateScreen() {
        if (_currLocationMarker == null) return

        with(_currLocationMarker!!) {
            if (_endLocationMarker == null) {
                _gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11f))
            } else {
                val builder = LatLngBounds.Builder()
                builder.include(position)
                builder.include(_endLocationMarker!!.position)
                val bounds = builder.build()
                val padding = 256 // offset from edges of the map in pixels
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                _gMap.animateCamera(cu)
            }
        }
    }

    private fun checkLocationPermission() {
        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (_mapInterface.showRequestPermissRationale(ACCESS_FINE_LOCATION)) {
                _mapInterface.checkLocationPermission(DialogInterface.OnClickListener { _, _ ->
                    _mapInterface.requestMyPermissions(
                        arrayOf(ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION
                    )
                })
            } else {
                _mapInterface.requestMyPermissions(
                    arrayOf(ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        configureMap()
                    }
                } else {
//                    _mapInterface.showToast("Разрешение отклонено")
                    _mapInterface.closeApp()
                }
                return
            }
        }
    }

    private fun checkSelfPermission(permission: String): Int {
        return ContextCompat.checkSelfPermission(_context, permission)
    }

    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_BUNDLE_START_LATLON, _currLocationMarker?.position)
        outState.putParcelable(KEY_BUNDLE_FINISH_LATLON, _endLocationMarker?.position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        _startLatLng = savedInstanceState.getParcelable(KEY_BUNDLE_START_LATLON)
        _endLatLng = savedInstanceState.getParcelable(KEY_BUNDLE_FINISH_LATLON)
    }

    private fun configureStartMarker(latLng: LatLng) {
        if(::_gMap.isInitialized) {
            _currLocationMarker?.remove()
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Моё местоположение")
            markerOptions.icon(bitmapDescriptorFromVector(_context, R.drawable.ic_start))
            _currLocationMarker = _gMap.addMarker(markerOptions)
        }
    }

    private fun configureEndMarker(latLng: LatLng) {
        if(::_gMap.isInitialized) {
            _endLocationMarker?.remove()
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title("Конец маршрута")
            markerOptions.icon(bitmapDescriptorFromVector(_context, R.drawable.ic_end))
            _endLocationMarker = _gMap.addMarker(markerOptions)
        }
    }

    private fun tryFindRoute() {
        if (_currLocationMarker != null && _endLocationMarker != null) {
            with(Pair(_currLocationMarker!!.position, _endLocationMarker!!.position)) {
            _dirManager.startDirection(this.first, this.second)
            _distanceManager.startDistance(this.first, this.second)
            }
        }
    }

    fun cleanMap() {
        _endLocationMarker?.remove()
        _endLocationMarker = null
        _endLatLng = null

        _dirManager.cleanMap()

        updateScreen()
    }
}