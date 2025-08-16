package com.github.droidworksstudio.launcher.ui.home

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.droidworksstudio.common.ColorIconsExtensions
import com.github.droidworksstudio.common.dpToPx
import com.github.droidworksstudio.common.getAllProfileAppIcons
import com.github.droidworksstudio.launcher.R
import com.github.droidworksstudio.launcher.data.entities.AppInfo
import com.github.droidworksstudio.launcher.databinding.ItemHomeBinding
import com.github.droidworksstudio.launcher.helper.PreferenceHelper
import com.github.droidworksstudio.launcher.listener.OnItemClickedListener
import com.github.droidworksstudio.launcher.utils.Constants
import javax.inject.Inject

class HomeViewHolder @Inject constructor(
    private val binding: ItemHomeBinding,
    private val onAppClickedListener: OnItemClickedListener.OnAppsClickedListener,
    private val onAppLongClickedListener: OnItemClickedListener.OnAppLongClickedListener,
    private val preferenceHelper: PreferenceHelper,
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("ClickableViewAccessibility")
    fun bind(appInfo: AppInfo) {
        binding.apply {
            // Get the current LayoutParams of appHiddenName
            val layoutAppNameParams = appHomeName.layoutParams as LinearLayoutCompat.LayoutParams

            // Set the margins
            layoutAppNameParams.topMargin = preferenceHelper.homeAppPadding.toInt()
            layoutAppNameParams.bottomMargin = preferenceHelper.homeAppPadding.toInt()

            appHomeName.layoutParams = layoutAppNameParams

            // Update appHomeName properties
            appHomeName.text = appInfo.appName
            appHomeName.setTextColor(preferenceHelper.appColor)
            appHomeName.textSize = preferenceHelper.appTextSize
            appHomeName.gravity = preferenceHelper.favoriteAppAlignment

            if (preferenceHelper.showAppIcon) {
                val appInfoMap = root.context.getAllProfileAppIcons()
                appInfoMap.forEach { (key, icon) ->
                    val userHandle: Int? = key.first
                    val packageName: String? = key.second
                    if (appInfo.packageName == packageName && appInfo.userHandle == userHandle) {
                        val easyDot = ContextCompat.getDrawable(itemView.context, R.drawable.app_easy_dot_icon)!!
                        val appIcon = icon
                        val nonNullDrawable: Drawable = appIcon ?: easyDot

                        val appIconSize = (preferenceHelper.appTextSize * if (preferenceHelper.iconPack == Constants.IconPacks.System) 2f else 1.1f).toInt()

                        val layoutParams = LinearLayoutCompat.LayoutParams(appIconSize, appIconSize)
                        val appNewIcon: Drawable? = if (preferenceHelper.iconPack == Constants.IconPacks.EasyDots) {
                            val newIcon = ContextCompat.getDrawable(itemView.context, R.drawable.app_easy_dot_icon)!!
                            val bitmap = ColorIconsExtensions.drawableToBitmap(nonNullDrawable)
                            val dominantColor = ColorIconsExtensions.getDominantColor(bitmap)
                            ColorIconsExtensions.recolorDrawable(newIcon, dominantColor)
                        } else if (preferenceHelper.iconPack == Constants.IconPacks.NiagaraDots) {
                            val newIcon = ContextCompat.getDrawable(itemView.context, R.drawable.app_niagara_dot_icon)!!
                            val bitmap = ColorIconsExtensions.drawableToBitmap(nonNullDrawable)
                            val dominantColor = ColorIconsExtensions.getDominantColor(bitmap)
                            ColorIconsExtensions.recolorDrawable(newIcon, dominantColor)
                        } else {
                            null
                        }

                        appHomeIcon.layoutParams = layoutParams
                        appHomeIcon.setImageDrawable(appNewIcon ?: nonNullDrawable)
                        appHomeIcon.visibility = View.VISIBLE

                        val parentLayout = appHomeName.parent as LinearLayoutCompat
                        parentLayout.orientation = LinearLayoutCompat.HORIZONTAL
                        parentLayout.removeAllViews()

                        when (preferenceHelper.favoriteAppAlignment) {
                            Gravity.START -> {
                                layoutParams.marginEnd = 10.dpToPx()
                                parentLayout.addView(appHomeIcon)
                                parentLayout.addView(appHomeName)
                            }

                            Gravity.END -> {
                                layoutParams.marginStart = 10.dpToPx()
                                parentLayout.addView(appHomeName)
                                parentLayout.addView(appHomeIcon)
                            }

                            else -> {
                                parentLayout.addView(appHomeName)
                            }
                        }
                    }
                }
            } else {
                appHomeIcon.visibility = View.GONE
            }

            appHomeName.setOnClickListener {
                onAppClickedListener.onAppClicked(appInfo)
            }

            appHomeName.setOnLongClickListener {
                onAppLongClickedListener.onAppLongClicked(appInfo)
                true
            }
        }
    }
}
