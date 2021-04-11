package com.jasen.kimjaeseung.morningbees.main

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.calendar.CalendarDialog
import com.jasen.kimjaeseung.morningbees.createmission.CreateMissionActivity
import com.jasen.kimjaeseung.morningbees.loadmissionphoto.LoadMissionPhotoActivity
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.model.error.ErrorResponse
import com.jasen.kimjaeseung.morningbees.model.main.MainResponse
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.model.missionurl.MissionUrl
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.participatemission.ParticipateMissionActivity
import com.jasen.kimjaeseung.morningbees.setting.SettingActivity
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.RoyalJellyActivity
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.URIPathHelper
import com.jasen.kimjaeseung.morningbees.util.getPriceAnnotation
import com.jasen.kimjaeseung.morningbees.util.showToast
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_participate_mission.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener, OnItemClick {

    // Properties

    private val service = MorningBeesService.create()
    private var userAccessToken = ""
    private var beeId = 0

    private val todayDate = LocalDate.now()
    private lateinit var targetDate: LocalDate

    private var difficulty = -1
    var myNickname = ""
    var todayBee = ""
    var nextBee = ""
    var isParticipateMission = false
    var isExistMission = false

    var missionUrlList = mutableListOf<MissionUrl?>()
    var currentPhotoPath = ""
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var beeTitle = ""

    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }

    // Life Cycle for Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userAccessToken = GlobalApp.prefs.accessToken

        initButtonListeners()
        initScrollListener()
        initIconColor()
        setTargetDate()
        requestMeApi()
    }

    override fun onResume() {
        super.onResume()
        requestMainApi()
    }

    // Callback Method

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.goMissionCreateButton -> gotoMissionCreate()
            R.id.changeTargetDateButton -> changeTargetDate()
            R.id.goToSettingButton -> gotoSetting()
            R.id.royalJellyCheckButton -> gotoRoyalJelly()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            data.data?.let { photoUri ->
                currentPhotoPath = URIPathHelper().getPath(this, photoUri).toString()
            }
            gotoMissionParticipate(currentPhotoPath, PICK_FROM_ALBUM)
        } else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            addPictureToGallery()
            gotoMissionParticipate(currentPhotoPath, PICK_FROM_CAMERA)
        } else if (requestCode == GO_TO_PARTICIPATE && resultCode == FINISH) {
            bottomSheetDialog.dismiss()
            missionUrlList = mutableListOf()
            requestMainApi()
        } else if (requestCode == GO_TO_PARTICIPATE && resultCode == RELOAD) {
            bottomSheetDialog.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            for (value in grantResults) {
                if (value != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission reject")
                }
            }
        }
    }

    // Init Method

    private fun initButtonListeners() {
        goMissionCreateButton.setOnClickListener(this)
        changeTargetDateButton.setOnClickListener(this)
        goToSettingButton.setOnClickListener(this)
        royalJellyCheckButton.setOnClickListener(this)
    }

    private fun initScrollListener() {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor =
            ContextCompat.getColor(this@MainActivity, R.color.mainStatusBarColor)

        mainNestedScrollView.viewTreeObserver.addOnScrollChangedListener(object :
            ViewTreeObserver.OnScrollChangedListener {
            override fun onScrollChanged() {
                val view = mainNestedScrollView.getChildAt(mainNestedScrollView.childCount - 1)

                if (mainNestedScrollView.scrollY in 0..80) {
                    window.statusBarColor =
                        ContextCompat.getColor(this@MainActivity, R.color.mainStatusBarColor)
                } else {
                    window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.white)
                }
            }
        })
    }

    private fun initIconColor() {
        goToSettingButton.setColorFilter(Color.parseColor("#7E7E7E"))
        mainNotificationButton.setColorFilter(Color.parseColor("#7E7E7E"))
        changeTargetDateButton.setColorFilter(Color.parseColor("#7E7E7E"))
    }

    // Click Event for RecyclerView Item

    override fun clickLoadMoreMission() {
        gotoLoadMissionPhoto()
    }

    override fun clickMissionParticipate() {
        if (isExistMission)
            participateMissionDialog()
        else {
            showToast { "미션이 아직 등록되지 않았습니다." }
        }
    }


    // Request API

    private fun requestMainApi() {
        service.main(
            userAccessToken,
            targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            GlobalApp.prefsBeeInfo.beeId
        )
            .enqueue(object : Callback<MainResponse> {
                override fun onFailure(call: Call<MainResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

                override fun onResponse(
                    call: Call<MainResponse>,
                    response: Response<MainResponse>
                ) {
                    when (response.code()) {
                        200 -> {
                            val missionInfoResponse = response.body()?.missions
                            val beeInfoResponse = response.body()?.beeInfo
                            missionUrlList = mutableListOf()

                            //beeInfo Response

                            if (beeInfoResponse != null) {
                                setLayoutToBeeInfo(beeInfoResponse)

                                val manager = beeInfoResponse.get("manager").asJsonObject
                                val managerId = manager.get("id").asInt
                                val managerNickname = manager.get("nickname").asString
                                val managerProfileImage = manager.get("profileImage").asString

                                GlobalApp.prefsBeeInfo.beeManagerNickname = managerNickname
                            }

                            // missionInfo Response

                            if (missionInfoResponse == null || missionInfoResponse.size() == 0) {
                                when {
                                    todayDate == targetDate -> {
                                        targetDateMissionText.text =
                                            getString(R.string.today_mission_photo)
                                        applyImageUrl(null)
                                        setLayoutToMission(NOT_EXIST_MISSION)
                                    }

                                    todayDate > targetDate -> {
                                        targetDateMissionText.text =
                                            getString(R.string.past_mission_photo)
                                        setLayoutToMission(NOT_EXIST_PAST_MISSION)
                                    }

                                    todayDate < targetDate -> {
                                        targetDateMissionText.text =
                                            getString(R.string.future_mission_photo)
                                        setLayoutToMission(NOT_EXIST_FUTURE_MISSION)
                                    }
                                }
                                setRecyclerView()
                            } else {
                                var countMissionUrlList = 0
                                for (i in 0 until missionInfoResponse.size()) {
                                    val missionItem = missionInfoResponse.get(i).asJsonObject

                                    val missionId = missionItem.get("missionId").asInt
                                    val imageUrl = missionItem.get("imageUrl").asString
                                    val type = missionItem.get("type").asInt
                                    val nickname = missionItem.get("nickname").asString
                                    val missionTitle = missionItem.get("missionTitle").asString

                                    if (type == 2) {
                                        Log.d(TAG, "nickname: $nickname myNickname: $myNickname")
                                        if (nickname == myNickname) {
                                            isParticipateMission = true
                                            missionUrlList.add(
                                                MissionUrl(
                                                    MissionUrl.MISSION_PARTICIPATE_IMAGE_TYPE,
                                                    imageUrl,
                                                    isParticipateMission
                                                )
                                            )
                                        } else {
                                            if (countMissionUrlList < 2) {
                                                missionUrlList.add(
                                                    MissionUrl(
                                                        MissionUrl.MISSION_PARTICIPATE_IMAGE_TYPE,
                                                        imageUrl,
                                                        isParticipateMission
                                                    )
                                                )
                                                countMissionUrlList++
                                            }
                                        }
                                    }

                                    if (type == 1) {
                                        isExistMission = true
                                        when {
                                            todayDate == targetDate -> {
                                                targetDateMissionText.text =
                                                    getString(R.string.today_mission_photo)
                                                missionTargetDateText.text =
                                                    getString(R.string.today_mission)
                                                missionDescriptionText.text = missionTitle
                                                applyImageUrl(imageUrl)
                                                setLayoutToMission(EXIST_MISSION)
                                            }

                                            todayDate > targetDate -> {
                                                targetDateMissionText.text =
                                                    getString(R.string.past_mission_photo)
                                                missionTargetDateText.text =
                                                    getString(R.string.past_mission_photo)

                                                missionDescriptionText.text =
                                                    missionItem.get("missionTitle").asString
                                                applyImageUrl(imageUrl)
                                                setLayoutToMission(EXIST_PAST_MISSION)
                                            }

                                            todayDate < targetDate -> {
                                                targetDateMissionText.text =
                                                    getString(R.string.future_mission_photo)
                                                missionTargetDateText.text =
                                                    getString(R.string.future_mission_photo)
                                                missionDescriptionText.text =
                                                    getString(R.string.tomorrow_mission_desc_text)

                                                Glide.with(this@MainActivity)
                                                    .load(imageUrl)
                                                    .apply(RequestOptions().override(312, 400))
                                                    .transform(
                                                        MultiTransformation(
                                                            CenterCrop(),
                                                            RoundedCorners(30),
                                                            BlurTransformation(40)
                                                        )
                                                    )
                                                    .error(R.drawable.not_upload_mission_img_view)
                                                    .into(missionImage)

                                                setLayoutToMission(EXIST_FUTURE_MISSION)
                                            }
                                        }
                                    }
                                }
                                setRecyclerView()
                            }
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")

                            applyImageUrl(null)
                            setLayoutToMission(NOT_EXIST_MISSION)
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")

                            when {
                                todayDate == targetDate -> {
                                    targetDateMissionText.text =
                                        getString(R.string.today_mission_photo)
                                    applyImageUrl(null)
                                    setLayoutToMission(NOT_EXIST_MISSION)
                                }

                                todayDate > targetDate -> {
                                    targetDateMissionText.text =
                                        getString(R.string.past_mission_photo)
                                    setLayoutToMission(NOT_EXIST_PAST_MISSION)
                                }

                                todayDate < targetDate -> {
                                    targetDateMissionText.text =
                                        getString(R.string.future_mission_photo)
                                    setLayoutToMission(NOT_EXIST_FUTURE_MISSION)
                                }
                            }

                            missionImage.setImageResource(R.drawable.not_upload_mission_img_view)
                            setLayoutToMission(NOT_EXIST_MISSION)
                            Log.d(TAG, message)
                        }
                    }
                }
            })
    }

    private fun requestMeApi() {
        service.me(userAccessToken)
            .enqueue(object : Callback<MeResponse> {
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when (response.code()) {
                        200 -> {
                            val meResponse = response.body()
                            myNickname = meResponse!!.nickname
                            GlobalApp.prefsBeeInfo.myNickname = myNickname
                            beeId = meResponse.beeId
                            GlobalApp.prefsBeeInfo.myEmail = meResponse.email
                            GlobalApp.prefsBeeInfo.beeId = beeId
                            requestMainApi()
                        }

                        400 -> {
                            val converter: Converter<ResponseBody, ErrorResponse> =
                                MorningBeesService.retrofit.responseBodyConverter<ErrorResponse>(
                                    ErrorResponse::class.java,
                                    ErrorResponse::class.java.annotations
                                )

                            val errorResponse = converter.convert(response.errorBody())

                            when (errorResponse.code) {
                                110 -> {
                                    val oldAccessToken = GlobalApp.prefs.accessToken
                                    GlobalApp.prefs.requestRenewalApi()
                                    val renewalAccessToken = GlobalApp.prefs.accessToken

                                    if (oldAccessToken == renewalAccessToken) {
                                        showToast { "다시 로그인해주세요." }
                                        gotoLogOut()
                                    } else
                                        requestMeApi()
                                }

                                else -> {
                                    showToast { errorResponse.message }
                                    finish()
                                }
                            }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }
                    }
                }

                override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }
            })
    }

    // View Design about Mission

    private fun setRecyclerView() {
        setMissionUrlType()
        missionParticipateRecyclerView.adapter = MainAdapter(missionUrlList, this)
        missionParticipateRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        missionParticipateRecyclerView.scrollToPosition(missionUrlList.size - 1)
    }

    private fun setMissionUrlType() {
        if (isParticipateMission) {
            for (i in 0 until missionUrlList.size) {

                if (missionUrlList[i]?.isMyImageUrl == true) {
                    val missionUrl = missionUrlList[i]
                    missionUrlList.removeAt(i)
                    missionUrlList.add(missionUrlList.size, missionUrl)
                    break
                }
            }

            if (missionUrlList.size >= 1) {
                missionUrlList.add(
                    0,
                    MissionUrl(MissionUrl.LOAD_MORE_MISSION_BUTTON_TYPE, null, null)
                )
            }
        } else {
            if (missionUrlList.size >= 1) {
                missionUrlList.add(
                    0,
                    MissionUrl(MissionUrl.LOAD_MORE_MISSION_BUTTON_TYPE, null, null)
                )
            }
            if (targetDate == todayDate && todayBee != myNickname) {
                missionUrlList.add(
                    missionUrlList.size,
                    MissionUrl(MissionUrl.MISSION_PARTICIPATE_BUTTON_TYPE, null, null)
                )
            }
        }
    }

    // View Design about Bee Info

    private fun setLayoutToBeeInfo(beeInfoResponse: JsonObject) {
        missionDifficultyImageWrapLayout.visibility = View.VISIBLE
        wrap_undefine_difficulty_btn.visibility = View.INVISIBLE

        difficulty = if (beeInfoResponse.get("todayDifficulty").isJsonNull)
            0
        else
            beeInfoResponse.get("todayDifficulty").asInt

        setDifficulty(difficulty)

        setMissionTimeImage(
            beeInfoResponse.get("startTime").toString(),
            beeInfoResponse.get("endTime").toString()
        )

        missionStartTimeText.text = beeInfoResponse.get("startTime").toString()
        missionEndTimeText.text = beeInfoResponse.get("endTime").toString()

        beeTitle = beeInfoResponse.get("title").toString().replace("\"", "")

        beeTitleView.text = beeTitle
        GlobalApp.prefsBeeInfo.beeTitle = beeTitle

        beeTotalMemberView.text = beeInfoResponse.get("memberCounts").toString()

        val totalPenalty = beeInfoResponse.get("totalPenalty").asInt.getPriceAnnotation()
        totalJelly.text = " ${totalPenalty}원"

        todayBee = beeInfoResponse.get("todayQuestioner").asJsonObject.get("nickname").toString()
            .replace("\"", "")
        todayQuestionerNickname.text = todayBee

        nextBee = beeInfoResponse.get("nextQuestioner").asJsonObject.get("nickname").toString()
            .replace("\"", "")
    }

    private fun setMissionTimeImage(_startTime: String, _endTime: String) {
        missionTimeDefinedWrapLayout.visibility = View.VISIBLE
        missionTimeUnDefinedWrapLayout.visibility = View.INVISIBLE

        val current = LocalDateTime.now()

        var startTime = _startTime
        var endTime = _endTime
        if (_startTime == "10")
            startTime += ":00:00"
        else
            startTime = "0$_startTime:00:00"

        if (_endTime == "10")
            endTime += ":00:00"
        else
            endTime = "0$_endTime:00:00"

        val targetStart =
            targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " $startTime"
        val targetEnd = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " $endTime"

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedTargetStart = LocalDateTime.parse(targetStart, formatter)
        val formattedTargetEnd = LocalDateTime.parse(targetEnd, formatter)

        if (current > formattedTargetStart && current < formattedTargetEnd) {
            lottie.visibility = View.VISIBLE
            missionTime.background =
                applicationContext.getDrawable(R.drawable.image_of_mission_time)
            lottie.playAnimation()
            lottie.repeatCount = ValueAnimator.INFINITE
        } else {
            missionTime.background =
                applicationContext.getDrawable(R.drawable.image_outside_of_mission_time)
            lottie.visibility = View.INVISIBLE
        }
    }

    private fun setDifficulty(difficulty: Int?) {
        when (difficulty) {
            0 -> missionDifficultyDefinedImage.setImageDrawable(getDrawable(R.drawable.icon_low_level))
            1 -> missionDifficultyDefinedImage.setImageDrawable(getDrawable(R.drawable.icon_middle_level))
            2 -> missionDifficultyDefinedImage.setImageDrawable(getDrawable(R.drawable.icon_high_level))
        }
    }

    private fun setLayoutToMission(state: Int) {
        if (state == EXIST_MISSION) {
            if (myNickname == todayBee || isParticipateMission) { // or 이미 미션을 participate 한 경우
                missionUploadWrapLayout.visibility = View.VISIBLE
                missionNotUploadWrapLayout.visibility = View.INVISIBLE
                goMissionCreateButton.visibility = View.INVISIBLE
            } else {
                missionUploadWrapLayout.visibility = View.VISIBLE
                missionNotUploadWrapLayout.visibility = View.INVISIBLE
                goMissionCreateButton.visibility = View.INVISIBLE
            }
        } else if (state == NOT_EXIST_MISSION) {
            if (myNickname == todayBee) {
                missionNotUploadText.text = getString(R.string.need_to_register_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                goMissionCreateButton.visibility = View.VISIBLE
            } else {
                missionNotUploadText.text = getString(R.string.no_exist_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                notUploadMissionImage.background =
                    applicationContext.getDrawable(R.drawable.not_upload_mission_img_view)
                goMissionCreateButton.visibility = View.INVISIBLE
            }
        } else if (state == EXIST_FUTURE_MISSION) {
            missionUploadWrapLayout.visibility = View.VISIBLE
            missionNotUploadWrapLayout.visibility = View.INVISIBLE
            goMissionCreateButton.visibility = View.INVISIBLE
        } else if (state == NOT_EXIST_FUTURE_MISSION) {
            if (myNickname == nextBee) {
                missionNotUploadText.text = getString(R.string.need_to_register_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                goMissionCreateButton.visibility = View.VISIBLE
            } else {
                missionNotUploadText.text = getString(R.string.no_exist_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                goMissionCreateButton.visibility = View.INVISIBLE
            }
        } else if (state == EXIST_PAST_MISSION) {
            missionUploadWrapLayout.visibility = View.VISIBLE
            missionNotUploadWrapLayout.visibility = View.INVISIBLE
            goMissionCreateButton.visibility = View.INVISIBLE
        } else if (state == NOT_EXIST_PAST_MISSION) {
            missionNotUploadText.text = getString(R.string.no_exist_mission)
            missionUploadWrapLayout.visibility = View.INVISIBLE
            missionNotUploadWrapLayout.visibility = View.VISIBLE
            goMissionCreateButton.visibility = View.INVISIBLE
        }
    }

    private fun applyImageUrl(imageUrl: String?) {
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            30f,
            applicationContext.resources.displayMetrics
        ).toInt()

        Glide.with(this@MainActivity)
            .load(imageUrl)
            .format(DecodeFormat.PREFER_ARGB_8888)
            .transform(MultiTransformation(CenterCrop(), RoundedCorners(px)))
            .error(R.drawable.not_upload_mission_img_view)
            .into(missionImage)
    }

    private fun setTargetDate() {
        targetDateText.text = todayDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        targetDate = todayDate
    }

    // TargetDate with Calendar

    private fun changeTargetDate() {
        val dialogFragment = CalendarDialog()
        dialogFragment.show(supportFragmentManager, "signature")

        dialogFragment.setDialogResult(object : CalendarDialog.OnMyDialogResult {
            override fun finish(hyphenTargetDate: String, noHypentargetDate: String) {

                targetDate = LocalDate.parse(hyphenTargetDate, DateTimeFormatter.ISO_DATE)
                targetDateText.text = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                missionUrlList = mutableListOf()
                requestMainApi()
            }
        })
    }

    // Mission Participate

    private fun participateMissionDialog() {
        bottomSheetDialog = BottomSheetDialog(
            this, R.style.BottomSheetDialogTheme
        )

        val bottomSheetView = LayoutInflater.from(applicationContext)
            .inflate(
                R.layout.fragment_participate_mission,
                findViewById(R.id.layout_mission_participate)
            )

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

        chkPermission()

        bottomSheetView.getGalleryMissionParticipateDialogButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                gotoGallery()
            }
        })

        bottomSheetView.takePictureMissionParticipateDialogButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                dispatchTakePictureIntent()
            }
        })
    }

    private fun dispatchTakePictureIntent() {
        val state: String = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            Log.d(TAG, "SD card is not mounted")
            return
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.jasen.kimjaeseung.morningbees",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, PICK_FROM_CAMERA)
                }
            }
        }
    }

    private fun addPictureToGallery() {
        val file = File(currentPhotoPath)
        var mediaScannerConnection: MediaScannerConnection? = null

        val mediaScannerClient = object : MediaScannerConnection.MediaScannerConnectionClient {
            override fun onMediaScannerConnected() {
                mediaScannerConnection?.scanFile(file.path, null)
                Log.d(TAG, "media scan success")
            }

            override fun onScanCompleted(path: String?, uri: Uri?) {
                Log.d(TAG, "media scan completed")
                mediaScannerConnection?.disconnect()
            }
        }

        mediaScannerConnection = MediaScannerConnection(this, mediaScannerClient)
        mediaScannerConnection.connect()

    }

    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    // Check Permission for Camera & Gallery

    private fun chkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                showRequestPermission()
            } else {
                Log.d(TAG, "---- already have permission ----")
            }
        }
    }

    private fun showRequestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION
        )
    }

    // Change Activity

    private fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    private fun gotoMissionParticipate(path: String?, state: Int) {
        startActivityForResult(
            Intent(this, ParticipateMissionActivity::class.java)
                .putExtra("photoPath", path)
                .putExtra("state", state)
                .putExtra(
                    "targetDate",
                    targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ), GO_TO_PARTICIPATE
        )
    }

    private fun gotoRoyalJelly() {
        startActivity(
            Intent(this, RoyalJellyActivity::class.java)
        )
    }

    private fun gotoMissionCreate() {
        if (beeId == 0) {
            showToast { getString(R.string.no_registered_bee) }
        } else {
            startActivityForResult(
                Intent(this, CreateMissionActivity::class.java)
                    .putExtra(
                        "targetDate",
                        targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    )
                    .putExtra("difficulty", difficulty)
                , GO_TO_PARTICIPATE
            )
        }
    }

    private fun gotoLoadMissionPhoto() {
        startActivity(
            Intent(this, LoadMissionPhotoActivity::class.java)
                .putExtra(
                    "targetDate",
                    targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
        )
    }

    private fun gotoSetting() {
        startActivity(
            Intent(this, SettingActivity::class.java)
        )
    }

    private fun gotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    // Companion

    companion object {
        private const val REQUEST_PERMISSION = 1000
        private const val PICK_FROM_ALBUM = 1001
        private const val PICK_FROM_CAMERA = 1002
        private const val GO_TO_PARTICIPATE = 1003

        private const val EXIST_MISSION = 1
        private const val NOT_EXIST_MISSION = 2
        private const val EXIST_FUTURE_MISSION = 3
        private const val NOT_EXIST_FUTURE_MISSION = 4
        private const val EXIST_PAST_MISSION = 5
        private const val NOT_EXIST_PAST_MISSION = 6

        private const val RELOAD = 120
        private const val FINISH = 121
        private const val TAG = "MainActivity"
    }
}