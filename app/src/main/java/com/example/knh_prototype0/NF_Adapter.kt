package com.example.knh_prototype0

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.knh_prototype0.databinding.NfRowBinding

//음식 영양정보 RecyclerView의 어댑터
class NF_Adapter(val items:ArrayList<NutritionFacts>) : RecyclerView.Adapter<NF_Adapter.MyViewHolder>()
{
    var itemClickListener:OnItemClickListener? = null
    var nowHolder : MyViewHolder? = null
    var selectedFid = -1;

    interface OnItemClickListener{
        fun OnItemClick(fomerHolder : MyViewHolder?, holder:MyViewHolder, view:View, selectedFid : Int, data:NutritionFacts, position:Int)
    }

    inner class MyViewHolder(val binding: NfRowBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init {
            binding.linearLayout.setOnClickListener {
                itemClickListener?.OnItemClick(nowHolder, this, it, selectedFid, items[adapterPosition], adapterPosition)
            }
        }

        fun setNowHolder(isSelected:Boolean)
        {
            if(isSelected)
            {
                //selectedFid = items[adapterPosition].fid
                this.binding.linearLayout.setBackgroundColor(Color.MAGENTA)
                nowHolder = this
            }
            else
            {
                this.binding.linearLayout.setBackgroundColor(Color.WHITE)
                nowHolder = this
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder
    {
        val view = NfRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int)
    {
        val item = items[position]
        holder.binding.fnameText.text = item.fname
        holder.binding.nutritionFacts.text = item.fname + " (" + item.pergram + "g 당) / 탄수화물 : " + item.carb + " / 단백질 : " + item.protein + " / 지방 : " + item.fat + " / 칼로리 : " + item.kcal

        if(selectedFid == item.fid)
        {
            holder.setNowHolder(true)
        }
        else
        {
            holder.setNowHolder(false)
        }
    }
}