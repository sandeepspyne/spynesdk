package com.spyneai.shoot.data

import com.spyneai.shoot.data.model.ShootData

class FileNameManager {

    fun getFileName(
        imageType : String,
        currentShoot : Int,
        list : ArrayList<ShootData>?,
        interiorSize : Int?,
        miscSize : Int?,
    ) : String{
        return when (imageType) {
            "Exterior" -> {
                imageType + "_" + currentShoot.plus(1)
            }

            "Interior" -> {
                val interiorList = list?.filter {
                    it.image_category == "Interior"
                }

                if (interiorList == null) {
                    imageType + "_" +interiorSize?.plus(1)
                } else {
                    imageType + "_" +
                            interiorSize?.plus(interiorList.size.plus(1))
                }
            }

            "Focus Shoot" -> {
                val miscList = list?.filter {
                    it.image_category == "Focus Shoot"
                }

                if (miscList == null) {
                    "Miscellaneous_" +
                            miscSize?.plus(1)
                } else {
                    "Miscellaneous_" +
                            miscSize?.plus(miscList.size.plus(1))
                }
            }

            "Footwear","Food & Beverages","Ecom","Food","Photo Box","Info" -> {
                imageType + "_" + currentShoot.plus(1)
            }

            else -> {
                System.currentTimeMillis().toString()
            }
        }
    }
}