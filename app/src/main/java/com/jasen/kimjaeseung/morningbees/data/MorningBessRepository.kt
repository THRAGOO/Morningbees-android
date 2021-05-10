package com.jasen.kimjaeseung.morningbees.data

import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.common.BaseRepository
import com.jasen.kimjaeseung.morningbees.common.Output
import com.jasen.kimjaeseung.morningbees.model.*
import com.jasen.kimjaeseung.morningbees.network.NetworkModule

class MorningBessRepository : BaseRepository() {
    private val service = NetworkModule.morningBeesService

    //repository에서 accessToken을 가져오는 함수를 사용하면 좋을 듯 -> 이유: SharedPreferences도 하나의 저장 수단.

    private var accessToken = GlobalApp.prefs.accessToken
    private var refreshToken = GlobalApp.prefs.refreshToken

    private var beeId = GlobalApp.prefsBeeInfo.beeId

    suspend fun requestRenewalApi() : Output<RenewalResponse>? {
        accessToken = GlobalApp.prefs.accessToken

        return safeApiCall(
            call = {service.renewal(accessToken, refreshToken)}, // 코루틴을 사용하기 때문에,
            error = "error"
        )
//        if (accessToken.count() == 0) {
//            val renewalRepository = safeApiCall(
//                call = {service.renewal(accessToken, refreshToken)}, // 코루틴을 사용하기 때문에,
//                error = "error"
//            )
//
//            renewalRepository?.let {
//                // let 은 지정된 값이 null 이 아닌 경우에 코드를 실행해야 할 때 사용됨
//                accessToken = renewalRepository.accessToken
//                GlobalApp.prefs.accessToken = accessToken
//            }
//        }
//        return accessToken
    }

    suspend fun requestMainApi(targetDate : String) : Output<MainResponse>? {
        return safeApiCall(
            call = {service.main(accessToken, targetDate, beeId)},
            error = "Main API Error Fetching"
        )
    }

    suspend fun requestMeApi(): Output<MeResponse>? {
        return safeApiCall(
            call = {service.me(accessToken)},
            error = "Me API Error Fetching"
        )
    }

    suspend fun requestValidNicknameApi(nickname: String) : Output<ValidNicknameResponse>? {
        return safeApiCall(
            call = {service.nameValidationCheck(nickname)},
            error = "Valid Nickname API Error Fetching"
        )
    }

    suspend fun requestSignInApi(signInRequest: SignInRequest) : Output<SignInResponse>? {
        return safeApiCall(
            call = {service.signIn(signInRequest)},
            error = "Sign In API Error fetching"
        )
    }

    suspend fun requestSignUpApi(signUpRequest: SignUpRequest) : Output<SignUpResponse>? {
        return safeApiCall(
            call = {service.signUp(signUpRequest)},
            error = "Sign Up API Error fetching"
        )
    }

    suspend fun requestCreateBeeApi(createBeeRequest: CreateBeeRequest) : Output<Void>? {
        return safeApiCall(
            call = { service.createBee(accessToken, createBeeRequest)},
            error = "Create Bee API Error fetching"
        )
    }

//    suspend fun requestRenewalApi() =
//        service.renewal(accessToken, refreshToken)

    suspend fun requestJoinBeeApi(joinBeeRequest: JoinBeeRequest) : Output<Void>? {
        return safeApiCall(
            call = {service.joinBee(accessToken, joinBeeRequest)},
            error = "Join Bee API Error fetching"
        )
    }


    suspend fun requestBeeWithdrawalApi() : Output<Void>? {
        return safeApiCall(
            call = {service.beeWithdrawal(accessToken)},
            error = "Bee Withdrawal API Error fetching"
        )
    }


    suspend fun requestMissionInfo(missionInfoRequest: MissionInfoRequest) : Output<List<Mission>>? {
        return safeApiCall(
            call = {service.missionInfo(
                accessToken,
                missionInfoRequest.
                targetDate, beeId
            )},
            error = "Mission Info API Error fetching"
        )
    }

    suspend fun requestMissionCreate(missionCreateRequest: MissionCreateRequest) : Output<Void>? {
        return safeApiCall(
            call = {service.missionCreate(
                accessToken,
                missionCreateRequest.image,
                beeId,
                missionCreateRequest.description,
                missionCreateRequest.type,
                missionCreateRequest.difficulty,
                missionCreateRequest.targetDate
            )},
            error = "Mission Create API Error fetching"
        )
    }

    suspend fun requestBeeInfoApi(beeInfoRequest: BeeInfoRequest) : Output<BeeInfoResponse>? {
        return safeApiCall(
            call = {
                service.beeInfo(
                    accessToken,
                    beeInfoRequest.beeId
                )
            },
            error = "Bee Info API Error fetching"
        )
    }

    suspend fun requestBeePenalty(beePenaltyRequest: BeePenaltyRequest) : Output<BeePenaltyResponse>? {
        return safeApiCall(
            call = {service.beePenalty(
                accessToken,
                beeId,
                beePenaltyRequest.status
            )},
            error = "Bee Penalty API Error fetching"
        )
    }

    suspend fun requestPaidApi(paidRequest: PaidRequest) : Output<Void>? {
        return safeApiCall(
            call = {service.paid(
                accessToken,
                beeId,
                paidRequest
            )},
            error = "Paid API Error fetching"
        )
    }
}