package com.example.myapplication.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.CheckListCase
import com.example.myapplication.DataModel.GetPackingInfoListModel
import com.example.myapplication.DataModel.GetPackingListModel
import com.example.myapplication.R

class PackingListAdapter(private var Dataset:ArrayList<GetPackingListModel>, private val context: Context) : RecyclerView.Adapter<PackingListAdapter.MyViewHolder>() {
    companion object{
        var currentPosition = -1
    }
    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textViewNo: TextView = view.findViewById(R.id.textview_no)
        val textViewCaseNo: TextView = view.findViewById(R.id.textview_case_no)
        val linearLayout: LinearLayout = view.findViewById(R.id.layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.packing_list_row, parent, false)
        return PackingListAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val textViewNo = holder.textViewNo
        val textViewCaseNo = holder.textViewCaseNo
        val linearLayout = holder.linearLayout

        textViewNo.text = Dataset[position].no
        textViewCaseNo.text = Dataset[position].caseNo

        if(currentPosition == position){
            linearLayout.setBackgroundColor(Color.RED)
        }else{
            linearLayout.setBackgroundColor(Color.WHITE)
        }

        linearLayout.setOnClickListener {
            val oldposition = currentPosition
            currentPosition = position
            notifyItemChanged(currentPosition)
            notifyItemChanged(oldposition)
        }

    }

    override fun getItemCount() = Dataset.size

}