package com.jasen.kimjaeseung.morningbees.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.app.GlobalApp
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.calendar.CalendarDialog
import com.jasen.kimjaeseung.morningbees.login.LoginActivity
import com.jasen.kimjaeseung.morningbees.missioncreate.MissionCreateActivity
import com.jasen.kimjaeseung.morningbees.missionparticipate.MissionParticipateActivity
import com.jasen.kimjaeseung.morningbees.model.main.MainResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.setting.SettingActivity
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_mission_participate.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.jasen.kimjaeseung.morningbees.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// MARK:~ Sign Up (코틀린 룰 찾아보기)
// 주석이 필요한 이유

// temp 단어 가급적이면 사용하지 말 것
class MainActivity : AppCompatActivity(), View.OnClickListener {

    // MARK:~ Properties

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

    var urlList = mutableListOf<String?>()

    var imageFile: File? = null
    private var bitmap: Bitmap? = null
    var image: File? = null
    lateinit var bottomSheetDialog: BottomSheetDialog

    private var beeTitle = ""

    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }

    // MARK:~ Life Cycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userAccessToken = GlobalApp.prefs.accessToken

        initButtonListeners()
        changeIconColor()
        setTargetDate()
        requestMeApi()

        window.statusBarColor
    }

    // MARK:~ Method Extension

    private fun Date.getString(): String {
        return SimpleDateFormat("yyyy-MM-dd").format(this)
    }

    fun Date.toString(type: String): String {
        return SimpleDateFormat(type).format(this)
    }

    // MARK:~ Button Click

    private fun initButtonListeners() {
        goMissionCreateButton.setOnClickListener(this)
        goMissionParticipateButton.setOnClickListener(this)
        changeTargetDateButton.setOnClickListener(this)
        goToSettingButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.goMissionCreateButton -> gotoMissionCreate()
            R.id.goMissionParticipateButton -> participateMissionDialog()
            R.id.changeTargetDateButton -> changeTargetDate()
            R.id.goToSettingButton -> gotoSetting()
        }
    }

    // MARK:~ Setting

    private fun gotoSetting() {
        startActivity(
            Intent(this, SettingActivity::class.java)
                .putExtra("myNickname", myNickname)
        )
    }

    // MARK:~ Main API Request

    private fun requestMainApi() {
        service.main(
            userAccessToken,
            targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            beeId
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
                            val mainResponse =
                                MainResponse(response.body()?.missions, response.body()?.beeInfo)
                            val missionsResponse = mainResponse.missions
                            val beeInfoResponse = mainResponse.beeInfo

                            //beeInfo
                            if (beeInfoResponse != null) {
                                setLayoutToBeeInfo(beeInfoResponse)

                                val manager = beeInfoResponse.get("manager").asJsonObject
                                val managerId = manager.get("id").asInt
                                val managerNickname = manager.get("nickname").asString
                                val managerProfileImage =
                                    manager.get("profileImage").asString

                                GlobalApp.prefsBeeInfo.beeManagerNickname = managerNickname
                            }

                            // missionsResponse
                            if (missionsResponse == null || missionsResponse.size() == 0) {
                                // Not Exist Mission
                                Log.d(TAG, "todayDate: $todayDate")
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
                            } else {
                                // Exist Mission
                                Log.d(TAG, "exist - todayDate: $todayDate")
                                for (i in 0 until missionsResponse.size()) {
                                    val missionItem = missionsResponse.get(i).asJsonObject

                                    val missionId = missionItem.get("missionId").asInt
                                    val imageUrl = missionItem.get("imageUrl").asString
                                    val type = missionItem.get("type").asInt
                                    val createdAt = missionItem.get("createdAt").asString
                                    val nickname = missionItem.get("nickname").asString

                                    if (type == 2) {
                                        urlList.add(imageUrl)
                                        if (nickname == myNickname) {
                                            isParticipateMission = true
                                        }
                                    }

                                    if (type == 1) {
                                        when {
                                            todayDate == targetDate -> {
                                                targetDateMissionText.text =
                                                    getString(R.string.today_mission_photo)
                                                missionTargetDateText.text =
                                                    getString(R.string.today_mission)
                                                applyImageUrl(imageUrl)
                                                setLayoutToMission(EXIST_MISSION)
                                            }

                                            todayDate > targetDate -> {
                                                targetDateMissionText.text =
                                                    getString(R.string.past_mission_photo)
                                                missionTargetDateText.text =
                                                    getString(R.string.past_mission_photo)
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

                                                val multi = MultiTransformation<Bitmap>(
                                                    BlurTransformation(25),
                                                    RoundedCorners(30)
                                                )

                                                Glide.with(this@MainActivity)
                                                    .load(imageUrl)
                                                    .centerCrop()
                                                    .apply(RequestOptions.bitmapTransform(multi))
                                                    .override(312, 400)
                                                    .error(R.drawable.not_upload_mission_img_view)
                                                    .into(missionImage)
                                                setLayoutToMission(EXIST_FUTURE_MISSION)
                                            }
                                        }
                                    }
                                }
                            }
                            initRecyclerView()
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
                            showToast { message }

                            applyImageUrl(null)
                            setLayoutToMission(NOT_EXIST_MISSION)

                            // or Error Pop UP 출력
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val message = jsonObject.getString("message")

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
                                    targetDateMissionText.text = getString(R.string.future_mission_photo)
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

    // MARK:~ View Design

    private fun initRecyclerView() {
        Log.d(TAG, "urlList.size: ${urlList.size}")
        if (urlList.size == 0) {
            urlList = mutableListOf(null, null, null)
        }

        val adapter = MissionParticipateAdapter(urlList, this)
        missionParticipateRecyclerView.adapter = adapter
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        missionParticipateRecyclerView.layoutManager = layoutManager
        missionParticipateRecyclerView.scrollToPosition(urlList.size - 1)
    }

    private fun setLayoutToBeeInfo(
        beeInfoResponse: JsonObject
    ) {
        missionDifficultyImageWrapLayout.visibility = View.VISIBLE
        wrap_undefine_difficulty_btn.visibility = View.INVISIBLE

        difficulty = if (beeInfoResponse.get("todayDifficulty").isJsonNull)
            0
        else
            beeInfoResponse.get("todayDifficulty").asInt

        setDifficulty(difficulty)

        missionTimeDefinedWrapLayout.visibility = View.VISIBLE
        missionTimeUnDefinedWrapLayout.visibility = View.INVISIBLE
        missionStartTimeText.text = beeInfoResponse.get("startTime").toString()
        missionEndTimeText.text = beeInfoResponse.get("endTime").toString()

        beeTitle = beeInfoResponse.get("title").toString().replace("\"", "")
        beeTitleView.text = beeTitle
        GlobalApp.prefsBeeInfo.beeTitle = beeTitle

        beeTotalMemberView.text = beeInfoResponse.get("memberCounts").toString()
        totalJelly.text = " ${beeInfoResponse.get("totalPenalty")}원"

        val todayProfileImage =
            beeInfoResponse.get("todayQuestioner").asJsonObject.get("profileImage").toString()
        val nextProfileImage =
            beeInfoResponse.get("nextQuestioner").asJsonObject.get("profileImage").toString()

        todayBee = beeInfoResponse.get("todayQuestioner").asJsonObject.get("nickname").toString()
            .replace("\"", "")
        todayQuestionerNickname.text = todayBee

        nextBee = beeInfoResponse.get("nextQuestioner").asJsonObject.get("nickname").toString()
            .replace("\"", "")

        Glide.with(this)
            .load(todayProfileImage)
            .centerCrop()
            .circleCrop()
//            .apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
            .override(45, 45)
//            .error(R.drawable.round_today_bee_img)
            .into(todayQuestionerImage)

        Glide.with(this)
            .load(nextProfileImage)
            .centerCrop()
            .circleCrop()
//            .apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
            .override(30, 30)
//            .error(R.drawable.round_next_bee_img)
            .into(nextQuestionerImage)
    }

    private fun setLayoutToMission(state: Int) {
        Log.d(TAG, "myNickname: $myNickname / todayBee: $todayBee")
        missionUploadWrapLayout.background = applicationContext.getDrawable(R.color.transparent)
        missionNotUploadWrapLayout.background = applicationContext.getDrawable(R.color.transparent)
        if (state == EXIST_MISSION) {
            if (myNickname == todayBee || isParticipateMission) { // or 이미 미션을 participate 한 경우
                missionUploadWrapLayout.visibility = View.VISIBLE
                missionNotUploadWrapLayout.visibility = View.INVISIBLE
                goMissionCreateButton.visibility = View.INVISIBLE
                goMissionParticipateButton.visibility = View.INVISIBLE
            } else {
                missionUploadWrapLayout.visibility = View.VISIBLE
                missionNotUploadWrapLayout.visibility = View.INVISIBLE
                goMissionCreateButton.visibility = View.INVISIBLE
                goMissionParticipateButton.visibility = View.VISIBLE
            }
        } else if (state == NOT_EXIST_MISSION) {
            if (myNickname == todayBee) {
                missionNotUploadText.text = getString(R.string.need_to_register_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                goMissionCreateButton.visibility = View.VISIBLE
                goMissionParticipateButton.visibility = View.INVISIBLE
            } else {
                missionNotUploadText.text = getString(R.string.no_exist_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                goMissionCreateButton.visibility = View.INVISIBLE
                goMissionParticipateButton.visibility = View.INVISIBLE
            }
        } else if (state == EXIST_FUTURE_MISSION) {
            missionUploadWrapLayout.visibility = View.VISIBLE
            missionNotUploadWrapLayout.visibility = View.INVISIBLE
            goMissionCreateButton.visibility = View.INVISIBLE
            goMissionParticipateButton.visibility = View.INVISIBLE
        } else if (state == NOT_EXIST_FUTURE_MISSION) {
            if (myNickname == nextBee) {
                missionNotUploadText.text = getString(R.string.need_to_register_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                goMissionCreateButton.visibility = View.VISIBLE
                goMissionParticipateButton.visibility = View.INVISIBLE
            } else {
                missionNotUploadText.text = getString(R.string.no_exist_mission)
                missionUploadWrapLayout.visibility = View.INVISIBLE
                missionNotUploadWrapLayout.visibility = View.VISIBLE
                goMissionCreateButton.visibility = View.INVISIBLE
                goMissionParticipateButton.visibility = View.INVISIBLE
            }
        } else if (state == EXIST_PAST_MISSION) {
            missionUploadWrapLayout.visibility = View.VISIBLE
            missionNotUploadWrapLayout.visibility = View.INVISIBLE
            goMissionCreateButton.visibility = View.INVISIBLE
            goMissionParticipateButton.visibility = View.INVISIBLE
        } else if (state == NOT_EXIST_PAST_MISSION) {
            missionNotUploadText.text = getString(R.string.no_exist_mission)
            missionUploadWrapLayout.visibility = View.INVISIBLE
            missionNotUploadWrapLayout.visibility = View.VISIBLE
            goMissionCreateButton.visibility = View.INVISIBLE
            goMissionParticipateButton.visibility = View.INVISIBLE
        }
    }

    private fun applyImageUrl(imageUrl: String?) {
        Glide.with(this@MainActivity)
            .load(imageUrl)
            .override(312, 400)
            .centerCrop()
            .apply(
                RequestOptions.bitmapTransform(
                    RoundedCorners(30)
                )
            )
            .error(R.drawable.not_upload_mission_img_view)
            .into(missionImage)
    }

    private fun setDifficulty(difficulty: Int?) {
        when (difficulty) {
            0 -> missionDifficultyDefinedImage.setImageDrawable(getDrawable(R.drawable.low_level))
            1 -> missionDifficultyDefinedImage.setImageDrawable(getDrawable(R.drawable.middle_level))
            2 -> missionDifficultyDefinedImage.setImageDrawable(getDrawable(R.drawable.high_level))
        }
    }

    private fun setTargetDate() {
        targetDateText.text = todayDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        targetDate = todayDate
    }

    private fun changeIconColor() {
        goToSettingButton.setColorFilter(Color.parseColor("#7E7E7E"))
        mainNotificationButton.setColorFilter(Color.parseColor("#7E7E7E"))
        changeTargetDateButton.setColorFilter(Color.parseColor("#7E7E7E"))
        missionParticipateButton.setColorFilter(Color.parseColor("#7E7E7E"))
    }

    // MARK:~ Change Date

    private fun changeTargetDate() {
        val dialogFragment = CalendarDialog()
        dialogFragment.show(supportFragmentManager, "signature")

        dialogFragment.setDialogResult(object : CalendarDialog.OnMyDialogResult {
            override fun finish(hyphenTargetDate: String, noHypentargetDate: String) {

                targetDate = LocalDate.parse(hyphenTargetDate, DateTimeFormatter.ISO_DATE)
                targetDateText.text = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                Log.d(TAG, "changeTargetDate() - targetDate: $targetDate")
                urlList = mutableListOf()
                requestMainApi()
            }
        })
    }

    // MARK:~ Me Api Request

    private fun requestMeApi() {
        service.me(userAccessToken)
            .enqueue(object : Callback<MeResponse> {
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when (response.code()) {
                        200 -> {
                            val meResponse: MeResponse? = response.body()
                            myNickname = meResponse!!.nickname
                            beeId = meResponse.beeId
                            GlobalApp.prefsBeeInfo.beeId = beeId
                            requestMainApi()
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")

                            if (code == 110) {
                                val oldAccessToken = GlobalApp.prefs.accessToken
                                GlobalApp.prefs.requestRenewalApi()
                                val renewalAccessToken = GlobalApp.prefs.accessToken

                                if (oldAccessToken == renewalAccessToken) {
                                    showToast { "다시 로그인해주세요." }
                                    gotoLogOut()
                                } else
                                    requestMeApi()
                            } else {
                                showToast { message }
                                finish()
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

    // MARK:~ Mission Participate

    private fun participateMissionDialog() {
        if (beeId == 0) {
            showToast { getString(R.string.no_registered_bee) }
        } else {
            bottomSheetDialog = BottomSheetDialog(
                this, R.style.BottomSheetDialogTheme
            )

            val bottomSheetView = LayoutInflater.from(applicationContext)
                .inflate(
                    R.layout.activity_mission_participate,
                    findViewById(R.id.layout_mission_participate)
                )

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()

            chkPermission()

            bottomSheetView.pc_get_picture_btn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    gotoGallery()
                }
            })

            bottomSheetView.pc_take_picture_btn.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    gotoCamera()
                }
            })
        }
    }

    private fun gotoGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }

    private fun gotoCamera() {
        val state: String = Environment.getExternalStorageState()
        if (state != Environment.MEDIA_MOUNTED) {
            Log.d(TAG, "SD card is not mounted")
            return
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageFile = createImageFile()
        imageFile?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val photoUri = FileProvider.getUriForFile(
                    this@MainActivity, "${application.packageName}.provider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            } else {
                val photoUri = Uri.fromFile(it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }
    }

    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "morningbees"
        val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        val storageDir = File(path)

        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FROM_ALBUM && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri: Uri = data.data!!

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, selectedImageUri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                gotoMissionParticipate(bitmap)
            } else {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                gotoMissionParticipate(bitmap)
            }
        } else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            val selectedImage = BitmapFactory.decodeFile(imageFile?.absolutePath)
            bitmap = selectedImage
            image = imageFile
            gotoMissionParticipate(bitmap)
        } else if (requestCode == GO_TO_PARTICIPATE && resultCode == FINISH) {
            bottomSheetDialog.dismiss()
            requestMainApi()
        }
    }

    // MARK:~ Check Permission

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


    // MARK:~ Change Activity

    // MARK:~ Log out

    private fun gotoLogOut() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .putExtra("RequestLogOut", "")
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    // MARK:~ Mission Participate

    private fun gotoMissionParticipate(bitmap: Bitmap?) {
        val intent = Intent(this, MissionParticipateActivity::class.java)
        val stream = ByteArrayOutputStream()

        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byte: ByteArray = stream.toByteArray()
            intent.putExtra("missionImage", byte)
            intent.putExtra("beeId", beeId)
            intent.putExtra("difficulty", difficulty)
            startActivityForResult(intent, GO_TO_PARTICIPATE)
        }
    }

    // MARK:~ Mission Create

    private fun gotoMissionCreate() {
        if (beeId == 0) {
            showToast { getString(R.string.no_registered_bee) }
        } else {
            val nextIntent = Intent(this, MissionCreateActivity::class.java)
            startActivity(nextIntent)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000
        private const val PICK_FROM_ALBUM = 1001
        private const val PICK_FROM_CAMERA = 1002
        private const val GO_TO_PARTICIPATE = 1003
        private const val FINISH = 121
        private const val EXIST_MISSION = 1
        private const val NOT_EXIST_MISSION = 2
        private const val EXIST_FUTURE_MISSION = 3
        private const val NOT_EXIST_FUTURE_MISSION = 4
        private const val EXIST_PAST_MISSION = 5
        private const val NOT_EXIST_PAST_MISSION = 6
        private const val TAG = "MainActivity"
    }
}
