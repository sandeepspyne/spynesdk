package com.spyneai.shoot.repository.model.project

import androidx.room.Embedded
import androidx.room.Relation
import com.spyneai.shoot.repository.model.sku.Sku

data class ProjectWithSku(
    @Embedded val project: Project?,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "project_uuid"
    )
    val skus: List<Sku>?
)