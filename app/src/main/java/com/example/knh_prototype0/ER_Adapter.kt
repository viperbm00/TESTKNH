package com.example.knh_prototype0

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.knh_prototype0.databinding.NfrRowBinding

//운동정보 기록 RecyclerView에 대한 어댑터.
class ER_Adapter(val items:ArrayList<ExerciseRecord>) : RecyclerView.Adapter<ER_Adapter.MyViewHolder>()
{
    var itemClickListener: ER_Adapter.OnItemClickListener? = null

    interface OnItemClickListener{
        fun OnItemClick(holder: ER_Adapter.MyViewHolder, view: View, data:ExerciseRecord, position:Int)
    }

    inner class MyViewHolder(val binding: NfrRowBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init {
            binding.nutritionFactsRecord.setOnClickListener {
                itemClickListener?.OnItemClick(this, it, items[adapterPosition], adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
    {
        val view = NfrRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int)
    {
        val item = items[position]
        holder.binding.nutritionFactsRecord.text =  convertDateFormat(item.recordtime) + " / " + item.exercise.ename + " / " + item.etime.toString() + " (분) / " + item.totalKcal.toString() + " (kcal)"
    }

    fun removeItem(pos:Int)
    {
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    //시간 정보를 간략하게 바꿔줍니다.
    fun convertDateFormat(date:String) : String
    {
        var result = date.substring(8, 12)
        result = result.substring(0, 2) + "시 " + result.substring(2, 4) + "분"

        return result
    }
}
