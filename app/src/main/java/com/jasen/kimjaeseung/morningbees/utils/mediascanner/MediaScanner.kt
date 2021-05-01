package com.jasen.kimjaeseung.morningbees.utils.mediascanner

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log

class MediaScanner private constructor(private val context: Context) {
    private var mMediaScanner: MediaScannerConnection? = null
    private var mMediaScannerClient: MediaScannerConnection.MediaScannerConnectionClient? = null

    fun mediaScanning(path: String) {
        if (mMediaScanner == null) {
            mMediaScannerClient = object : MediaScannerConnection.MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {
                    mMediaScanner!!.scanFile(path, null)
                    Log.d(TAG, "media scan success")
                }

                override fun onScanCompleted(
                    path: String,
                    uri: Uri
                ) {
                    Log.d(TAG, "media scan completed")
                    mMediaScanner!!.disconnect()
                }
            }
            mMediaScanner = MediaScannerConnection(context, mMediaScannerClient)
        }
        mMediaScanner!!.connect()
    }

    companion object {
        private const val TAG = "MediaScanner.kt"

        fun newInstance(context: Context): MediaScanner {
            return MediaScanner(context)
        }
    }
}