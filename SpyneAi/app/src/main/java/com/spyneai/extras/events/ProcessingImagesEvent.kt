package com.spyneai.extras.events

class ProcessingImagesEvent {

    private var notificationID: Int = 0

    public fun  getNotificationID(): Int {
        return notificationID;
    }

    public fun  setNotificationID(notificationID:Int){
        this.notificationID = notificationID;
    }

}