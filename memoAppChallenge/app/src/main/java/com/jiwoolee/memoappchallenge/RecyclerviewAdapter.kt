package com.jiwoolee.memoappchallenge

import android.content.Context
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jiwoolee.memoappchallenge.room.Memo
import kotlinx.android.synthetic.main.item_cat.view.*

class RecyclerviewAdapter(val context: Context, val memos: List<Memo>) : RecyclerView.Adapter<RecyclerviewAdapter.Holder>() {
    private lateinit var itemTitle: TextView
    private lateinit var itemContent: TextView
    private lateinit var itemThumnail: ImageView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cat, parent, false)
        itemTitle = view.tv_item_title
        itemContent = view.tv_item_content
        itemThumnail = view.iv_item_thumbnail
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return memos.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(memos[position])
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(memo: Memo) {
            itemTitle.text = memo.memoTitle
            itemContent.text = memo.memoContent
            if (memo.memoImages?.get(0) != "") {
                val array: ByteArray = Base64.decode(memo.memoImages?.get(0), Base64.DEFAULT)
                Glide.with(itemView.context)
                    .load(array)
                    .fitCenter()
                    .into(itemThumnail)
            }
        }
    }
}