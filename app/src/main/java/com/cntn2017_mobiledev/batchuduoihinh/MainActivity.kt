package com.cntn2017_mobiledev.batchuduoihinh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import java.net.URISyntaxException

class MainActivity : AppCompatActivity() {
    lateinit var mSocket : Socket
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

        mSocket.connect()
    }

    private fun init() {
        try {
            mSocket = IO.socket("http://192.168.0.114:8515")
        } catch(e : URISyntaxException){
            Log.e("CONG", "URI syntax exception")
        }
    }
}
