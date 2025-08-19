package com.github.droidworksstudio.launcher.ui.drawer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.github.droidworksstudio.common.launchApp
import com.github.droidworksstudio.common.showLongToast
import com.github.droidworksstudio.launcher.R
import com.github.droidworksstudio.launcher.adapter.drawer.DrawAdapter
import com.github.droidworksstudio.launcher.data.entities.AppInfo
import com.github.droidworksstudio.launcher.databinding.FragmentDrawModBinding
import com.github.droidworksstudio.launcher.helper.AppHelper
import com.github.droidworksstudio.launcher.helper.BiometricHelper
import com.github.droidworksstudio.launcher.helper.PreferenceHelper
import com.github.droidworksstudio.launcher.listener.OnItemClickedListener
import com.github.droidworksstudio.launcher.listener.OnItemMoveListener
import com.github.droidworksstudio.launcher.listener.OnSwipeTouchListener
import com.github.droidworksstudio.launcher.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject


@AndroidEntryPoint
class DrawFragmentMod : Fragment(),
    OnItemClickedListener.OnAppsClickedListener,
    OnItemClickedListener.OnAppLongClickedListener,
    OnItemClickedListener.OnAppStateClickListener,
    OnItemClickedListener.BottomSheetDismissListener,
    BiometricHelper.Callback {
    private var _binding: FragmentDrawModBinding? = null

    private val binding get() = _binding!!

    private val viewModel: AppViewModel by viewModels()

    private lateinit var context: Context

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    @Inject
    lateinit var fingerHelper: BiometricHelper

    @Inject
    lateinit var appHelper: AppHelper

    private val drawAdapter: DrawAdapter by lazy { DrawAdapter(this, this, preferenceHelper) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentDrawModBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appHelper.dayNightMod(requireContext(), binding.drawView)
        super.onViewCreated(view, savedInstanceState)

        context = requireContext()

        setupRecyclerView()
        observeFavorite()
        observeSwipeTouchListener()
    }

    private fun setupRecyclerView() {

        binding.drawAdapter.apply {
            adapter = drawAdapter
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(false)
        }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun observeFavorite() {
        viewModel.compareInstalledAppInfo()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.drawApps.collect {
                drawAdapter.submitList(it)
                updateNoAppsTextViewVisibility(it.isEmpty())
            }
        }
    }

    private fun updateNoAppsTextViewVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.noApps.visibility = View.VISIBLE
            binding.drawAdapter.visibility = View.GONE
        } else {
            binding.noApps.visibility = View.GONE
            binding.drawAdapter.visibility = View.VISIBLE
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun observeSwipeTouchListener() {
        binding.apply {
            fragmentContainer.setOnTouchListener(getSwipeGestureListener(context))
            drawAdapter.setOnTouchListener(getSwipeGestureListener(context))
        }
    }

    private fun getSwipeGestureListener(context: Context): View.OnTouchListener {
        return object : OnSwipeTouchListener(context, preferenceHelper) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                findNavController().navigateUp()
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                findNavController().navigateUp()
            }
        }
    }

    private fun observeBioAuthCheck(appInfo: AppInfo) {
        if (!appInfo.lock) {
            context.launchApp(appInfo)
        } else {
            fingerHelper.startBiometricAuth(appInfo, this)
        }
    }

    override fun onAppStateClicked(appInfo: AppInfo) {
        viewModel.update(appInfo)
    }

    override fun onAppClicked(appInfo: AppInfo) {
        observeBioAuthCheck(appInfo)
    }

    override fun onAuthenticationSucceeded(appInfo: AppInfo) {
        context.showLongToast(getString(R.string.authentication_succeeded))
        context.launchApp(appInfo)
    }

    override fun onAuthenticationFailed() {
        context.showLongToast(getString(R.string.authentication_failed))
    }

    override fun onAuthenticationError(errorCode: Int, errorMessage: CharSequence?) {
        context.showLongToast(
            getString(R.string.authentication_error).format(
                errorMessage,
                errorCode
            )
        )
    }

    override fun onAppLongClicked(appInfo: AppInfo) {
        //
    }

    override fun onBottomSheetDismissed() {
        //
    }
}
