package com.spyneai.dashboard.data.model


import com.google.gson.annotations.SerializedName

data class LocationsRes(
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("coordinates")
        val coordinates: Coordinates,
        @SerializedName("id")
        val id: Int,
        @SerializedName("location_id")
        val locationId: String,
        @SerializedName("location_name")
        val locationName: String,
        @SerializedName("threshold_distance_in_meters")
        val thresholdDistanceInMeters: Int
    ) {
        data class Coordinates(
            @SerializedName("city")
            val city: String,
            @SerializedName("country")
            val country: String,
            @SerializedName("latitude")
            val latitude: Double,
            @SerializedName("longitude")
            val longitude: Double,
            @SerializedName("postalCode")
            val postalCode: String
        )
    }
}