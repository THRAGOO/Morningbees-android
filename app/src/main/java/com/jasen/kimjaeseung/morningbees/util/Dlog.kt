package com.jasen.kimjaeseung.morningbees.util

import android.util.Log
import com.jasen.kimjaeseung.morningbees.GlobalApp
import java.lang.StringBuilder

class Dlog { //for log in debug mode
    fun e(message : String){
        if (GlobalApp().DEBUG) Log.e(TAG,buildLogMsg(message))
    }

    fun w(message : String){
        if (GlobalApp().DEBUG) Log.w(TAG,buildLogMsg(message))
    }

    fun i(message : String){
        if (GlobalApp().DEBUG) Log.i(TAG,buildLogMsg(message))
    }

    fun d(message : String){
        if (GlobalApp().DEBUG) Log.d(TAG,buildLogMsg(message))
    }

    fun v(message : String){
        if (GlobalApp().DEBUG) Log.v(TAG,buildLogMsg(message))
    }

    fun buildLogMsg (message: String) : String{
        val ste = Thread.currentThread().stackTrace[4]
        val sb = StringBuilder()

        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);

        return sb.toString()

    }

    companion object{
        private const val TAG = "DebugLog"
    }
}