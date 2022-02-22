package com.spyneai.base.network

import com.spyneai.BaseApplication
import com.spyneai.draft.data.PagedSkuRes
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.orders.data.paging.ProjectPagedRes
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.shoot.repository.model.project.CreateProjectAndSkuRes
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.project.ProjectBody
import com.spyneai.shoot.repository.model.sku.Sku
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProjectApi {

    @POST("nv1/projects/offline-create-project-skus")
    suspend fun createProject(@Body projectBody: ProjectBody) : CreateProjectAndSkuRes

    @GET("nv1/app/get-project")
    suspend fun getPagedProjects(
        @Query("pageNo") pageNo: Int,
        @Query("count") count: Int = 10,
        @Query("sortBy") sortBy: String = "DESC",
        @Query("auth_key") authKey: String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString(),
        @Query("status") status: String = "draft"
    ) : ProjectPagedRes


    @GET("nv1/app/get-project-sku")
    suspend fun getPagedSku(
        @Query("pageNo") pageNo: Int,
        @Query("projectId") projectId: String,
        @Query("count") count: Int = 50,
        @Query("sortBy") sortBy: String = "DESC",
        @Query("videoData") videoData: Int,
        @Query("auth_key") authKey: String = Utilities.getPreference(BaseApplication.getContext(),AppConstants.AUTH_KEY).toString(),
    ) : PagedSkuRes


}