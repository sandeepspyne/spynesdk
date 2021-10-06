package com.spyneai.shoot.data

import com.spyneai.needs.AppConstants
import com.spyneai.shoot.data.model.ShootData

class FileNameManager {

    fun getFileName(
        fromDrafts : Boolean,
        imageType : String,
        shootNumber : Int,
        sequence : Int,
        list : ArrayList<ShootData>?,
        interiorSize : Int?,
        miscSize : Int?
    ) : String{
        if (fromDrafts){
            return when (imageType) {
                "Exterior" -> {
                    imageType + "_" + shootNumber.plus(
                        1
                    )
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
                "Footwear" -> {
                    imageType + "_" + shootNumber.plus(
                        1
                    )
                }
                "Food & Beverages" -> {
                    imageType + "_" + shootNumber.plus(
                        1
                    )
                }
                "Ecom" -> {
                    imageType + "_" + shootNumber.plus(
                        1
                    )
                }
                else -> {
                    System.currentTimeMillis().toString()
                }
            }
        }else{
            return when (imageType) {
                "Exterior" -> {
                    imageType + "_" + sequence.plus(1)
                }
                "Interior" -> {
                    val interiorList = list?.filter {
                        it.image_category == "Interior"
                    }

                    if (interiorList == null) {
                        imageType + "_1"
                    } else {
                        imageType + "_" + interiorList.size.plus(
                            1
                        )
                    }
                }
                "Focus Shoot" -> {
                    val miscList = list?.filter {
                        it.image_category == "Focus Shoot"
                    }

                    if (miscList == null) {
                        "Miscellaneous" + "_1"
                    } else {
                        "Miscellaneous_" + miscList.size.plus(1)
                    }
                }
                "Footwear" -> {
                    imageType + "_" + shootNumber.plus(
                        1
                    )
                }
                "Food" -> {
                    imageType + "_" + shootNumber.plus(
                        1
                    )
                }
                "Ecom" -> {
                    imageType + "_" + shootNumber.plus(
                        1
                    )
                }
                else -> {
                    System.currentTimeMillis().toString()
                }
            }
        }

    }


//    if (viewModel.fromDrafts) {
//        filename += when (viewModel.categoryDetails.value?.imageType) {
//            "Exterior" -> {
//                viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
//                    1
//                )
//            }
//            "Interior" -> {
//                val list = viewModel.shootList.value
//
//                val interiorList = list?.filter {
//                    it.image_category == "Interior"
//                }
//
//                if (interiorList == null) {
//                    viewModel.categoryDetails.value?.imageType!! + "_" +
//                            requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0)
//                                .plus(1)
//                } else {
//                    viewModel.categoryDetails.value?.imageType!! + "_" +
//                            requireActivity().intent.getIntExtra(AppConstants.INTERIOR_SIZE, 0)
//                                .plus(interiorList.size.plus(1))
//                }
//
//            }
//            "Focus Shoot" -> {
//                val list = viewModel.shootList.value
//
//                val miscList = list?.filter {
//                    it.image_category == "Focus Shoot"
//                }
//
//                if (miscList == null) {
//                    "Miscellaneous_" +
//                            requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
//                                .plus(1)
//                } else {
//                    "Miscellaneous_" +
//                            requireActivity().intent.getIntExtra(AppConstants.MISC_SIZE, 0)
//                                .plus(miscList.size.plus(1))
//                }
//            }
//            "Footwear" -> {
//                viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
//                    1
//                )
//            }
//            "Food & Beverages" -> {
//                viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
//                    1
//                )
//            }
//            "Ecom" -> {
//                viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
//                    1
//                )
//            }
//            else -> {
//                System.currentTimeMillis().toString()
//            }
//        }
//    } else {
//        filename += if (viewModel.shootList.value == null)
//            viewModel.categoryDetails.value?.imageType + "_1"
//        else {
//            val size = viewModel.shootList.value!!.size.plus(1)
//            val list = viewModel.shootList.value
//
//            when (viewModel.categoryDetails.value?.imageType) {
//                "Exterior" -> {
//                    viewModel.categoryDetails.value?.imageType!! + "_" + size
//                }
//                "Interior" -> {
//
//                    val interiorList = list?.filter {
//                        it.image_category == "Interior"
//                    }
//
//                    if (interiorList == null) {
//                        viewModel.categoryDetails.value?.imageType!! + "_1"
//                    } else {
//                        viewModel.categoryDetails.value?.imageType!! + "_" + interiorList.size.plus(
//                            1
//                        )
//                    }
//                }
//                "Focus Shoot" -> {
//                    val miscList = list?.filter {
//                        it.image_category == "Focus Shoot"
//                    }
//
//                    if (miscList == null) {
//                        "Miscellaneous" + "_1"
//                    } else {
//                        "Miscellaneous_" + miscList.size.plus(1)
//                    }
//                }
//                "Footwear" -> {
//                    viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
//                        1
//                    )
//                }
//                "Food" -> {
//                    viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
//                        1
//                    )
//                }
//                "Ecom" -> {
//                    viewModel.categoryDetails.value?.imageType!! + "_" + viewModel.shootNumber.value?.plus(
//                        1
//                    )
//                }
//                else -> {
//                    System.currentTimeMillis().toString()
//                }
//            }
//        }
//    }
}