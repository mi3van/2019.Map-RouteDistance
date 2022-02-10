package com.test_ebook.managers

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.test_ebook.R
import com.test_ebook.urlHelpers.TaskLoadedCallback
import com.test_ebook.urlHelpers.route.FetchURL
import com.test_ebook.urlHelpers.route.PointsParser

/**
 * Created by Ivan Kuzmin on 2019-10-07;
 * 3van@mail.ru;
 * Copyright © 2019 Example. All rights reserved.
 */

const val DIR_MODE_DRIVING = "driving"

class DirectionsManager(private val _context: Context,
                        private val _map: GoogleMap): TaskLoadedCallback {
    private var _dirPolyline: Polyline? = null
    private val routeColor = ResourcesCompat.getColor(
        _context.resources,
        R.color.colorPrimaryDark,
        _context.theme)

    fun startDirection(start: LatLng, end: LatLng) {
        cleanMap()
        val url = getUrl(start, end, DIR_MODE_DRIVING)
        val parser =
            PointsParser(this, DIR_MODE_DRIVING, routeColor)
        FetchURL(parser).execute(url, DIR_MODE_DRIVING)
    }

    override fun onTaskDone(vararg values: Any?) {
        _dirPolyline = _map.addPolyline(values[0] as PolylineOptions?)
    }

    private fun getUrl(start: LatLng, end: LatLng, directionMode: String): String {
        val strStart = "origin=${start.latitude},${start.longitude}"
        val strEnd = "destination=${end.latitude},${end.longitude}"
        val mode = "mode=$directionMode"
        val parameters = "$strStart&$strEnd&$mode"

        val output = "json"

        val url = "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=${
        _context.getString(R.string.google_maps_key)}"
        return url
    }

    fun cleanMap() {
        _dirPolyline?.remove()
    }
}