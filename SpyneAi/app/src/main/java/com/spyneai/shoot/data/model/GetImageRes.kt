package com.spyneai.shoot.data.model


import com.google.gson.annotations.SerializedName

data class GetImageRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int
) {
    data class Data(
        @SerializedName("angle")
        val angle: Int,
        @SerializedName("background_id")
        val backgroundId: String,
        @SerializedName("created_on")
        val createdOn: String,
        @SerializedName("debug_data")
        val debugData: String,
        @SerializedName("enterprise_id")
        val enterpriseId: String,
        @SerializedName("frame_seq_no")
        val frameSeqNo: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("image_category")
        val imageCategory: String,
        @SerializedName("image_id")
        val imageId: String,
        @SerializedName("image_name")
        val imageName: String,
        @SerializedName("image_rating")
        val imageRating: Any,
        @SerializedName("image_score")
        val imageScore: Any,
        @SerializedName("input_image_hres_url")
        val inputImageHresUrl: String,
        @SerializedName("input_image_lres_url")
        val inputImageLresUrl: String,
        @SerializedName("is_hidden")
        val isHidden: Int,
        @SerializedName("manual_retouch")
        val manualRetouch: String,
        @SerializedName("output_image_hres_url")
        val outputImageHresUrl: String,
        @SerializedName("output_image_lres_url")
        val outputImageLresUrl: String,
        @SerializedName("output_image_lres_wm_url")
        val outputImageLresWmUrl: String,
        @SerializedName("overlay_id")
        val overlayId: Int,
        @SerializedName("process_data")
        val processData: String,
        @SerializedName("process_finish_time")
        val processFinishTime: String,
        @SerializedName("process_start_time")
        val processStartTime: String,
        @SerializedName("prod_cat_name")
        val prodCatName: String,
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("reject_comment")
        val rejectComment: Any,
        @SerializedName("reject_reason")
        val rejectReason: Any,
        @SerializedName("removebg_type")
        val removebgType: Any,
        @SerializedName("sku_id")
        val skuId: String,
        @SerializedName("source")
        val source: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("tags")
        val tags: String,
        @SerializedName("updated_on")
        val updatedOn: String,
        @SerializedName("upload_type")
        val uploadType: String,
        @SerializedName("user_id")
        val userId: String,
        @SerializedName("verified_status")
        val verifiedStatus: String
    )
}