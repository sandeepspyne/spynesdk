package com.spyneai.orders.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import kotlinx.coroutines.flow.Flow

@ExperimentalPagingApi
class PagedRepository(
    private val service: ProjectApi,
    private val appDatabase: AppDatabase
) {

//    fun getSearchResultStream(): Flow<PagingData<ProjectPagedRes.ProjectPagedResItem>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = NETWORK_PAGE_SIZE,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = { ProjectDataSource(service) }
//        ).flow
//    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PAGE_INDEX = 0
    }

    fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(pageSize = DEFAULT_PAGE_SIZE, enablePlaceholders = true)
    }

    fun getSearchResultStream(
        pagingConfig: PagingConfig = getDefaultPageConfig()): Flow<PagingData<ProjectPagedRes.ProjectPagedResItem>> {
        if (appDatabase == null) throw IllegalStateException("Database is not initialized")

        val pagingSourceFactory = { appDatabase.getPagingDao().getAllProjects() }
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = pagingSourceFactory,
            remoteMediator = PagingMediator(service, appDatabase)
        ).flow
    }
}