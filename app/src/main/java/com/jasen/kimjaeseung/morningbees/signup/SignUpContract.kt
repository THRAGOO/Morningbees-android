package com.jasen.kimjaeseung.morningbees.signup

import com.jasen.kimjaeseung.morningbees.mvp.BasePresenter
import com.jasen.kimjaeseung.morningbees.mvp.BaseView
import com.jasen.kimjaeseung.morningbees.model.SignUpRequest

interface SignUpContract {
    interface View : BaseView{
        fun showToastView(toastMessage : () -> String)
        fun gotoMain()
        fun nicknameValidCheck(i: Int)
        fun gotoBeforeJoin()
        fun gotoLogOut()
        fun finish()
    }

    interface Presenter : BasePresenter<View>{
        fun nameValidMorningbeesServer(tempName: String)
        fun signUpMorningbeesServer(
            signUpRequest: SignUpRequest
        )
    }


}