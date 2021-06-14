package com.spyneai.shoot.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spyneai.base.network.ClipperApiClient
import com.spyneai.base.network.Resource
import com.spyneai.base.network.SpyneAiApiClient
import com.spyneai.base.repository.BaseRepository
import com.spyneai.shoot.data.model.UploadImageResponse
import com.spyneai.shoot.data.room.ShootDataBase
import com.spyneai.shoot.data.room.entities.ShootEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ShootRepository : BaseRepository() {

    private var spyneApi = SpyneAiApiClient().getClient()
    private var clipperApi = ClipperApiClient().getClient()

    private val _uploadImageResonse: MutableLiveData<Resource<UploadImageResponse>> =
        MutableLiveData()
    val uploadImageResonse: LiveData<Resource<UploadImageResponse>>
        get() = _uploadImageResonse

    companion object{
        var shootDataBase: ShootDataBase? = null
        var shootEntity: ShootEntity? = null
        fun initializeDB(context: Context): ShootDataBase{
            return ShootDataBase.invoke(context)
        }
    }

     fun insertShootData(context: Context, shootEntity: ShootEntity){
        shootDataBase = initializeDB(context)
        CoroutineScope(IO).launch {
            shootDataBase?.getShootDao()?.insertShootData(shootEntity)
        }
    }

    fun getShootData(context: Context, sku_id: String): LiveData<ShootEntity>?{
        shootDataBase = initializeDB(context)
        shootDataBase?.getShootDao()?.getShootData(sku_id)
        return shootEntity as LiveData<ShootEntity>
    }

    suspend fun uploadImage(
        project_id: RequestBody,
        sku_id: RequestBody,
        image_category: RequestBody,
        auth_key: RequestBody,
        image: MultipartBody.Part
    ) = safeApiCall {
        clipperApi.uploadImage(project_id, sku_id, image_category, auth_key, image)
    }
    suspend fun getSubCategories(
        authKey : String,prodId : String
    ) = safeApiCall {
        clipperApi.getSubCategories(authKey, prodId)
    }


}