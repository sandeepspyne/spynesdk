package com.spyneai.shoot.data

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.ClipperApiStagingClient
import com.spyneai.shoot.data.model.Image
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


class ShootRepository : BaseRepository() {

    private var clipperApi = ClipperApiClient().getClient()
    private var clipperStagingApi = ClipperApiStagingClient().getClient()

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
        tags: RequestBody,
        image: MultipartBody.Part,
    ) = safeApiCall {
        clipperApi.uploadImage(
            project_id,
            sku_id,
            image_category,
            auth_key, upload_type,
            sequenceNo,
            tags,
            image)
    }

    suspend fun getPreSignedUrl(
        uploadType : String,
        image : Image
    ) = safeApiCall {
        clipperApi.getPreSignedUrl(
            image.projectId,
            image.skuId,
            image.categoryName,
            image.name,
            image.overlayId?.toInt(),
            uploadType,
            image.sequence!!,
            image.isReclick != 0,
            image.isReshoot != 0,
            image.meta,
            image.debugData,
            image.angle!!
        )
    }

    suspend fun markUploaded(
        imageId : String
    ) = safeApiCall {
        clipperApi.markUploaded(imageId)
    }

    suspend fun uploadImageWithAngle(
        project_id: RequestBody,
        sku_id: RequestBody,
        image_category: RequestBody,
        auth_key: RequestBody,
        upload_type: RequestBody,
        sequenceNo : Int,
        angle : Int,
        image: MultipartBody.Part,
    ) = safeApiCall {
        clipperApi.uploadImageWithAngle(project_id, sku_id, image_category, auth_key, upload_type,sequenceNo, angle, image)
    }

    suspend fun createProject(authKey: String,
                              projectName : String,
                              prodCatId : String,
                              dynamicLayout : JSONObject? = null,
                              location_data : JSONObject? = null
    ) = safeApiCall {
        clipperApi.createProject(authKey, projectName, prodCatId,dynamicLayout,location_data)
    }

    suspend fun createSku(authKey: String,projectId : String
                          ,prodCatId : String,prodSubCatId : String,
                          skuName : String,total_frames : Int,
                          images : Int, videos : Int,

    ) = safeApiCall {
        clipperApi.createSku(authKey, projectId, prodCatId, prodSubCatId, skuName.uppercase(),
            total_frames,
        images,
        videos)
    }

    suspend fun updateVideoSku(
        skuId: String,
        prodSubCatId : String,
        initialImageCount: Int
    )= safeApiCall {
        clipperApi.updateVideoSku(
            skuId,
            prodSubCatId,
            initialImageCount
        )
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
        project_id:  String,
    ) = safeApiCall{
        clipperApi.skuProcessState(auth_key, project_id)
    }

    suspend fun checkUploadStatus(
        auth_key: String,
        image_name:  String
    ) = safeApiCall{
        clipperApi.checkUploadStatus(auth_key, image_name)
    }

    suspend fun updateFootwearSubcategory(
        authKey: String,
        skuId:  String,
        initialImageCount:  Int,
        subCatId:  String
    ) = safeApiCall{
        clipperApi.updateFootwearSubcategory(authKey, skuId, initialImageCount, subCatId)
    }

    suspend fun skuProcessStateWithBackgroundId(
        auth_key: String,
        project_id:  String,
        background_id:  Int
    ) = safeApiCall{
        clipperApi.skuProcessStateWithBackgroundId(auth_key, project_id, background_id)
    }

    suspend fun skuProcessStateWithShadowOption(
        auth_key: String,
        project_id:  String,
        background_id:  Int,
        shadow:  String
    ) = safeApiCall{
        clipperApi.skuProcessStateWithShadowOption(auth_key, project_id, background_id, shadow)
    }


    suspend fun sendFilesData(
        authKey: String,
        data : String
    ) = safeApiCall {
        clipperApi.sendFilesData(authKey,data)
    }


    suspend fun getProjectName(
        authKey : String
    ) = safeApiCall {
        clipperApi.getProjectName(authKey)
    }

    suspend fun getOverlayIds(ids: JSONArray
    ) = safeApiCall {
        clipperApi.getOverlayIds(ids)
    }

    suspend fun getImageData(
        imageId : String
    ) = safeApiCall {
        clipperApi.getImageData(imageId)
    }

}