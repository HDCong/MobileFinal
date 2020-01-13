package com.cntn2017_mobiledev.batchuduoihinh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_play_online.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class PlayOnlineActivity : AppCompatActivity() {
    lateinit var mSocket: Socket
    lateinit var arrayListChat : ArrayList<Chat>
    lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_online)
        init()
        try {
            mSocket = IO.socket("http://192.168.1.6:8515")
        } catch (e: URISyntaxException) {
            Log.e("CONG", e.message)
        }
        mSocket.connect()
        registerView()

        mSocket.on("RoomID",getRoomID)
        mSocket.on("test",getSomething)

        arrayListChat.add(Chat("a",true,"mess5"))
        chatAdapter.setItem(arrayListChat)
        listViewChat.adapter= chatAdapter

    }

    private fun init() {
        arrayListChat = ArrayList<Chat>()
        arrayListChat.add(Chat("b",false,"mess0"))

        arrayListChat.add(Chat("a",true,"mess1"))
        arrayListChat.add(Chat("b",false,"mess2"))
        arrayListChat.add(Chat("a",true,"mess3"))
        arrayListChat.add(Chat("c",false,"mess4"))
        chatAdapter = ChatAdapter(this);
        chatAdapter.setItem(arrayListChat)
        listViewChat.adapter= chatAdapter
    }

    private fun registerView() {
        buttonDisconnect.setOnClickListener {
            mSocket.disconnect()
        }
        buttonGetRoom.setOnClickListener {
            try {
                mSocket.emit("getRoomID")
            } catch (ex: Exception) {
                throw ex

            }
        }
        buttonSend.setOnClickListener{
            val obj = JSONObject()
            obj.put("roomID","3")
            obj.put("user","top")
            obj.put("Message","Ahihi do ngoc")

            arrayListChat.add(Chat("top",true,"mess8"))
            chatAdapter.setItem(arrayListChat)

            mSocket.emit("chat", obj)

        }
        buttonConnect.setOnClickListener {
            mSocket.connect()
        }
    }


    private fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mSocket.disconnect()

    }
    private val getRoomID = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            var roomid: String
            try {
                roomid = data.getString("roomid")
                makeToast(roomid)
                Log.e("CONG", "romid : "+ roomid)

            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }
    private val getSomething = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            Log.e("COng", args[0].toString())
            try {
                makeToast(data.getString("user"))//toString())
                Log.e("CONG",data.getString("user"))
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }

}
