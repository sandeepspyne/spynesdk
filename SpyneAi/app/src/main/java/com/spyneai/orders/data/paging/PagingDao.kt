package com.spyneai.orders.data.paging

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PagingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doggoModel: List<ProjectPagedRes.ProjectPagedResItem>) : List<Long>

    @Query("SELECT * FROM projectpagedresitem")
    fun getAllProjects(): PagingSource<Int, ProjectPagedRes.ProjectPagedResItem>

    @Query("DELETE FROM projectpagedresitem")
    suspend fun clearAllProjects()
}