package com.spyneai.shoot.data

class SequeneNumberManager {

    fun getSequenceNumber(
        fromDrafts : Boolean,
        imageType : String,
        currentShoot : Int,
        shootListSize : Int,
        exteriorSize : Int,
        interiorSize : Int,
        miscSize : Int,
        sequence: Int
    ) : Int {
        if (fromDrafts) {
            return when (imageType) {
                "Exterior" -> {
                    currentShoot
                }
                "Interior" -> {
                        exteriorSize.plus(interiorSize).plus(shootListSize.plus(1))
                }
                "Focus Shoot" -> {
                    exteriorSize.plus(interiorSize)
                        .plus(miscSize)
                        .plus(shootListSize
                            .plus(1))
                }
                "Footwear","Food & Beverages","E-Commerce" -> {
                    currentShoot
                }
                else -> currentShoot
            }
        } else {
            return if (imageType == "Exterior")
                sequence.plus(1)
            else
                shootListSize.plus(1)
        }
    }
}