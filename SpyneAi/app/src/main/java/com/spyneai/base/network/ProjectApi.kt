package com.spyneai.base.network

import com.spyneai.shoot.repository.model.project.CreateProjectAndSkuRes
import com.spyneai.shoot.repository.model.project.ProjectBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ProjectApi {

    @POST("v1/projects/offline-create-project-skus")
    suspend fun createProject(@Body projectBody: ProjectBody) : CreateProjectAndSkuRes
}