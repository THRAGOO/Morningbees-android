package com.jasen.kimjaeseung.morningbees.common

import android.util.Log
import retrofit2.Response
import java.io.IOException

open class BaseRepository {
    // suspend() 함수를 call 매개변수로 보냄
    // Kotlin High-Order Functions: 함수를 변수로 넘겨주거나 이를 반환
    // -> 뒤의 Response<T>, error : String 은 반환값

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>, error: String): T? {
        val result = safeApiCallOutput(call, error)
        var output: T? = null

        when (result) {
            is Output.Success -> output = result.output
            is Output.Error -> Log.e("error", "error")
        }
        return output
    }

    private suspend fun <T : Any> safeApiCallOutput(
        call: suspend () -> Response<T>, error: String
    ): Output<T> {
        val response = call.invoke()

        return if (response.isSuccessful)
            Output.Success(response.body()!!)
        else
            Output.Error(IOException("oops"))
    }
}