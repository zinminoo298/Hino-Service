package com.example.myapplication.Adapter

import android.content.Context
import android.graphics.Color
import android.provider.ContactsContract.Data
import android.service.autofill.Dataset
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.CheckListCase
import com.example.myapplication.DataModel.GetDetailSKBModel
import com.example.myapplication.DataModel.GetPackingInfoListModel
import com.example.myapplication.R

class PackingInfoListAdapter(private var Dataset:ArrayList<GetPackingInfoListModel>, private val context: Context) : RecyclerView.Adapter<PackingInfoListAdapter.MyViewHolder>() {
    var newposition = -1
    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textViewNo: TextView = view.findViewById(R.id.textview_no)
        val textViewPartNo: TextView = view.findViewById(R.id.textview_part_no)
        val textViewQty:TextView = view.findViewById(R.id.textview_qty)
        val linearLayout:LinearLayout = view.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.packing_info_list_row, parent, false)
        return PackingInfoListAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val textViewNo = holder.textViewNo
        val textViewPartNo = holder.textViewPartNo
        val textViewQty = holder.textViewQty
        val linearLayout = holder.linearLayout

        textViewNo.text = Dataset[position].no
        textViewPartNo.text = Dataset[position].partNo
        textViewQty.text = Dataset[position].packingQty.toString()

        if(newposition == position){
            linearLayout.setBackgroundColor(Color.GREEN)
        }else{
            linearLayout.setBackgroundColor(Color.WHITE)
        }

        linearLayout.setOnClickListener {
            val oldposition = newposition
            newposition = position
            notifyItemChanged(newposition)
            notifyItemChanged(oldposition)
            CheckListCase().asyncRecyclerItemChange(context, Dataset[newposition].partNo!!, Dataset[newposition].packingQty!!)
        }
    }

    override fun getItemCount() = Dataset.size

}