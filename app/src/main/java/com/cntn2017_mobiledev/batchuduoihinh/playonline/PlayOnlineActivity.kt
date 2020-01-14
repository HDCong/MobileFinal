package com.cntn2017_mobiledev.batchuduoihinh.playonline

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cntn2017_mobiledev.batchuduoihinh.*
import com.cntn2017_mobiledev.batchuduoihinh.adapter.ChatAdapter
import com.cntn2017_mobiledev.batchuduoihinh.adapter.RankingAdapter
import com.cntn2017_mobiledev.batchuduoihinh.models.Chat
import com.cntn2017_mobiledev.batchuduoihinh.models.User
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_play_online.*
import kotlinx.android.synthetic.main.chat_layout.view.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.security.SecureRandom


class PlayOnlineActivity : AppCompatActivity(), View.OnClickListener {
    var doubleBackToExitPressedOnce = false
    lateinit var mSocket: Socket
    var flag = 0
    var rooomid = ""
    //    var str =""
    var countResponse = 0
    var totalScore: Long = 0
    var time: Long = 0
    var username = ""
    lateinit var listUser: ArrayList<User>
    var solution = ""
    var urlPic = ""
    var round = 0
    var currentIdx = 0
    var userSolution = ""
    val secureRandom = SecureRandom()
    var myButtons = ArrayList<Button>(16)
    var countSelected = 0
    lateinit var buttonStart: Button
    lateinit var listViewPlayer: ListView
    lateinit var t: List<String>
    var timer = timer(30000, 1000)
    lateinit var rankingView: View

    lateinit var listing: ListView

    lateinit var builder: AlertDialog.Builder

    lateinit var rankingAdapter: RankingAdapter

    lateinit var chatView: View

    lateinit var listChatContent: ListView

    lateinit var builderChat: AlertDialog.Builder

    lateinit var chatAdapter: ChatAdapter

    lateinit var arrayListChat: ArrayList<Chat>

