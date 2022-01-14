package com.art.fartapp.ui.farters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.art.fartapp.databinding.ItemFarterBinding
import com.art.fartapp.db.Farter

class FartersAdapter(private val listener: FartersAdapter.OnItemClickListener) :
    ListAdapter<Farter, FartersAdapter.FartersViewHolder>(FarterDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FartersViewHolder {
        val binding = ItemFarterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FartersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FartersViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class FartersViewHolder(private val binding: ItemFarterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val farter = getItem(position)
                        listener.onItemClick(farter)
                    }
                }
            }
        }

        fun bind(farter: Farter) {
            binding.apply {
                farterName.text = farter.name
                farterDateCreated.text = farter.createdDateFormatted
                avatarCard.setCardBackgroundColor(farter.color)
                textFirstLatter.text = farter.name.first().uppercase()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(farter: Farter)
    }


    class FarterDiffUtil : DiffUtil.ItemCallback<Farter>() {
        override fun areItemsTheSame(oldItem: Farter, newItem: Farter): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Farter, newItem: Farter): Boolean =
            oldItem == newItem
    }
}