package com.wagyufari.dzikirqu.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wagyufari.dzikirqu.base.BaseViewHolder
import com.wagyufari.dzikirqu.constants.LocaleConstants
import com.wagyufari.dzikirqu.constants.LocaleConstants.locale
import com.wagyufari.dzikirqu.data.room.dao.getNoteDao
import com.wagyufari.dzikirqu.databinding.ItemNoteBinding
import com.wagyufari.dzikirqu.databinding.ItemNoteFolderBinding
import com.wagyufari.dzikirqu.model.Note
import com.wagyufari.dzikirqu.ui.note.composer.NoteComposerActivity
import com.wagyufari.dzikirqu.util.ViewUtils
import com.wagyufari.dzikirqu.util.io
import com.wagyufari.dzikirqu.util.main


class NoteAdapter : ListAdapter<Any, BaseViewHolder>(NoteStaggeredDiff) {

    private var mListener: Callback? = null

    object NoteStaggeredDiff : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Note && newItem is Note){
                oldItem.id == newItem.id
            } else{
                oldItem.toString() == newItem.toString()
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Note && newItem is Note){
                oldItem == newItem
            } else{
                oldItem.toString() == newItem.toString()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is Note) 0 else 1
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            0 -> {
                val viewBinding = ItemNoteBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                NoteListViewHolder(viewBinding)
            }
            else -> {
                val viewBinding = ItemNoteFolderBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
                NoteFolderViewHolder(viewBinding)
            }
        }
    }

    fun setListener(listener: Callback) {
        this.mListener = listener
    }

    interface Callback {
        fun onSelectFolder(folder:String)
    }

    inner class NoteFolderViewHolder(private val mBinding: ItemNoteFolderBinding) :
        BaseViewHolder(mBinding.root) {

        @SuppressLint("CheckResult")
        override fun onBind(position: Int) {

            if (position == 0){
                val params = mBinding.root.layoutParams as RecyclerView.LayoutParams
                if (position == 0) {
                    params.setMargins(
                        ViewUtils.dpToPx(12),
                        ViewUtils.dpToPx(20),
                        ViewUtils.dpToPx(12),
                        ViewUtils.dpToPx(8)
                    )
                }
            }

            mBinding.name.text = getItem(position) as String
            mBinding.root.context.io {
                val count = mBinding.root.context.getNoteDao().getNoteByFolderSuspend(getItem(position) as String)
                mBinding.root.context.main {
                    mBinding.count.text = String.format(LocaleConstants.N_NOTES.locale(), count.count())
                }
            }
            mBinding.clickable.setOnClickListener {
                mBinding.root.context.apply {
                    mListener?.onSelectFolder(getItem(position) as String)
                }
            }
        }
    }

    inner class NoteListViewHolder(private val mBinding: ItemNoteBinding) :
        BaseViewHolder(mBinding.root) {

        @SuppressLint("CheckResult")
        override fun onBind(position: Int) {
            val data = getItem(position) as Note

            if (position == 0){
                val params = mBinding.root.layoutParams as RecyclerView.LayoutParams
                if (position == 0) {
                    params.setMargins(
                        ViewUtils.dpToPx(12),
                        ViewUtils.dpToPx(20),
                        ViewUtils.dpToPx(12),
                        ViewUtils.dpToPx(8)
                    )
                }
            }

            mBinding.title.text = if (data.title?.isNotBlank() == true) data.title else "Untitled data"
            mBinding.subtitle.isVisible = data.subtitle?.isNotBlank() == true
            mBinding.text.isVisible = data.content?.isNotBlank() == true
            mBinding.subtitle.text = data.subtitle
            mBinding.clickable.setOnClickListener {
                mBinding.root.context.apply {
                    startActivity(NoteComposerActivity.newIntent(this,
                        data.id,
                        false))
                }
            }
            mBinding.date.text = data.updatedDate
            mBinding.text.text = data.content?.replace("\n", " ")
        }
    }

}