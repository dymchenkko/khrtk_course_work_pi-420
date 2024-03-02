package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

@SuppressLint("ResourceType")
class ListClientsAppointmentsAdapter(context: Context, dataArrayList: ArrayList<ListClientsAppointmentData>) :
    ArrayAdapter<ListClientsAppointmentData>(
        context, R.layout.list_item_clients_appointment,
        dataArrayList
    ) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val listData = getItem(position)
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_clients_appointment, parent, false)
        }
        val listName = view?.findViewById<TextView>(R.id.listName)
        val listTime:TextView = view?.findViewById(R.id.listTime) as TextView
        val listDate:TextView = view.findViewById(R.id.listDate) as TextView

        listName?.text = listData!!.name
        listTime.text = listData.hour + ":" + listData.minute
        listDate.text = listData.day + "/" + listData.month + "/" + listData.year

        return view
    }
}