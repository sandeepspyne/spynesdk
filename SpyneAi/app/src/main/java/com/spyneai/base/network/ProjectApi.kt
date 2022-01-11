package com.spyneai.base.network

import com.spyneai.orders.data.paging.ProjectPagedRes
import com.spyneai.orders.data.response.GetProjectsResponse
import com.spyneai.shoot.repository.model.project.CreateProjectAndSkuRes
import com.spyneai.shoot.repository.model.project.ProjectBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ProjectApi {

    @POST("v1/projects/offline-create-project-skus")
    suspend fun createProject(@Body projectBody: ProjectBody) : CreateProjectAndSkuRes

    @GET("v1/get-data/get-project")
    suspend fun getPagedProjects(
        @Query("pageNo") pageNo: Int,
        @Query("count") count: Int = 10,
        @Query("sortBy") sortBy: String = "ASC",
        @Query("auth_key") authKey: String = "e590700a-0f58-4b91-b947-93d1a32484a1",
        @Query("status") status: String = "draft"
    ) : ProjectPagedRes
}