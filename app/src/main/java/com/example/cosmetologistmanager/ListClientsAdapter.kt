package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

@Suppress("NAME_SHADOWING")
@SuppressLint("ResourceType")
class ListClientsAdapter(context: Context, dataArrayList: ArrayList<ListClientData>) :
    ArrayAdapter<ListClientData>(
        context, R.layout.list_client_item,
        dataArrayList
    ) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val listData = getItem(position)
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_client_item, parent, false)
        }
        val listName = view?.findViewById<TextView>(R.id.clientName)
        listName?.text = listData!!.surname + " " + listData!!.name  + " " + listData!!.patronymic.toString()
        return view!!
    }
}