package com.example.myapplication.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DataModel.GetOrderDetailModel
import com.example.myapplication.DataModel.OrderListModel
import com.example.myapplication.R

class OrderListAdapter(private var Dataset:ArrayList<OrderListModel>, private val context: Context) : RecyclerView.Adapter<OrderListAdapter.MyViewHolder>() {
    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textViewNo: TextView = view.findViewById(R.id.textview_no)
        val textViewPartNo: TextView = view.findViewById(R.id.textview_part_no)
        val textViewQty: TextView = view.findViewById(R.id.textview_qty)
        val textViewR:TextView = view.findViewById(R.id.textview_r)
        val textViewP:TextView = view.findViewById(R.id.textview_p)
        val textViewD:TextView = view.findViewById(R.id.textview_d)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.list_order_row, parent, false)
        return OrderListAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val textViewNo = holder.textViewNo
        val textViewPartNo = holder.textViewPartNo
        val textViewQty = holder.textViewQty
        val textViewR = holder.textViewR
        val textViewP = holder.textViewP
        val textViewD = holder.textViewD

        textViewNo.text = (position + 1).toString()
        textViewPartNo.text = Dataset[position].partNo
        textViewQty.text = Dataset[position].qty
        textViewR.text = Dataset[position].r
        textViewP.text = Dataset[position].p
        textViewD.text = Dataset[position].d
    }

    override fun getItemCount() = Dataset.size

}