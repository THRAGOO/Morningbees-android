package com.jasen.kimjaeseung.morningbees.data

import android.content.SharedPreferences
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.common.BaseRepository
import com.jasen.kimjaeseung.morningbees.model.*
import com.jasen.kimjaeseung.morningbees.network.NetworkModule
import retrofit2.Response

class MorningBessRepository : BaseRepository() {
    private val service = NetworkModule.morningBeesService

    //repository에서 accessToken을 가져오는 함수를 사용하면 좋을 듯 -> 이유: SharedPreferences도 하나의 저장 수단.

    private var accessToken = GlobalApp.prefs.accessToken
    private var refreshToken = GlobalApp.prefs.refreshToken

    private var beeId = GlobalApp.prefsBeeInfo.beeId

    suspend fun checkAccessToken() : String {
//        accessToken = GlobalApp.prefs.accessToken

        if (accessToken.count() == 0) {
            val renewalRepository = safeApiCall(
                call = {service.renewal(accessToken, refreshToken)},
                error = "error"
            )
            renewalRepository?.let {
                // let 은 지정된 값이 null 이 아닌 경우에 코드를 실행해야 할 때 사용됨
                accessToken = renewalRepository.accessToken
                GlobalApp.prefs.accessToken = accessToken
            }
        }

        return accessToken
    }

    suspend fun requestMainApi(targetDate : String) : MainResponse? {
        return safeApiCall(
            call = {service.main(accessToken, targetDate, beeId)},
            error = "Error fetching news"
        )
    }

    suspend fun requestMeApi() {
        val meResponse = safeApiCall(
            call = {service.me(accessToken)},
            error = "Error fetching news"
        )

        meResponse?.let {
            beeId = meResponse.beeId
            GlobalApp.prefsBeeInfo.beeId = beeId
        }
    }

    suspend fun requestValidNicknameApi(nickname: String) =
        service.nameValidationCheck(nickname)

    suspend fun requestSignInApi(signInRequest: SignInRequest) =
        service.signIn(signInRequest)

    suspend fun requestSignUpApi(signUpRequest: SignUpRequest) =
        service.signUp(signUpRequest)

    suspend fun requestCreateBeeApi(createBeeRequest: CreateBeeRequest) =
        service.createBee(
            accessToken,
            createBeeRequest
        )

    suspend fun requestRenewalApi() =
        service.renewal(accessToken, refreshToken)

    suspend fun requestJoinBeeApi(joinBeeRequest: JoinBeeRequest) =
        service.joinBee(
            accessToken,
            joinBeeRequest
        )

    suspend fun requestBeeWithdrawalApi() =
        service.beeWithdrawal(accessToken)

    suspend fun requestMissionInfo(missionInfoRequest: MissionInfoRequest) =
        service.missionInfo(
            accessToken,
            missionInfoRequest.targetDate,
            beeId
        )

    suspend fun requestMissionCreate(missionCreateRequest: MissionCreateRequest) =
        service.missionCreate(
            accessToken,
            missionCreateRequest.image,
            beeId,
            missionCreateRequest.description,
            missionCreateRequest.type,
            missionCreateRequest.difficulty,
            missionCreateRequest.targetDate
        )

    suspend fun requestBeeInfoApi(beeInfoRequest: BeeInfoRequest) =
        service.beeInfo(
            accessToken,
            beeInfoRequest.beeId
        )

    suspend fun requestBeePenalty(beePenaltyRequest: BeePenaltyRequest) =
        service.beePenalty(
            accessToken,
            beeId,
            beePenaltyRequest.status
        )

    suspend fun requestPaidApi(paidRequest: PaidRequest) =
        service.paid(
            accessToken,
            beeId,
            paidRequest
        )
}