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
import com.github.droidworksstudio.launcher.adapter.drawer.DrawOrderAdapter
import com.github.droidworksstudio.launcher.data.entities.AppInfo
import com.github.droidworksstudio.launcher.databinding.FragmentDrawOrderBinding
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
class DrawOrderFragment : Fragment(),
    OnItemClickedListener.OnAppStateClickListener,
    OnItemClickedListener.BottomSheetDismissListener,
    OnItemClickedListener.OnAppLongClickedListener,
    OnItemMoveListener.OnItemActionListener,
    BiometricHelper.Callback {
    private var _binding: FragmentDrawOrderBinding? = null

    private val binding get() = _binding!!

    private val viewModel: AppViewModel by viewModels()

    private lateinit var context: Context

    @Inject
    lateinit var preferenceHelper: PreferenceHelper

    @Inject
    lateinit var fingerHelper: BiometricHelper

    @Inject
    lateinit var appHelper: AppHelper

    private val drawOrderAdapter: DrawOrderAdapter by lazy { DrawOrderAdapter(this, preferenceHelper) }

    override fun onSortManually(appInfo: AppInfo) {
        val items = drawOrderAdapter.currentList.toMutableList()
        val manuallySortedApps = items.filter { it.globalAppOrder != -1 }.toMutableList()
        manuallySortedApps.add(appInfo)
        manuallySortedApps.forEachIndexed { index, app ->
            app.globalAppOrder = index
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateAppOrder(manuallySortedApps)
        }
    }

    override fun onSortAutomatically(appInfo: AppInfo) {
        appInfo.globalAppOrder = -1
        viewModel.update(appInfo)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDrawOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appHelper.dayNightMod(requireContext(), binding.drawOrderView)
        super.onViewCreated(view, savedInstanceState)

        context = requireContext()

        setupRecyclerView()
        observeDrawApps()
        observeHomeAppOrder()
        observeSwipeTouchListener()
    }

    private fun setupRecyclerView() {
        binding.drawOrderAdapter.apply {
            adapter = drawOrderAdapter
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(false)
        }
    }

    private fun handleDragAndDrop(oldPosition: Int, newPosition: Int) {
        val items = drawOrderAdapter.currentList.toMutableList()
        val movedItem = items.removeAt(oldPosition)
        items.add(newPosition, movedItem)

        val manuallySortedApps = items.filter { it.globalAppOrder != -1 }
        manuallySortedApps.forEachIndexed { index, appInfo ->
            appInfo.globalAppOrder = index
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateAppOrder(manuallySortedApps)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun observeDrawApps() {
        viewModel.compareInstalledAppInfo()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.drawApps.collect {
                drawOrderAdapter.submitList(it)
                updateNoAppsTextViewVisibility(it.isEmpty())
            }
        }
    }

    private fun updateNoAppsTextViewVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.noApps.visibility = View.VISIBLE
            binding.drawOrderAdapter.visibility = View.GONE
        } else {
            binding.noApps.visibility = View.GONE
            binding.drawOrderAdapter.visibility = View.VISIBLE
        }
    }

    private fun observeHomeAppOrder() {
        binding.drawOrderAdapter.adapter = drawOrderAdapter
        val listener: OnItemMoveListener.OnItemActionListener = drawOrderAdapter

        val simpleItemTouchCallback = object : ItemTouchHelper.Callback() {

            override fun onChildDraw(
                canvas: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float,
                dY: Float, actionState: Int, isCurrentlyActive: Boolean,
            ) {
                if (isCurrentlyActive) {
                    viewHolder.itemView.alpha = 0.5f
                } else {
                    viewHolder.itemView.alpha = 1f
                }
                super.onChildDraw(
                    canvas, recyclerView, viewHolder,
                    dX, dY,
                    actionState, isCurrentlyActive
                )
            }

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ): Int {
                val appInfo = drawOrderAdapter.currentList[viewHolder.bindingAdapterPosition]
                val dragFlags = if (appInfo.globalAppOrder != -1) {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                } else {
                    0
                }
                val swipeFlags = 0
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val items = drawOrderAdapter.currentList.toMutableList()
                val manuallySortedCount = items.count { it.globalAppOrder != -1 }
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                if (toPosition >= manuallySortedCount) {
                    return false
                }
                handleDragAndDrop(fromPosition, toPosition)
                return listener.onViewMoved(fromPosition, toPosition)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                @Suppress("DEPRECATION")
                listener.onViewSwiped(viewHolder.adapterPosition)
            }

            override fun isLongPressDragEnabled() = false
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)

        drawOrderAdapter.setItemTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(binding.drawOrderAdapter)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun observeSwipeTouchListener() {
        binding.apply {
            fragmentContainer.setOnTouchListener(getSwipeGestureListener(context))
            drawOrderAdapter.setOnTouchListener(getSwipeGestureListener(context))
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

    override fun onAppStateClicked(appInfo: AppInfo) {
        viewModel.update(appInfo)
    }

    override fun onViewMoved(oldPosition: Int, newPosition: Int): Boolean {
        return true
    }
}
