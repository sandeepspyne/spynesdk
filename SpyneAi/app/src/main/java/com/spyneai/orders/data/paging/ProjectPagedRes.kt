package com.spyneai.orders.data.paging


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

class ProjectPagedRes : ArrayList<ProjectPagedRes.ProjectPagedResItem>(){
    @Entity
    data class ProjectPagedResItem(
        @PrimaryKey
        @SerializedName("project_id")
        val projectId: String,
        @SerializedName("prod_cat_id")
        val prodCatId: String,
        @SerializedName("category")
        val category: String,
        @SerializedName("created_on")
        val createdOn: String,
        @SerializedName("project_name")
        val projectName: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("total_sku")
        val totalSku: Int
    )
}