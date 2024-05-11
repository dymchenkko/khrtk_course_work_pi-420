package com.example.cosmetologistmanager

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
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
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.sortIncomesExpenses.adapter = adapter
        }

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
                            expense?.month!!.toInt(),
                            expense?.year!!.toInt()
                        )
                    ) {
                        dataArrayList.add(listData)
                        sum_expense += expense?.price!!.toInt()
                        binding.expenseSum.text = sum_expense.toString() + " грн."
                        binding.res.text = (sum_income - sum_expense).toString() + " грн."
                    }
                    listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                    binding.listReview.setAdapter(listAdapter)
                    //binding.listReview.setClickable(true)
                    Log.d("list of appointments", items.toString())
                }

                if (dataArrayList.size == 0) {
                    //binding.noClients.visibility = View.VISIBLE
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
                                        listData?.day!!.toInt(),
                                        listData?.month!!.toInt(),
                                        listData?.year!!.toInt()
                                    )
                                ) {
                                    dataArrayList.add(listData)
                                    sum_income += new_appointment?.price!!.toInt()
                                    binding.incomeSum.text =  "Доход: "+ sum_income.toString() + " грн."
                                    binding.expenseSum.text =  "Витрати: "+ sum_expense.toString() + " грн."
                                    binding.res.text =
                                        "Результат: "+ (sum_income - sum_expense).toString() + " грн."
                                }
                                listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                                binding.listReview.setAdapter(listAdapter)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }
        }

        binding.listReview.setAdapter(listAdapter)

        binding.listReview.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
            if (dataArrayList[i].kind == OperationKind.Expense){
                val intent = Intent(this@ReportView, EditExpense::class.java)
                intent.putExtra("hash", dataArrayList[i].hash)
                startActivity(intent)
            } else {
                val intent = Intent(this@ReportView, EditAppointmentActivity::class.java)
                intent.putExtra("hash", dataArrayList[i].hash)
                startActivity(intent)
            }
        })

        binding.sortIncomesExpenses.setOnItemSelectedListener(object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                var sort:String = binding.sortIncomesExpenses.getSelectedItem().toString()

                if (sort.equals("За датою")) {
                    ArrayAdapter.createFromResource(
                        this@ReportView,
                        R.array.sort_types_date,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.extraSort.adapter = adapter
                    }
                    binding.extraSort.visibility = View.VISIBLE
                }
                else if (sort.equals("За алфавітним порядком")){
                    ArrayAdapter.createFromResource(
                        this@ReportView,
                        R.array.sort_types_alphabet,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.extraSort.adapter = adapter
                    }
                    dataArrayList = ArrayList(sortListAlphabetically(dataArrayList))
                    listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                    binding.listReview.setAdapter(listAdapter)                }
                else {
                    ArrayAdapter.createFromResource(
                        this@ReportView,
                        R.array.sort_types_price,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.extraSort.adapter = adapter
                    }
                    binding.extraSort.visibility = View.VISIBLE
                    dataArrayList = ArrayList(sortListData(dataArrayList, true, true))
                    listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                    binding.listReview.setAdapter(listAdapter)

                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // your code here
            }
        })
        /*binding.listReview.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l ->
        val intent = Intent(this@ReportView, MainActivity::class.java)
        //intent.putExtra("hash", dataArrayList[i].hash)
        startActivity(intent)
    })*/


        /*val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        val fileOutput =
          path.toString() + "/test.xls"

        var filepath = path.toString() + "/test.xlsx"
        //val filepath = "./test.xlsx"
        /*val fileOutput: File =
            File(storageVolume.getDirectory().getPath() + "/Download/ProgrammerWorld.xls")*/
            filepath= Environment.getExternalStorageDirectory().toString() + "/test.xls";

        writeToExcelFile(fileOutput)
        readFromExcelFile(fileOutput)*/
        //Create a class variable that is your activities request code
        //Create a class variable that is your activities request code

//Create the intent and start the activity

