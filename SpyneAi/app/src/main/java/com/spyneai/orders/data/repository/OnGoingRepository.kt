package com.spyneai.orders.data.repository

import com.spyneai.base.BaseRepository
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.orders.data.response.ProjectCountResponse

class OnGoingRepository : BaseRepository() {

    private var spyneApi = SpyneAiApiClient().getClient()

    private var clipperApi = ClipperApiClient().getClient()

    suspend fun getProjectCount(
        tokenId: String
    ) = ProjectCountResponse(12,32)

}