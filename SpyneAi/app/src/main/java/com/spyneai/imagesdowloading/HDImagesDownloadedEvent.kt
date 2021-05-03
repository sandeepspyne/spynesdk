package com.spyneai.imagesdowloading

class HDImagesDownloadedEvent {
    private var skuId: String = ""

    public fun  getSkuId(): String {
        return skuId;
    }

    public fun  setSkuId(skuId : String){
        this.skuId = skuId;
    }
}