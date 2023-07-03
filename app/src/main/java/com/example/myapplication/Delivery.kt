package com.example.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.DeliveryOrderDetailAdapter
import com.example.myapplication.DataModel.GetOrderDetailModel
import com.example.myapplication.DataQuery.DeliveryQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Delivery : AppCompatActivity() {
    companion object{
        lateinit var textViewTotal:TextView
        lateinit var textViewDate:TextView
        lateinit var spinnerRound:Spinner
        lateinit var editTextCaseNo: EditText
        lateinit var buttonTransferCase: Button
        lateinit var buttonCloseCase: Button
        lateinit var orderDetailList:ArrayList<GetOrderDetailModel>
        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager
        lateinit var cardView: CardView
        var roundList = ArrayList<String>()
        var date = ""
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        textViewDate = findViewById(R.id.textview_date)
        textViewTotal = findViewById(R.id.textview_total)
        spinnerRound = findViewById(R.id.spinner_round)
        editTextCaseNo = findViewById(R.id.editText_case_no)
        buttonTransferCase = findViewById(R.id.button_transfer_case)
        buttonCloseCase = findViewById(R.id.button_close_case)
        recyclerView = findViewById(R.id.recyclerview_delivery_normal)
        cardView = findViewById(R.id.cardView_back)
        orderDetailList = ArrayList<GetOrderDetailModel>()

        onLoad()

        editTextCaseNo.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (editTextCaseNo.text.toString().isNotEmpty()) {
                    if(spinnerRound.selectedItem.toString() != ""){
                        asyncCaseNo()
                    }
                    else{
                        Gvariable().alarm(this)
                        Gvariable().messageAlertDialog(this,"กรุณาเลือกรอบการส่ง",layoutInflater) //select delivery round
                        editTextCaseNo.selectAll()
                        editTextCaseNo.requestFocus()
                    }
                } else {
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this,"กรุณาแสกน CaseNo",layoutInflater) //scan case no.
                    editTextCaseNo.selectAll()
                    editTextCaseNo.requestFocus()
                }
            }
            false
        })


        buttonTransferCase.setOnClickListener {
            val intent = Intent(this,TransferCaseNo::class.java)
            startActivity(intent)
        }

        buttonCloseCase.setOnClickListener {
            if (editTextCaseNo.text.toString().isNotEmpty()) {
                closeCase()
            } else {
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this,"กรุณาแสกน CaseNo",layoutInflater)
                editTextCaseNo.selectAll()
                editTextCaseNo.requestFocus()
            }
        }

        spinnerRound.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                editTextCaseNo.requestFocus()
                editTextCaseNo.selectAll()
            }
        }

        textViewDate.setOnClickListener {
            datePicker()
        }

        cardView.setOnClickListener {
            finish()
            super.onBackPressed()
        }

    }

    private fun onLoad(){
        setSpinnerRound()
        textViewTotal.text = ""
        textViewDate.text = date
    }

    private fun setSpinnerRound(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roundList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRound.adapter = arrayAdapter
        spinnerRound.setSelection(0)
    }

    private fun asyncCaseNo() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            caseNo()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Delivery)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    loadRecyclerView()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun asyncCloseCase(sDeliveryDate:String, sRound:String, sCaseNo: String, user:String) {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            DeliveryQuery().closeCase(sDeliveryDate, Integer.parseInt(sRound), sCaseNo, user)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Delivery)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    //clear recycler view
                    editTextCaseNo.text.clear()
                    editTextCaseNo.requestFocus()
                    orderDetailList.clear()
                    loadRecyclerView()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun caseNo(){
        val sCaseNo = editTextCaseNo.text.toString().trim()
        val caseStatus = DeliveryQuery().getCaseNoStatus(sCaseNo)
        when (caseStatus) {
            "C" -> {
                Handler(Looper.getMainLooper()).post {
                    editTextCaseNo.selectAll()
                    editTextCaseNo.requestFocus()
                }
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "CaseNo : $sCaseNo ปิดเรียบร้อยแล้ว", layoutInflater)
            }
            "" -> {
                Handler(Looper.getMainLooper()).post {
                    editTextCaseNo.selectAll()
                    editTextCaseNo.requestFocus()
                }
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "ไม่พบสถานะ CaseNo : $sCaseNo", layoutInflater)
            }
            else -> {
                listPackingCase(sCaseNo)
                Handler(Looper.getMainLooper()).post {
                    editTextCaseNo.selectAll()
                    editTextCaseNo.requestFocus()
                }
            }
        }
    }

    private fun listPackingCase(sCaseNo:String){
        orderDetailList = DeliveryQuery().getOrderDetail(sCaseNo)
        if(orderDetailList.isNotEmpty()){
            var totalQty = 0
            for(i in 0 until orderDetailList.size){
                totalQty += orderDetailList[i].packQty!!
            }
            Handler(Looper.getMainLooper()).post {
                textViewTotal.text  = " Total : $totalQty K/B"
            }
        }
        else{
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "ไม่พบข้อมูล CaseNo : $sCaseNo", layoutInflater)
            Handler(Looper.getMainLooper()).post {
                editTextCaseNo.selectAll()
                editTextCaseNo.requestFocus()
            }
        }
    }

    private fun closeCase(){
        var alphaMonth = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
        var caseMonth = 0
        var caseYear = 0
        var sCaseNo = ""
        var sDeliveryDate = ""
        var sRound = ""

        sCaseNo = editTextCaseNo.text.toString().trim()
        sRound = spinnerRound.selectedItem.toString()
        sDeliveryDate = textViewDate.text.toString().trim()
        caseMonth = Integer.parseInt(sDeliveryDate.split("-").toTypedArray()[1])
        caseYear = Integer.parseInt(sDeliveryDate.split("-").toTypedArray()[2])
        sDeliveryDate = "$caseMonth-${Integer.parseInt(sDeliveryDate.split("-").toTypedArray()[0])}-$caseYear"

        if(sCaseNo.substring(3,4) == alphaMonth[caseMonth-1]){
            if(orderDetailList.isNotEmpty()){
                //show Dialog
                alertDialog(sCaseNo, sDeliveryDate, sRound, Gvariable.userName!!.trim())
            }
        }
        else{
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "CaseNo ไม่ตรงตาม Delivery Date", layoutInflater)
                editTextCaseNo.selectAll()
                editTextCaseNo.requestFocus()
        }
    }

    private fun alertDialog(caseNo:String, deliveryDate:String, round:String, user:String){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.alert_dialog   , null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        val buttonYes = view.findViewById<Button>(R.id.button_yes)
        val buttonNo = view.findViewById<Button>(R.id.button_no)
        val textView = view.findViewById<TextView>(R.id.txt_text)

        textView.text = "ปิด CaseNo: $caseNo?"

        buttonYes.setOnClickListener {
            dialog.dismiss()
            asyncCloseCase(deliveryDate, round, caseNo, user)
        }
        buttonNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun datePicker(){
        val arrayList = date.split("-").toTypedArray()
        val year = Integer.parseInt(arrayList[2])
        val month = Integer.parseInt(arrayList[1]) - 1
        val day = Integer.parseInt(arrayList[0])
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val mth = monthOfYear + 1

                date = "${String.format("%02d",dayOfMonth)}-${String.format("%02d",mth)}-$year"
                textViewDate.text = date
            },
            year,
            month,
            day
        )
        dpd.show()
    }

    private fun loadRecyclerView(){
        viewManager = LinearLayoutManager(this)
        viewAdapter = DeliveryOrderDetailAdapter(orderDetailList,this)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}