package com.spyneai.needs

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import com.spyneai.activity.DownloadingActivity
import java.io.File

class SingleMediaScanner(downloadingActivity: DownloadingActivity, file: File) : MediaScannerConnection.MediaScannerConnectionClient {

    private var mMs: MediaScannerConnection? = null
    private var mFile: File? = null

    fun SingleMediaScanner(context: Context?, f: File) {
        mFile = f
        mMs = MediaScannerConnection(context, this)
        mMs!!.connect()
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        mMs!!.disconnect()
    }

    override fun onMediaScannerConnected() {
        mMs!!.scanFile(mFile!!.absolutePath, null)
    }
}