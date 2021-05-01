package com.jasen.kimjaeseung.morningbees.ui.signin

class LoginPresenter : LoginContract.Presenter {
    private var loginView: LoginContract.View? = null

    override fun takeView(view: LoginContract.View) {
        loginView = view
    }

    override fun dropView() {
        loginView = null
    }
}