    lateinit var editcontent: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_online)

        flag = intent.extras?.getInt("flag") as Int
        rooomid = intent.extras?.getString("id") as String
        username = intent.extras?.getString("name") as String

        connectToSocket()
        createWaitingView()
        registerView()

        rankingView = LayoutInflater.from(this).inflate(R.layout.ranking_layout, null, false)
        rankingAdapter =
            RankingAdapter(this)
        listing = rankingView.findViewById(R.id.listviewRanking) as ListView
        builder = AlertDialog.Builder(this)
            .setView(rankingView)
            .setTitle("Ranking")

        chatView = LayoutInflater.from(this).inflate(R.layout.chat_layout, null, false)
        listChatContent = chatView.findViewById(R.id.listViewChat) as ListView
        builderChat = AlertDialog.Builder(this)
            .setView(chatView)
            .setTitle("Chat")
        arrayListChat = ArrayList<Chat>()
        chatAdapter =
            ChatAdapter(this)


        mSocket.on("question", getQuestionFromServer)

        mSocket.on("updatePoint", updatePoint)

        mSocket.on("listPlayer", getListPlayer)

        mSocket.on("chatResponse", onChatResponse)

    }

    private fun registerView() {
        buttonRank.setOnClickListener {

            val par = rankingView.parent
            if (par != null) {
                val par2 = par as ViewGroup
                par2.removeView(rankingView)

            }
            val mAlertDialog = builder.show()

        }

        buttonChat.setOnClickListener {
            val par = chatView.parent
            if (par != null) {
                val par2 = par as ViewGroup
                par2.removeView(chatView)

            }
            editcontent = chatView.findViewById(R.id.editTextChat) as EditText

            val mAlertDialog = builderChat.show()

            chatView.buttonSend.setOnClickListener {
                var obj = JSONObject()
                obj.put("roomID", rooomid)
                obj.put("Message", editcontent.text.toString())
                obj.put("user", username)
                arrayListChat.add(
                    Chat(
                        username,
                        true,
                        editcontent.text.toString()
                    )
                )
                chatAdapter.setItem(arrayListChat)
                listChatContent.adapter = chatAdapter
                mSocket.emit("chat", obj)
                editcontent.setText("")
            }
        }

//        mSocket.emit("updateList", rooomid,username)

        buttonHintOnl.setOnClickListener {
            if (totalScore > 1000) {
                if (currentIdx == solution.length - 2) {
                    Toast.makeText(this, "Còn chữ cuối tự đoán đi :)", Toast.LENGTH_SHORT).show()

                } else {
                    myButtons[currentIdx].text = solution[currentIdx].toString()
                    userSolution += solution[currentIdx].toString()
                    currentIdx++
                    countSelected++
                    totalScore -= 1000
                }
            } else {
                Toast.makeText(this, "Số điểm ít hơn 1000", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to configure and return an instance of CountDownTimer object
    private fun timer(millisInFuture: Long, countDownInterval: Long): CountDownTimer {
        return object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                textViewCountDown.setText("Còn lại: " + millisUntilFinished / 1000)
                time = millisUntilFinished
            }

            override fun onFinish() {
                textViewCountDown.setText("Hết giờ")
                time = 0
//                round++
                countResponse = 0
                mSocket.emit("start-game", rooomid, round)
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun createWaitingView() {
        textViewShowRound.text = "Đang Đợi"
        if (flag == 1) {
            buttonStart = Button(this)
            buttonStart.setLayoutParams(LinearLayout.LayoutParams(450, 150))
            buttonStart.text = "Bắt đầu"
            buttonStart.setOnClickListener(this)
            buttonStart.setId(8988)
            buttonStart.setLayoutParams(
                TableLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 3f
                )
            )
            layoutPictureQuestion.addView(buttonStart)
        }
        listViewPlayer = ListView(this)
        listViewPlayer.setId(8981)
        listViewPlayer.setLayoutParams(
            TableLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
        )
        layoutPictureQuestion.addView(listViewPlayer)

        mSocket.emit("updateList", rooomid.toString(), username + "o create ")

    }

    private fun connectToSocket() {
        try {
            mSocket = IO.socket("https://chasing-word.herokuapp.com/")
        } catch (e: URISyntaxException) {
            Log.e("CONG", e.message)
        }
        mSocket.connect()

        mSocket.emit("updateList", rooomid.toString(), username + "o connect")

    }

    private fun resetCountDowntimer() {

        timer.cancel()
        timer.start()

    }

    private val getQuestionFromServer = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            try {
                urlPic = data.getString("url")
                solution = data.getString("sol")
                solution = solution.toUpperCase()
                round = data.getInt("rou")

                removeView()
                linearButton.visibility = View.VISIBLE
                initView()
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }

    private val updatePoint = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            Log.e("CONG", "Vao update point")
            val data = args[0] as JSONObject
            try {
                val name = data.getString("user")
                val newpoint = data.getString("point")
                Log.e("CONG", name + "-" + newpoint)
                for (i in 0 until listUser.size) {
                    if (name == listUser[i].userName) {
                        listUser[i].totalPoint = newpoint
                    }
                    Log.e(
                        "CONG-Point",
                        listUser[i].userName + "- " + listUser[i].totalPoint.toString()
                    )
                }
                val temp = listUser.sortedByDescending { it.totalPoint?.toLong() }
                val sorted = ArrayList<User>()

                temp.forEach {
                    sorted.add(it)
                }

                rankingAdapter.setItem(sorted)
                listing.adapter = rankingAdapter

                countResponse++
                Log.e("CONG", "count: " + countResponse.toString())
                if (countResponse == listUser.size) {
                    countResponse = 0

                    mSocket.emit("start-game", rooomid, round)

                    removeView()

                    initView()
                }

            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }

    private val getListPlayer = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            Log.e("CONG", "VAO GET LIST")
            val data = args[0] as JSONObject
            try {
                var s = data.getString("list")
                    .replace("[", "")
                    .replace("{\"clientName\":", "")
                    .replace("}", "")
                    .replace("]", "")
                    .replace("\"", "")

                t = s.split(",")
                Log.e("CONG", t.toString())

                if (t != null) {
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1, t
                    )

                    listViewPlayer.adapter = adapter

                    Log.e("CONG", "DA SET")
                    listUser = ArrayList<User>()
                    for (i in 0 until t.size) {
                        listUser.add(
                            User(
                                t[i],
                                "0"
                            )
                        )
                    }

                    rankingAdapter.setItem(listUser)
                    listing.adapter = rankingAdapter

                }
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }

    private val onChatResponse = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            Log.e("CONG", "VAO chat response")
            val data = args[0] as JSONObject
            try {
                var userChat = data.getString("user")
                var messageChat = data.getString("message")
                arrayListChat.add(
                    Chat(
                        userChat,
                        false,
                        messageChat
                    )
                )
                chatAdapter.setItem(arrayListChat)
                listChatContent.adapter = chatAdapter


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
        mSocket.emit("getQuestionRoom")
    }

    private fun initRound() {
        textViewShowRound.text = "Vòng " + round.toString()
    }

    private fun createImageViewQuestion() {
        val imageViewPicture = ImageView(this)
        Picasso.with(this).load(urlPic).into(imageViewPicture)
        layoutPictureQuestion.addView(imageViewPicture)
        buttonHintOnl.visibility = View.VISIBLE
        resetCountDowntimer()
        countResponse = 0
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
            btn.setBackgroundResource(R.drawable.tile_hover) // set back ground
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
            btn.setBackgroundResource(R.drawable.tile_hover) // set back ground
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
            btn.layoutParams = LinearLayout.LayoutParams(100, 100)
            btn.id = 8515 + i
            btn.setBackgroundResource(R.drawable.word_button)
            layoutButtonAnswer.addView(btn)
            myButtons.add(btn)
        }
    }

    override fun onClick(v: View?) {
        var button = v as Button
        if (v.id == 8988) {
            if (listUser.size >= 2) {
                mSocket.emit("start-game", rooomid, round)
                Log.e("CONG", "START GAME")
            } else {
                Toast.makeText(this, "Đợi người vô chơi đi ba", Toast.LENGTH_SHORT).show()
            }
            return

        }

        if (currentIdx >= myButtons.size) return
        myButtons[currentIdx].text = button.text
        userSolution += button.text
        currentIdx++
        v.setEnabled(false)
        v.text = ""
        countSelected++
        if (countSelected == solution.length) {
            Log.e("CONG", "bang rui nek")
            if (userSolution == solution) {
                for (i in 0..solution.length - 1) {
                    myButtons[i].setBackgroundResource(R.drawable.tile_true)
                }
                textViewResult.text = "Đúng rồi"
                textViewResult.visibility = View.VISIBLE
                totalScore += time
            } else {
                for (i in 0..solution.length - 1) {
                    myButtons[i].setBackgroundResource(R.drawable.tile_false)
                }
                textViewResult.text = "Sai rồi"
                textViewResult.visibility = View.VISIBLE

            }
            Log.e("CONG", "vao receive")
            val obj = JSONObject()
            obj.put("roomID", rooomid)
            obj.put("username", username)
            obj.put("point", totalScore.toString())
            Log.e("CONG-POINT", totalScore.toString())
            mSocket.emit("receive", obj)
        }
    }

    private fun removeView() {
        myButtons.clear()
        layoutPictureQuestion.removeAllViews()
        layoutButtonAnswer.removeAllViews()
        layoutButtonSelectFirst.removeAllViews()
        layoutButtonSelectSecond.removeAllViews()
        textViewResult.setText("")
        userSolution = ""
        countSelected = 0
        currentIdx = 0
    }


    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Click lan nua de thoat", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
