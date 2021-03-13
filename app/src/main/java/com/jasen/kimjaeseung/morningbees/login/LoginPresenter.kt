package com.jasen.kimjaeseung.morningbees.login

class LoginPresenter : LoginContract.Presenter {
    private var loginView: LoginContract.View? = null

    override fun takeView(view: LoginContract.View) {
        loginView = view
    }

    override fun dropView() {
        loginView = null
    }
}