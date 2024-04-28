package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class ReportAdapter(context: Context, dataArrayList: ArrayList<ListReportData>) :
    ArrayAdapter<ListReportData>(
        context, R.layout.list_item_report,
        dataArrayList
    ) {
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val listData = getItem(position)
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_report, parent, false)
        }
        val listName = view?.findViewById<TextView>(R.id.listName)
        val listDate: TextView = view?.findViewById<TextView>(R.id.listDate) as TextView
        val listPrice: TextView = view?.findViewById<TextView>(R.id.listPrice) as TextView
        val listImage: ImageView = view?.findViewById<ImageView>(R.id.expense_icon) as ImageView

        listName?.text = listData!!.name
        listDate.text = addLeadingZero(listData.day) + "/" + addLeadingZero(listData.month) + "/" + listData.year
        listPrice?.text = listData!!.price + " грн."
        if (listData.kind == OperationKind.Income) {
            listPrice.setTextColor(Integer.parseUnsignedInt("FF018786",16))
            listImage.setImageResource(R.drawable.income)
        }
        else {
            listPrice.setTextColor(Integer.parseUnsignedInt("FFFF0000",16))
            listImage.setImageResource(R.drawable.expense_icon)
        }
        return view
    }
}