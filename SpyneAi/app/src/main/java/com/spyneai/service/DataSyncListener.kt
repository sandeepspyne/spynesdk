package com.spyneai.service


interface DataSyncListener {
    fun inProgress(title: String,type: ServerSyncTypes)
    fun onCompleted(title: String, type: ServerSyncTypes, stopService: Boolean)
    fun onConnectionLost(title: String,type: ServerSyncTypes)
}