package com.test_ebook.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import com.test_ebook.R
import com.test_ebook.managers.AlertsManager
import com.test_ebook.managers.DistanceInterface
import com.test_ebook.managers.MapInterface
import com.test_ebook.managers.MapManager
import kotlinx.android.synthetic.main.activity_maps.*
import kotlin.system.exitProcess

class MapsActivity : AppCompatActivity(), MapInterface, DistanceInterface {

    private lateinit var _mapManager: MapManager
    private val _alertsManager = AlertsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val provider = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        _mapManager = MapManager(baseContext, mapFragment, provider, this)

        clearRoute?.setOnClickListener {
            _mapManager.cleanMap()
            distanceLayout?.visibility = GONE
        }
    }

    override fun requestMyPermissions(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun showRequestPermissRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

    override fun checkIsLocationAvailable(): Boolean {
        return _alertsManager.checkIsLocationAvailable()
    }

    override fun checkIsInternetAvailable(): Boolean {
        return _alertsManager.checkIsInternetAvailable()
    }

    override fun checkLocationPermission(listener: DialogInterface.OnClickListener) {
        _alertsManager.checkLocationPermission(listener)
    }

    override fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    override fun closeApp() {
        closeApplication()
    }

    override fun distanceResult(float: String) {
        distanceLayout?.visibility = VISIBLE
        distanceText?.text = "${getString(R.string.distance)} $float"
    }

    override fun onResume() {
        super.onResume()
        _alertsManager.checkIsInternetAvailable()
        _alertsManager.checkIsLocationAvailable()
        _mapManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        _mapManager.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        _mapManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _mapManager.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        _mapManager.onRestoreInstanceState(savedInstanceState)
    }

    private fun closeApplication() {
        finish()
        exitProcess(0)
    }
}
