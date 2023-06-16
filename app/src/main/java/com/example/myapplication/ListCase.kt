package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.CaseListAdapter
import com.example.myapplication.DataModel.GetPackingInfoListModel
import com.example.myapplication.DataQuery.PackingQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ListCase : AppCompatActivity() {
    companion object{
        lateinit var cardView: CardView
        lateinit var editTextCaseNo: EditText
        lateinit var textViewTotal: TextView
        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager
        var caseList = ArrayList<GetPackingInfoListModel>()
        var total = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_case)

        cardView = findViewById(R.id.cardView_back)
        editTextCaseNo = findViewById(R.id.editText_case)
        textViewTotal = findViewById(R.id.textview_total)
        recyclerView = findViewById(R.id.recyclerview)

        loadRecyclerView()
        textViewTotal.text = total.toString()

        editTextCaseNo.setOnKeyListener(View.OnKeyListener{_,_,event ->
            if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(editTextCaseNo.text.toString().trim().isNotEmpty()){
                    asyncSearchCaseNo(editTextCaseNo.text.toString().trim())
                }else{
                    Gvariable().alarm(this)
                    Gvariable().messageAlertDialog(this, "ไม่พบ CaseNo นี้ในระบบ", layoutInflater)
                    editTextCaseNo.nextFocusDownId = editTextCaseNo.id
                    editTextCaseNo.text.clear()
                }
            }
            false
        })

        cardView.setOnClickListener {
            finish()
            super.onBackPressed()
        }

    }

    private fun asyncSearchCaseNo(sCaseNo:String){
        val deferred = lifecycleScope.async(Dispatchers.IO) {
            caseList.clear()
            caseList = PackingQuery().getListPackingInfoByCaseNo(sCaseNo)
            total = 0
            total = PackingQuery().getTotalPackQtyByCaseNo(sCaseNo)
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (deferred.isActive) {
                val progressDialogBuilder = Gvariable().createProgressDialog(this@ListCase)
                try {
                    progressDialogBuilder.show()
                    deferred.await()

                } finally {
                    loadRecyclerView()
                    textViewTotal.text = total.toString()
                    editTextCaseNo.selectAll()
                    editTextCaseNo.nextFocusDownId = editTextCaseNo.id
                    progressDialogBuilder.cancel()
                }
            } else {
                loadRecyclerView()
                textViewTotal.text = total.toString()
                editTextCaseNo.selectAll()
                editTextCaseNo.nextFocusDownId = editTextCaseNo.id
                deferred.await()

            }
        }
    }

    private fun loadRecyclerView(){
        viewManager = LinearLayoutManager(this)
        viewAdapter = CaseListAdapter(caseList, this)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}