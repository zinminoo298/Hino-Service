package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.DataQuery.DeliveryQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TransferCaseNo : AppCompatActivity() {
    companion object{
        lateinit var buttonConfirm:Button
        lateinit var buttonCancel: Button
        lateinit var editTextTransferFrom: EditText
        lateinit var editTextTransferTo: EditText
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer_case_no)

        buttonConfirm = findViewById(R.id.button_confirm)
        buttonCancel = findViewById(R.id.button_cancel)
        editTextTransferFrom = findViewById(R.id.editText_o_case)
        editTextTransferTo = findViewById(R.id.editText_n_case)

        buttonConfirm.setOnClickListener {
            if(editTextTransferFrom.text.toString().isEmpty() || editTextTransferTo.text.toString().isEmpty()){
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "กรุณาระบุ CaseNo", layoutInflater)
                if(editTextTransferFrom.text.toString().isEmpty()){
                    editTextTransferFrom.requestFocus()
                }

                if(editTextTransferTo.text.toString().isEmpty()){
                    editTextTransferTo.requestFocus()
                }
            }
            else{
                alertDialog()
            }
        }

        buttonCancel.setOnClickListener {
            super.onBackPressed()
            this.finish()
        }
    }

    private fun alertDialog(){
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

        textView.text = "ต้องการโอน CaseNo?"

        buttonYes.setOnClickListener {
            dialog.dismiss()
            asyncConfirm()
        }
        buttonNo.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun asyncConfirm() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            confirm()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@TransferCaseNo)
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

    private fun confirm(){
        val sOCaseNo = editTextTransferFrom.text.toString()
        val sNCaseNo = editTextTransferTo.text.toString()
        if(DeliveryQuery().getCaseNoStatus(sOCaseNo) != "C"){
            if(sOCaseNo.substring(0,4) == sNCaseNo.substring(0,4)){
                if(DeliveryQuery().transferCaseNo(sOCaseNo, sNCaseNo)){
                    Gvariable().messageOkDialog(this, "โอนข้อมูลเรียบร้อย", layoutInflater)
                }
                else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "การถ่ายโอนล้มเหลว!", layoutInflater)
                }
            }
            else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "ประเภท Order ไม่ตรงกัน!", layoutInflater)
            }
        }else{
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "CaseNo ทำการ Delivery เรียบร้อยแล้ว", layoutInflater)
        }
    }
}