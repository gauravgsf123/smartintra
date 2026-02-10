package com.swayze.smartintra.ui.vehicle_load_unload

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swayze.smartintra.R
import com.swayze.smartintra.ui.vehicle_load_unload.database.VehicleListData


class VechileLoadAdapter : RecyclerView.Adapter<VechileLoadAdapter.MyViewHolder>() {
    var itemClick: ((VehicleListData) -> Unit)? = null
    private var stockList = listOf<VehicleListData>()
    private lateinit var context:Context
    fun setItems(stockList: List<VehicleListData>,context: Context) {
        this.stockList = stockList
        this.context=context
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_vechicle_load_unload_list, parent, false)
        ).apply {
            itemClick = { i ->
                this@VechileLoadAdapter.itemClick?.invoke(i)
            }
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val viewHolder = holder
        viewHolder.bindView(stockList.get(position),position,context)
    }

    override fun getItemCount(): Int {
        return stockList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemClick: ((VehicleListData) -> Unit)? = null
        private lateinit var slNo: TextView
        private lateinit var scanCode: TextView
        private lateinit var cNote: TextView
        private lateinit var linearLayout:LinearLayout
        fun bindView(stock: VehicleListData, i: Int, context: Context) {
            slNo = itemView.findViewById(R.id.sl_no)
            scanCode = itemView.findViewById(R.id.scan_code)
            cNote = itemView.findViewById(R.id.c_note)
            linearLayout = itemView.findViewById(R.id.linearLayout)
            slNo.text = (i+1).toString()
            cNote.text = stock.CNoteNo
            scanCode.text = stock.BarCodeNo//.toString()

            if(stock.isScan == true){
                linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.green_light))
            }else{
                linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.red_light))
            }

            itemView.setOnClickListener{
                itemClick?.invoke(stock)
            }
        }
    }
}