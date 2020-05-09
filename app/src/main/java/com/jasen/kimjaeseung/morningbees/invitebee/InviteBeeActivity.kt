package com.jasen.kimjaeseung.morningbees.invitebee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService

import kotlinx.android.synthetic.main.activity_invite_bee.*


class InviteBeeActivity (private val callbackListener : CallbackListener) : DialogFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(arguments != null){
            beeNameText.text = (arguments!!.getString("beeid") + "에 참여해")
        }

        isCancelable = false
        return inflater.inflate(R.layout.activity_invite_bee, container, false)
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accept_invitebee_button.setOnClickListener{
            //초대 수락 -> 초대받은 bee main으로 감
            //send back data to PARENT fragment using callback
            callbackListener.onDataReceived("accept")
            dismiss()
        }

        close_inviteView_button.setOnClickListener{
            callbackListener.onDataReceived("deny")
            dismiss()
        }
    }
}


