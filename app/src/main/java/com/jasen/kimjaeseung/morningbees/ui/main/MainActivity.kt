package com.jasen.kimjaeseung.morningbees.ui.main

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.calendar.CalendarDialog
import com.jasen.kimjaeseung.morningbees.createmission.CreateMissionActivity
import com.jasen.kimjaeseung.morningbees.loadmissionphoto.LoadMissionPhotoActivity
import com.jasen.kimjaeseung.morningbees.model.*
import com.jasen.kimjaeseung.morningbees.participatemission.ParticipateMissionActivity
import com.jasen.kimjaeseung.morningbees.setting.SettingActivity
import com.jasen.kimjaeseung.morningbees.setting.royaljelly.RoyalJellyActivity
import com.jasen.kimjaeseung.morningbees.ui.signin.SignInActivity
import com.jasen.kimjaeseung.morningbees.utils.*
import com.jasen.kimjaeseung.morningbees.utils.mediascanner.MediaScanner
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_participate_mission.view.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener,
    OnItemClick {

    // Properties
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

    private lateinit var mainViewModel : MainViewModel

    // Life Cycle for Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        userAccessToken = GlobalApp.prefs.accessToken

        initButtonListeners()
        initScrollListener()
        initIconColor()
        setTargetDate()

       val viewModelFactory = mainViewModel.createFactory()
        mainViewModel = ViewModelProvider(this, viewModelFactory).get(mainViewModel::class.java)
        mainViewModel.checkAccessToken()
        mainViewModel.requestApi(targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

        mainViewModel.mainMissionsLiveData.observe(this, Observer {
            // mission info update in ui
            updateMissionInfo(it)
        })

        mainViewModel.mainBeeInfoLiveData.observe(this, Observer {
            // bee info update in ui
            updateBeeInfo(it)
        })
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
            gotoMissionParticipate(currentPhotoPath,
                PICK_FROM_ALBUM
            )
        } else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            val mediaScanner = MediaScanner.newInstance(this)
            try {
                mediaScanner.mediaScanning(currentPhotoPath)
                gotoMissionParticipate(currentPhotoPath,
                    PICK_FROM_CAMERA
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "Media Scan Error: $e")
            }
        } else if (requestCode == GO_TO_PARTICIPATE && resultCode == FINISH) {
            bottomSheetDialog.dismiss()
            missionUrlList = mutableListOf()
//            requestMainApi()
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

    private fun updateMissionInfo(missionInfoResponse: JsonArray?){
        if (missionInfoResponse == null || missionInfoResponse.size() == 0){
            when {
                todayDate == targetDate -> {
                    targetDateMissionText.text = getString(R.string.today_mission_photo)
                    applyImageUrl(null)
                    setLayoutToMission(NOT_EXIST_MISSION)
                }

                todayDate > targetDate -> {
                    targetDateMissionText.text = getString(R.string.past_mission_photo)
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
                val missionItem  = Gson().fromJson(missionInfoResponse, Mission::class.java)

                if (missionItem.type == 2) {
                    Log.d(TAG, "nickname: ${missionItem.nickname} myNickname: $myNickname")
                    if (missionItem.nickname == myNickname) {
                        isParticipateMission = true
                        missionUrlList.add(
                            MissionUrl(
                                MissionUrl.MISSION_PARTICIPATE_IMAGE_TYPE,
                                missionItem.imageUrl,
                                isParticipateMission
                            )
                        )
                    } else {
                        if (countMissionUrlList < 2) {
                            missionUrlList.add(
                                MissionUrl(
                                    MissionUrl.MISSION_PARTICIPATE_IMAGE_TYPE,
                                    missionItem.imageUrl,
                                    isParticipateMission
                                )
                            )
                            countMissionUrlList++
                        }
                    }
                }

                if (missionItem.type == 1) {
                    isExistMission = true
                    when {
                        todayDate == targetDate -> {
                            targetDateMissionText.text = getString(R.string.today_mission_photo)
                            missionTargetDateText.text = getString(R.string.today_mission)
                            missionDescriptionText.text = missionItem.missionTitle
                            applyImageUrl(missionItem.imageUrl)
                            setLayoutToMission(EXIST_MISSION)
                        }

                        todayDate > targetDate -> {
                            targetDateMissionText.text = getString(R.string.past_mission_photo)
                            missionTargetDateText.text = getString(R.string.past_mission_photo)
                            missionDescriptionText.text = missionItem.missionTitle
                            applyImageUrl(missionItem.imageUrl)
                            setLayoutToMission(EXIST_PAST_MISSION)
                        }

                        todayDate < targetDate -> {
                            targetDateMissionText.text = getString(R.string.future_mission_photo)
                            missionTargetDateText.text = getString(R.string.future_mission_photo)
                            missionDescriptionText.text = getString(R.string.tomorrow_mission_desc_text)

                            Glide.with(this@MainActivity)
                                .load(missionItem.imageUrl)
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

    private fun updateBeeInfo(beeInfoResponse: JsonObject){
        setLayoutToBeeInfo(beeInfoResponse)

    }

    // View Design about Mission

    private fun setRecyclerView() {
        setMissionUrlType()
        missionParticipateRecyclerView.adapter =
            MainAdapter(
                missionUrlList,
                this
            )
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
                    MissionUrl(
                        MissionUrl.LOAD_MORE_MISSION_BUTTON_TYPE,
                        null,
                        null
                    )
                )
            }
        } else {
            if (missionUrlList.size >= 1) {
                missionUrlList.add(
                    0,
                    MissionUrl(
                        MissionUrl.LOAD_MORE_MISSION_BUTTON_TYPE,
                        null,
                        null
                    )
                )
            }
            if (targetDate == todayDate && todayBee != myNickname) {
                missionUrlList.add(
                    missionUrlList.size,
                    MissionUrl(
                        MissionUrl.MISSION_PARTICIPATE_BUTTON_TYPE,
                        null,
                        null
                    )
                )
            }
        }
    }

    // View Design about Bee Info

    private fun setLayoutToBeeInfo(response: JsonObject) {
        val beeInfoResponse = Gson().fromJson(response, BeeInfo::class.java)

        missionDifficultyImageWrapLayout.visibility = View.VISIBLE
        wrap_undefine_difficulty_btn.visibility = View.INVISIBLE

        setDifficulty(beeInfoResponse.todayDifficulty)

        setMissionTimeImage(
            beeInfoResponse.startTime.toString(),
            beeInfoResponse.endTime.toString()
        )

        missionStartTimeText.text = beeInfoResponse.startTime.toString()
        missionEndTimeText.text = beeInfoResponse.endTime.toString()

        beeTitleView.text = beeInfoResponse.title.replace("\"", "")


        beeTotalMemberView.text = beeInfoResponse.memberCounts.toString()

        val totalPenalty = beeInfoResponse.totalPenalty.getPriceAnnotation()
        totalJelly.text = " ${totalPenalty}원"

        todayBee = beeInfoResponse.todayQuestioner.get("nickname").toString()
            .replace("\"", "")
        todayQuestionerNickname.text = todayBee

        nextBee = beeInfoResponse.nextQuestioner.get("nickname").toString()
            .replace("\"", "")
    }

    private fun setMissionTimeImage(_startTime: String, _endTime: String) {
        missionTimeDefinedWrapLayout.visibility = View.VISIBLE
        missionTimeUnDefinedWrapLayout.visibility = View.INVISIBLE

        val current = LocalDateTime.now()

        var startTime = _startTime
        var endTime = _endTime

        when (_startTime){
            "10" -> startTime += ":00:00"
            else -> startTime = "0$_startTime:00:00"
        }

        when (_endTime){
            "10" -> endTime += ":00:00"
            else -> endTime = "0$_endTime:00:00"
        }

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

    // xml 버튼에 main API 요청 onClick 이벤트 달기
    private fun changeTargetDate() {
        val dialogFragment = CalendarDialog()
        dialogFragment.show(supportFragmentManager, "signature")

        dialogFragment.setDialogResult(object : CalendarDialog.OnMyDialogResult {
            override fun finish(hyphenTargetDate: String, noHypentargetDate: String) {

                targetDate = LocalDate.parse(hyphenTargetDate, DateTimeFormatter.ISO_DATE)
                targetDateText.text = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                missionUrlList = mutableListOf()
//                requestMainApi()
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
                    startActivityForResult(takePictureIntent,
                        PICK_FROM_CAMERA
                    )
                }
            }
        }
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
            Intent(this, SignInActivity::class.java)
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
                ),
            GO_TO_PARTICIPATE
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
                ,
                GO_TO_PARTICIPATE
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
        startActivityForResult(intent,
            PICK_FROM_ALBUM
        )
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