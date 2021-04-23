package com.spyneai.videorecording.model

class ProcessVideoEvent {
    private var skuId: String = ""

    public fun  getSkuId(): String {
        return skuId;
    }

    public fun  setSkuId(shootStatus:String){
        this.skuId = shootStatus;
    }
}