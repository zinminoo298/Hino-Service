package com.example.myapplication.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DataModel.GetOrderDetailModel
import com.example.myapplication.R

class DeliveryOrderDetailAdapter(private var Dataset:ArrayList<GetOrderDetailModel>, private val context: Context) : RecyclerView.Adapter<DeliveryOrderDetailAdapter.MyViewHolder>() {
    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textViewNo: TextView = view.findViewById(R.id.textview_no)
        val textViewPartNo: TextView = view.findViewById(R.id.textview_part_no)
        val textViewOrderNo: TextView = view.findViewById(R.id.textview_order_no)
        val textViewQty: TextView = view.findViewById(R.id.textview_qty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.delivery_normal_detail_row, parent, false)
        return DeliveryOrderDetailAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val textViewNo = holder.textViewNo
        val textViewPartNo = holder.textViewPartNo
        val textViewOrderNo = holder.textViewOrderNo
        val textViewQty = holder.textViewQty

        textViewNo.text = (position + 1).toString()
        textViewPartNo.text = Dataset[position].partNo
        textViewOrderNo.text = Dataset[position].orderNo
        textViewQty.text = Dataset[position].packQty.toString()
    }

    override fun getItemCount() = Dataset.size

}