package com.spyneai.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.spyneai.BaseApplication
import com.spyneai.captureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootRepository

import com.spyneai.shoot.repository.model.project.ProjectBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap
import com.spyneai.base.network.Resource
import com.spyneai.base.room.AppDatabase
import com.spyneai.posthog.Events


class ProjectSkuSync(
    val context: Context,
    val db: AppDatabase,
    val listener: DataSyncListener,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false,
    var isActive: Boolean = false
) {

    val TAG = "ProjectSkuSync"

    companion object{
        @Volatile
        private var INSTANCE: ProjectSkuSync? = null

        fun getInstance(context: Context,listener: DataSyncListener): ProjectSkuSync {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = ProjectSkuSync(
                        context,
                        AppDatabase.getInstance(BaseApplication.getContext()),
                        listener
                    )

                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    fun projectSyncParent(type : String, startedBy : String?) {
        Log.d(TAG, "projectSyncParent: ")
        context.captureEvent(Events.PROJECT_SYNC_TRIGGERED,HashMap<String,Any?>().apply {
            put("type",type)
            put("service_started_by",startedBy)
            put("upload_running", isActive)
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.PROJECT_SYNC_TRIGGERED, true)

        if (Utilities.getBool(context, AppConstants.PROJECT_SYNC_TRIGGERED, true)
            &&
            !isActive
        ) {
            if (context.isInternetActive())
                GlobalScope.launch(Dispatchers.Default) {
                    Log.d(TAG, "uploadParent: start")
                    isActive = true
                    context.captureEvent(Events.PROJECT_SYNC_STARTED,HashMap())
                    startProjectSync()
                }
            else {
                isActive = false
                listener.onConnectionLost("Create Project Stopped",ServerSyncTypes.CREATE)
                Log.d(TAG, "uploadParent: connection lost")
            }
        }
    }

    suspend fun  startProjectSync(){
        do {
            val projectWithSku = db.projectDao().getProjectWithSkus()

            Log.d(TAG, "startProjectSync: "+Gson().toJson(projectWithSku))

            if (connectionLost){
                val count = db.projectDao().getPendingProjects()
                context.captureEvent(
                    Events.PROJECT_CONNECTION_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("project_remaining",count)
                        }
                )
                isActive = false
                listener.onConnectionLost("Create Project Stopped",ServerSyncTypes.CREATE)
                break
            }else {
                if (projectWithSku == null){
                    val count = db.projectDao().getPendingProjects()
                    context.captureEvent(
                        Events.ALL_PROJECTS_CREATED_BREAKS,
                        HashMap<String,Any?>().apply {
                            put("project_remaining",count)
                        }
                    )
                    break
                }else {
                    //in progress listener
                    listener.inProgress("Creating project ${projectWithSku.project?.projectName} and its sku's",ServerSyncTypes.CREATE)
                    isActive = true

                    val properties = HashMap<String,Any?>().apply {
                        put("project_id",projectWithSku.project?.uuid)
                        put("data",Gson().toJson(projectWithSku))
                    }

                    context.captureEvent(
                        Events.SELECTED_PROJECT,
                        properties
                    )

                    if (retryCount >4){
                        //skip project
                        val skip = db.projectDao().skipProject(
                            projectWithSku.project?.uuid!!,
                            projectWithSku.project.toProcessAt.plus( projectWithSku.project.retryCount * AppConstants.RETRY_DELAY_TIME)
                        )

                        context.captureEvent(
                            Events.PROJECTED_SKIPPED,
                            properties.apply {
                                put("db_count",skip)
                            }
                        )
                        continue
                    }
                    var projectBody: ProjectBody? = null

                    projectWithSku.let {
                        it.project?.let { project ->
                            val skuList = ArrayList<ProjectBody.SkuData>()

                            it.skus?.let { skus ->
                                skus.forEach { sku ->
                                    if (!sku.isCreated && sku.isSelectAble){
                                        skuList.add(
                                            ProjectBody.SkuData(
                                                skuId = sku.skuId,
                                                localId = sku.uuid,
                                                skuName = sku.skuName!!,
                                                prodCatId = sku.categoryId!!,
                                                prodSubCatId = sku.subcategoryId,
                                                initialNo = sku.initialFrames!!,
                                                totalFramesNo = sku.totalFrames!!,
                                                imagePresent = sku.imagePresent,
                                                videoPresent = sku.videoPresent
                                            ))

                                        Log.d(TAG, "startProjectSync: "+Gson().toJson(sku))
                                    }
                                }
                            }

                            projectBody = ProjectBody(
                                projectData = ProjectBody.ProjectData(
                                    projectName = project.projectName!!,
                                    localId = project.uuid,
                                    projectId = project.projectId,
                                    categoryId = project.categoryId!!,
                                    dynamicLayout = ProjectBody.ProjectData.DynamicLayout(project.dynamicLayout),
                                    locationData = ProjectBody.ProjectData.LocationData(project.locationData)
                                ),
                                skuData = skuList
                            )
                        }
                    }

                    val s = ""

                    projectBody?.let {
                        if (it.skuData.isNotEmpty()){
                            if (it.skuData[0].skuId != null && (it.skuData[0].imagePresent == 1 && it.skuData[0].videoPresent == 1)){
                                //update combo sku
                                updateComboShootSku(it.projectData.localId,it.skuData[0])
                            }else {
                                createProject(it)
                            }
                        }
                    }

                    continue
                }
            }


        }while (projectWithSku != null)

        if (!connectionLost){
            //get pending projects count
            val count = db.projectDao().getPendingProjects()

            listener.onCompleted("All Projects Created",ServerSyncTypes.CREATE)
            isActive = false

//            if (count > 0){
//                val project = db.projectDao().getOldestProject()
//                val scheduleTime = project.toProcessAt.minus(System.currentTimeMillis())
//                if (scheduleTime > 0){
//                    Log.d(TAG, "selectLastImageAndUpload: "+scheduleTime)
//                    Log.d(TAG, "selectLastImageAndUpload: "+ TimeUnit.MILLISECONDS.toMinutes(scheduleTime))
//                    Log.d(TAG, "selectLastImageAndUpload: "+project.toProcessAt.toDate())
//                    val handler = Handler(Looper.getMainLooper())
//
//                    handler.postDelayed({
//                        Log.d(TAG, "selectLastImageAndUpload: "+project.toProcessAt.toDate())
//                        GlobalScope.launch(Dispatchers.IO) {
//                            startProjectSync()
//                        }
//                    },scheduleTime)
//                }
//                else{
//                    startProjectSync()
//                }
//            }else {
//                listener.onCompleted("All Projects Created",SeverSyncTypes.CREATE,false)
//                Utilities.saveBool(context, AppConstants.PROJECT_SYNC_RUNNING, false)
//            }
        }


    }

    private suspend fun createProject(projectBody: ProjectBody): Boolean {
        Log.d(TAG, "createProject: ")
        val response = ShootRepository().createProject(projectBody)

        val properties = HashMap<String,Any?>().apply {
            put("project_id",projectBody.projectData.localId)
            put("data",Gson().toJson(projectBody))
        }

        context.captureEvent(
            Events.CREATE_PROJECT_INITIATED,
            properties
        )

        if (response is Resource.Failure){
            retryCount++
            context.captureEvent(
                Events.CREATE_PROJECT_FAILED,
                properties.apply {
                    put("project_id",projectBody.projectData.localId)
                    put("response",response)
                    put("throwable",response.throwable)
                }
            )
            return false
        }

        val res = (response as Resource.Success).value

        Log.d(TAG, "createProject: "+Gson().toJson(res))

        val projectId = res.data.projectId

        context.captureEvent(
            Events.PROJECT_CREATED,
            properties.apply {
                put("project_id",projectBody.projectData.localId)
                put("response",res)
            }
        )

        //update project
        val update = db.projectDao().updateProjectServerId(projectBody.projectData.localId,projectId)

        context.captureEvent(
            Events.PROJECT_DB_UPDATE,
            properties.apply {
                put("project_id",projectBody.projectData.localId)
                put("db_update",update)
            }
        )

        res.data.skusList.forEachIndexed { index, skus ->
            val ss = db.shootDao().updateSkuAndImageIds(projectId,skus.localId,skus.skuId)

            if (projectBody.skuData[index].prodSubCatId == "360_exterior"){
                val s = db.shootDao().updateVideoSkuAndProjectIds(projectId,skus.skuId,skus.localId)
                Log.d(TAG, "createProject: $s")
            }
        }

        retryCount = 0
        return true
    }

    private suspend fun updateComboShootSku(projecctUuid: String,sku: ProjectBody.SkuData): Boolean {
        val response = ShootRepository().updateVideoSku(sku.skuId!!, sku.prodSubCatId!!, sku.initialNo)

        val properties = HashMap<String,Any?>().apply {
            put("sku_id",sku.skuId!!)
            put("data",Gson().toJson(sku))
        }

        context.captureEvent(
            Events.UPDATE_COMBO_SKU_INTIATED,
            properties
        )

        if (response is Resource.Failure){
            retryCount++
            context.captureEvent(
                Events.UPDATE_COMBO_SKU_FAILED,
                properties.apply {
                    put("sku_id",sku.skuId!!)
                    put("response",response)
                    put("throwable",response.throwable)
                }
            )
            return false
        }

        val res = (response as Resource.Success).value

        Log.d(TAG, "createProject: "+Gson().toJson(res))

        context.captureEvent(
            Events.COMBO_SKU_UPDATED,
            properties.apply {
                put("sku_id",sku.skuId!!)
                put("response",res)
            }
        )

        //update project
        val update = db.shootDao().updateProjectAndSkuCreated(projecctUuid,sku.localId)

        context.captureEvent(
            Events.COMBO_SKU_DB_UPDATED,
            properties.apply {
                put("sku_id",sku.skuId!!)
                put("db_update",update)
            }
        )

        retryCount = 0
        return true
    }
}