package com.example.myapplication.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DataModel.GetPackingInfoListModel
import com.example.myapplication.R

class CaseListAdapter(private var Dataset:ArrayList<GetPackingInfoListModel>, private val context: Context) : RecyclerView.Adapter<CaseListAdapter.MyViewHolder>() {
    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textViewNo: TextView = view.findViewById(R.id.textview_no)
        val textViewPartNo: TextView = view.findViewById(R.id.textview_part_no)
        val textViewQty: TextView = view.findViewById(R.id.textview_qty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.list_case_row, parent, false)
        return CaseListAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val textViewNo = holder.textViewNo
        val textViewPartNo = holder.textViewPartNo
        val textViewQty = holder.textViewQty

        textViewNo.text = (position + 1).toString()
        textViewPartNo.text = Dataset[position].partNo
        textViewQty.text = Dataset[position].packingQty.toString()
    }

    override fun getItemCount() = Dataset.size

}