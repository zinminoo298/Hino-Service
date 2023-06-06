package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.DeliveryQuery
import com.example.myapplication.DataQuery.GetTimeQuery
import com.example.myapplication.DataQuery.PackingQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class Packing : AppCompatActivity() {
    companion object{
        lateinit var cardViewBack: CardView
        lateinit var spinnerCaseNo: Spinner
        lateinit var editTextKB: EditText
        lateinit var editTextQty: EditText
        lateinit var editTextSticker: EditText
        lateinit var editTextCheckOrder: EditText
        lateinit var textViewDate: TextView
        lateinit var spinnerOrderNo: Spinner
        lateinit var textViewPartNo: TextView
        lateinit var textViewBarcodeType: TextView
        var countCaseNo = 0
        var date = ""
        var currentDate =""
        var PartNo = ""
        var orderNoList = ArrayList<String>()
        var caseNoList = ArrayList<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_packing)

        cardViewBack = findViewById(R.id.cardView_back)
        spinnerCaseNo = findViewById(R.id.spinner_case_no)
        spinnerOrderNo = findViewById(R.id.spinner_order_no)
        editTextKB = findViewById(R.id.editText_kb)
        editTextQty = findViewById(R.id.editText_qty)
        editTextSticker = findViewById(R.id.editText_sticker)
        editTextCheckOrder = findViewById(R.id.editText_check_order)
        textViewDate = findViewById(R.id.textview_date)
        textViewPartNo = findViewById(R.id.textView_part_no)

        asyncOnLoad()

        editTextKB.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (editTextKB.text.toString().isNotEmpty()) {
                    PartNo = ""
                    //check serial Barcode
                    if (checkSerialBarcode()) {
                        var barcodeType = textViewBarcodeType.text.toString()
                        when(barcodeType){
                            "2D" -> {

                            }

                            "SKB" -> {

                            }

                            "PHT" -> {

                            }
                        }
                    } else {
                        Gvariable().messageAlertDialog(
                            this,
                            "กรุณาแสกนเอกสาร Kanban",
                            layoutInflater
                        )
                        Gvariable().alarm(this)
                        editTextKB.selectAll()
                        editTextKB.requestFocus()
                    }
                } else {
                    Gvariable().messageAlertDialog(
                        this,
                        "กรุณาแสกนเอกสาร Serial Kanban เท่านั้น",
                        layoutInflater
                    )
                    Gvariable().alarm(this)
                    editTextKB.requestFocus()
                }
            }
            false
        })
    }

    private fun asyncDelete(){

        val deferred = lifecycleScope.async(Dispatchers.IO) {

        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
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

    private fun checkSerialBarcode() : Boolean {
        editTextKB.setText(editTextKB.text.toString().uppercase().trim())
        var inputBarcode = editTextKB.text.toString().split("|").toTypedArray()
        var partNo = ""
        var serialKanban = ""
        var docSerial = ""

        if(inputBarcode.size == 18){
            var pageKanban = inputBarcode[14].split("/").toTypedArray()
            serialKanban = String.format("%04d", Integer.parseInt(pageKanban[0]))
            partNo = inputBarcode[0].substring(3,8)+"-"+inputBarcode[0].substring(8,13)
            docSerial = partNo +"-"+inputBarcode[11]+"-"+serialKanban
            PartNo = docSerial
            textViewBarcodeType.text = "2D"
        }
        else if (editTextKB.text.toString().length == 15){
            PartNo = editTextKB.text.toString()
            textViewBarcodeType.text = "SKB"
        }

        else if(editTextKB.text.toString().length == 11){
            PartNo = editTextKB.text.toString()
            textViewBarcodeType.text = "PHT"
            docSerial = PartNo
        }
        else{
            return false
        }
        return true
    }

    private fun asyncOnLoad(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            val getDate = GetTimeQuery().timeServer()
            val date = getDate.split("|").toTypedArray()[0]
            val date1 = getDate.split("|").toTypedArray()[1]
            currentDate = date
            listOrderNo(date1)
            listCaseNo()

        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@Packing)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    textViewDate.text = currentDate
                    loadSpinnerCaseNo()
                    loadSpinnerOrderNo()
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    fun listOrderNo(orderDate:String){
        orderNoList.clear()
        val showOrderList = DeliveryQuery().showOrder( orderDate, "", "")
        for(i in 0 until showOrderList.size){
            orderNoList.add(showOrderList[i].substringBefore("|"))
        }
    }

    fun listCaseNo(){
        caseNoList.clear()
        caseNoList = PackingQuery().getListCaseNoByUser(Gvariable.userName.toString())
    }

    private fun loadSpinnerOrderNo(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, orderNoList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOrderNo.adapter = arrayAdapter
    }

    private fun loadSpinnerCaseNo(){
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, caseNoList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCaseNo.adapter = arrayAdapter
    }


}