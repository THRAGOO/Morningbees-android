package com.jasen.kimjaeseung.morningbees.missioncreate
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.jasen.kimjaeseung.morningbees.R
import com.jasen.kimjaeseung.morningbees.main.MainActivity
import com.jasen.kimjaeseung.morningbees.network.MorningBeesService
import com.jasen.kimjaeseung.morningbees.util.Dlog
import com.jasen.kimjaeseung.morningbees.util.URIPathHelper
import com.jasen.kimjaeseung.morningbees.util.showToast
import kotlinx.android.synthetic.main.activity_mission_create.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class MissionCreateActivity: AppCompatActivity(), View.OnClickListener {
    private lateinit var accessToken : String
    val service =  MorningBeesService.create()
    var difficulty : Int = -1
    lateinit var description : String
    val type : Int = 1
    private var beeId : Int = 0
    var tempFile : File? = null     // 카메라로 찍은 사진 File (갤러리에 저장)
    var bitmap : Bitmap? = null     // 갤러리에서 가져온 사진 bitmap
    var image : File? = null  // 서버에 보낼 image data
    // 앱이 카메라 권한을 가지고 있는지 확인하는 변수 ( 카메라 권한이 없다면 -1 반환 )
    private val permissionCheckCamera by lazy{
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
    }
    private fun chkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(permissionCheckCamera == PackageManager.PERMISSION_DENIED){
                //권한 없음
                showRequestPermission()
            }
            else{
                //권한 있음
                Log.d(TAG, "---- already have permission ----")
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_create)
        if (intent.hasExtra("accessToken")) {
            accessToken = intent.getStringExtra("accessToken")
        }
        if(intent.hasExtra("beeId")){
            beeId = intent.getIntExtra("beeId", 0)
        }
        initButtonListeners()
//        missionCreateServer()
        chkPermission()
    }
    override fun onClick(v: View) {
        val i = v.id
        when(i){
            R.id.go_back_main_btn -> gotoMain(accessToken)
            R.id.mission_create_btn -> missionCreateServer()
            R.id.take_picture_btn -> gotoCamera()
            R.id.get_picture_btn -> gotoGallery()
            R.id.reload_img_btn -> changeWrapView(CLICK_IMAGEVIEW)
            R.id.difficulty_hard_btn -> setMissionDifficulty(2)
            R.id.difficulty_normal_btn -> setMissionDifficulty(1)
            R.id.difficulty_easy_btn -> setMissionDifficulty(0)
        }
    }
    fun missionCreateServer(){
        description = description_mission.text.toString()
/*
        if(bitmap != null) {
            image = bitmapToByteArray(bitmap!!)
        }*/

        if(difficulty == -1){
            showToast { "난이도 설정해주세요. " }
        }
        else if( image == null){
            showToast { "사진을 선택해 주세요." }
        }
        else if (description == ""){
            showToast { "미션 타이틀을 등록해주세요. " }
        }
        else {
            val imageFile:File = image!!
            val testImage : MultipartBody.Part = MultipartBody.Part.createFormData("image",imageFile.name,imageFile.asRequestBody("image/*".toMediaTypeOrNull()))
            service.missionCreate(accessToken,testImage,beeId,description,type,difficulty).enqueue(object : Callback<Void> {
                override fun onFailure(call : Call<Void>, t:Throwable){
                    Dlog().d(t.toString())
                }
                override fun onResponse(
                    call : Call<Void>,
                    response: Response<Void>
                ){
                    when (response.code()){
                        201 -> {
                            gotoMain(accessToken)
                            Log.d(TAG, "mission create success")
                        }
                        400 ->{
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")
                            showToast { message }
                        }
                        500 -> { //internal server error
                            val jsonObject = JSONObject(response.errorBody()!!.string())
                            val timestamp = jsonObject.getString("timestamp")
                            val status = jsonObject.getString("status")
                            val message = jsonObject.getString("message")
                            val code = jsonObject.getInt("code")
                            showToast { message }
                        }
                    }
                }
            })
        }
    }
    private fun showRequestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION){
            for (value in grantResults){
                if( value != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "permission reject")
                }
            }
        }
    }
    fun gotoGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_FROM_ALBUM)
    }
    fun gotoCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        tempFile = createImageFile()
        tempFile?.let{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                // 누가 버전 이상부터는 파일의 uri를 다른 앱으로 전송시킬때 그대로 노출시키면 에러 발생
                // fileProvider를 이용해 파일 주소를 감싸주는 코드 사용
                val photoUri = FileProvider.getUriForFile(
                    this,
                    "${application.packageName}.provider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
            else{
                val photoUri = Uri.fromFile(it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, PICK_FROM_CAMERA)
            }
        }
    }
    private fun createImageFile() : File? {
        val timeStamp : String = SimpleDateFormat("yyyMMdd_HHmmss").format(Date())
        val imageFileName = "morningbees"
        // 외부 앱 전용 저장소
        val path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
        // 외부 공용 저장소
        // https://everyshare.tistory.com/44
        // 모르겠다 ㅋ..
        val storageDir = File(path)
        if(!storageDir.exists()) {
            storageDir.mkdirs()
        }
        Log.d(TAG, "path 경로: $path")
        try {
            val tempFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            Log.d(TAG, "tempFile: $tempFile")
            return tempFile
        } catch(e: IOException){
            e.printStackTrace()
        }
        return null;
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?){
        super.onActivityResult(requestCode, resultCode, intent)
        if(requestCode == PICK_FROM_ALBUM){
            intent?.data?.let { photoUri ->
                val selectedImage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                    val source = ImageDecoder.createSource(contentResolver, photoUri)
                    // ImageDecorder.source를 객체로 전달해 Bitmap 생성
                    ImageDecoder.decodeBitmap(source)
                }
                else {
                    contentResolver.openInputStream(photoUri)?.use{ inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
                upload_img_view.setImageBitmap(selectedImage)
                bitmap = selectedImage!!

                image = File(URIPathHelper().getPath(this,photoUri))

            }
        }
        else if(requestCode == PICK_FROM_CAMERA){
            // 카메라에서는 intent, data == null
            val selectedImage = BitmapFactory.decodeFile(tempFile?.absolutePath)
            upload_img_view.setImageBitmap(selectedImage)
            bitmap = selectedImage

            image = tempFile
        }
        //bitmapToByteArray(bitmap)
        changeWrapView(LOAD_IMAGEVIEW)
    }
    /*
    fun bitmapToByteArray(bitmap: Bitmap?) : ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,100, stream)
        val byteArray = stream.toByteArray()
        return byteArray
    }
     */
    fun Bitmap.convertToByteArray() : ByteArray {
        //minimum number of bytes that can be used to store this bitmap's pixels
        val size = this.byteCount
        //allocate new instances which will hold bitmap
        val buffer = ByteBuffer.allocate(size)
        val bytes = ByteArray(size)
        //copy the bitmap's pixels into the specified buffer
        this.copyPixelsToBuffer(buffer)
        //rewinds buffer (buffer position is set to zero and the mark is discarded)
        buffer.rewind()
        //transfer bytes from buffer into the given destination array
        buffer.get(bytes)
        //return bitmap's pixels
        return bytes
    }
    fun changeWrapView(status : Int){ // wrap view change
        Log.d(TAG, "status : $status")
        if(status == CLICK_IMAGEVIEW){
            wrap_click_img_view.visibility = View.VISIBLE
            wrap_load_img_view.visibility = View.INVISIBLE
        }
        if(status == LOAD_IMAGEVIEW){
            wrap_load_img_view.visibility = View.VISIBLE
            wrap_click_img_view.visibility = View.INVISIBLE
        }
    }
    private fun  gotoMain(accessToken : String){
        val nextIntent = Intent(this, MainActivity::class.java)
        nextIntent.putExtra("accessToken", accessToken)
        startActivity(nextIntent)
    }
    fun setMissionDifficulty(mDifficulty : Int){
        difficulty = mDifficulty
    }
    fun initButtonListeners(){
        go_back_main_btn.setOnClickListener(this)
        mission_create_btn.setOnClickListener(this)
        take_picture_btn.setOnClickListener(this)
        get_picture_btn.setOnClickListener(this)
        reload_img_btn.setOnClickListener(this)
        difficulty_hard_btn.setOnClickListener(this)
        difficulty_normal_btn.setOnClickListener(this)
        difficulty_easy_btn.setOnClickListener(this)
    }

    companion object {
        private const val REQUEST_PERMISSION = 1000
        private const val PICK_FROM_ALBUM = 1001
        private const val PICK_FROM_CAMERA = 1002
        private const val LOAD_IMAGEVIEW = 1
        private const val CLICK_IMAGEVIEW = 2
        private const val TAG = "MissionCreateActivity"
    }
}