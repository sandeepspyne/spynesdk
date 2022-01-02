package com.spyneai.dashboard.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.spyneai.dashboard.repository.model.category.DynamicLayout
import com.spyneai.dashboard.repository.model.category.ProjectDialog
import com.spyneai.dashboard.response.NewCategoriesResponse


@Dao
interface DashboardDao {

    @Insert
    fun insert(obj: List<NewCategoriesResponse.Category>) : List<Long>

    @Transaction
    fun saveCategoriesData(
        categories: List<NewCategoriesResponse.Category>,
        dynamicLayout: List<DynamicLayout>){
        insert(categories)
    }

    @Query("SELECT * FROM category")
    fun getAll(): List<NewCategoriesResponse.Category>
}