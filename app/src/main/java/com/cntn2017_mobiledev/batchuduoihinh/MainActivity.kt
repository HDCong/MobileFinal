package com.cntn2017_mobiledev.batchuduoihinh

import android.content.Intent
import android.os.Bundle
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
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.get_room_dialog.view.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {
    lateinit var mSocket: Socket
    lateinit var t: List<String>
    lateinit var roomOptionView: View
    lateinit var spinner: Spinner
    lateinit var builder: AlertDialog.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerView()
        // Connect
        try {
            mSocket = IO.socket("http://192.168.1.6:8515")
            mSocket.connect()

        } catch (e: URISyntaxException) {
            Log.e("CONG", e.message)
        }
        roomOptionView = LayoutInflater.from(this).inflate(R.layout.get_room_dialog, null, false)
        spinner = roomOptionView.findViewById(R.id.spinnerID) as Spinner
        builder = AlertDialog.Builder(this)
            .setView(roomOptionView)
            .setTitle("Choose room")
        mSocket.on("RoomID", getRoomID)
        mSocket.on("CreatedRoom", createdRoom)
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
            var editname = roomOptionView.findViewById(R.id.editTextNameUser) as EditText
            roomOptionView.buttonCreate.setOnClickListener {
                mSocket.emit("client-create",editname.text)

            }
            roomOptionView.buttonJoinRoom.setOnClickListener {
                val obj = JSONObject()
                obj.put("roomID", spinner.selectedItem.toString())
                if(editname.text.length<3){
                    makeToast("Tên phải dài hơn 3 kí tự")
                }
                obj.put("name", editname.text)

                Log.e("CONG", spinner.selectedItem.toString())
                mSocket.emit("joinRoom", obj)
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
    private val createdRoom = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            try {
                Log.e("Cong", data.getString("roomID"))
                val intent = Intent(this, PlayOnlineActivity::class.java)
                intent.putExtra("flag", 1)
                startActivity(intent)

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
}
