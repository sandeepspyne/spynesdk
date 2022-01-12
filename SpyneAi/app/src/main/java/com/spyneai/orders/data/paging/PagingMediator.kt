package com.spyneai.orders.data.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException
import androidx.room.withTransaction
import com.google.gson.Gson
import com.spyneai.orders.data.paging.PagedRepository.Companion.DEFAULT_PAGE_INDEX

@ExperimentalPagingApi
class PagingMediator(val doggoApiService: ProjectApi, val appDatabase: AppDatabase) :
    RemoteMediator<Int, ProjectPagedRes.ProjectPagedResItem>() {

    val TAG = "PagingMediator"

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, ProjectPagedRes.ProjectPagedResItem>
    ): MediatorResult {

        val pageKeyData = getKeyPageData(loadType, state)
        val page = when (pageKeyData) {
            is MediatorResult.Success -> {
                return pageKeyData
            }
            else -> {
                pageKeyData as Int
            }
        }

        try {
            val response = doggoApiService.getPagedProjects(page)
            val isEndOfList = response.isEmpty()
            appDatabase.withTransaction {
                // clear all tables in the database
//                if (loadType == LoadType.REFRESH) {
//                    appDatabase.getRepoDao().clearRemoteKeys()
//                    appDatabase.getPagingDao().clearAllProjects()
//                }
                val prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1
                val nextKey = if (isEndOfList) null else page + 1
                val keys = response.map {
                    RemoteKeys(projectId = it.projectId, prevKey = prevKey, nextKey = nextKey)
                }

               // val finalList = ArrayList<ProjectPagedRes.ProjectPagedResItem>()

                val s = appDatabase.getRepoDao().insertAll(keys)
                Log.d(TAG, "load: ${Gson().toJson(s)}")
                val ss = appDatabase.getPagingDao().insertWithCheck(response)
                Log.d(TAG, "load: ${Gson().toJson(ss)}")
            }
            return MediatorResult.Success(endOfPaginationReached = isEndOfList)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    /**
     * this returns the page key or the final end of list success result
     */
    suspend fun getKeyPageData(loadType: LoadType, state: PagingState<Int, ProjectPagedRes.ProjectPagedResItem>): Any? {
       return when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getClosestRemoteKey(state)
                remoteKeys?.nextKey?.minus(1) ?: DEFAULT_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getFirstRemoteKey(state)
                val prevKey = remoteKeys?.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getLastRemoteKey(state)
                val nextKey = remoteKeys?.nextKey
                if (nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                nextKey
            }
        }
    }

    /**
     * get the last remote key inserted which had the data
     */
    private suspend fun getLastRemoteKey(state: PagingState<Int, ProjectPagedRes.ProjectPagedResItem>): RemoteKeys? {
        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { doggo -> appDatabase.getRepoDao().remoteKeysProjectId(doggo.projectId) }
    }

    /**
     * get the first remote key inserted which had the data
     */
    private suspend fun getFirstRemoteKey(state: PagingState<Int, ProjectPagedRes.ProjectPagedResItem>): RemoteKeys? {
        val s = state.pages
            .firstOrNull() { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { doggo -> appDatabase.getRepoDao().remoteKeysProjectId(doggo.projectId) }

        return s
    }

    /**
     * get the closest remote key inserted which had the data
     */
    private suspend fun getClosestRemoteKey(state: PagingState<Int, ProjectPagedRes.ProjectPagedResItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.projectId?.let { repoId ->
                appDatabase.getRepoDao().remoteKeysProjectId(repoId)
            }
        }
    }

}