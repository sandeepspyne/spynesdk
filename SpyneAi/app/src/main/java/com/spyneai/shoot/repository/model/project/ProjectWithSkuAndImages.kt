package com.spyneai.shoot.repository.model.project

import androidx.room.Embedded
import androidx.room.Relation
import com.spyneai.shoot.repository.model.image.Image
import com.spyneai.shoot.repository.model.sku.Sku

data class ProjectWithSkuAndImages(
    val skus: Sku,
    val images: List<Image>?
)