package com.spyneai.shoot.data.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "shoot_table")
data class ShootEntity(
    //shootData
    @ColumnInfo(name = "image_id")
    var image_id: String? = null,
    @ColumnInfo(name = "auth_key")
    var auth_key: String? = null,
    @ColumnInfo(name = "total_images")
    var total_images: Int? = null,
    @ColumnInfo(name = "image_uploaded")
    var image_uploaded: Int? = null,
    @ColumnInfo(name = "sku_id")
    var sku_id: String? = null,
    @ColumnInfo(name = "category_name")
    var category_name: String? = null,

    //car
    @ColumnInfo(name = "background_id")
    var background_id: String? = null,
    @ColumnInfo(name = "dealership_logo")
    var dealership_logo: String? = null,

    //footwear
    @ColumnInfo(name = "marketplace_id")
    var marketplace_id: String? = null,
    @ColumnInfo(name = "background_colour")
    var background_colour: String? = null,
    ){
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var Id: Int? = null
}