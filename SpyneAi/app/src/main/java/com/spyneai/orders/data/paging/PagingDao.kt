package com.spyneai.orders.data.paging

import androidx.paging.PagingSource
import androidx.room.*
import com.spyneai.BaseApplication
import com.spyneai.getUuid
import com.spyneai.shoot.repository.model.project.Project

@Dao
interface PagingDao {

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

            if (it.status == "In Progress")
                it.status = "ongoing"

            it.status = it.status.lowercase()

            val dbItem = getProject(it.projectId!!)

            if (dbItem == null){
                list.add(it)
            }else {
                if (it.skuCount > dbItem.skuCount)
                    list.add(it)
            }

        }

        insertAll(list)
    }

    @Query("SELECT * FROM project where status = :status")
    fun getAllProjects(status: String = "Draft"): PagingSource<Int, Project>

    @Query("SELECT * FROM project where projectId = :projectId")
    fun getProject(projectId: String): Project

    @Query("DELETE FROM project")
    suspend fun clearAllProjects()

    @Query("SELECT * FROM project where status = :status LIMIT :limit OFFSET :offset")
    suspend fun getProjectsWithLimitAndSkip(offset: Int,status: String = "Draft",limit: Int = 10) : List<Project>

//    @Query("SELECT * FROM projectpagedresitem where status = :status ORDER BY createdOn DESC LIMIT :limit OFFSET :offset")
//    suspend fun getProjectsWithLimitAndSkip(offset: Int,status: String = "Draft",limit: Int = 10) : List<Project>


}