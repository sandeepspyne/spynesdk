package com.spyneai.orders.data.paging

import android.net.Uri
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.google.gson.Gson
import com.spyneai.BaseApplication
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import com.spyneai.isInternetActive
import com.spyneai.orders.data.paging.PagedRepository.Companion.DEFAULT_PAGE_INDEX
import com.spyneai.shoot.repository.model.project.Project
import retrofit2.HttpException
import java.io.IOException


class ProjectDataSource(
    private val service: ProjectApi,
    val appDatabase: AppDatabase,
    val status: String
    ) : PagingSource<Int, ProjectPagedRes.ProjectPagedResItem>() {

    val TAG = "ProjectDataSource"

    @ExperimentalPagingApi
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProjectPagedRes.ProjectPagedResItem> {
        val page = params.key ?: DEFAULT_PAGE_INDEX
        Log.d(TAG, "load: "+page)
        //val apiQuery = query + IN_QUALIFIER

        return try {
            if (BaseApplication.getContext().isInternetActive()){
                val response = service.getPagedProjects(
                    pageNo = page,
                    status = status
                )

                val prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1
                val nextKey = if (response.isNullOrEmpty()) null else page + 1

                appDatabase.withTransaction {
                    val ss = appDatabase.getPagingDao().insertWithCheck(response)
                    Log.d(TAG, "load: ${Gson().toJson(ss)}")
                }

                val finalResponse = appDatabase.getPagingDao().getProjectsWithLimitAndSkip(
                    offset = page.times(10),
                    status = status
                )

                LoadResult.Page(
                    finalResponse,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }else{
                val response = appDatabase.getPagingDao().getProjectsWithLimitAndSkip(
                    offset = page.times(10),
                    status = status
                )

                val prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1
                val nextKey = if (response.isNullOrEmpty()) null else page + 1


                LoadResult.Page(
                    response,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }

////            val response = service.getPagedProjects(apiQuery, "position", params.loadSize)
//            val repos = service.getPagedProjects("apiQuery", "position")
//            //val repos = response.items
//            val nextKey = if (repos.isEmpty()) {
//                null
//            } else {
//                // initial load size = 3 * NETWORK_PAGE_SIZE
//                // ensure we're not requesting duplicating items, at the 2nd request
//                position + (params.loadSize / 10)
//            }
//            LoadResult.Page(
//                data = repos,
//                prevKey = if (position == GITHUB_STARTING_PAGE_INDEX) null else position - 1,
//                nextKey = nextKey
//            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ProjectPagedRes.ProjectPagedResItem>): Int? {
        // We need to get the previous key (or next key if previous is null) of the page
        // that was closest to the most recently accessed index.
        // Anchor position is the most recently accessed index
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}