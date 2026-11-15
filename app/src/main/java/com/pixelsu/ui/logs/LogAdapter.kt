package com.pixelsu.ui.logs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixelsu.R
import com.pixelsu.databinding.ItemLogBinding
import com.pixelsu.model.LogEntry
import com.pixelsu.model.LogLevel
import com.pixelsu.model.LogCategory

class LogAdapter : ListAdapter<LogEntry, LogAdapter.LogViewHolder>(DiffCallback()) {

    class LogViewHolder(private val binding: ItemLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(log: LogEntry) {
            binding.apply {
                tvTime.text = log.formattedTimestamp
                tvLevel.text = log.level.name
                tvTag.text = log.tag
                tvMessage.text = log.message
                
                tvLevel.setTextColor(getLevelColor(log.level))
                
                tvCategory.text = log.category.name
                tvCategory.setBackgroundResource(getCategoryBackground(log.category))
                
                if (log.throwable != null && log.throwable!!.isNotBlank()) {
                    tvThrowable.text = log.throwable
                    tvThrowable.visibility = View.VISIBLE
                } else {
                    tvThrowable.visibility = View.GONE
                }
                
                root.setOnClickListener {
                    tvThrowable.visibility = if (tvThrowable.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                }
            }
        }

        private fun getLevelColor(level: LogLevel): Int {
            return when (level) {
                LogLevel.VERBOSE -> binding.root.context.getColor(R.color.text_disabled)
                LogLevel.DEBUG -> binding.root.context.getColor(R.color.info)
                LogLevel.INFO -> binding.root.context.getColor(R.color.success)
                LogLevel.WARN -> binding.root.context.getColor(R.color.warning)
                LogLevel.ERROR -> binding.root.context.getColor(R.color.error)
                LogLevel.ASSERT -> binding.root.context.getColor(R.color.error_dark)
            }
        }

        private fun getCategoryBackground(category: LogCategory): Int {
            return when (category) {
                LogCategory.GENERAL -> R.drawable.badge_general
                LogCategory.ROOT_CHECK -> R.drawable.badge_root
                LogCategory.ROOT_ENABLE -> R.drawable.badge_root
                LogCategory.ROOT_DISABLE -> R.drawable.badge_root
                LogCategory.TEMP_ROOT -> R.drawable.badge_temp
                LogCategory.ROOT_HIDE -> R.drawable.badge_hide
                LogCategory.EXPLOIT -> R.drawable.badge_exploit
                LogCategory.EXPLOIT_EXECUTION -> R.drawable.badge_exploit
                LogCategory.SETTINGS -> R.drawable.badge_settings
                LogCategory.BOOT -> R.drawable.badge_boot
                LogCategory.PACKAGE_CHANGE -> R.drawable.badge_package
                LogCategory.ERROR -> R.drawable.badge_error
                LogCategory.SECURITY -> R.drawable.badge_security
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean {
            return oldItem == newItem
        }
    }
}
