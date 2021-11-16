package com.spyneai.shoot.data

import com.spyneai.BaseApplication
import com.spyneai.base.network.Resource
import com.spyneai.needs.AppConstants
import com.spyneai.needs.Utilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateExteriorAngles(
    val skuId : String,
    val angles : Int,
    val subcatId : String
) {

    fun update(){
        GlobalScope.launch(Dispatchers.Default){
            val response = ShootRepository().updateFootwearSubcategory(
                Utilities.getPreference(BaseApplication.getContext(), AppConstants.AUTH_KEY).toString(),
                skuId,
                angles,
                subcatId
            )

            when(response){
                is Resource.Failure -> {
                    update()
                }
            }
        }
    }
}