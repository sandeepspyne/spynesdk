package com.spyneai.draft.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.google.gson.Gson
import com.spyneai.BaseApplication
import com.spyneai.base.network.ClipperApi
import com.spyneai.base.network.ProjectApi
import com.spyneai.base.room.AppDatabase
import com.spyneai.isInternetActive
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.sku.Sku
import retrofit2.HttpException
import java.io.IOException

class ImageDataSource(
    private val service: ClipperApi,
    val appDatabase: AppDatabase,
    val skuId: String?,
    val skuUuid : String,
) : PagingSource<Int, Image>() {

    val TAG = "SkuDataSource"

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> {
        val page = params.key ?: 0
        Log.d(TAG, "load: "+page)

        return try {
            if (skuId != null && BaseApplication.getContext().isInternetActive()){
                val response = service.getImagesOfSku(
                    skuId = skuId,
                    authKey = "c0451cb9-ed40-4da6-992f-c2481f17120f"
                )

                val prevKey = if (page == 0) null else page - 1
                val nextKey = null

                appDatabase.withTransaction {
                    val ss = appDatabase.shootDao().insertImagesWithCheck(response.data as ArrayList<Image>,skuUuid)
                    Log.d(TAG, "load: ${Gson().toJson(ss)}")
                }

                val finalResponse = appDatabase.shootDao().getImagesBySkuId(
                    skuUuid = skuUuid
                )

                LoadResult.Page(
                    finalResponse,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }else{
                val response = appDatabase.shootDao().getImagesBySkuId(
                    skuUuid = skuUuid
                )

                val prevKey = if (page == 0) null else page - 1
                val nextKey = null


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

    override fun getRefreshKey(state: PagingState<Int, Image>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


}