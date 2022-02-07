package com.spyneai.orders.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import com.spyneai.shoot.repository.model.project.Project
import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
class PagedRepository(
    private val service: ProjectApi,
    private val appDatabase: AppDatabase,
    private val status : String
) {

    fun getSearchResultStream(): Flow<PagingData<Project>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProjectDataSource(service,appDatabase,status) }
        ).flow
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PAGE_INDEX = 0
    }
}