package com.spyneai.service


interface DataSyncListener {
    fun inProgress(title: String,type: ServerSyncTypes)
    fun onCompleted(title: String, type: ServerSyncTypes)
    fun onConnectionLost(title: String,type: ServerSyncTypes)
}