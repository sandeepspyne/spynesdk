package com.spyneai.shoot.repository.model.project

import androidx.paging.PagingSource
import androidx.room.*
import com.spyneai.getTimeStamp
import com.spyneai.getUuid
import java.lang.Exception

@Dao
interface ProjectDao {

    @Insert
    fun insertProject(obj: Project) : Long

    @Update
    fun updateProject(project: Project): Int

    @Query("update project set projectId = :projectId, isCreated = :isCreated where uuid = :uuid")
    fun updateProjectServerId(uuid: String,projectId: String,isCreated: Boolean = true): Int

    @Query("select COUNT(*) from project where isCreated = :isCreated ")
    fun getPendingProjects(isCreated: Boolean = false) : Int

    @Query("select * from project where isCreated = :isCreated LIMIT :limit")
    fun getOldestProject(isCreated: Boolean = false,limit: Int = 1) : Project

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doggoModel: List<Project>) : List<Long>

    @Transaction
    suspend fun insertWithCheck(response: List<Project>){
        val list = ArrayList<Project>()

        response.forEach {
            if (it.uuid ==  null)
                it.uuid = getUuid()

            if (it.status == "Done")
                it.status = "completed"

            if (it.status == "In Progress" || it.status == "Yet to Start")
                it.status = "ongoing"

            it.status = it.status.lowercase()
            it.isCreated = true

            val dbItem = getProject(it.uuid)

            if (dbItem == null){
                val project = getProjectByProjectId(it.projectId)
                if (project == null){
                    it.createdAt = getTimeStamp(it.createdOn)
                    list.add(it)
                }

            }else {
                if (it.skuCount > dbItem.skuCount || it.processedCount > dbItem.processedCount)
                    list.add(it)
            }
        }

        insertAll(list)
    }



    @Query("Select * from project where projectId = :projectId")
    fun getProjectByProjectId(projectId: String?) : Project


    @Query("SELECT * FROM project where status = :status")
    fun getAllProjects(status: String = "draft"): PagingSource<Int, Project>

    @Query("SELECT * FROM project where uuid = :uuid")
    fun getProject(uuid: String): Project

    @Query("DELETE FROM project")
    suspend fun clearAllProjects()

    @Query("SELECT * FROM project where status = :status order by createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getProjectsWithLimitAndSkip(offset: Int,status: String = "Draft",limit: Int = 10) : List<Project>

    @Query("select * from project")
    fun getAllProjects() : List<Project>

    @Query("Select * from project where isCreated = :isCreated  and toProcessAt <= :currentTime LIMIT :limit")
    fun getProjectWithSkus(isCreated: Boolean = false, currentTime: Long = System.currentTimeMillis(),limit: Int = 1) : ProjectWithSku

    @Query("update project set toProcessAt = :toProcessAt, retryCount = retryCount + 1 where uuid = :uuid ")
    fun skipProject(uuid: String,toProcessAt: Long) : Int


    @Query("select * from project where projectName = :projectName ")
    suspend fun getProjectByName(projectName: String) : Project

    @Query("SELECT * FROM project where status = 'draft'")
    fun getDraftProjects(): List<Project>

}