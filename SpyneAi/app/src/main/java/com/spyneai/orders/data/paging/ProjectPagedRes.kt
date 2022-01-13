package com.spyneai.orders.data.paging


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.spyneai.shoot.repository.model.project.Project

class ProjectPagedRes : ArrayList<Project>(){
}