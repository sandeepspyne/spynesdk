package com.spyneai.draft.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.sku.Sku
import kotlinx.coroutines.flow.Flow

class ImageRepository(
    private val service: ClipperApi,
    val appDatabase: AppDatabase,
    val skuId: String?,
    val skuUuid : String,
) {

    fun getSearchResultStream(): Flow<PagingData<Image>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ImageDataSource(service,appDatabase,skuId,skuUuid) }
        ).flow
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 100
        const val DEFAULT_PAGE_INDEX = 0
    }

}