package com.jiwoolee.memoappchallenge.adapter

import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jiwoolee.memoappchallenge.R
import com.jiwoolee.memoappchallenge.databinding.ItemMemoBinding
import com.jiwoolee.memoappchallenge.room.Memo

class RecyclerviewAdapter(val memos: ArrayList<Memo>, val listener: OnItemClick) : RecyclerView.Adapter<RecyclerviewAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val view = DataBindingUtil.inflate<ItemMemoBinding>(LayoutInflater.from(parent.context), R.layout.item_memo, parent, false)
        return CustomViewHolder(view)
    }

    class CustomViewHolder(var binding: ItemMemoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(memo: Memo) {
            binding.apply {
                memoItem = memo
            }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val memo = memos[position]
        holder.apply {
            bind(memo)
            itemView.tag = memo
        }

        holder.itemView.setOnClickListener {
            listener.onClick(memos[position])
        }
    }

    override fun getItemCount(): Int {
        return memos.size
    }
}

@BindingAdapter("setImage")
fun bindingImageFromRes(view: ImageView, imageString: String?) {
    val image: ByteArray = Base64.decode(imageString, Base64.DEFAULT)
    Glide.with(view.context)
        .load(image)
        .fitCenter()
        .into(view)
}
