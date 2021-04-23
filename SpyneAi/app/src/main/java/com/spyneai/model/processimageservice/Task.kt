package com.spyneai.model.processImageService

import com.spyneai.model.sku.Photos
import java.io.File

class Task {



    var skuName: String = ""
    var skuId: String = ""
    var shootId: String = ""
    var tokenId: String = ""
    var windows: String = ""
    var exposures: String = ""

    var catName: String = ""
    var marketplaceId: String = ""
    var backgroundColour: String = ""

    var backgroundSelect: String = ""

    var imageFileList = ArrayList<File>()
    var imageFileListFrames = ArrayList<Int>()

    var imageInteriorFileList = ArrayList<File>()
    var imageInteriorFileListFrames = ArrayList<Int>()

    var imageFocusedFileList = ArrayList<File>()
    var imageFocusedFileListFrames = ArrayList<Int>()

    var totalImagesToUploadIndex = 0

    var totalImagesToUpload = 0

    var isCompleted = false

    var isFailure = false

    var mainImage: String = ""
    var mainImageInterior: String = ""
    var mainImageFocused: String = ""

    var photoList = ArrayList<Photos>()
    var photoListInteriors = ArrayList<Photos>()
    var photoListFocused = ArrayList<Photos>()

    var afterImagesCar = ArrayList<String>()

    var dealershipLogo: String = ""
    var dealershipCornerList = ArrayList<String>()

    var countGif: Int = 0

    var cornerCount: Int = 0

    var cornerPosition: String = ""

    var imageList = ArrayList<String>()
    var imageListAfter = ArrayList<String>()
    var interiorList = ArrayList<String>()

    var gifLink: String = ""

    var processingRetry: Int = 0
    var uploadingRetry: Int = 0

    var retryCount: Int = 5




}