package com.pixelsu.ui.hide

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pixelsu.R
import com.pixelsu.databinding.ItemAppHideBinding
import com.pixelsu.model.AppInfo
import com.pixelsu.utils.PixelSULogger

class AppHideAdapter(
    private val hideConfig: com.PixelSU.model.HideListConfig,
    private val onAppToggle: (AppInfo, Boolean) -> Unit
) : ListAdapter<AppInfo, AppHideAdapter.AppViewHolder>(DiffCallback()) {

    fun updateHideConfig(config: com.PixelSU.model.HideListConfig) {
        // Trigger rebind to update hide status
        notifyDataSetChanged()
    }

    class AppViewHolder(private val binding: ItemAppHideBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appInfo: AppInfo, isHidden: Boolean, onToggle: (AppInfo, Boolean) -> Unit) {
            binding.apply {
                tvAppName.text = appInfo.appName
                tvPackageName.text = appInfo.packageName
                
                // Set icon if available
                if (appInfo.iconPath.isNotBlank()) {
                    ivAppIcon.setImageURI(android.net.Uri.parse(appInfo.iconPath))
                } else {
                    ivAppIcon.setImageResource(R.drawable.ic_android)
                }
                
                // System app badge
                tvSystemBadge.visibility = if (appInfo.isSystemApp) View.VISIBLE else View.GONE
                
                // Hide status
                switchHide.isChecked = isHidden
                switchHide.setOnCheckedChangeListener { _, checked ->
                    onToggle(appInfo, checked)
                }
                
                root.setOnClickListener { onToggle(appInfo, !isHidden) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppHideBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = getItem(position)
        val isHidden = hideConfig.targetPackages.contains(appInfo.packageName)
        holder.bind(appInfo, isHidden, onAppToggle)
    }

    class DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
