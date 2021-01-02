package com.jasen.kimjaeseung.morningbees.signup

import com.jasen.kimjaeseung.morningbees.mvp.BasePresenter
import com.jasen.kimjaeseung.morningbees.mvp.BaseView
import com.jasen.kimjaeseung.morningbees.model.signup.SignUpRequest

//contract 인터페이스 = 해당 view와 presenter가 어떤 메소드를 가져야한다는 것을 보여줌
interface SignUpContract {
    //presenter에서 view를 업데이트하기 위한 이벤트
    interface View : BaseView{
        fun showToastView(toastMessage : () -> String)
        //fun showToastMessage(msg: String)
       // fun gotoMain(accessToken : String, refreshToken : String)
        fun nicknameValidCheck(i: Int)
        fun gotoBeeCreate(accessToken: String, refreshToken: String)

    }

    //view에서 호출할때 이벤트
    interface Presenter : BasePresenter<View>{
        fun nameValidMorningbeesServer(tempName: String)
        fun signUpMorningbeesServer(
            signUpRequest: SignUpRequest
        )
        fun meServer(accessToken : String, refreshToken : String)
    }


}