//Create the intent and start the activity

        binding.downloadReport.setOnClickListener {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                /*val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                i.addCategory(Intent.CATEGORY_DEFAULT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "application/vnd.ms-excel"
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory().toUri())

                //intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)))
                startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999)*/
                /*val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addCategory(Intent.CATEGORY_DEFAULT)

                // Specify a starting directory URI to Downloads directory
                val downloadsUri = Uri.parse("content://com.android.externalstorage.documents/tree/downloads")
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri)
                startActivityForResult(intent, 9999)*/
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addCategory(Intent.CATEGORY_DEFAULT)

                // Specify a starting directory URI to Downloads directory
                val downloadsUri = Uri.parse("content://com.android.externalstorage.documents/tree/downloads")
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri)

                startActivityForResult(intent, 9999)
            }
            /*val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
            val current = LocalDateTime.now().format(formatter)
            val fileOutput =
                path.toString() + "/" + current + ".xls"
            writeToExcelFile(fileOutput)*/
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            9999 -> {
                if (data != null) {
                    Log.i("Test", "Result URI " + data.data)
                    Log.i("Test", "Result URI " + data.dataString)
                    Log.i("Test", "Result URI " + data.action)
                    Log.i("Test", "Result URI " + data)
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(
                        data.data,
                        DocumentsContract.getTreeDocumentId(data.data)
                    )
                    val path: String = getPath(this, docUri)!!
                    Log.i("Test", "Result URI " + path)
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

                    if (!path.startsWith(downloadsDir)) {
                        Toast.makeText(this, "Звіт можна зберегти тільки всередині папки Downloads!", Toast.LENGTH_SHORT).show()
                    } else {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                        val current = LocalDateTime.now().format(formatter)
                        val fileOutput =
                            path.toString() + "/" + current + ".xls"
                        writeToExcelFile(fileOutput)
                    }
                }
                //val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                //Log.i("Test", "path " + path)

                /*val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                val current = LocalDateTime.now().format(formatter)
                val fileOutput =
                    path.toString() + "/" + current + ".xls"
                writeToExcelFile(fileOutput)*/
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
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
                                        expense?.month!!.toInt(),
                                        expense?.year!!.toInt()
                                    )
                                ) {
                                    dataArrayList.add(listData)
                                    sum_expense += expense?.price!!.toInt()
                                    binding.expenseSum.text = sum_expense.toString() + " грн."
                                    binding.res.text = (sum_income - sum_expense).toString() + " грн."
                                }
                                listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                                binding.listReview.setAdapter(listAdapter)
                                //binding.listReview.setClickable(true)
                                //Log.d("list of appointments", items.toString())
                            }

                            if (dataArrayList.size == 0) {
                                //binding.noClients.visibility = View.VISIBLE
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
                                        listData?.day!!.toInt(),
                                        listData?.month!!.toInt(),
                                        listData?.year!!.toInt()
                                    )
                                ) {
                                    dataArrayList.add(listData)
                                    sum_income += new_appointment?.price!!.toInt()
                                    binding.incomeSum.text =  "Доход: "+ sum_income.toString() + " грн."
                                    binding.expenseSum.text =  "Витрати: "+ sum_expense.toString() + " грн."
                                    binding.res.text =
                                        "Результат: "+ (sum_income - sum_expense).toString() + " грн."
                                }
                                listAdapter = ReportAdapter(this@ReportView, dataArrayList)
                                binding.listReview.setAdapter(listAdapter)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
            }
        }

    }
    fun writeToExcelFile(filepath: String) {
        val xlWb = HSSFWorkbook()
        val xlWs = xlWb.createSheet()

        for (i in 0..dataArrayList.size-1) {
            var cell = xlWs.createRow(i+1)
            cell.createCell(0).setCellValue(dataArrayList.get(i).name)
            cell.createCell(1).setCellValue(dataArrayList.get(i).price   +" грн.")
            if (dataArrayList.get(i).kind == OperationKind.Income ) {
                cell.createCell(2).setCellValue("Дохід")
            } else {
                cell.createCell(2).setCellValue("Витрата")
            }
        }
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

        val text = "Звіт був успішно завантажений під назвою: " + filepath
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(this@ReportView, text, duration)
        toast.show()
        binding.downloadReport.isClickable = false
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
    fun sortListByCriteria(
        list: List<ListReportData>,
        operationKind: OperationKind
    ): List<ListReportData> {
        return list.sortedWith(compareBy<ListReportData> { item ->
            item.price.toDouble()
        }.thenBy { item ->
            when (operationKind) {
                OperationKind.Expense -> if (item.kind == OperationKind.Expense) 0 else 1
                OperationKind.Income -> if (item.kind == OperationKind.Income) 0 else 1
            }
        })
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


    fun sortListAlphabetically(list: List<ListReportData>): List<ListReportData> {
        return list.sortedBy { it.name.lowercase() }
    }

}
