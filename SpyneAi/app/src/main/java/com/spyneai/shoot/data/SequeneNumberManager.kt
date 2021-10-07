package com.spyneai.shoot.data

class SequeneNumberManager {

    fun getSequenceNumber(
        fromDrafts : Boolean,
        imageType : String,
        shootNumber : Int,
        shootListSize : Int,
        exteriorSize : Int,
        interiorSize : Int,
        miscSize : Int,
        sequence: Int
    ) : Int {
        if (fromDrafts) {
            return when (imageType) {
                "Exterior" -> {
                    shootNumber.plus(1)
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
                    shootNumber.plus(1)
                }
                else -> shootNumber.plus(1)
            }
        } else {
            return if (imageType == "Exterior")
                sequence.plus(1)
            else
                shootListSize.plus(1)
        }
    }
}