package com.spyneai.shoot.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.spyneai.base.room.User

import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.project.Project
import com.spyneai.shoot.repository.model.sku.Sku

@Dao
interface ShootDao {

    @Insert
    fun insertProject(obj: Project) : Long

    @Query("SELECT * FROM project where status = 'draft'")
    fun getDraftProjects(): List<Project>

    @Insert
    fun insertSku(obj: Sku) : Long

    @Insert
    fun insertImage(obj: Image) : Long
}