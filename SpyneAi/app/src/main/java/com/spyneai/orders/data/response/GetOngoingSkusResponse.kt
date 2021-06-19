package com.spyneai.orders.data.response

import com.google.gson.annotations.SerializedName

data class GetOngoingSkusResponse(
    @SerializedName("count") val count : Int,
    @SerializedName("data") val data : List<Data>,
    @SerializedName("message") val message : String,
    @SerializedName("status") val status : Int
){
    data class Data(
        @SerializedName("category") val category : String,
        @SerializedName("created_date") val created_date : String,
        @SerializedName("sku_id") val sku_id : String,
        @SerializedName("sku_name") val sku_name : String,
        @SerializedName("source") val source : String,
        @SerializedName("status") val status : String,
        @SerializedName("sub_category") val sub_category : String,
        @SerializedName("thumbnail") val thumbnail : String,
        @SerializedName("total_exterior") val total_exterior : Int,
        @SerializedName("total_focus") val total_focus : String,
        @SerializedName("total_images") val total_images : Int,
        @SerializedName("total_interior") val total_interior : String,
        @SerializedName("total_processed") val total_processed : String
    )
}

