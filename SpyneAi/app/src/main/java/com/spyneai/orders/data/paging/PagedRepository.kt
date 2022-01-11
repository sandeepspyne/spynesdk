package com.spyneai.orders.data.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ProjectApi
import kotlinx.coroutines.flow.Flow

class PagedRepository(private val service: ProjectApi) {

    fun getSearchResultStream(): Flow<PagingData<ProjectPagedRes.ProjectPagedResItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProjectDataSource(service) }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 10
    }
}