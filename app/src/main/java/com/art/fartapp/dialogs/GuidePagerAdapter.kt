package com.art.fartapp.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.art.fartapp.databinding.ItemUserGuideBinding
import com.art.fartapp.model.UserGuidePage

class GuidePagerAdapter :
    ListAdapter<UserGuidePage, GuidePagerAdapter.GuidePagerViewHolder>(PagesDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuidePagerViewHolder {
        val binding =
            ItemUserGuideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuidePagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuidePagerViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    class GuidePagerViewHolder(private val binding: ItemUserGuideBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(page: UserGuidePage) {
            binding.apply {
                itemImage.setImageDrawable(page.image)
                itemText.text = page.text
            }
        }

    }

    class PagesDiffUtil : DiffUtil.ItemCallback<UserGuidePage>() {
        override fun areItemsTheSame(oldItem: UserGuidePage, newItem: UserGuidePage): Boolean =
            oldItem.text == newItem.text

        override fun areContentsTheSame(oldItem: UserGuidePage, newItem: UserGuidePage): Boolean =
            oldItem == newItem
    }
}