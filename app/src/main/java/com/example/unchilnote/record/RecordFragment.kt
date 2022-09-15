package com.example.unchilnote.record

import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.unchilnote.MemoViewModelFactory
import com.example.unchilnote.R
import com.example.unchilnote.data.RepositoryProvider
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.databinding.GalleryFragmentBinding
import com.example.unchilnote.databinding.RecordFragmentBinding
import com.example.unchilnote.databinding.WriteFragmentBinding
import com.example.unchilnote.gallery.GalleryFragment
import com.example.unchilnote.gallery.ImageViewer
import com.example.unchilnote.list.ListFragment
import com.example.unchilnote.utils.*
import com.example.unchilnote.weather.WeatherViewModel
import com.example.unchilnote.write.WriteFragment
import com.example.unchilnote.write.WriteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File

class RecordFragment internal constructor(): Fragment() {
    private val logTag = RecordFragment::class.java.name
    private val sharedViewModel: SharedViewModel by activityViewModels()
    lateinit var viewModel: RecordViewModel
    private lateinit var rootDirectory: String
    private var _fragmentBinding:  RecordFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!

    inner class RecordPagerAdapter( val fragment: RecordFragment): FragmentStateAdapter(fragment){

        override fun getItemCount(): Int = viewModel.recordList.size
        override fun createFragment(position: Int): Fragment {

            return RecordViewerFragment.newInstance(viewModel.recordList[position] )
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragmentBinding = RecordFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this, MemoViewModelFactory( requireContext().applicationContext)).get(
            RecordViewModel::class.java)
        viewModel.apply {
            parentFragmentName = requireParentFragment()::class.java.name
            arguments?.let {
                it.getString(ARG_ROOTDIR)?.let {
                    rootDirectory = it
                    setRecordList(it, requireContext())
                    when(parentFragmentName){
                        DETAILFRAGMENT -> sharedViewModel.setRecordTextMap()
                    }
                }
            }
        }



        fragmentBinding.fabBtnRecordDelete.isVisible =
            viewModel.recordList.size != 0
                    && rootDirectory != viewModel.recordDirectory



        viewModel.apply {
            snackbar.observe(viewLifecycleOwner) {
                it?.let {
                    MessageUtils.msgToSnackBar(
                        fragmentBinding.root, null, it, Snackbar.LENGTH_LONG, false
                    )
                    clearSnackbarMessage()
                }
            }
            progressBarVisible.observe(viewLifecycleOwner){
                fragmentBinding.recordProgressBar.visibility =
                    if(it) View.VISIBLE else View.GONE
            }
        }


        return fragmentBinding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        fragmentBinding.viewPagerRecord.apply {
            offscreenPageLimit = 2
            adapter = RecordPagerAdapter(this@RecordFragment)
            TabLayoutMediator( fragmentBinding.recordFragmentViewPagerTab, this) { tab, position ->
                tab.text = (position+1).toString()
            }.attach()
        }


        fragmentBinding.fabBtnRecordDelete.apply {
            if( viewModel.recordList.size == 0) {
                isVisible = false
            }
            setOnClickListener {
                clickDeleteBtn()
            }
        }

    }


    private fun clickDeleteBtn( ) {
        val index = fragmentBinding.viewPagerRecord.currentItem
        viewModel.recordList.getOrNull(index)?.let {
            MaterialAlertDialogBuilder(requireContext())
                .apply {
                    setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, ALERT_IMAGE))
                    setTitle(context.getString(R.string.Dialog_Delete_Title))
                    setMessage(context.getString(R.string.Dialog_Delete_Message))
                    setNeutralButton(CANCEL) { _, _ -> }
                    setPositiveButton(OK) { _, _ -> deletePagerItem(index) }
                }.create().show()
        }
    }

    private fun deletePagerItem(index:Int ) {

        val mediaFile = viewModel.recordList[index]
        val fileName = mediaFile.name
        mediaFile.delete()

        MediaScannerConnection.scanFile(
            requireContext(), arrayOf(mediaFile.absolutePath), null, null)

        viewModel.recordList.removeAt(index)

        sharedViewModel.removeRecordTextMap(fileName)


        fragmentBinding.viewPagerRecord.apply {
            adapter = RecordPagerAdapter(this@RecordFragment)
            TabLayoutMediator( fragmentBinding.recordFragmentViewPagerTab, this) { tab, position ->
                tab.text = (position+1).toString()
            }.attach()
        }


        fragmentBinding.fabBtnRecordDelete.isVisible =
            viewModel.recordList.size != 0

    }




    private fun loadRecordText(fileName: String) :String {


        val recordText =
            when(requireParentFragment()){
                is WriteFragment -> {
                    val parentViewModel: WriteViewModel by viewModels(
                        ownerProducer = { requireParentFragment() }
                    )
                    viewModel.recordTextMap[fileName] ?: ""
                }
                else -> { ""}
            }
        return recordText
    }

    override fun onDestroy() {
        _fragmentBinding = null
        super.onDestroy()
    }



}