package com.jasen.kimjaeseung.morningbees.createlink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import kotlin.math.min


class CreateLinkActivity : AppCompatActivity() {
    var beeid = 0
    var shortLink : Uri? = null
    var strLink : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = getIntent()
        beeid = intent.getIntExtra("beeid", 0)
        createDynamicLink()

    }

    fun createDynamicLink(){
        val invitationLink = "https://www.app.thragoo.com/?beeid=${beeid}"
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(invitationLink)
            domainUriPrefix = "https://thragoo.page.link"
            androidParameters { minimumVersion = 1 }
        }

        val dynamicLinkUri = dynamicLink.uri
        //shareLink(dynamicLinkUri)
        val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse(invitationLink)
            domainUriPrefix = "https://thragoo.page.link"

            androidParameters { minimumVersion = 1 }
        }.addOnCompleteListener(this,  OnCompleteListener<ShortDynamicLink>(){
           if (it.isSuccessful){
               shortLink = it.result?.shortLink
               strLink = shortLink.toString()
               Log.d(TAG, "addOnCompleteListener/shortLink: $shortLink")
               Log.d(TAG, "addOnCompleteListener/strLink: $strLink")

               shareLink(strLink)
           }
        })
        /*
        }.addOnSuccessListener { result ->
            shortLink = result.shortLink.toString()
            Log.d(TAG, "shortLink: $shortLink")
        }.addOnFailureListener {
            // Error
            // ...
            Log.d("log_tag", "==> ${it.localizedMessage}", it)
        }
        */
    }

    fun shareLink(shortLink : String){

        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        Log.d(TAG, "shareLink/shortLink : $shortLink")
        intent.putExtra(Intent.EXTRA_TEXT, "Try this amazing app: $shortLink")
        startActivity(Intent.createChooser(intent, "Share Link"))

        //startActivity(intent)
        finish()
    }

    companion object{
        val TAG = "createLinkActivity"
    }
}

