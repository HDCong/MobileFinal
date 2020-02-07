package com.cntn2017_mobiledev.batchuduoihinh.playoffline

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.cntn2017_mobiledev.batchuduoihinh.BuildConfig
import com.cntn2017_mobiledev.batchuduoihinh.R
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.activity_play_offline.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URISyntaxException
import java.security.SecureRandom


class PlayOfflineActivity : AppCompatActivity(), View.OnClickListener {
    var isMute = false
    var doubleBackToExitPressedOnce = false
    lateinit var mPlayer: MediaPlayer
    lateinit var mSocket: Socket
    var help = 3
    var solution = ""
    var urlPic = ""
    var round = 1
    var currentIdx = 0
    var userSolution = ""
    val secureRandom = SecureRandom()
    var myButtons = ArrayList<Button>(16)
    var countSelected = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_offline)

        connectToSocket()
        requestReadPermissions()

        getQuestionAndAnswer()
        mSocket.on("QuestionNek", getQuestionFromServer)

        mPlayer = MediaPlayer.create(this, R.raw.correct)
        registerView()
    }

    private fun registerView() {

        buttonHint.setOnClickListener {
            if (help > 0) {
                if (currentIdx >= solution.length - 1) {
                    Toast.makeText(this, "Còn chữ cuối tự đoán đi :)", Toast.LENGTH_SHORT).show()
                } else {
                    myButtons[currentIdx].text = solution[currentIdx].toString()
                    userSolution += solution[currentIdx].toString()
                    currentIdx++
                    countSelected++
                    help--
                }
            } else {
                Toast.makeText(this, "Chỉ được gợi ý tối đa 3 chữ cái 1 vòng", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        buttonShare.setOnClickListener {
            takeScreenShot()
        }
        buttonSkip.setOnClickListener {
            removeView()
            getQuestionAndAnswer()
        }
        buttonSound.setOnClickListener {
            if(isMute){
                isMute = false
                buttonSound.setBackgroundResource(R.drawable.speaker)
            }
            else{
                isMute=true
                buttonSound.setBackgroundResource(R.drawable.mute)
            }
        }

    }


    private fun takeScreenShot() {

        try {
            requestReadPermissions()
            val mPath =
                Environment.getExternalStorageDirectory().absolutePath.toString() + "/" + "CW_17TN" + ".jpeg"
            // create bitmap screen capture
            val v1 = window.decorView.rootView
            v1.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(v1.drawingCache)
            v1.isDrawingCacheEnabled = false
            val imageFile = File(mPath)

            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            //setting screenshot in imageview
            val filePath = imageFile.path
            Log.e("Cong", filePath)
            shareIntent(filePath)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun shareIntent(sharePath: String) {
        val file = File(sharePath)
        lateinit var uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".fileprovider",
                file
            );
        } else {
            uri = Uri.fromFile(file);
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(intent)
    }

    private fun requestReadPermissions() {

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {

                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        //openSettingsDialog();
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener(object : PermissionRequestErrorListener {
                override fun onError(error: DexterError) {
                    Toast.makeText(applicationContext, "Some Error! ", Toast.LENGTH_SHORT).show()
                }
            })
            .onSameThread()
            .check()
    }

    private fun connectToSocket() {
        try {
            val sv: String = getString(R.string.serverAddress)
            mSocket = IO.socket(sv)
        } catch (e: URISyntaxException) {
            Log.e("CONG", e.message)
        }
        mSocket.connect()
    }

    private val getQuestionFromServer = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            Log.e("Cong", data.getString("url").toString())
            try {
                urlPic = data.getString("url")
                solution = data.getString("sol")
                solution = solution.toUpperCase()
                initView()
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }

    private fun initView() {

        initRound()

        createImageViewQuestion()
        // User choose
        createButtonToSelect()
        // The selected
        createSelectedButton()
        buttonShare.visibility = View.VISIBLE
        buttonSkip.visibility = View.VISIBLE

    }

    private fun getQuestionAndAnswer() {
        mSocket.emit("getQuestion")
    }

    private fun initRound() {
        relativeFirst.visibility = View.VISIBLE
        textViewShowRound.text = "Vòng " + round.toString()
    }

    private fun createImageViewQuestion() {
        Picasso.with(this).load(urlPic).transform(RoundedCornersTransformation(80, 5))
            .into(imageViewPicture)
    }

    fun randomQuestions(): ArrayList<String> {
        val arrS = ArrayList<String>()
        val tm = secureRandom.nextInt(25) + 65
        for (i in 0 until solution.length) {
            arrS.add(solution.get(i).toString() + "")
        }
        for (i in 0 until 16 - solution.length) {
            arrS.add(tm.toChar() + "")
        }
        return arrS
    }

    fun check(arrSolution: ArrayList<Int>, n: Int): Boolean {
        for (i in 0..arrSolution.size - 1) {
            if (n == arrSolution[i]) {
                return false
            }
        }
        return true
    }

    private fun createButtonToSelect() {
        var arraySolution = ArrayList<Int>()
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        var width = displayMetrics.widthPixels
        // For the first row
        for (i in 0..7) {
            val btn = Button(this) // tao nut
            btn.layoutParams = LinearLayout.LayoutParams((width - 50) / 8, 150) // set layout
            btn.setBackgroundResource(R.drawable.btn_choose) // set back ground
            val font = Typeface.createFromAsset(assets, "fonts/pacifo.ttf")
            btn.setTypeface(font)
            btn.setOnClickListener(this) // set on click listener
            while (btn.text === "") {
                val tmp: Int = secureRandom.nextInt(16)
                if (check(arraySolution, tmp)) {
                    btn.text = randomQuestions().get(tmp)
                    randomQuestions().removeAt(tmp)
                    arraySolution.add(tmp)
                }
            }
            layoutButtonSelectFirst.addView(btn)
        }
        for (i in 0..7) {
            val btn = Button(this) // tao nut
            btn.layoutParams = LinearLayout.LayoutParams((width - 50) / 8, 150) // set layout
            btn.setBackgroundResource(R.drawable.btn_choose) // set back ground
            val font = Typeface.createFromAsset(assets, "fonts/pacifo.ttf")
            btn.setTypeface(font)
            btn.setOnClickListener(this) // set on click listener
            while (btn.text === "") {
                val tmp: Int = secureRandom.nextInt(16)
                if (check(arraySolution, tmp)) {
                    btn.text = randomQuestions().get(tmp)
                    randomQuestions().removeAt(tmp)
                    arraySolution.add(tmp)
                }
            }
            layoutButtonSelectSecond.addView(btn)
        }
    }

    private fun createSelectedButton() {
        for (i in 0 until solution.length) {
            var btn = Button(this)
            btn.layoutParams = LinearLayout.LayoutParams(110, 110)
            btn.id = 8515 + i
            btn.setBackgroundResource(R.drawable.word_button)
            val font = Typeface.createFromAsset(assets, "fonts/pacifo.ttf")
            btn.setTypeface(font)
            layoutButtonAnswer.addView(btn)
            myButtons.add(btn)
        }
    }

    override fun onClick(v: View?) {
        var button = v as Button
        if (currentIdx >= myButtons.size) return
        myButtons[currentIdx].text = button.text
        userSolution += button.text
        currentIdx++
        v.setEnabled(false)
        v.text = ""
        countSelected++

        if (countSelected == solution.length) {

            if (userSolution == solution) {
                for (i in 0..solution.length - 1) {
                    myButtons[i].setBackgroundResource(R.drawable.btn_true)
                }
                if (mPlayer != null && isMute==false) {
                    mPlayer.release()
                    mPlayer = MediaPlayer.create(this, R.raw.correct)
                    mPlayer.start()
                }
                textViewResult.text = "Đúng rồi"
                textViewResult.visibility = View.VISIBLE
                Handler().postDelayed(
                    {
                        removeView()
                        round++
                        getQuestionAndAnswer()
                    },
                    2000 // value in milliseconds
                )
            } else {
                for (i in 0..solution.length - 1) {
                    myButtons[i].setBackgroundResource(R.drawable.btn_false)
                }
                if (mPlayer != null && isMute==false) {
                    mPlayer.release()
                    mPlayer = MediaPlayer.create(this, R.raw.incorrect)
                    mPlayer.start()
                }
                textViewResult.text = "Sai rồi"
                textViewResult.visibility = View.VISIBLE
                Handler().postDelayed(
                    {
                        removeView()
                        initView()
                    },
                    2000 // value in milliseconds
                )
            }
        }
    }

    private fun removeView() {
        myButtons.clear()
        layoutButtonAnswer.removeAllViews()
        layoutButtonSelectFirst.removeAllViews()
        layoutButtonSelectSecond.removeAllViews()
        textViewResult.setText("")
        userSolution = ""
        countSelected = 0
        currentIdx = 0
        help = 3
        buttonSkip.visibility = View.GONE
        buttonShare.visibility = View.GONE
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Click lần nữa để thoát", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
