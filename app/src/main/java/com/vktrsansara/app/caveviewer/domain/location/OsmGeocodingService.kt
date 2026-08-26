package com.vktrsansara.app.caveviewer.domain.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class GeocodingResult(
    val name: String,
    val lat: Double,
    val lon: Double
)

/**
 * Lightweight search service for OpenStreetMap Nominatim geocoding with strict rate limiting.
 */
object OsmGeocodingService {
    private var lastRequestTime = 0L

    suspend fun search(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // Rate limiting: strictly at most 1 request per 1.1 seconds (Nominatim policy)
        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastRequestTime
        if (timeSinceLast < 1100) {
            delay(1100 - timeSinceLast)
        }
        lastRequestTime = System.currentTimeMillis()

        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=5&accept-language=ru"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "CaveViewer-App/3.0 (Android; Speleo GIS)")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().readText()
                val jsonArray = JSONArray(jsonText)
                val results = mutableListOf<GeocodingResult>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val displayName = obj.optString("display_name", "")
                    val latStr = obj.optString("lat", "")
                    val lonStr = obj.optString("lon", "")
                    val lat = latStr.toDoubleOrNull()
                    val lon = lonStr.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        results.add(
                            GeocodingResult(
                                name = displayName,
                                lat = lat,
                                lon = lon
                            )
                        )
                    }
                }
                results
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
