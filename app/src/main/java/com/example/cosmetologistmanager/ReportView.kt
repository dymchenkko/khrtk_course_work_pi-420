package com.example.cosmetologistmanager

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.cosmetologistmanager.databinding.ActivityReportViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class ReportView : AppCompatActivity() {
    private lateinit var binding: ActivityReportViewBinding
    private lateinit var firebaseAuth: FirebaseAuth
    var dataArrayList = ArrayList<ListReportData>()
    var listAdapter: ReportAdapter? = null
    var sum_expense = 0
    var sum_income = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_view)
        binding = ActivityReportViewBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        setContentView(binding.root)
        val intent = this.intent
        ArrayAdapter.createFromResource(
            this,
            R.array.sort_types,
            R.layout.spinner_list
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.sortIncomesExpenses.adapter = adapter
        }
        binding.sortIncomesExpenses.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);
        binding.extraSort.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);


        val is_expenses_checked = intent.getBooleanExtra("expenses", false)
        val is_incomes_checked = intent.getBooleanExtra("incomes", false)
        val day_from = intent.getIntExtra("day_from", 0)
        val day_to = intent.getIntExtra("day_to", 0)
        val month_from = intent.getIntExtra("month_from", 0)
        val month_to = intent.getIntExtra("month_to", 0)
        val year_from = intent.getIntExtra("year_from", 0)
        val year_to = intent.getIntExtra("year_to", 0)
        val items = mutableListOf<String>()

        val user = firebaseAuth.currentUser
        user?.let {
            var uid = it.uid
if (is_expenses_checked) {
    FirebaseDatabase.getInstance().reference.child("expenses").child(uid)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val expense: Expense? = snapshot.getValue(Expense::class.java)
                    Log.d("hash", snapshot?.key + "")
                    Log.d("expense", expense?.name.toString())
                    Log.d("expense", expense?.day.toString())

                    var listData = ListReportData(
                        expense?.name.toString(),
                        expense?.day.toString(),
                        expense?.month.toString(),
                        expense?.year.toString(),
                        expense?.price.toString(),
                        OperationKind.Expense,
                        snapshot?.key.toString()
                    )
                    if (isDateInRange(
                            day_from,
                            month_from,
                            year_from,
                            day_to,
                            month_to,
                            year_to,
                            expense?.day!!.toInt(),
                            expense.month!!.toInt(),
                            expense.year!!.toInt()
                        )
                    ) {
                        dataArrayList.add(listData)
                        sum_expense += expense.price!!.toInt()
                        binding.incomeSum.text =  "Доход: "+ sum_income.toString() + " грн."
                        binding.expenseSum.text =  "Витрати: "+ sum_expense.toString() + " грн."
                        binding.res.text =
                            "Результат: "+ (sum_income - sum_expense).toString() + " грн."

                    }
                    dataArrayList = ArrayList(sortListData(dataArrayList, false, true))
                    listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                    binding.listReview.setAdapter(listAdapter)
                }

                if (dataArrayList.size == 0) {
                    binding.incomeSum.text =  "Доход: 0 грн."
                    binding.expenseSum.text =  "Витрати: 0 грн."
                    binding.res.text =
                        "Результат: 0 грн."
                    binding.noReport.visibility = View.VISIBLE
                    binding.downloadReport.isClickable = false

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
}
        }
        user?.let {
            val uid = it.uid
            if (is_incomes_checked) {

                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val new_appointment: Appointment? =
                                    snapshot.getValue(Appointment::class.java)
                                Log.d("hash", snapshot?.key + "")
                                var listData = ListReportData(
                                    new_appointment?.procedure.toString(),
                                    new_appointment?.day.toString(),
                                    new_appointment?.month.toString(),
                                    new_appointment?.year.toString(),
                                    new_appointment?.price.toString(),
                                    OperationKind.Income,
                                    snapshot?.key.toString()
                                )
                                //Log.d("new_appointment price", listData.price.toString())

                                //sum_income = sum_income + new_appointment?.price?.toInt()!!

                                if (isDateInRange(
                                        day_from,
                                        month_from,
                                        year_from,
                                        day_to,
                                        month_to,
                                        year_to,
                                        listData.day.toInt(),
                                        listData.month.toInt(),
                                        listData.year.toInt()
                                    )
                                ) {
                                    dataArrayList.add(listData)
                                    sum_income += new_appointment?.price!!.toInt()
                                    binding.incomeSum.text =  "Доход: "+ sum_income.toString() + " грн."
                                    binding.expenseSum.text =  "Витрати: "+ sum_expense.toString() + " грн."
                                    binding.res.text =
                                        "Результат: "+ (sum_income - sum_expense).toString() + " грн."
                                    Log.e("array length4", dataArrayList.size.toString())

                                }
                                dataArrayList = ArrayList(sortListData(dataArrayList, false, true))

                                listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                                binding.listReview.adapter = listAdapter
                            }
                            if (dataArrayList.size == 0) {
                                binding.incomeSum.text =  "Доход: 0 грн."
                                binding.expenseSum.text =  "Витрати: 0 грн."
                                binding.res.text =
                                    "Результат: 0 грн."
                                binding.noReport.visibility = View.VISIBLE
                                binding.downloadReport.isClickable = false

                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }
        }

        binding.listReview.adapter = listAdapter
        binding.listReview.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                if (dataArrayList[i].kind == OperationKind.Expense){
                    val intent = Intent(this@ReportView, EditExpense::class.java)
                    intent.putExtra("hash", dataArrayList[i].hash)
                    startActivity(intent)
                } else {
                    val intent = Intent(this@ReportView, EditAppointmentActivity::class.java)
                    intent.putExtra("hash", dataArrayList[i].hash)
                    startActivity(intent)
                }
            }

        binding.sortIncomesExpenses.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                sort_all()
                Log.e("array after sort", dataArrayList.size.toString())

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })

        binding.extraSort.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                sort_all2()
                Log.e("array after sort", dataArrayList.size.toString())

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        }

        binding.downloadReport.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@ReportView)
            builder
                .setMessage("Ви точно завантажити звіт?")
                .setTitle("Звіт буде завантажено у .xls (excel) файл")
                .setPositiveButton("Так") { dialog, which ->
                    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
                    val current = LocalDateTime.now().format(formatter)
                    val fileOutput =
                        path.toString() + "/" + current + ".xls"
                    writeToExcelFile(fileOutput, current)
                }
                .setNegativeButton("Ні") { dialog, which ->
                }

            val dialog: AlertDialog = builder.create()
            dialog.show()

        }

    }

    override fun onRestart() {
        super.onRestart()
        dataArrayList = ArrayList<ListReportData>()
        var sum_expense = 0;
        var sum_income = 0;
        val intent = this.intent

        val is_expenses_checked = intent.getBooleanExtra("expenses", false)
        val is_incomes_checked = intent.getBooleanExtra("incomes", false)
        val day_from = intent.getIntExtra("day_from", 0)
        val day_to = intent.getIntExtra("day_to", 0)
        val month_from = intent.getIntExtra("month_from", 0)
        val month_to = intent.getIntExtra("month_to", 0)
        val year_from = intent.getIntExtra("year_from", 0)
        val year_to = intent.getIntExtra("year_to", 0)
        val user = firebaseAuth.currentUser
        user?.let {
            var uid = it.uid
            if (is_expenses_checked) {
                FirebaseDatabase.getInstance().reference.child("expenses").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        @SuppressLint("SetTextI18n")
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val expense: Expense? = snapshot.getValue(Expense::class.java)
                                Log.d("hash", snapshot?.key + "")
                                Log.d("expense", expense?.name.toString())
                                Log.d("expense", expense?.day.toString())

                                var listData = ListReportData(
                                    expense?.name.toString(),
                                    expense?.day.toString(),
                                    expense?.month.toString(),
                                    expense?.year.toString(),
                                    expense?.price.toString(),
                                    OperationKind.Expense,
                                    snapshot?.key.toString()
                                )
                                if (isDateInRange(
                                        day_from,
                                        month_from,
                                        year_from,
                                        day_to,
                                        month_to,
                                        year_to,
                                        expense?.day!!.toInt(),
                                        expense.month!!.toInt(),
                                        expense.year!!.toInt()
                                    )
                                ) {
                                    dataArrayList.add(listData)
                                    sum_expense += expense.price!!.toInt()
                                    binding.incomeSum.text =  "Доход: "+ sum_income.toString() + " грн."
                                    binding.expenseSum.text =  "Витрати: "+ sum_expense.toString() + " грн."
                                    binding.res.text =
                                        "Результат: "+ (sum_income - sum_expense).toString() + " грн."

                                }
                                sort_all()
                                sort_all2()
                            }

                            if (dataArrayList.size == 0) {
                                binding.incomeSum.text =  "Доход: 0 грн."
                                binding.expenseSum.text =  "Витрати: 0 грн."
                                binding.res.text =
                                    "Результат: 0 грн."
                                binding.noReport.visibility = View.VISIBLE
                                binding.downloadReport.isClickable = false

                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }
        }
        user?.let {
            val uid = it.uid
            if (is_incomes_checked) {

                FirebaseDatabase.getInstance().reference.child("appointments").child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val new_appointment: Appointment? =
                                    snapshot.getValue(Appointment::class.java)
                                Log.d("hash", snapshot?.key + "")
                                val listData = ListReportData(
                                    new_appointment?.procedure.toString(),
                                    new_appointment?.day.toString(),
                                    new_appointment?.month.toString(),
                                    new_appointment?.year.toString(),
                                    new_appointment?.price.toString(),
                                    OperationKind.Income,
                                    snapshot?.key.toString()
                                )
                                //Log.d("new_appointment price", listData.price.toString())

                                //sum_income = sum_income + new_appointment?.price?.toInt()!!

                                if (isDateInRange(
                                        day_from,
                                        month_from,
                                        year_from,
                                        day_to,
                                        month_to,
                                        year_to,
                                        listData.day.toInt(),
                                        listData.month.toInt(),
                                        listData.year.toInt()
                                    )
                                ) {
                                    dataArrayList.add(listData)
                                    sum_income += new_appointment?.price!!.toInt()
                                    binding.incomeSum.text =  "Доход: "+ sum_income.toString() + " грн."
                                    binding.expenseSum.text =  "Витрати: "+ sum_expense.toString() + " грн."
                                    binding.res.text =
                                        "Результат: "+ (sum_income - sum_expense).toString() + " грн."
                                    Log.e("array length2", dataArrayList.size.toString())

                                }
                                sort_all()
                                sort_all2()
                            }
                            if (dataArrayList.size == 0) {
                                binding.incomeSum.text =  "Доход: 0 грн."
                                binding.expenseSum.text =  "Витрати: 0 грн."
                                binding.res.text =
                                    "Результат: 0 грн."
                                binding.noReport.visibility = View.VISIBLE
                                binding.downloadReport.isClickable = false
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }
        }

    }
    fun writeToExcelFile(filepath: String, current: String) {
        val xlWb = HSSFWorkbook()
        val xlWs = xlWb.createSheet()

        for (i in 0..dataArrayList.size-1) {
            var cell = xlWs.createRow(i+1)
            cell.createCell(0).setCellValue(dataArrayList.get(i).name)
            cell.createCell(1).setCellValue(dataArrayList.get(i).price   +" грн.")
            if (dataArrayList[i].kind == OperationKind.Income ) {
                cell.createCell(2).setCellValue("Дохід")
            } else {
                cell.createCell(2).setCellValue("Витрата")
            }
        }

        Log.e("array lengthh", dataArrayList.size.toString())
        var cell1 = xlWs.createRow(dataArrayList.size+2)
        cell1.createCell(0).setCellValue("Усього доходу:")
        cell1.createCell(1).setCellValue(sum_income.toString()  +" грн.")

        var cell2 = xlWs.createRow(dataArrayList.size+3)
        cell2.createCell(0).setCellValue("Усього витрат:")
        cell2.createCell(1).setCellValue(sum_expense.toString()  +" грн.")

        var cell3 = xlWs.createRow(dataArrayList.size+4)
        cell3.createCell(0).setCellValue("Результат:")
        cell3.createCell(1).setCellValue((sum_income - sum_expense).toString()  +" грн.")

        val outputStream = FileOutputStream(filepath)
        xlWb.write(outputStream)
        xlWb.close()

        val text = "Звіт завантажений під назвою: " + current
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this@ReportView, text, duration)
        toast.show()
        //binding.downloadReport.isClickable = false
    }

    fun isDateInRange(day1: Int, month1: Int, year1: Int, day2: Int, month2: Int, year2: Int, day3: Int, month3: Int, year3: Int): Boolean {
        // Convert dates to milliseconds
        val date1 = Calendar.getInstance().apply { set(year1, month1+1, day1) }.timeInMillis
        val date2 = Calendar.getInstance().apply { set(year2, month2+1, day2) }.timeInMillis
        val date3 = Calendar.getInstance().apply { set(year3, month3, day3) }.timeInMillis
        Log.d("month1", month1.toString())
        Log.d("month2", month2.toString())
        Log.d("month3", month3.toString())

        // Check if date3 is between date1 and date2
        return date3 in date1..date2
    }
    private fun sortListReportDataByDate(list: ArrayList<ListReportData>, is_normal: Boolean): ArrayList<ListReportData> {
        var sortedList = ArrayList(list.sortedWith(compareBy({ it.year.toInt() }, { it.month.toInt() }, { it.day.toInt() })))
        if (is_normal) {
            sortedList.reverse()
        }
        return sortedList
    }
    fun sortListData(list: ArrayList<ListReportData>, isIncome: Boolean, lowestFirst: Boolean): List<ListReportData> {
        // Separate incomes and expenses into two lists
        val incomes = ArrayList<ListReportData>()
        val expenses = ArrayList<ListReportData>()

        for (item in list) {
            if (item.kind == OperationKind.Income)
                incomes.add(item)
            else
                expenses.add(item)
        }

        // Sort incomes and expenses by price
        incomes.sortBy { it.price.toDouble() }
        expenses.sortBy { it.price.toDouble() }

        // Reverse the order if lowestFirst is true
        if (!lowestFirst) {
            incomes.reverse()
            expenses.reverse()
        }

        // Concatenate the arrays based on isIncome flag
        val sortedList = if (isIncome) incomes + expenses else expenses + incomes

        return sortedList
    }


    fun sortListAlphabetically(list: List<ListReportData>, normal: Boolean): List<ListReportData> {
        if (!normal) {
            return list.sortedBy { it.name.lowercase() }.reversed()

        }
        return list.sortedBy { it.name.lowercase() }
    }

    fun sort_all() {
        val sort: String = binding.sortIncomesExpenses.getSelectedItem().toString()

        if (sort == "За датою") {
            ArrayAdapter.createFromResource(
                this@ReportView,
                R.array.sort_types_date,
                R.layout.spinner_list
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.extraSort.adapter = adapter
            }
            binding.sortIncomesExpenses.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);
            binding.extraSort.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);

            binding.extraSort.visibility = View.VISIBLE
            var is_normal = true
            val extraSort: String = binding.extraSort.getSelectedItem().toString()

            if (extraSort == "Найстаріші") {
                is_normal = false
            }
            binding.extraSort.visibility = View.VISIBLE
            dataArrayList = ArrayList(sortListReportDataByDate(dataArrayList, is_normal))
            listAdapter = ReportAdapter(this@ReportView, dataArrayList)
            binding.listReview.setAdapter(listAdapter)
        } else if (sort == "За алфавітним порядком") {
            ArrayAdapter.createFromResource(
                this@ReportView,
                R.array.sort_types_alphabet,
                R.layout.spinner_list
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.extraSort.adapter = adapter
            }
            binding.extraSort.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);
            binding.sortIncomesExpenses.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);

            val extraSort: String = binding.extraSort.getSelectedItem().toString()

            if (extraSort.equals("Від Я до А (Z до A)")) {
                dataArrayList = ArrayList(sortListAlphabetically(dataArrayList, false))
            } else {
                dataArrayList = ArrayList(sortListAlphabetically(dataArrayList, true))
            }
            listAdapter = ReportAdapter(this@ReportView, dataArrayList)
            binding.listReview.setAdapter(listAdapter)
        } else {
            ArrayAdapter.createFromResource(
                this@ReportView,
                R.array.sort_types_price,
                R.layout.spinner_list
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.extraSort.adapter = adapter
            }
            binding.sortIncomesExpenses.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);
            binding.extraSort.getBackground().setColorFilter(getResources().getColor(R.color.spinner_color), PorterDuff.Mode.SRC_ATOP);

            var isIncome = true
            var lowestFirst = true
            if (sort == "Спочатку витрати, потім доходи") {
                isIncome = false
            }
            val extraSort: String = binding.extraSort.getSelectedItem().toString()

            if (extraSort == "Спочатку найбільша ціна") {
                lowestFirst = false
            }
            binding.extraSort.visibility = View.VISIBLE
            dataArrayList = ArrayList(sortListData(dataArrayList, isIncome, lowestFirst))
            listAdapter = ReportAdapter(this@ReportView, dataArrayList)
            binding.listReview.setAdapter(listAdapter)

        }

    }
    fun sort_all2() {
        val sort: String = binding.sortIncomesExpenses.getSelectedItem().toString()

        if (sort == "За датою") {
            var is_normal = true
            val extraSort: String = binding.extraSort.getSelectedItem().toString()

            if (extraSort == "Найстаріші") {
                is_normal = false
            }
            binding.extraSort.visibility = View.VISIBLE
            dataArrayList = ArrayList(sortListReportDataByDate(dataArrayList, is_normal))
            listAdapter = ReportAdapter(this@ReportView, dataArrayList)
            binding.listReview.setAdapter(listAdapter)

        } else if (sort == "За алфавітним порядком") {
            val extraSort: String = binding.extraSort.getSelectedItem().toString()

            if (extraSort == "Від Я до А (Z до A)") {
                dataArrayList = ArrayList(sortListAlphabetically(dataArrayList, false))
            } else {
                dataArrayList = ArrayList(sortListAlphabetically(dataArrayList, true))
            }
            listAdapter = ReportAdapter(this@ReportView, dataArrayList)
            binding.listReview.setAdapter(listAdapter)
        } else {
            var isIncome = true
            var lowestFirst = true
            if (sort == "Спочатку витрати, потім доходи") {
                isIncome = false
            }
            val extraSort: String = binding.extraSort.getSelectedItem().toString()

            if (extraSort.equals("Спочатку найбільша ціна")) {
                lowestFirst = false
            }
            binding.extraSort.visibility = View.VISIBLE
            dataArrayList = ArrayList(sortListData(dataArrayList, isIncome, lowestFirst))
            listAdapter = ReportAdapter(this@ReportView, dataArrayList)
            binding.listReview.setAdapter(listAdapter)
        }
    }
}
