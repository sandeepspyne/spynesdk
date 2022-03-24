package com.spyneai.shoot.repository.model.sku

import android.util.Log
import androidx.room.*
import com.spyneai.getTimeStamp
import com.spyneai.getUuid
import com.spyneai.needs.AppConstants
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project

@Dao
interface SkuDao {

    @Transaction
    fun saveSku(sku : Sku,project: Project){
        val skuid = insertSku(sku)
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: $skuid")
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: "+sku.projectUuid)
        val projectudpate = updateProjectSkuCount(project.uuid)
        Log.d(AppConstants.SHOOT_DAO_TAG, "saveSku: $projectudpate")
    }

    @Query("UPDATE project SET skuCount = skuCount + 1, isCreated = :isCreated WHERE uuid =:uuid ")
    fun updateProjectSkuCount(uuid: String,isCreated: Boolean = false) : Int

    @Insert
    fun insertSku(obj: Sku) : Long

    @Update
    fun updateSku(sku: Sku): Int

    @Query("update sku set totalFramesUpdated =:isTotalFramesUpdated, isProcessed = :isProcessed where uuid = :uuid ")
    fun updateSkuProcessed(uuid: String,isTotalFramesUpdated: Boolean = true, isProcessed: Boolean = true)

    @Query("select COUNT(*) from sku where isProcessed = :isProcessed and isCreated = :isCreated and backgroundId != 'DEFAULT_BG_ID'")
    fun getPendingSku(isProcessed: Boolean = false,isCreated: Boolean = true) : Int

    @Query("select * from sku where isProcessed = :isProcessed and isCreated = :isCreated LIMIT :limit")
    fun getOldestSku(isProcessed: Boolean = false,isCreated: Boolean = true,limit: Int = 1): Sku

    @Query("select * from sku")
    fun getAllSKus() : List<Sku>

    @Query("SELECT * FROM sku where projectUuid = :projectUuid")
    fun getSkusByProjectId(projectUuid: String) : List<Sku>

    @Query("SELECT * FROM sku where backgroundId = :backgroundId and projectUuid = :projectUuid")
    fun getDraftSkusByProjectId(projectUuid: String,backgroundId: String = AppConstants.DEFAULT_BG_ID) : List<Sku>


    @Query("select * from sku where projectUuid = :uuid")
    fun getSkuWithProjectUuid(uuid: String) : Sku


    @Query("SELECT * FROM sku where projectUuid = :projectUuid order by createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getSkusWithLimitAndSkip(offset: Int,projectUuid: String,limit: Int = 50) : List<Sku>

    @Transaction
    suspend fun insertSkuWithCheck(response: ArrayList<Sku>, projectUuid: String,projectId: String? = null){
        val list = ArrayList<Sku>()

        response.forEach {
            if (it.uuid ==  null)
                it.uuid = getUuid()

            it.projectUuid = projectUuid
            it.projectId = projectId

            if (it.status == "Done"){
                it.status = "completed"
                it.isProcessed = true
            }

            if (it.status == "In Progress"){
                it.status = "ongoing"
                it.isProcessed = true
            }


            it.status = it.status.lowercase()

            val dbItem = getSku(it.uuid)

            if (dbItem == null){
                val sku = getSkuBySkuId(it.skuId)

                if (sku == null){
                    if (it.backgroundId == null)
                        it.backgroundId = AppConstants.DEFAULT_BG_ID

                    it.createdAt = getTimeStamp(it.createdOn)

                    list.add(it)
                }

            }else {
                if (it.backgroundId == null){
                    it.backgroundId = dbItem.backgroundId
                    it.backgroundName = dbItem.backgroundName
                }

                if (it.imagesCount > dbItem.imagesCount)
                    list.add(it)
            }

        }

        insertAll(list)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skuList: List<Sku>) : List<Long>


    @Query("select * from sku where uuid = :uuid")
    fun getSku(uuid: String) : Sku

    @Query("select * from sku where skuId = :skuId")
    fun getSkuBySkuId(skuId: String?) : Sku

    @Query("Select * from sku where isProcessed = :isProcessed and isCreated = :isCreated and backgroundId != 'DEFAULT_BG_ID' and toProcessAt <= :currentTime LIMIT :limit")
    fun getProcessAbleSku(isProcessed: Boolean = false, isCreated: Boolean = true, currentTime: Long = System.currentTimeMillis(),limit: Int = 1) : Sku


    @Query("update sku set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid ")
    fun skipSku(uuid: String,toProcessAt: Long) : Int
}