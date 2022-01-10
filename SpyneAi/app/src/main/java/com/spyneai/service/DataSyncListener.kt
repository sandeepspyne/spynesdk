package com.spyneai.service


interface DataSyncListener {
    fun inProgress(title: String,type: SeverSyncTypes)
    fun onCompleted(title: String,type: SeverSyncTypes,stopService: Boolean)
    fun onConnectionLost(title: String,type: SeverSyncTypes)
}