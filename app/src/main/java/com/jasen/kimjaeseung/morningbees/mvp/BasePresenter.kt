package com.jasen.kimjaeseung.morningbees.mvp

interface BasePresenter<T> {
    fun takeView(view: T)
    fun dropView()
}