package com.spyneai.shoot.repository.model.project


import com.google.gson.annotations.SerializedName

data class CreateProjectAndSkuRes(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String
) {
    data class Data(
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("skusList")
        val skusList: List<Skus>
    ) {
        data class Skus(
            @SerializedName("local_id")
            val localId: String,
            @SerializedName("sku_id")
            val skuId: String,
            @SerializedName("sku_name")
            val skuName: String
        )
    }
}