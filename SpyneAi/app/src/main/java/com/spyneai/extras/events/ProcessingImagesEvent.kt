package com.spyneai.extras.events

class ProcessingImagesEvent {

    private var shootStatus: String = ""

    public fun  getShootStatus(): String {
        return shootStatus;
    }

    public fun  setShootStatus(shootStatus:String){
        this.shootStatus = shootStatus;
    }

}