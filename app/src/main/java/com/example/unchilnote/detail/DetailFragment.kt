package com.example.unchilnote.detail

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.*
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.unchilnote.MemoViewModelFactory
import com.example.unchilnote.R
import com.example.unchilnote.data.RepositoryProvider
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.databinding.DetailFragmentBinding
import com.example.unchilnote.databinding.WeatherFragmentBinding
import com.example.unchilnote.gallery.GalleryFragment
import com.example.unchilnote.googlemap.MapsFragment
import com.example.unchilnote.list.ListFragmentDirections
import com.example.unchilnote.record.RecordFragment
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.BizLogic.Companion.setTagsMap
import com.example.unchilnote.utils.ConvFormat.Companion.toSelectedKeyString
import com.example.unchilnote.utils.ResourceUtils.Companion.createTagChips
import com.example.unchilnote.write.WriteFragmentDirections
import com.example.unchilnote.write.WriteViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar

class DetailFragment : Fragment() {

    private val logTag = DetailFragment::class.java.name
    private val shareViewModel: SharedViewModel by activityViewModels()
    lateinit var viewModel: DetailViewModel
    private var _fragmentBinding: DetailFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragmentBinding = DetailFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this, MemoViewModelFactory( requireContext().applicationContext)).get(
            DetailViewModel::class.java)
        viewModel.makeArgs(requireContext())


        createTagChips(fragmentBinding.chipDetailFragment.chipGroup, requireContext())

        getTagsMap()

        fragmentBinding.bottomDetail.bottomAppBar
            .replaceMenu(ResourceUtils
                .getResourceID(PUBLIC_CALLER, DETAIL_MENU))

        fragmentBinding.bottomDetail.bottomAppBar.setOnMenuItemClickListener{
            setMenuProcess(it)
            true
        }

        fragmentBinding.bottomDetail.bottomFab.apply {
            contentDescription = LIST
            setImageResource( ResourceUtils.getResourceID(PUBLIC_CALLER, LIST))
            setOnClickListener {

                when (viewModel.isChipsDisplay) {
                    true -> {
                 //       viewModel.isUpdate = true
                        viewModel.isChipsDisplay = !viewModel.isChipsDisplay
                        setTagsCollapsed()
                    }
                    false -> {
                        /*
                        if(viewModel.isUpdate) {
                            setTagsMap( fragmentBinding.chipDetailFragment.chipGroup )
                            viewModel.updateMemo(id=null)
                        }

                         */
                        it.findNavController().navigate(DetailFragmentDirections.actionDetailFragmentToListFragment())
                    }
                }
            }
        }


        viewModel.apply {
/*
            progressBarVisible.observe(viewLifecycleOwner){
                fragmentBinding.weatherProgressBar.visibility =
                    if(it) View.VISIBLE else View.GONE
            }
*/
            snackbar.observe(viewLifecycleOwner) {
                it?.let {
                    MessageUtils.msgToSnackBar(
                        fragmentBinding.root, null, it, Snackbar_LENGTH, false
                    )
                    clearSnackbarMessage()
                }
            }

            replaceContainer(SNAPSHOT)

            memoTbl.observe(viewLifecycleOwner) {
                memoId = it.id
                isSecret = it.isSecret
                isPin = it.isPin


                fragmentBinding.textViewDetailAttachCnt.text = it.desc
                fragmentBinding.textViewDetailTag.text = it.snippets

                if(it.recordCnt > 0) replaceContainer(RECORD)
                else fragmentBinding.detailFragmentRecordFragmentContainer.visibility = View.GONE

                if(it.photoCnt > 0) replaceContainer(PHOTO)
                else fragmentBinding.detailFragmentPhotoFragmentContainer.visibility = View.GONE

                setMenuProcess(null)
            }
        }

        return fragmentBinding.root
    }



    private fun getTagsMap() {
        TAG_INFO.forEach { key, value ->
            when(key){
                CLIMBING -> TAG_INFO.replace( key, viewModel.memoTagTbl.climbing )
                TRACKING -> TAG_INFO.replace( key, viewModel.memoTagTbl.tracking )
                CAMPING -> TAG_INFO.replace( key, viewModel.memoTagTbl.camping )
                TRAVEL -> TAG_INFO.replace( key, viewModel.memoTagTbl.travel )
                CULTURE -> TAG_INFO.replace( key, viewModel.memoTagTbl.culture )
                ART -> TAG_INFO.replace( key, viewModel.memoTagTbl.art )
                TRAFFIC -> TAG_INFO.replace( key, viewModel.memoTagTbl.traffic )
                RESTAURANT -> TAG_INFO.replace( key, viewModel.memoTagTbl.restaurant )
            }
        }

        TAG_INFO.onEachIndexed { index, entry ->
            fragmentBinding.chipDetailFragment.chipGroup.forEachIndexed { idx, view ->
                if( index == idx) {
                    val chip = view as Chip
                    chip.isChecked = entry.value
                    return@forEachIndexed
                }
            }
        }
    }
    private fun setTagsCollapsed() {

        fragmentBinding.bottomDetail.bottomFab.apply {
            contentDescription = LIST
            setImageResource(ResourceUtils.getResourceID(PUBLIC_CALLER, LIST))
        }

        BottomSheetBehavior.from(fragmentBinding.detailFragmentBottomSheetChip).state =
            BottomSheetBehavior.STATE_COLLAPSED

        viewModel._snackbar.value =
            getString(R.string.SnackBarMessage_Label_Ok)

        fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_LABEL_IDX]
            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, LABEL_IMAGE))

        fragmentBinding.textViewDetailTag.text =
            setTagsMap( fragmentBinding.chipDetailFragment.chipGroup ).toSelectedKeyString()

        viewModel.updateMemo(id= R.id.menu_detail_label)

    }

    private fun setTagsExpended() {

        fragmentBinding.bottomDetail.bottomFab.apply {
            contentDescription = LABEL
            setImageResource(ResourceUtils.getResourceID(PUBLIC_CALLER, LABEL_IMAGE))
        }
        BottomSheetBehavior.from(fragmentBinding.detailFragmentBottomSheetChip).state =
            BottomSheetBehavior.STATE_EXPANDED
        viewModel._snackbar.value =
            getString(R.string.SnackBarMessage_Label_Set)
        fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_LABEL_IDX]
            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, CHECK))
    }

    @SuppressLint("MissingPermission")
    fun setMenuProcess(it: MenuItem?) {
        it?.let {
            viewModel.apply {
                when (it.itemId) {
                    R.id.menu_detail_label -> {
                        isChipsDisplay = !isChipsDisplay
                //        isUpdate = true
                        when (isChipsDisplay) {
                            true -> setTagsExpended()
                            false -> setTagsCollapsed()
                        }
                    }
                    R.id.menu_detail_secret -> {
                        isSecret = !isSecret
                   //     isUpdate = true
                        viewModel.updateMemo(id= R.id.menu_detail_secret)
                        when (isSecret) {
                            true -> {
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Secret_On)
                                it.setIcon(
                                    ResourceUtils.getResourceID(
                                        PUBLIC_CALLER,
                                        SECRET_LOCK_IMAGE
                                    )
                                )
                            }
                            false -> {
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Secret_Off)
                                it.setIcon(
                                    ResourceUtils.getResourceID(
                                        PUBLIC_CALLER,
                                        SECRET_OPEN_IMAGE
                                    )
                                )
                            }
                        }
                    }
                    R.id.menu_detail_pin -> {
                        isPin = !isPin
                   //     isUpdate = true
                        viewModel.updateMemo(id= R.id.menu_detail_pin)
                        when (isPin) {
                            true -> {
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Pin_On)
                                it.setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PIN_ON_IMAGE))
                            }
                            false -> {
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Pin_Off)
                                it.setIcon(
                                    ResourceUtils.getResourceID(
                                        PUBLIC_CALLER,
                                        PIN_OFF_IMAGE
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if( it == null ) {
            viewModel.apply {
                when (isSecret) {
                    true -> {
                        fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_SECRET_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, SECRET_LOCK_IMAGE))
                    }
                    false -> {
                        fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_SECRET_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, SECRET_OPEN_IMAGE))
                    }
                }
                when (isPin) {
                    true -> {
                        fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_PIN_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PIN_ON_IMAGE))
                    }
                    false -> {
                        fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_PIN_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PIN_OFF_IMAGE))
                    }
                }

                when(isChipsDisplay) {
                    true -> fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_LABEL_IDX]
                        .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, CHECK))
                    false -> fragmentBinding.bottomDetail.bottomAppBar.menu[DETAIL_MENU_LABEL_IDX]
                        .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, LABEL_IMAGE))
                }
            }
        }
    }

    fun replaceContainer(containerName: String) {
        childFragmentManager.commit {
            when(containerName) {

                SNAPSHOT -> {
                    replace<GalleryFragment>( fragmentBinding.detailFragmentSnapshotFragmentContainer.id,
                        args = viewModel.args_Snapshot)
                }
                RECORD -> {
                    replace<RecordFragment>( fragmentBinding.detailFragmentRecordFragmentContainer.id,
                        args = viewModel.args_record)
                }
                PHOTO -> {
                    replace<GalleryFragment>( fragmentBinding.detailFragmentPhotoFragmentContainer.id,
                        args = viewModel.args_Photo)
                }
            }
            setReorderingAllowed(true)
        }
    }


}