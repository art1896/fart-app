package com.art.fartapp.dialogs.bottomsheet

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ToggleButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.art.fartapp.R
import com.art.fartapp.databinding.ItemSoundsBinding
import com.art.fartapp.db.Sound
import com.art.fartapp.util.getResourceId
import com.art.fartapp.util.getUriToResource


private const val TAG = "SoundsAdapter"

class AudioItemAdapter(
    private val onItemClickListener: OnItemClickListener,
    private val context: Context
) :
    ListAdapter<Sound, AudioItemAdapter.AudioItemsViewHolder>(SoundsDiffUtil()) {

    private var mediaPlayer: MediaPlayer? = null
    private var playingHolder: AudioItemsViewHolder? = null
    private var checkedHolder: AudioItemsViewHolder? = null
    private var playingPosition = -1
    private var checkedPosition = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioItemsViewHolder {
        val binding = ItemSoundsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioItemsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
        if (position == playingPosition) {
            playingHolder = holder
            updatePlayingView()
        } else {
            updateNonPlayingView(holder)
        }
        if (position == checkedPosition) {
            checkedHolder = holder
            updateCheckedView()
        } else {
            updateNonCheckedView(holder)
        }
    }

    override fun onViewRecycled(holder: AudioItemsViewHolder) {
        super.onViewRecycled(holder)
        if (playingPosition == holder.absoluteAdapterPosition) {
            updateNonPlayingView(playingHolder)
            playingHolder = null
        }
        if (checkedPosition == holder.absoluteAdapterPosition) {
            updateNonCheckedView(checkedHolder)
            checkedHolder = null
        }
    }

    private fun updateCheckedView() {
        checkedHolder!!.itemView.findViewById<ToggleButton>(R.id.checked_toggle).isChecked = true
    }

    private fun updatePlayingView() {
        if (mediaPlayer!!.isPlaying) {
            playingHolder!!.ivPlayPause!!.setImageResource(R.drawable.ic_round_pause_24)
        } else {
            playingHolder!!.ivPlayPause!!.setImageResource(R.drawable.ic_round_play_arrow_24)
        }
    }

    fun stopPlayer() {
        if (null != mediaPlayer) {
            releaseMediaPlayer()
        }
    }

    private fun updateNonPlayingView(holder: AudioItemsViewHolder?) {
        holder?.ivPlayPause?.setImageResource(R.drawable.ic_round_play_arrow_24)
    }

    private fun updateNonCheckedView(holder: AudioItemsViewHolder?) {
        holder?.itemView?.findViewById<ToggleButton>(R.id.checked_toggle)?.isChecked = false
    }

    inner class AudioItemsViewHolder(private val binding: ItemSoundsBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        var ivPlayPause: ImageView? = null

        init {
            ivPlayPause = itemView.findViewById(R.id.play_pause_toggle)
            ivPlayPause!!.setOnClickListener(this)
            binding.apply {
                root.setOnClickListener {
                    if (absoluteAdapterPosition != checkedPosition) {
                        checkedPosition = absoluteAdapterPosition
                        if (null != checkedHolder && checkedHolder?.itemView?.findViewById<ToggleButton>(
                                R.id.checked_toggle
                            )!!.isChecked
                        ) {
                            updateNonCheckedView(checkedHolder!!)
                        }
                        checkedHolder = this@AudioItemsViewHolder
                        onItemClickListener.onItemClick(getItem(absoluteAdapterPosition))
                    }
                    updateCheckedView()
                }
            }
        }

        override fun onClick(v: View?) {
            if (absoluteAdapterPosition == playingPosition) {
                if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                } else {
                    mediaPlayer!!.start()
                }
            } else {
                playingPosition = absoluteAdapterPosition
                if (mediaPlayer != null) {
                    if (null != playingHolder) {
                        updateNonPlayingView(playingHolder!!)
                    }
                    mediaPlayer!!.release()
                }
                playingHolder = this
                startMediaPlayer(context.getResourceId(currentList[playingPosition].rawRes, "raw"))
            }
            updatePlayingView()
        }

        fun bind(sound: Sound) {
            binding.apply {
                avatar.setImageResource(this.root.context.getResourceId(sound.iconRes, "drawable"))
                textSoundName.text = sound.name
                textSoundDuration.text = sound.duration
            }
        }
    }

    private fun releaseMediaPlayer() {
        if (null != playingHolder) {
            updateNonPlayingView(playingHolder!!)
        }
        mediaPlayer!!.release()
        mediaPlayer = null
        playingPosition = -1
    }

    private fun startMediaPlayer(audioResId: Int) {
        mediaPlayer =
            MediaPlayer.create(context, context.getUriToResource(audioResId))
        mediaPlayer?.setOnCompletionListener { releaseMediaPlayer() }
        mediaPlayer?.start()
    }


    interface OnItemClickListener {
        fun onItemClick(sound: Sound)
    }


    class SoundsDiffUtil : DiffUtil.ItemCallback<Sound>() {
        override fun areItemsTheSame(oldItem: Sound, newItem: Sound): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Sound, newItem: Sound): Boolean =
            oldItem == newItem
    }
}
