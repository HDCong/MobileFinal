package com.cntn2017_mobiledev.batchuduoihinh.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cntn2017_mobiledev.batchuduoihinh.models.Chat
import com.cntn2017_mobiledev.batchuduoihinh.R
import java.util.*


class ChatAdapter(context: Context) : BaseAdapter() {


    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    var items = ArrayList<Chat>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val chat = items.get(position)

        if (convertView == null) {
            if (chat.flag == true)
                view = inflater.inflate(R.layout.row_items_chat_owner, parent, false)
            else
                view = inflater.inflate(R.layout.row_item_chat_other, parent, false)
        } else {
            view = convertView
        }
        var name = view.findViewById(R.id.textViewName) as TextView
        var message = view.findViewById(R.id.textViewMessage) as TextView

        if(chat.flag==false){
            name.visibility=View.VISIBLE
        }

        name.text = chat.mOwner
        message.text = chat.mContent

        return view
    }
    fun setItem(list: ArrayList<Chat>) {
        this.items = list
        notifyDataSetChanged()
    }
    override fun getItem(p0: Int): Any {
        return items.get(p0)
    }
    override fun getCount(): Int {
        return items.size
    }
    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }
}