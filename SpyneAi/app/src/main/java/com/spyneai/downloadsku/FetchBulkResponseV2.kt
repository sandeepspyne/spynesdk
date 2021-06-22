package com.spyneai.downloadsku

import com.google.gson.annotations.SerializedName

data class FetchBulkResponseV2 (
    @SerializedName("staus") val staus : Int,
    @SerializedName("message") val message : String,
    @SerializedName("sku_status") val sku_status : String,
    @SerializedName("paid") val paid : Boolean,
    @SerializedName("data") val data : List<Data>
){
    data class Data (

        @SerializedName("id") val id : Int,
        @SerializedName("enterprise_id") val enterprise_id : String,
        @SerializedName("user_id") val user_id : String,
        @SerializedName("image_id") val image_id : String,
        @SerializedName("image_name") val image_name : String,
        @SerializedName("sku_id") val sku_id : String,
        @SerializedName("project_id") val project_id : String,
        @SerializedName("frame_seq_no") val frame_seq_no : String,
        @SerializedName("image_category") val image_category : String,
        @SerializedName("input_image_hres_url") val input_image_hres_url : String,
        @SerializedName("input_image_lres_url") val input_image_lres_url : String,
        @SerializedName("output_image_hres_url") val output_image_hres_url : String,
        @SerializedName("output_image_lres_url") val output_image_lres_url : String,
        @SerializedName("output_image_lres_wm_url") val output_image_lres_wm_url : String,
        @SerializedName("background_id") val background_id : Int,
        @SerializedName("status") val status : String,
        @SerializedName("source") val source : String,
        @SerializedName("created_on") val created_on : String,
        @SerializedName("updated_on") val updated_on : String
    )
}