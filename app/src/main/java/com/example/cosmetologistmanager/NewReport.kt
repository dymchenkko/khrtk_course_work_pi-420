package com.example.cosmetologistmanager

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.cosmetologistmanager.databinding.ActivityNewReportBinding
import java.time.LocalDate
import java.util.Locale

class NewReport : AppCompatActivity() {
    private lateinit var binding: ActivityNewReportBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_report)
        val locale = Locale("UA")
        Locale.setDefault(locale)
        binding = ActivityNewReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createNewReport.setOnClickListener{
            val day_from = binding.datePickerButtonfrom.dayOfMonth
            val day_to = binding.datePickerButtonto.dayOfMonth
            val month_from = binding.datePickerButtonfrom.month
            val month_to = binding.datePickerButtonto.month
            val year_from = binding.datePickerButtonfrom.year
            val year_to = binding.datePickerButtonto.year
            if (!binding.checkExpensesSwitch.isChecked && !binding.checkIncomesSwitch.isChecked){
                val text = "Відмітьте хоча б одне поле (Доходи/Розходи)!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this@NewReport, text, duration)
                toast.show()
            }
            else if (!isEarlierOrSame(day_from, month_from, year_from, day_to, month_to, year_to)){
                val text = "Оберіть початкову дату раніше за кінцеву дату!"
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(this@NewReport, text, duration)
                toast.show()
            }
            else {
                val intent = Intent(this@NewReport, ReportView::class.java)
                intent.putExtra("expenses", binding.checkExpensesSwitch.isChecked)
                intent.putExtra("incomes", binding.checkIncomesSwitch.isChecked)
                intent.putExtra("day_from", day_from)
                intent.putExtra("day_to", day_to)
                intent.putExtra("month_from", month_from)
                intent.putExtra("month_to", month_to)
                intent.putExtra("year_from", year_from)
                intent.putExtra("year_to", year_to)
                startActivity(intent)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun isEarlierOrSame(day1: Int, month1: Int, year1: Int, day2: Int, month2: Int, year2: Int): Boolean {
        val date1 = LocalDate.of(year1, month1, day1)
        val date2 = LocalDate.of(year2, month2, day2)

        var result = false

        if (date1 == date2 || !date1.isAfter(date2)) {
            result = true
        }
        return result
    }
}