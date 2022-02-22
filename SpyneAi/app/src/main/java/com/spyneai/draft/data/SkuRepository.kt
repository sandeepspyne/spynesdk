package com.spyneai.draft.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import com.spyneai.orders.data.paging.ProjectDataSource
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku
import kotlinx.coroutines.flow.Flow

class SkuRepository(
    private val service: ProjectApi,
    val appDatabase: AppDatabase,
    val projectId: String?,
    val projectUuid : String,
    val videoData : Int
) {

    fun getSearchResultStream(): Flow<PagingData<Sku>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SkuDataSource(service,appDatabase,projectId,projectUuid,videoData) }
        ).flow
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PAGE_INDEX = 0
    }

}