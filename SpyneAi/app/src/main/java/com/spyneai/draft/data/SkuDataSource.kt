package com.spyneai.draft.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.google.gson.Gson
import com.spyneai.BaseApplication
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import com.spyneai.isInternetActive
import com.spyneai.orders.data.paging.PagedRepository
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku
import retrofit2.HttpException
import java.io.IOException

class SkuDataSource(
    private val service: ProjectApi,
    val appDatabase: AppDatabase,
    val projectId: String?,
    val projectUuid : String,
) : PagingSource<Int, Sku>() {

    val TAG = "SkuDataSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Sku> {
        val page = params.key ?: 0
        Log.d(TAG, "load: "+page)

        return try {
            if (projectId != null && BaseApplication.getContext().isInternetActive()){
                val response = service.getPagedSku(
                    pageNo = page,
                    projectId = projectId
                )

                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (response.isNullOrEmpty()) null else page + 1

                appDatabase.withTransaction {
                    val ss = appDatabase.shootDao().insertWithCheck(response)
                    Log.d(TAG, "load: ${Gson().toJson(ss)}")
                }

                val finalResponse = appDatabase.shootDao().getSkusWithLimitAndSkip(
                    offset = page.times(10),
                    projectUuid = projectUuid
                )

                LoadResult.Page(
                    finalResponse,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }else{
                val response = appDatabase.shootDao().getSkusWithLimitAndSkip(
                    offset = page.times(10),
                    projectUuid = projectUuid
                )

                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (response.isNullOrEmpty()) null else page + 1


                LoadResult.Page(
                    response,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Sku>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


}