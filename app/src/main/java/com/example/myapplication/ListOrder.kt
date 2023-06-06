package com.example.myapplication

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Adapter.OrderListAdapter
import com.example.myapplication.DataModel.OrderListModel

class ListOrder : AppCompatActivity() {
    companion object{
        lateinit var recyclerView: RecyclerView
        lateinit var viewAdapter: RecyclerView.Adapter<*>
        lateinit var viewManager: RecyclerView.LayoutManager
        var orderList = ArrayList<OrderListModel>()
    }
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_order)

        recyclerView = findViewById(R.id.recyclerview_order)
//        orderList = ArrayList<OrderListModel>()

        loadRecyclerView()
    }

    private fun loadRecyclerView(){
        viewManager = LinearLayoutManager(this)
        viewAdapter = OrderListAdapter(orderList, this)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }
}