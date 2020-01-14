package com.cntn2017_mobiledev.batchuduoihinh.mainactivity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cntn2017_mobiledev.batchuduoihinh.R
import com.cntn2017_mobiledev.batchuduoihinh.playoffline.PlayOfflineActivity
import com.cntn2017_mobiledev.batchuduoihinh.playonline.PlayOnlineActivity
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.get_room_dialog.view.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {
    var doubleBackToExitPressedOnce = false

    lateinit var mSocket: Socket
    lateinit var t: List<String>
    lateinit var roomOptionView: View

    lateinit var spinner: Spinner

    lateinit var builder: AlertDialog.Builder

    lateinit var intents : Intent

    lateinit var editname :EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerView()
        // Connect
        try {
            mSocket = IO.socket("https://chasing-word.herokuapp.com/")
            mSocket.connect()

        } catch (e: URISyntaxException) {
            Log.e("CONG", e.message)
        }
        intents = Intent(this,
            PlayOnlineActivity::class.java)
        roomOptionView = LayoutInflater.from(this).inflate(R.layout.get_room_dialog, null, false)

        spinner = roomOptionView.findViewById(R.id.spinnerID) as Spinner

        builder = AlertDialog.Builder(this)
            .setView(roomOptionView)
            .setTitle("Choose room")

        mSocket.on("RoomID", getRoomID)
        mSocket.on("CreatedRoom", roomCreate)
        mSocket.on("joinedRoom", joinedRoom)
        mSocket.on("errorOnAction", getReason)
    }

    private fun registerView() {
        buttonOffline.setOnClickListener {
            Log.e("CONG", "offline clicked")
            val intent = Intent(this, PlayOfflineActivity::class.java)
            startActivity(intent)

        }
        buttonOnline.setOnClickListener {
            Log.e("CONG", "online clicked")

            mSocket.emit("getRoomID")

            val par = roomOptionView.parent
            if (par != null)
            {
                val par2 = par as ViewGroup
                par2.removeView(roomOptionView)

            }
            val mAlertDialog = builder.show()
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            var width = displayMetrics.widthPixels
            var height = displayMetrics.heightPixels
            mAlertDialog.getWindow()?.setLayout((width*0.8).toInt(), (height*0.8).toInt()); //Controlling width and height.


            editname = roomOptionView.findViewById(R.id.editTextNameUser) as EditText

            roomOptionView.buttonCreate.setOnClickListener {
                if(editname.text.length <3 ){
                    makeToast("Tên phải dài hơn 3 kí tự")
                }
                else{
                    mSocket.emit("client-create",editname.text)
                    mAlertDialog.dismiss()
                }

            }
            roomOptionView.buttonJoinRoom.setOnClickListener {
                val obj = JSONObject()
                obj.put("roomID", spinner.selectedItem.toString())
                if(editname.text.length<3){
                    makeToast("Tên phải dài hơn 3 kí tự")
                }
                else{
                    if(spinner.selectedItem.toString().length> 0){
                        makeToast("Bạn phải chọn phòng trước")
                    }
                    else{
                        obj.put("name", editname.text)
                        Log.e("CONG", spinner.selectedItem.toString())
                        mSocket.emit("joinRoom", obj)
                        mAlertDialog.dismiss()
                    }
                }
            }
            roomOptionView.buttonCancel.setOnClickListener {
                mAlertDialog.dismiss()
            }
        }
    }

    private val getRoomID = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            try {
                var s = data.getString("roomid")
                    .replace("[", "")
                    .replace("{\"roomID\":", "")
                    .replace("}", "")
                    .replace("]", "")
                t = s.split(",")
                if (t != null) {
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item, t
                    )
                    spinner.adapter = adapter
                }
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }
    private val roomCreate = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            Log.e("Cong", ("vao create roomID"))

            val data = args[0] as JSONObject
            try {
                Log.e("Cong", data.getString("roomid"))

                intents.putExtra("flag", 1)
                intents.putExtra("id", data.getString("roomid"))
                intents.putExtra("name", editname.text.toString())
                startActivity(intents)
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }
    private val joinedRoom = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            Log.e("Cong", ("vao joined roomID"))

            val data = args[0] as JSONObject
            try {
                Log.e("Cong", data.getString("ok"))

                intents.putExtra("flag", 0)
                intents.putExtra("id", spinner.selectedItem.toString())
                intents.putExtra("name", editname.text.toString())
                startActivity(intents)

            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }
    private val getReason = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            try {
                Log.e("Cong", data.getString("reason"))
                makeToast(data.getString("reason"))
            } catch (e: JSONException) {
                return@Runnable
            }
        })
    }

    private fun makeToast(string: String) {

        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this,"Click lan nua de thoat",Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
