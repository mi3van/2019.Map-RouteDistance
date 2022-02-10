package com.test_ebook.managers

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.test_ebook.R
import com.test_ebook.urlHelpers.TaskLoadedCallback
import com.test_ebook.urlHelpers.distance.DistanceParser
import com.test_ebook.urlHelpers.distance.FetchURLDistance

/**
 * Created by Ivan Kuzmin on 2019-10-08;
 * 3van@mail.ru;
 * Copyright © 2019 Example. All rights reserved.
 */

interface DistanceInterface {
    fun distanceResult(float: String)
}

const val DIST_UNITS_IMPERIAL = "imperial"

class DistanceManager(private val _context: Context,
                        private val _listener: DistanceInterface
): TaskLoadedCallback {

    fun startDistance(start: LatLng, end: LatLng) {
        val url = getUrl(start, end, DIST_UNITS_IMPERIAL)
        val parser = DistanceParser(this)
        FetchURLDistance(parser).execute(url)
    }

    override fun onTaskDone(vararg values: Any?) {
        if (values.isNotEmpty()) {
            with(values[0]) {
                if (this is String) {
                    _listener.distanceResult(this)
                }
            }
        }
    }

    private fun getUrl(start: LatLng, end: LatLng, units: String): String {
        val strStart = "origins=${start.latitude},${start.longitude}"
        val strEnd = "destinations=${end.latitude},${end.longitude}"
        val mode = "units=$units"
        val parameters = "$mode&$strStart&$strEnd"

        val format = "json"

        val url = "https://maps.googleapis.com/maps/api/distancematrix/$format?$parameters&key=${
        _context.getString(R.string.google_maps_key)}"
        return url
    }
}