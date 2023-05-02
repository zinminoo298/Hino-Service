package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataModel.GetOrderDetailModel
import com.example.myapplication.DataModel.GetOrderModel
import com.example.myapplication.DataQuery.DeliveryQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Delivery : AppCompatActivity() {
    companion object{
        lateinit var textViewTotal:TextView
        lateinit var textVieDate:TextView
        lateinit var spinnerRound:Spinner
        lateinit var editTextCaseNo: EditText
        lateinit var buttonTransferCase: Button
        lateinit var buttonCloseCase: Button
        lateinit var orderDetailList:ArrayList<GetOrderDetailModel>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery)

        textVieDate = findViewById(R.id.textview_date)
        spinnerRound = findViewById(R.id.spinner_round)
        editTextCaseNo = findViewById(R.id.editText_case_no)
        buttonTransferCase = findViewById(R.id.button_transfer_case)
        buttonCloseCase = findViewById(R.id.button_close_case)
        orderDetailList = ArrayList<GetOrderDetailModel>()

        editTextCaseNo.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (editTextCaseNo.text.toString().isNotEmpty()) {
                    if(spinnerRound.selectedItem.toString() != ""){

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
                asyncCloseCase()
            } else {
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this,"กรุณาแสกน CaseNo",layoutInflater)
                editTextCaseNo.selectAll()
                editTextCaseNo.requestFocus()
            }
        }
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
                    progressDialogBuilder.cancel()

                }
            } else {
                deferred.await()

            }
        }
    }

    private fun asyncCloseCase() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            closeCase()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Delivery)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    //clear recycler view
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun caseNo(){
        var sCaseNo = spinnerRound.selectedItem.toString()
        var sRound = ""
        var caseStatus = DeliveryQuery().getCaseNoStatus(sCaseNo)
        when (caseStatus) {
            "C" -> {
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "CaseNo : $sCaseNo ปิดเรียบร้อยแล้ว", layoutInflater)
            }
            "" -> {
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "ไม่พบสถานะ CaseNo : $sCaseNo", layoutInflater)
            }
            else -> {
                Handler(Looper.getMainLooper()).post {
                    editTextCaseNo.selectAll()
                    editTextCaseNo.requestFocus()
                    listPackingCase(sCaseNo)
                }
            }
        }
    }

    private fun listPackingCase(sCaseNo:String){
        orderDetailList = DeliveryQuery().getOrderDetail(sCaseNo)
        var totalQty = 0
        for(i in 0 until orderDetailList.size){
            totalQty += orderDetailList[2].packQty!!
        }
    }

    private fun closeCase(){
        var sFlag = false
        var alphaMonth = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L")
        var caseMonth = 0
        var caseYear = 0
        var sCaseNo = ""
        var sDeliveryDate = ""
        var sRound = ""

        sCaseNo = editTextCaseNo.text.toString().trim()
        sRound = spinnerRound.selectedItem.toString()
        sDeliveryDate = textVieDate.text.toString().trim()
        caseMonth = Integer.parseInt(sDeliveryDate.split("/").toTypedArray()[1])
        caseYear = Integer.parseInt(sDeliveryDate.split("/").toTypedArray()[2])
        sDeliveryDate = "$caseMonth/${Integer.parseInt(sDeliveryDate.split("/").toTypedArray()[0])}/$caseYear"

        if(sCaseNo.substring(4,5) == alphaMonth[caseMonth-1]){
            if(orderDetailList.isNotEmpty()){
                //show Dialog
                if(alertDialog(sCaseNo)){
                    sFlag = DeliveryQuery().closeCase(sDeliveryDate, sRound, sCaseNo, Gvariable.userName!!.trim())
                    if(sFlag){
                        //play sound OK
                        Handler(Looper.getMainLooper()).post {
                            editTextCaseNo.text.clear()
                            editTextCaseNo.requestFocus()
                            orderDetailList.clear()
                            //clear recycler view
                        }
                    }
                }
            }
        }
        else{
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "CaseNo ไม่ตรงตาม Delivery Date", layoutInflater)
            Handler(Looper.getMainLooper()).post {
                editTextCaseNo.selectAll()
                editTextCaseNo.requestFocus()
                //clear recycler view
            }
        }
    }

    private fun alertDialog(caseNo:String) : Boolean{
        var clickState = false
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.alert_dialog   , null)
        builder.setView(view)
        builder.setTitle("Close Case?")
        val dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
        val buttonYes = view.findViewById<Button>(R.id.button_yes)
        val buttonNo = view.findViewById<Button>(R.id.button_no)
        val textView = view.findViewById<TextView>(R.id.txt_text)

        textView.text = "ปิด CaseNo: $caseNo?"

        buttonYes.setOnClickListener {
            clickState = true
            dialog.dismiss()
        }
        buttonNo.setOnClickListener {
            clickState = false
            dialog.dismiss()
        }
        return clickState
    }
}