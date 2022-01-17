package com.spyneai.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.spyneai.captureEvent
import com.spyneai.isInternetActive
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import com.spyneai.shoot.data.ShootRepository
import com.spyneai.shoot.repository.db.ShootDao
import com.spyneai.shoot.repository.model.project.ProjectBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap
import com.spyneai.base.network.Resource
import com.spyneai.posthog.Events

class ProjectSkuSync(
    val context: Context,
    val shootDao: ShootDao,
    val listener: DataSyncListener,
    var retryCount: Int = 0,
    var connectionLost: Boolean = false
) {

    val TAG = "ProjectSkuSync"

    fun projectSyncParent(type : String, startedBy : String?) {
        Log.d(TAG, "projectSyncParent: ")
        context.captureEvent(Events.PROJECT_SYNC_TRIGGERED,HashMap<String,Any?>().apply {
            put("type",type)
            put("service_started_by",startedBy)
            put("upload_running", Utilities.getBool(context, AppConstants.PROJECT_SYNC_RUNNING, false))
        })

        //update triggered value
        Utilities.saveBool(context, AppConstants.PROJECT_SYNC_TRIGGERED, true)

        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            if (Utilities.getBool(context, AppConstants.PROJECT_SYNC_TRIGGERED, true)
                &&
                !Utilities.getBool(context, AppConstants.PROJECT_SYNC_RUNNING, false)
            ) {
                if (context.isInternetActive())
                    GlobalScope.launch(Dispatchers.Default) {
                        Log.d(TAG, "uploadParent: start")
                        Utilities.saveBool(context, AppConstants.PROJECT_SYNC_RUNNING, true)
                        context.captureEvent(Events.PROJECT_SYNC_STARTED,HashMap())
                        startProjectSync()
                    }
                else {
                    Utilities.saveBool(context, AppConstants.PROJECT_SYNC_RUNNING, false)
                    listener.onConnectionLost("Create Project Stopped",ServerSyncTypes.CREATE)
                    Log.d(TAG, "uploadParent: connection lost")
                }
            }
        }, getRandomNumberInRange().toLong())
    }

    suspend fun  startProjectSync(){
        do {
            val projectWithSku = shootDao.getProjectWithSkus()

            Log.d(TAG, "startProjectSync: "+Gson().toJson(projectWithSku))

            if (connectionLost){
                val count = shootDao.getPendingProjects()
                context.captureEvent(
                    Events.PROJECT_CONNECTION_BREAK,
                    HashMap<String,Any?>()
                        .apply {
                            put("project_remaining",count)
                        }
                )
                Utilities.saveBool(context,AppConstants.PROJECT_SYNC_RUNNING,false)
                listener.onConnectionLost("Create Project Stopped",ServerSyncTypes.CREATE)
                break
            }else {
                if (projectWithSku == null){
                    val count = shootDao.getPendingProjects()
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
                        val skip = shootDao.skipProject(
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
                                    categoryId = project.categoryId!!,
                                    dynamicLayout = ProjectBody.ProjectData.DynamicLayout(project.dynamicLayout),
                                    locationData = ProjectBody.ProjectData.LocationData(project.locationData)
                                ),
                                skuData = skuList
                            )
                        }
                    }

                    createProject(projectBody!!)

                    continue
                }
            }


        }while (projectWithSku != null)

        if (!connectionLost){
            //get pending projects count
            val count = shootDao.getPendingProjects()

            listener.onCompleted("All Projects Created",ServerSyncTypes.CREATE,false)
            Utilities.saveBool(context, AppConstants.PROJECT_SYNC_RUNNING, false)

//            if (count > 0){
//                val project = shootDao.getOldestProject()
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
        val update = shootDao.updateProjectServerId(projectBody.projectData.localId,projectId)

        context.captureEvent(
            Events.PROJECT_DB_UPDATE,
            properties.apply {
                put("project_id",projectBody.projectData.localId)
                put("db_update",update)
            }
        )

        res.data.skusList.forEach {
            shootDao.updateSkuAndImageIds(projectId,it.localId,it.skuId)
        }

        retryCount = 0
        return true
    }

    fun getRandomNumberInRange(): Int {
        val r = Random()
        return r.nextInt(100 - 10 + 1) + 10
    }
}