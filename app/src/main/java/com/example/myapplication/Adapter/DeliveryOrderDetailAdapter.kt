package com.example.myapplication.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DataModel.GetOrderDetailModel
import com.example.myapplication.R

class DeliveryOrderDetailAdapter(private var Dataset:ArrayList<GetOrderDetailModel>, private val context: Context) : RecyclerView.Adapter<DeliveryOrderDetailAdapter.MyViewHolder>() {
    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.delivery_normal_detail_row, parent, false)
        return DeliveryOrderDetailAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    }

    override fun getItemCount() = Dataset.size

}