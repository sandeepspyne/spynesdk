package com.spyneai.dashboard.data.model

data class GetAttendanceStatusRes(
    val `data`: Data,
    val message: String,
    val status: Int
) {
    data class Data(
        val checkin_image: String,
        val checkin_location: CheckinLocation,
        val checkin_time: String,
        val checkout_image: String,
        val checkout_location: CheckoutLocation,
        val checkout_time: String,
        val created_on: String,
        val distance_meters: Int,
        val enterprise_id: String,
        val hours: Int,
        val id: Int,
        val location_in_id: String,
        val location_out_id: String,
        val sku_count: Int,
        val updated_on: String,
        val user_id: String
    ) {
        data class CheckinLocation(
            val city: String,
            val country: String,
            val latitude: Double,
            val longitude: Double,
            val postalCode: String
        )

        data class CheckoutLocation(
            val city: String,
            val country: String,
            val latitude: Double,
            val longitude: Double,
            val postalCode: String
        )
    }
}