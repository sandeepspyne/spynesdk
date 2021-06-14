package com.spyneai.shoot.data.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.spyneai.shoot.data.room.entities.ShootEntity

@Dao
interface ShootDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShootData(shootEntity: ShootEntity): Long

    @Query("SELECT * FROM shoot_table WHERE sku_id =:sku_id")
    fun getShootData(sku_id: String?): LiveData<ShootEntity>


    @Query("DELETE FROM shoot_table WHERE sku_id = :sku_id")
    fun deleteShootData(sku_id: String?, shootEntity: ShootEntity)

    @Query("Update shoot_table SET total_images=:total_images WHERE sku_id =:sku_id")
    fun updateTotalImages(total_images: String, sku_id: String?)

    @Query("Update shoot_table SET image_uploaded=:image_uploaded WHERE sku_id =:sku_id")
    fun updateImageUploaded(image_uploaded: String, sku_id: String?)

    //car

    @Query("Update shoot_table SET background_id=:background_id WHERE sku_id =:sku_id")
    fun updateBackgroundId(background_id: String, sku_id: String?)

    @Query("Update shoot_table SET dealership_logo=:dealership_logo WHERE sku_id =:sku_id")
    fun updateDealershipLogo(dealership_logo: String, sku_id: String?)

    //footwear

    @Query("Update shoot_table SET marketplace_id=:marketplace_id WHERE sku_id =:sku_id")
    fun updateMarketplaceId(marketplace_id: String, sku_id: String?)

    @Query("Update shoot_table SET background_colour=:background_colour WHERE sku_id =:sku_id")
    fun updateBackgroundColour(background_colour: String, sku_id: String?)


}