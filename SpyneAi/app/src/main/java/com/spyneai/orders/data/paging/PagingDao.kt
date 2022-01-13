package com.spyneai.orders.data.paging

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface PagingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doggoModel: List<ProjectPagedRes.ProjectPagedResItem>) : List<Long>

    @Transaction
    suspend fun insertWithCheck(response: List<ProjectPagedRes.ProjectPagedResItem>){
        val list = ArrayList<ProjectPagedRes.ProjectPagedResItem>()

        response.forEach {
            if (it.status == "Done")
                it.status = "completed"

            if (it.status == "In Progress")
                it.status = "ongoing"

            it.status = it.status.lowercase()

            val dbItem = getProject(it.projectId)

            if (dbItem == null){
                list.add(it)
            }else {
                if (it.totalSku > dbItem.totalSku)
                    list.add(it)
            }

        }

        insertAll(list)
    }
    @Query("SELECT * FROM projectpagedresitem where status = :status")
    fun getAllProjects(status: String = "Draft"): PagingSource<Int, ProjectPagedRes.ProjectPagedResItem>

    @Query("SELECT * FROM projectpagedresitem where projectId = :projectId")
    fun getProject(projectId: String): ProjectPagedRes.ProjectPagedResItem

    @Query("DELETE FROM projectpagedresitem")
    suspend fun clearAllProjects()

    @Query("SELECT * FROM projectpagedresitem where status = :status LIMIT :limit OFFSET :offset")
    suspend fun getProjectsWithLimitAndSkip(offset: Int,status: String = "Draft",limit: Int = 10) : List<ProjectPagedRes.ProjectPagedResItem>

//    @Query("SELECT * FROM projectpagedresitem where status = :status ORDER BY createdOn DESC LIMIT :limit OFFSET :offset")
//    suspend fun getProjectsWithLimitAndSkip(offset: Int,status: String = "Draft",limit: Int = 10) : List<ProjectPagedRes.ProjectPagedResItem>


}