package com.jasen.kimjaeseung.morningbees.data

import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.*
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.network.NetworkModule

class MorningBessRepository() {
    private val service = NetworkModule.morningBeesService

    /*
    * 제네릭을 사용해서 어디까지 repository가 커버해줘야할까?
    * 1. main api, me api 등등 모든 요청을 따로 suspend로 빼서 만들어준다.
    * -> 이렇게 해야할거 같은데 ? 왜냐하면 service.main ~ service.me 등 뒤에 오는 함수가 다 다르니까
    * */

    //repository에서 accessToken을 가져오는 함수를 사용하면 좋을듯

    private val accessToken = GlobalApp.prefs.accessToken
    private val refreshToken = GlobalApp.prefs.refreshToken

    private val beeId = GlobalApp.prefsBeeInfo.beeId

    suspend fun requestMainApi(targetDate : String) =
        service.main(
            accessToken,
            targetDate,
            beeId)

    suspend fun requestMeApi() =
        service.me(accessToken)

    suspend fun requestValidNicknameApi(nickname : String) =
        service.nameValidationCheck(
            nickname)

    suspend fun requestSignInApi(signInRequest: SignInRequest) =
        service.signIn(signInRequest)

    suspend fun requestSignUpApi(signUpRequest: SignUpRequest) =
        service.signUp(signUpRequest)

    suspend fun requestCreateBeeApi(createBeeRequest: CreateBeeRequest) =
        service.createBee(
            accessToken,
            createBeeRequest)

    suspend fun requestRenewalApi() =
        service.renewal(accessToken, refreshToken)

    suspend fun requestJoinBeeApi(joinBeeRequest: JoinBeeRequest) =
        service.joinBee(
            accessToken,
            joinBeeRequest)

    suspend fun requestBeeWithdrawalApi() =
        service.beeWithdrawal(accessToken)

    suspend fun requestMissionInfo(missionInfoRequest: MissionInfoRequest) =
        service.missionInfo(
            accessToken,
            missionInfoRequest.targetDate,
            beeId)

    suspend fun requestMissionCreate(missionCreateRequest: MissionCreateRequest) =
        service.missionCreate(
            accessToken,
            missionCreateRequest.image,
            beeId,
            missionCreateRequest.description,
            missionCreateRequest.type,
            missionCreateRequest.difficulty,
            missionCreateRequest.targetDate)

    suspend fun requestBeeInfoApi(beeInfoRequest : BeeInfoRequest) =
        service.beeInfo(
            accessToken,
            beeInfoRequest.beeId)

    suspend fun requestBeePenalty(beePenaltyRequest : BeePenaltyRequest) =
        service.beePenalty(
            accessToken,
            beeId,
            beePenaltyRequest.status)

    suspend fun requestPaidApi(paidRequest: PaidRequest) =
        service.paid(
            accessToken,
            beeId,
            paidRequest.penalties
        )
}