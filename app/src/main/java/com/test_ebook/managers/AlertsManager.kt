package com.test_ebook.managers

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


/**
 * Created by Ivan Kuzmin on 2019-10-07;
 * 3van@mail.ru;
 * Copyright © 2019 Example. All rights reserved.
 */

class AlertsManager(private val _activity: AppCompatActivity) {

    fun checkIsInternetAvailable(): Boolean {
        var isConnected = false
        val cm = _activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    isConnected = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        isConnected = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        isConnected = true
                    }
                }
            }
        }

        if(!isConnected) {
            // notify user
            AlertDialog.Builder(_activity)
                .setMessage("Соединение интернет отключено")
                .setPositiveButton("Открыть настройки") { _: DialogInterface, _: Int ->
                    _activity.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }
                .setNegativeButton("Отмена", null)
                .create()
                .show()
        }

        return isConnected
    }

    fun checkIsLocationAvailable(): Boolean {
        val lm = _activity.getSystemService(Context.LOCATION_SERVICE) as (LocationManager)
        var gpsEnabled = false
        var networkEnabled = false

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch(e: Exception) {
            e.printStackTrace()
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch(e: Exception) {
            e.printStackTrace()
        }

        val isLocationDisabled = !gpsEnabled && !networkEnabled
        if(isLocationDisabled) {
            // notify user
            AlertDialog.Builder(_activity)
                .setMessage("Геолокация отключена")
                .setPositiveButton("Открыть настройки") { _: DialogInterface, _: Int ->
                    _activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("Отмена", null)
                .create()
                .show()
        }
        return !isLocationDisabled
    }

    fun checkLocationPermission(listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(_activity)
            .setTitle("Необходимо разрешение текущего местоположения")
            .setMessage("Данному приложению для корректной работы необходимо разрешение местоположения," +
                    " пожалуйста, включите разрешение для использования местоположения")
            .setPositiveButton("Ок", listener)
            .create()
            .show()
    }
}