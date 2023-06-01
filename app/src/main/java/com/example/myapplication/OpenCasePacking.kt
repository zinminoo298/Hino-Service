package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.PackingListAdapter
import com.example.myapplication.DataModel.GetPackingListModel
import com.example.myapplication.DataQuery.GetTimeQuery
import com.example.myapplication.DataQuery.PackingQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class OpenCasePacking : AppCompatActivity() {
    companion object{
        lateinit var editTextCaseNo: EditText
        lateinit var textViewTotal: TextView
        lateinit var buttonDelete: Button
        lateinit var buttonNext: Button
        lateinit var cardViewBack : CardView
        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager
        var caseList = ArrayList<GetPackingListModel>()
        var totalCase = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_case_packing)

        editTextCaseNo = findViewById(R.id.editText_case_no)
        textViewTotal = findViewById(R.id.textview_total)
        buttonDelete = findViewById(R.id.button_delete)
        buttonNext = findViewById(R.id.button_next)
        cardViewBack = findViewById(R.id.cardView_back)
        recyclerView = findViewById(R.id.recyclerview_list_case)

        loadRecyclerView()
        textViewTotal.text = "Total Case : $totalCase"
        editTextCaseNo.requestFocus()

        buttonDelete.setOnClickListener {
            if(PackingListAdapter.currentPosition != -1){
                alertDialog("ลบ CaseNo?")
            }
            else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "เลือก CaseNo ที่ต้องการลบ", layoutInflater)
            }
        }

        buttonNext.setOnClickListener {
            asyncNext()
        }

        editTextCaseNo.setOnKeyListener(View.OnKeyListener { _, _, event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(editTextCaseNo.text.toString().isNotEmpty()){
                    asyncScanCaseNo()
                }else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "กรุณาแสกน CaseNo", layoutInflater)
                }
            }
            false
        })

        cardViewBack.setOnClickListener{
            finish()
            super.onBackPressed()
        }
    }

    private fun loadRecyclerView(){
        viewManager = LinearLayoutManager(this)
        viewAdapter = PackingListAdapter(caseList, this)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun asyncNext() {
        var cntCaseNo = 0
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            cntCaseNo = PackingQuery().getCountCaseByUser(Gvariable.userName.toString())
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@OpenCasePacking)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    progressDialogBuilder.cancel()
                    if(cntCaseNo > 0){
                        val intent = Intent(this@OpenCasePacking, Packing::class.java)
                        startActivity(intent)
                    }
                    else{
                        Gvariable().alarm(this@OpenCasePacking)
                        Gvariable().messageAlertDialog(this@OpenCasePacking, "กรุณาระบุ CaseNo", layoutInflater)
                    }

                }
            } else {
                deferred.await()

            }
        }
    }

    private fun asyncDelete(){
        val caseNo = caseList[PackingListAdapter.currentPosition].caseNo!!
        var sFlag = false
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            sFlag = PackingQuery().deleteCaseNo(caseNo, Gvariable.userName.toString())
            caseList = PackingQuery().getCaseNoByUser(Gvariable.userName.toString())
            totalCase = PackingQuery().getCountCaseByUser(Gvariable.userName.toString())
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@OpenCasePacking)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    if(sFlag ){
                        loadRecyclerView()
                        PackingListAdapter.currentPosition = -1
                        editTextCaseNo.setText("")
                        editTextCaseNo.requestFocus()
                    }
                    else{
                        Gvariable().alarm(this@OpenCasePacking)
                        Gvariable().messageAlertDialog(this@OpenCasePacking, "ไม่สามารถถอดออกได้ CaseNo!", layoutInflater)
                        editTextCaseNo.setText("")
                        editTextCaseNo.requestFocus()
                    }
                    textViewTotal.text = "Total Case : $totalCase"
                    progressDialogBuilder.cancel()

                }
            } else {
                deferred.await()

            }
        }
    }

    private fun asyncScanCaseNo() {
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            scanCaseNo()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@OpenCasePacking)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    editTextCaseNo.requestFocus()
                    editTextCaseNo.text.clear()
                    loadRecyclerView()
                    textViewTotal.text = "Total Case : $totalCase"
                    progressDialogBuilder.cancel()
                }
            } else {
                deferred.await()

            }
        }
    }

    private fun scanCaseNo(){
        val caseNo = editTextCaseNo.text.toString().trim()
        if(PackingQuery().checkCaseWithMaster(caseNo) > 0){
            val countCaseNo = PackingQuery().getCountCaseNo("", caseNo)
            if(countCaseNo <= 0){
                val caseStatus = PackingQuery().getCaseMatchStatus("", caseNo)
                if(caseStatus == 1){
                    PackingQuery().updateReuseCaseFlag(caseNo, Gvariable.userName.toString())
                }
                PackingQuery().insertCaseNoMatch("", caseNo, Gvariable.userName.toString(), 0)
                caseList = PackingQuery().getCaseNoByUser(Gvariable.userName.toString())
                totalCase = PackingQuery().getCountCaseByUser(Gvariable.userName.toString())
            }
            else{
                Gvariable().alarm(this)
                Gvariable().messageAlertDialog(this, "CaseNo ถูกจองเรียบร้อยแล้ว", layoutInflater)
            }
        }else{
            Gvariable().alarm(this)
            Gvariable().messageAlertDialog(this, "ไม่พบ CaseNo นี้ในระบบ", layoutInflater)
        }
    }

    private fun asyncPacking(){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            Packing.date = GetTimeQuery().timeServer().split("|").toTypedArray()[1]
            Packing().listOrderNo()
            Packing().listCaseNo()
            Packing.countCaseNo = PackingQuery().getCaseNoList(Gvariable.userName.toString())
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@OpenCasePacking)

                try {
                    progressDialogBuilder.show()
                    deferred.await()
                } finally {
                    progressDialogBuilder.cancel()
                    val intent = Intent(this@OpenCasePacking, Packing::class.java)
                    startActivity(intent)
                }
            } else {
                deferred.await()
            }
        }
    }

    private fun alertDialog(text:String){
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

        textView.text = text

        buttonYes.setOnClickListener {
            dialog.dismiss()
            asyncDelete()
        }

        buttonNo.setOnClickListener {
            dialog.dismiss()
        }
    }
}