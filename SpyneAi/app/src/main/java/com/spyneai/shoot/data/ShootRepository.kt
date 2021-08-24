package com.spyneai.shoot.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import okhttp3.MultipartBody
import okhttp3.RequestBody


class ShootRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getSubCategories(
        authKey : String,prodId : String
    ) = safeApiCall {
        clipperApi.getSubCategories(authKey, prodId)
    }

    suspend fun getOverlays(authKey: String, prodId: String,
                            prodSubcategoryId : String, frames : String) = safeApiCall {
        clipperApi.getOverlays(authKey, prodId, prodSubcategoryId, frames)
    }

    suspend fun uploadImage(
        project_id: RequestBody,
        sku_id: RequestBody,
        image_category: RequestBody,
        auth_key: RequestBody,
        upload_type: RequestBody,
        sequenceNo : Int,
        image: MultipartBody.Part
    ) = safeApiCall {
        clipperApi.uploadImage(project_id, sku_id, image_category, auth_key, upload_type,sequenceNo,image)
    }

    suspend fun createProject(authKey: String,projectName : String,
                              prodCatId : String
    ) = safeApiCall {
        clipperApi.createProject(authKey, projectName, prodCatId)
    }

    suspend fun createSku(authKey: String,projectId : String
                          ,prodCatId : String,prodSubCatId : String,
                          skuName : String,total_frames : Int
    ) = safeApiCall {
        clipperApi.createSku(authKey, projectId, prodCatId, prodSubCatId, skuName,total_frames)
    }

    suspend fun getProjectDetail(
        tokenId: String,
        projectId:  String
    ) = safeApiCall{
        clipperApi.getProjectDetail(tokenId, projectId)
    }

    suspend fun updateTotalFrames(
        skuId: String,
        totalFrames:  String,
        authKey:  String
    ) = safeApiCall{
        clipperApi.updateTotalFrames(authKey,skuId,totalFrames)
    }
    suspend fun skuProcessState(
        auth_key: String,
        project_id:  String
    ) = safeApiCall{
        clipperApi.skuProcessState(auth_key, project_id)
    }



}