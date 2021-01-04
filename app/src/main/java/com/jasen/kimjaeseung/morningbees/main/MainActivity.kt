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
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.dynamiclinks.ShortDynamicLink

import com.google.firebase.ktx.Firebase
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.model.me.MeResponse
import com.jasen.kimjaeseung.morningbees.calendar.CalendarDialog
import com.jasen.kimjaeseung.morningbees.missioncreate.MissionCreateActivity
import com.jasen.kimjaeseung.morningbees.missionparticipate.MissionParticipateActivity
import com.jasen.kimjaeseung.morningbees.model.beeinfo.BeeInfoResponse
import com.jasen.kimjaeseung.morningbees.model.main.MainResponse
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.setting.SettingActivity
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_mission_participate.view.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.net.URL
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.iosParameters
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.jasen.kimjaeseung.morningbees.util.*
import retrofit2.http.Url

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val service = MorningBeesService.create()
    private lateinit var userAccessToken: String
    private var beeId: Int = 0
    private lateinit var targetDateStr: String
    private lateinit var todayDateStr: String
    private var targetDateInt: Int = 0
    private var todayDateInt: Int = 0
    private var difficulty: Int = -1

    //meServer response
    var alreadyJoin: Boolean? = false
    var myNickname: String? = null

    //mainServer response
    var todayBee: String? = null

    //recyclerView
    private lateinit var adapter: MainRecyclerViewAdapter
    private lateinit var layoutManager: LinearLayoutManager
    var urlList = mutableListOf<URL?>(null, null, null)

    //mission participate
    var tempFile: File? = null             // 카메라로 찍은 사진 File (갤러리에 저장)
    private var bitmap: Bitmap? = null     // 갤러리에서 가져온 사진 bitmap
    var image: File? = null                // 갤러리에서 가져온 사진 File
    lateinit var bottomSheetDialog: BottomSheetDialog

    //beeinfo response
    var isManager : Boolean? = null
    var title : String? = null
    var missionTitle : String? = null
    var totalPay : Int? = 0
    var todayUser : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userAccessToken = Singleton.getAccessToken()
        Log.d(TAG, "userAccessToken: $userAccessToken")

        initButtonListeners()
        changeIconColor()
        setTargetDate()
        meServer()
        //beeInfoServer()
        scrollview.scrollTo(0, scroll.top)
        initRecyclerView()
    }

    private fun initButtonListeners() {
        go_mission_create_btn.setOnClickListener(this)
        go_mission_participate_btn.setOnClickListener(this)
        calendar_btn.setOnClickListener(this)
        go_createlink_btn.setOnClickListener(this)
        setting_button.setOnClickListener(this)
    }

    private fun changeIconColor(){
        setting_button.setColorFilter(Color.parseColor("#7E7E7E"))
        notification_button.setColorFilter(Color.parseColor("#7E7E7E"))
        calendar_btn.setColorFilter(Color.parseColor("#7E7E7E"))
        mission_participate_img_btn.setColorFilter(Color.parseColor("#7E7E7E"))
    }

    private val permissionCheckCamera by lazy {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }

    private fun setTargetDate() {
        val curDate = Date()
        todayDateInt = SimpleDateFormat("yyyyMMdd").format(curDate).toInt()
        targetDateInt = SimpleDateFormat("yyyyMMdd").format(curDate).toInt()
        todayDateStr = SimpleDateFormat("yyyy-MM-dd").format(curDate)
        targetDateStr = SimpleDateFormat("yyyy-MM-dd").format(curDate)
        today_date.text = todayDateStr
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.go_mission_create_btn -> gotoMissionCreate()
            R.id.go_mission_participate_btn -> missionParticipateDialog()
            R.id.calendar_btn -> changeTargetDate()
            R.id.go_createlink_btn -> createDynamicLink()
            R.id.setting_button -> gotoSetting()
        }
    }

    private fun gotoSetting(){
        val intent = Intent(this, SettingActivity::class.java)
        intent.putExtra("nickName", myNickname)
        intent.putExtra("accessToken", userAccessToken)
        intent.putExtra("beeId", beeId)
        startActivity(intent)
    }

    private fun createDynamicLink() {
        val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse("https://www.app.thragoo.com?beeId=$beeId")
            domainUriPrefix = "https://thragoo.page.link"
            iosParameters("com.thragoo.Morningbees-iOS") {}
        }.addOnCompleteListener(this,  OnCompleteListener<ShortDynamicLink>(){
            if (it.isSuccessful){
                val shortLink = it.result?.shortLink
                val strLink = shortLink.toString()
                shareLink(strLink)
            }
        })
    }

    private fun shareLink(shortLink : String){
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Try this amazing app: $shortLink")
        startActivity(Intent.createChooser(intent, "Share Link"))
    }

    private fun changeTargetDate() {
        val dialogFragment = CalendarDialog()
        dialogFragment.show(supportFragmentManager, "signature")

        dialogFragment.setDialogResult(object : CalendarDialog.OnMyDialogResult {
            override fun finish(str: String, _str: String) {
                targetDateStr = str
                targetDateInt = _str.toInt()
                today_date.text = targetDateStr
                mainServer(beeId)
            }
        })
    }

    private fun mainServer(beeId: Int) {
        service.main(userAccessToken, targetDateStr, beeId)
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
                            val mainResponse = MainResponse(response.body()?.missions, response.body()?.beeInfo)
                            val missionsResponse = mainResponse.missions
                            val beeInfoResponse = mainResponse.beeInfo

                            // missionsResponse
                            if(missionsResponse == null) {
                                urlList = mutableListOf(null, null, null)
                                initRecyclerView()
                                //mission 생성 X
                                when {
                                    todayDateInt == targetDateInt -> {
                                        mission_img_txt.text = getString(R.string.today_mission_photo)
                                        applyImageUrl(null)
                                        setLayoutToMission(NOT_EXIST_MISSION)
                                    }

                                    todayDateInt > targetDateInt -> {
                                        mission_img_txt.text = getString(R.string.past_mission_photo)
                                        not_upload_mission_text.text = getString(R.string.need_to_register_mission)
                                        setLayoutToMission(NOT_EXIST_ANOTHER_MISSION)
                                    }

                                    todayDateInt < targetDateInt -> {
                                        mission_img_txt.text = getString(R.string.future_mission_photo)
                                        not_upload_mission_text.text = getString(R.string.no_exist_mission)
                                        setLayoutToMission(NOT_EXIST_ANOTHER_MISSION)
                                    }
                                }
                            }
                            else {
                                // mission 생성 O
                                for(i in 0 until missionsResponse.size()){
                                    val item = missionsResponse.get(i)

                                    val missionId = item.asJsonObject.get("missionId").asInt
                                    val imageUrl = item.asJsonObject.get("imageUrl").asString
                                    val type = item.asJsonObject.get("type").asInt
                                    val createdAt = item.asJsonObject.get("createdAt").asString

                                    if(type != 2){
                                        // 미션 출제일 경우
                                        val dateFormatInt = SimpleDateFormat("yyyyMMdd")
                                        targetDateInt = dateFormatInt.format(dateFormatInt.parse(createdAt)).toInt()

                                        when {
                                            todayDateInt == targetDateInt -> {
                                                mission_img_txt.text = getString(R.string.today_mission_photo)
                                                mission_upload_text.text =getString(R.string.today_mission)
                                                applyImageUrl(imageUrl)
                                                setLayoutToMission(EXIST_MISSION)
                                            }

                                            todayDateInt > targetDateInt -> {
                                                mission_img_txt.text = getString(R.string.past_mission_photo)
                                                mission_upload_text.text = getString(R.string.past_mission_photo)
                                                applyImageUrl(imageUrl)
                                                setLayoutToMission(EXIST_ANOTHER_MISSION)
                                            }

                                            todayDateInt < targetDateInt -> {
                                                mission_img_txt.text = getString(R.string.future_mission_photo)
                                                mission_upload_text.text = getString(R.string.future_mission_photo)
                                                mission_desc_text.text = getString(R.string.tomorrow_mission_desc_text)

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
                                                    .into(today_mission_image)
                                                setLayoutToMission(EXIST_ANOTHER_MISSION)
                                            }
                                        }
                                    } else {
                                        val url = URL(imageUrl)
                                        urlList.add(url) // 추가 해야함
                                    }
                                }
                                initRecyclerView()
                            }
                            //beeInfo
                            if(beeInfoResponse != null){
                                val totalPenalty = beeInfoResponse.get("totalPenalty").asInt
                                val memberCounts = beeInfoResponse.get("memberCounts").asInt
                                val startTime = beeInfoResponse.get("startTime").asInt
                                val endTime = beeInfoResponse.get("endTime").asInt
                                val title = beeInfoResponse.get("title").asString
                                val todayDifficulty = beeInfoResponse.get("todayDifficulty").asInt

                                val todayQuestioner = beeInfoResponse.get("todayQuestioner")
                                val todayNickname = todayQuestioner.asJsonObject.get("nickname").toString()
                                val todayProfileImage = todayQuestioner.asJsonObject.get("profileImage").toString()

                                val nextQuestioner = beeInfoResponse.get("nextQuestioner")
                                val nextNickname = nextQuestioner.asJsonObject.get("nickname").toString()
                                val nextProfileImage = nextQuestioner.asJsonObject.get("profileImage").toString()
                                difficulty = todayDifficulty
                                todayBee = todayNickname
                                setLayoutToBeeInfo(title, todayDifficulty, startTime, endTime, memberCounts, totalPenalty, todayProfileImage, nextProfileImage)
                            }
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
                            //applyImageUrl(null)
                            today_mission_image.setImageResource(R.drawable.not_upload_mission_img_view)
                            setLayoutToMission(NOT_EXIST_MISSION)
                            Log.d(TAG, message)
                        }
                    }
                }
            })
    }

    private fun setLayoutToBeeInfo(title : String, todayDifficulty : Int, startTime : Int, endTime : Int, memberCounts : Int, totalPenalty : Int, todayProfileImage : String, nextProfileImage : String){
        wrap_define_difficulty_btn.visibility = View.VISIBLE
        wrap_undefine_difficulty_btn.visibility = View.INVISIBLE
        setDifficulty(todayDifficulty)

        wrap_defined_time_btn.visibility = View.VISIBLE
        wrap_undefined_time_btn.visibility = View.INVISIBLE
        mission_start_time_txt.text = startTime.toString()
        mission_end_time_txt.text = endTime.toString()

        bee_title_text.text = title
        bee_total_number_text.text = memberCounts.toString()
        total_pay.text = " ${totalPenalty}원"


        Glide.with(this)
            .load(todayProfileImage)
            //.circleCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
            .override(45, 45)
            .error(R.drawable.round_today_bee_img)
            .into(today_bee_img)

        Glide.with(this)
            .load(nextProfileImage)
            //.circleCrop()
            .apply(RequestOptions.bitmapTransform(RoundedCorners(100)))
            .override(30, 30)
            .error(R.drawable.round_next_bee_img)
            .into(next_bee_img)

    }

    private fun setLayoutToMission(state: Int) {
        if (state == EXIST_MISSION) {
            if (myNickname == todayBee) {
                wrap_upload_mission_view.visibility = View.VISIBLE
                wrap_not_upload_mission_view.visibility = View.INVISIBLE
                go_mission_create_btn.visibility = View.INVISIBLE
                go_mission_participate_btn.visibility = View.VISIBLE
            } else {
                wrap_upload_mission_view.visibility = View.VISIBLE
                wrap_not_upload_mission_view.visibility = View.INVISIBLE
                go_mission_create_btn.visibility = View.INVISIBLE
                go_mission_participate_btn.visibility = View.VISIBLE
            }
        } else if (state == NOT_EXIST_MISSION) {
            if (myNickname == todayBee) {
                Log.d(TAG, "state: myNickname == todayBee")
                wrap_upload_mission_view.visibility = View.INVISIBLE
                wrap_not_upload_mission_view.visibility = View.VISIBLE
                go_mission_create_btn.visibility = View.VISIBLE
                go_mission_participate_btn.visibility = View.INVISIBLE
                not_upload_mission_text.text = getString(R.string.need_to_register_mission)
            } else {
                wrap_upload_mission_view.visibility = View.INVISIBLE
                wrap_not_upload_mission_view.visibility = View.VISIBLE
                //go_mission_create_btn.visibility = View.INVISIBLE
                //go_mission_participate_btn.visibility = View.INVISIBLE
                //not_upload_mission_text.text = getString(R.string.no_exist_mission)

                // 임시 create btn visibility
                not_upload_mission_text.text = getString(R.string.need_to_register_mission)
                go_mission_create_btn.visibility = View.VISIBLE
                go_mission_participate_btn.visibility = View.INVISIBLE
            }
        } else if (state == EXIST_ANOTHER_MISSION) {
            wrap_upload_mission_view.visibility = View.VISIBLE
            wrap_not_upload_mission_view.visibility = View.INVISIBLE
            go_mission_create_btn.visibility = View.INVISIBLE
            go_mission_participate_btn.visibility = View.INVISIBLE

        } else if (state == NOT_EXIST_ANOTHER_MISSION) {
            wrap_upload_mission_view.visibility = View.INVISIBLE
            wrap_not_upload_mission_view.visibility = View.VISIBLE
            go_mission_create_btn.visibility = View.INVISIBLE
            go_mission_participate_btn.visibility = View.INVISIBLE
            not_upload_mission_text.text = getString(R.string.no_exist_mission)
        }
    }

    private fun applyImageUrl(imageUrl : String?){
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
            .into(today_mission_image)
    }

    private fun setDifficulty(difficulty: Int) {
        when (difficulty) {
            0 -> mission_difficulty_img.setImageDrawable(getDrawable(R.drawable.low_level))
            1 -> mission_difficulty_img.setImageDrawable(getDrawable(R.drawable.middle_level))
            2 -> mission_difficulty_img.setImageDrawable(getDrawable(R.drawable.high_level))
        }
    }

    private fun meServer() {
        service.me(userAccessToken)
            .enqueue(object : Callback<MeResponse> {
                override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                    when (response.code()) {
                        200 -> {
                            val meResponse: MeResponse? = response.body()
                            myNickname = meResponse?.nickname
                            alreadyJoin = meResponse?.alreadyJoin
                            beeId = meResponse!!.beeId
                            mainServer(beeId)
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                            finish()
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

    private fun beeInfoServer(){
        service.beeInfo(userAccessToken, beeId)
            .enqueue(object : Callback<BeeInfoResponse>{
                override fun onResponse(call: Call<BeeInfoResponse>, response: Response<BeeInfoResponse>){
                    when(response.code()){
                        200 -> {
                            try {
                                val beeInfoResponse : BeeInfoResponse? = response.body()
                                val accessToken = beeInfoResponse?.accessToken
                                isManager = beeInfoResponse?.isManager
                                title = beeInfoResponse?.title
                                missionTitle = beeInfoResponse?.missionTitle
                                totalPay = beeInfoResponse?.totalPay
                                todayUser = beeInfoResponse?.todayUser

                                meServer()
                            } catch (e: JSONException) {
                            }
                        }

                        400 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }

                        500 -> {
                            val jsonObject = JSONObject(response.errorBody()?.string())
                            val message = jsonObject.getString("message")
                            showToast { message }
                        }
                    }
                }

                override fun onFailure(call: Call<BeeInfoResponse>, t: Throwable) {
                    Dlog().d(t.toString())
                }

            })
    }

    private fun gotoMissionCreate() {
        if (beeId == 0) {
            showToast { getString(R.string.no_registered_bee) }
        } else {
            val nextIntent = Intent(this, MissionCreateActivity::class.java)
            nextIntent.putExtra("accessToken", userAccessToken)
            nextIntent.putExtra("beeId", beeId)
            nextIntent.putExtra("type", 1)
            startActivity(nextIntent)
        }
    }

    private fun missionParticipateDialog() {
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
        tempFile = createImageFile()
        tempFile?.let {
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
            val tempFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            Log.d(TAG, "tempFile: $tempFile")
            return tempFile
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
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                gotoMissionParticipate(bitmap)
            }
        } else if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            val selectedImage = BitmapFactory.decodeFile(tempFile?.absolutePath)
            bitmap = selectedImage
            image = tempFile
            gotoMissionParticipate(bitmap)
        } else if (requestCode == GO_TO_PARTICIPATE && resultCode == FINISH) {
            bottomSheetDialog.dismiss()
        }
    }

    private fun gotoMissionParticipate(bitmap: Bitmap?) {
        val intent = Intent(this, MissionParticipateActivity::class.java)
        val stream = ByteArrayOutputStream()

        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byte: ByteArray = stream.toByteArray()
            intent.putExtra("missionImage", byte)
            intent.putExtra("accessToken", userAccessToken)
            intent.putExtra("beeId", beeId)
            intent.putExtra("difficulty", difficulty)
            startActivityForResult(intent, GO_TO_PARTICIPATE)
        }
    }

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
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            for (value in grantResults) {
                if (value != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "permission reject")
                }
            }
        }
    }

    private fun initRecyclerView() {
        adapter = MainRecyclerViewAdapter(urlList, this)
        main_recycler_view.adapter = adapter
        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
        main_recycler_view.layoutManager = layoutManager
        main_recycler_view.scrollToPosition(urlList.size-1)
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000
        private const val PICK_FROM_ALBUM = 1001
        private const val PICK_FROM_CAMERA = 1002
        private const val GO_TO_PARTICIPATE = 1003
        private const val FINISH = 121
        private const val EXIST_MISSION = 1
        private const val NOT_EXIST_MISSION = 2
        private const val EXIST_ANOTHER_MISSION = 3
        private const val NOT_EXIST_ANOTHER_MISSION = 4
        private const val TAG = "MainActivity"
    }
}
