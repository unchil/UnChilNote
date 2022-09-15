package com.example.unchilnote.list


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.unchilnote.MemoViewModelFactory
import com.example.unchilnote.R
import com.example.unchilnote.data.RepositoryProvider
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.data.dataset.MEMO_TBL
import com.example.unchilnote.databinding.ListFragmentBinding
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.BizLogic.Companion.initTagsMap
import com.example.unchilnote.utils.BizLogic.Companion.setTagsMap
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.bottomsheet.BottomSheetBehavior


class ListFragment : Fragment() {

    private val logTag = ListFragment::class.java.name
    private val shareViewModel: SharedViewModel by activityViewModels()
    lateinit var viewModel:ListViewModel
    private var _fragmentBinding:  ListFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!
    private lateinit var currentAdapter: MemoListAdapter
    private lateinit var keyguardManager: KeyguardManager


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragmentBinding = ListFragmentBinding.inflate(inflater, container, false)


        viewModel = ViewModelProvider(this, MemoViewModelFactory( requireContext().applicationContext)).get(
            ListViewModel::class.java)

        shareViewModel.apply{
            repository = RepositoryProvider.getRepository(requireContext().applicationContext)
            googleMap = null
            googleMapType = GoogleMap.MAP_TYPE_NORMAL
            googleMapZoom = 18F
        }



        ResourceUtils.createTagChips(fragmentBinding.chipListFragment.chipGroup, requireContext())

        fragmentBinding.bottomList.bottomAppBar
            .replaceMenu(ResourceUtils
                .getResourceID(PUBLIC_CALLER, LIST_MENU))

        fragmentBinding.bottomList.bottomAppBar.setOnMenuItemClickListener{
            when(it.itemId) {
                R.id.menu_list_place -> {
                    this.findNavController()
                        .navigate(ListFragmentDirections.actionListFragmentToMapsFragment())
                    true
                }
                R.id.menu_list_search -> {
                    viewModel.isChipsDisplay = !viewModel.isChipsDisplay
                    when (viewModel.isChipsDisplay) {
                        true -> setTagsExpended()
                        false -> setTagsCollapsed()
                    }
                    true
                }
                else -> { false }
            }
        }

        fragmentBinding.bottomList.bottomFab.apply {
            contentDescription = WRITE
            setImageResource( ResourceUtils.getResourceID(PUBLIC_CALLER, WRITE))
            setOnClickListener {
                when (viewModel.isChipsDisplay) {
                    true -> {
                        viewModel.isChipsDisplay = !viewModel.isChipsDisplay
                        setTagsCollapsed()
                    }
                    false -> {
                        FileUtils.clearCacheFile(requireContext())
                        shareViewModel.apply {
                            if (playListStat.size > 0 ) playListStat.clear()
                            clearRecordTextMap()
                        }

                        it.findNavController().navigate(ListFragmentDirections.actionListFragmentToWriteFragment())
                    }
                }
            }
        }

        viewModel.memoList.observe(viewLifecycleOwner){
            setAdapter(it)
        }

        val swipeHelperCallerback = SwipeHelperCallback()
        swipeHelperCallerback.setClamp(SWIPEHOLDINGWIDTH)
        ItemTouchHelper(swipeHelperCallerback).attachToRecyclerView(fragmentBinding.memoGrid)

        fragmentBinding.swipeRefresh.setOnRefreshListener {
            viewModel.memoList.observe(viewLifecycleOwner){
                setAdapter(it)
                fragmentBinding.swipeRefresh.isRefreshing = false
            }
        }

        fragmentBinding.memoGrid.apply {
            setOnTouchListener { view, motionEvent ->
                swipeHelperCallerback.removePreviousClamp(this)
                false
            }
        }

        keyguardManager = requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        viewModel.apply {
            snackbar.observe(viewLifecycleOwner) {
                it?.let {
                    MessageUtils.msgToSnackBar(
                        fragmentBinding.root, null, it, Snackbar_LENGTH, false
                    )
                    clearSnackbarMessage()
                }
            }
            progressBarVisible.observe(viewLifecycleOwner) {
                //
            }
        }

        return fragmentBinding.root
    }


    private fun setTagsExpended() {

        fragmentBinding.bottomList.bottomFab.apply {
            contentDescription = OK
            setImageResource( ResourceUtils.getResourceID(PUBLIC_CALLER, SEARCH_IMAGE))
        }

        initTagsMap()
        BottomSheetBehavior.from(fragmentBinding.listFragmentBottomSheetChip).state =
            BottomSheetBehavior.STATE_EXPANDED
        viewModel._snackbar.value =
            getString(R.string.SnackBarMessage_Search_Set)

        fragmentBinding.bottomList.bottomAppBar.menu[LIST_MENU_LABEL_IDX]
            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, CHECK))
    }

    private fun setTagsCollapsed() {

        fragmentBinding.bottomList.bottomFab.apply {
            contentDescription = WRITE
            setImageResource(ResourceUtils.getResourceID(PUBLIC_CALLER, WRITE))
        }

        setTagsMap(fragmentBinding.chipListFragment.chipGroup)
        BottomSheetBehavior.from(fragmentBinding.listFragmentBottomSheetChip).state =
            BottomSheetBehavior.STATE_COLLAPSED
        viewModel._snackbar.value =
            getString(R.string.SnackBarMessage_Search_Ok)

        fragmentBinding.bottomList.bottomAppBar.menu[LIST_MENU_LABEL_IDX]
            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, SEARCH_IMAGE))

        viewModel.searchMemoList.observe(viewLifecycleOwner){
            setAdapter(it)
            initTagsMap()
        }
    }


    private fun setAdapter( memoList: List<MEMO_TBL>) {
        currentAdapter = MemoListAdapter()
        currentAdapter.submitList(memoList)
        fragmentBinding.memoGrid.adapter = currentAdapter
    }

    fun getBiometricPrompt():BiometricPrompt {
        return BiometricPrompt(this, ContextCompat.getMainExecutor(requireContext()),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel._snackbar.value = errString.toString()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)

                    when(viewModel.biometricCheckType) {
                        BIOMETRIC_CHECK_TYPE_VIEW -> {
                            findNavController().navigate(ListFragmentDirections.actionListFragmentToDetailFragment())
                        }
                        BIOMETRIC_CHECK_TYPE_SHARE -> {
                            currentAdapter.shareItem(viewModel, requireContext())
                        }
                        BIOMETRIC_CHECK_TYPE_DELETE -> {
                            currentAdapter.deleteItem(viewModel, requireContext())
                        }
                        else -> { }
                    }

                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel._snackbar.value = getString(R.string.BioMetric_Fail)
                }
            })
    }

}