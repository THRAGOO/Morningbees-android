package com.jasen.kimjaeseung.morningbees.ui.signin

class SignInPresenter : SignInContract.Presenter {
    private var loginView: SignInContract.View? = null

    override fun takeView(view: SignInContract.View) {
        loginView = view
    }

    override fun dropView() {
        loginView = null
    }
}