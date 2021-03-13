package com.jasen.kimjaeseung.morningbees.network

import android.graphics.Bitmap
import android.os.AsyncTask

class getImageFromURL: AsyncTask<String, Void, Bitmap>() {
    lateinit var imgPath : String
    lateinit var bitmap : Bitmap

    override fun doInBackground(vararg params: String?): Bitmap {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getBitmap(){
        val imgThread : Thread = Thread(){

        }
    }
}