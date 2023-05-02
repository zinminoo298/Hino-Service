package com.example.myapplication.Adapter

import android.content.Context
import android.provider.ContactsContract.Data
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DataModel.GetDetailSKBModel
import com.example.myapplication.R

class SkbStatusListAdapter(private var Dataset:ArrayList<GetDetailSKBModel>, private val context: Context) : RecyclerView.Adapter<SkbStatusListAdapter.MyViewHolder>() {
    class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textViewDetail: TextView = view.findViewById(R.id.textview_detail)
        val textViewDate: TextView = view.findViewById(R.id.textview_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.skb_status_list_row, parent, false)
        return SkbStatusListAdapter.MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val textViewDetail = holder.textViewDetail
        val textViewDate = holder.textViewDate

        textViewDetail.text = Dataset[position].detail
        textViewDate.text = Dataset[position].date
    }

    override fun getItemCount() = Dataset.size

}