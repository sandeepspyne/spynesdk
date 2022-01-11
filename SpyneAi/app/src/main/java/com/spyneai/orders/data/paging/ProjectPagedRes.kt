package com.spyneai.orders.data.paging


import com.google.gson.annotations.SerializedName

class ProjectPagedRes : ArrayList<ProjectPagedRes.ProjectPagedResItem>(){
    data class ProjectPagedResItem(
        @SerializedName("category")
        val category: String,
        @SerializedName("created_on")
        val createdOn: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("project_name")
        val projectName: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("total_sku")
        val totalSku: Int
    )
}