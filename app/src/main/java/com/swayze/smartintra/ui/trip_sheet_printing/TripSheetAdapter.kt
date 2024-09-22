package com.swayze.smartintra.ui.trip_sheet_printing

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swayze.smartintra.R

class TripSheetAdapter: RecyclerView.Adapter<TripSheetAdapter.MyViewHolder>() {
    var itemClick: ((TripSheetResponse) -> Unit)? = null
    private var stockList = listOf<TripSheetResponse>()
    private lateinit var context : Context
    fun setItems(stockList: List<TripSheetResponse>,context: Context) {
        this.context = context
        this.stockList = stockList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_trip_sheet_list, parent, false)
        ).apply {
            itemClick = { i ->
                this@TripSheetAdapter.itemClick?.invoke(i)
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
        var itemClick: ((TripSheetResponse) -> Unit)? = null
        private lateinit var cNoteNumber: TextView
        private lateinit var cartonNo: TextView
        private lateinit var constLayout: ConstraintLayout
        @SuppressLint("ResourceAsColor")
        fun bindView(model: TripSheetResponse, i: Int, context: Context) {
            cNoteNumber = itemView.findViewById(R.id.tv_c_note_number)
            cartonNo = itemView.findViewById(R.id.tv_carton_no)
            constLayout = itemView.findViewById(R.id.const_top)
            cNoteNumber.text = model.CNoteNo
            cartonNo.text = model.BarCodeNo
            if(model.printDone == true){
                constLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.green_light))
            }else{
                constLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.red_light))
            }

            itemView.setOnClickListener{
                itemClick?.invoke(model)
            }
        }
    }
}