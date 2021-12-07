package com.spyneai.reshoot.data

import com.spyneai.orders.data.response.ImagesOfSkuRes

class SelectedImagesHelper {
    companion object {
        var selectedOverlayIds = HashMap<Int,ReshootImage>()
        var selectedImages = ArrayList<ImagesOfSkuRes.Data>()
    }
}