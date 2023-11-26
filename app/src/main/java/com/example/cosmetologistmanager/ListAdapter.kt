package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import com.example.cosmetologistmanager.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.cosmetologistmanager.ListData

@SuppressLint("ResourceType")
class ListAdapter(context: Context, dataArrayList: ArrayList<ListData>) :
    ArrayAdapter<ListData>(
        context, R.layout.list_item,
        dataArrayList!!
    ) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val listData = getItem(position)
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }
        val listName = view?.findViewById<TextView>(R.id.listName)
        val listTime:TextView = view?.findViewById<TextView>(R.id.listTime) as TextView
        listName?.text = listData!!.name
        listTime?.text = listData!!.hour + ":" + listData!!.minute
        return view
    }
}