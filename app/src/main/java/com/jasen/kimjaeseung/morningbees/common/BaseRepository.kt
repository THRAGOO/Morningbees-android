package com.jasen.kimjaeseung.morningbees.common

import android.util.Log
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.ErrorResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.network.NetworkModule
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.lang.Exception

open class BaseRepository {
//    private val service = NetworkModule.morningBeesService
//
//    private var accessToken = GlobalApp.prefs.accessToken
//    private var refreshToken = GlobalApp.prefs.refreshToken

    // suspend() 함수를 call 매개변수로 보냄
    // Kotlin High-Order Functions: 함수를 변수로 넘겨주거나 이를 반환
    // -> 뒤의 Response<T>, error : String 은 반환값

    // safeApiCall 함수 안에서 statusCode == 400, 500 등에 대한 에러 처리 코드가 구현되어야할 것 같다.
    // 왜냐하면 MorningbeesRepository에서 에러 핸들링을 하게 되면 중복코드 많이 생길 것으로 예상됨..
    // 아니면 MorningbeesRepository에서 에러 핸들링해주는 함수 구현해도 ㄱㅊ을듯

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, error: String): Output<T>? {
        // withContext를 사용하는 방법도 있었으나, thread 를 변경해줘야할 이유를 잘 모르겠어서 적용 X
        try {
            val response = call.invoke()
            return Output.Success(response.body()!!)
        } catch (throwable: Throwable) {
            return when (throwable) {
                is IOException -> Output.NetworkError

                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    Output.Error(code, errorResponse)
                }

                else -> {
                    Output.Error(null, null)
                }
            }
        }
    }

    private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
        return try {
            NetworkModule.retrofit.responseBodyConverter<ErrorResponse>(
                ErrorResponse::class.java,
                ErrorResponse::class.java.annotations
            ).convert(throwable.response().errorBody())
        } catch (e: Exception) {
            null
        }
    }
}