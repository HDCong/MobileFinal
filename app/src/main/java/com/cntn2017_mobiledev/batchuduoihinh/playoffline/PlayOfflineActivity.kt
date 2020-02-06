package com.cntn2017_mobiledev.batchuduoihinh.playoffline

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cntn2017_mobiledev.batchuduoihinh.R
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_play_offline.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.security.SecureRandom


class PlayOfflineActivity : AppCompatActivity(), View.OnClickListener {
    var doubleBackToExitPressedOnce = false

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

        getQuestionAndAnswer()
        mSocket.on("QuestionNek", getQuestionFromServer)

        registerView()
    }

    private fun registerView() {

        buttonHint.setOnClickListener {
            if (help > 0) {
                if (currentIdx >= solution.length - 1) {
                    Toast.makeText(this, "Còn chữ cuối tự đoán đi :)", Toast.LENGTH_SHORT).show()
                }
                else {
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
            shareIntent()
        }
    }

    private fun shareIntent() {


    }

    private fun takeScreenShot() {


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
    }

    private fun getQuestionAndAnswer() {
        mSocket.emit("getQuestion")
    }

    private fun initRound() {
        relativeFirst.visibility = View.VISIBLE
        textViewShowRound.text = "Vòng " + round.toString()
    }

    private fun createImageViewQuestion() {
        Picasso.with(this).load(urlPic).into(imageViewPicture)
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
//        Log.e("CONG", arrS.toString())
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
            val font = Typeface.createFromAsset(assets,"fonts/pacifo.ttf")
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
            val font = Typeface.createFromAsset(assets,"fonts/pacifo.ttf")
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
            btn.layoutParams = LinearLayout.LayoutParams(120, 120)
            btn.id = 8515 + i
            btn.setBackgroundResource(R.drawable.word_button)
            val font = Typeface.createFromAsset(assets,"fonts/pacifo.ttf")
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
//            layoutButtonSelectFirst.isEnabled=false
//            layoutButtonSelectSecond.isEnabled=false

            if (userSolution == solution) {
                for (i in 0..solution.length - 1) {
                    myButtons[i].setBackgroundResource(R.drawable.btn_true)
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
//            layoutButtonSelectFirst.isEnabled=true
//            layoutButtonSelectSecond.isEnabled=true

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
