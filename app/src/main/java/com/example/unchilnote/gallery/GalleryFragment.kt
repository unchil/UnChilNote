package com.example.unchilnote.gallery

import android.media.MediaScannerConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.unchilnote.MemoViewModelFactory
import com.example.unchilnote.R
import com.example.unchilnote.camera.CameraFragment
import com.example.unchilnote.data.RepositoryProvider
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.databinding.GalleryFragmentBinding
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.ResourceUtils.Companion.getResourceID
import com.example.unchilnote.weather.WeatherViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator


class GalleryFragment internal constructor(): Fragment() {
    private val logTag = GalleryFragment::class.java.name
    private val sharedViewModel: SharedViewModel by activityViewModels()
    lateinit var viewModel: GalleryViewModel
    lateinit var parentFragmentName: String
    private var _fragmentBinding: GalleryFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!

    private lateinit var rootDirectory: String

    inner class ImagePagerAdapter( val fragment: GalleryFragment): FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = viewModel.imageList.size
        override fun createFragment(position: Int): Fragment {
            return ImageViewer.newInstance( ::GalleryFragment.name, Thread.currentThread().name, viewModel.imageList[position])
        }
    }

    fun initSelfCallData() {
        sharedViewModel.apply {
            selfCallMap.remove(rootDirectory)


        }
    }
    fun setSelfCallData() {
        sharedViewModel.apply {
            selfCallMap.put(rootDirectory, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentBinding = GalleryFragmentBinding.inflate(inflater, container, false)

        rootDirectory = GalleryFragmentArgs.fromBundle(requireArguments()).rootDir
        viewModel = ViewModelProvider(
            this,
            MemoViewModelFactory(requireContext().applicationContext)
        ).get(
            GalleryViewModel::class.java
        )

        if(savedInstanceState == null) {
            viewModel.apply {
                _isGallerySelfCall =
                    if (sharedViewModel.selfCallMap.contains(rootDirectory))
                        sharedViewModel.selfCallMap.get(rootDirectory) == true
                    else false
                initSelfCallData()
                setImageList(rootDirectory, requireContext())
            }
        }


        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentBinding.viewPagerGallery.apply {
            offscreenPageLimit = 2
            adapter = ImagePagerAdapter(this@GalleryFragment)
            TabLayoutMediator( fragmentBinding.galleryFragmentViewPagerTab, this) { tab, position ->
                tab.text = (position+1).toString()
            }.attach()
        }

        if( viewModel.imageList.size == 0) {
            fragmentBinding.fabBtnGalleryDelete.isVisible = false
          //  fragmentBinding.fabBtnGallerySelf.isVisible = false
        }

        if(arrayOf(viewModel.snapshotDirectory, viewModel.photoDirectory).contains(rootDirectory))
            fragmentBinding.fabBtnGalleryDelete.isVisible = false

/*
        fragmentBinding.fabBtnGalleryDelete.isVisible =
            !arrayOf(viewModel.snapshotDirectory, viewModel.photoDirectory).contains(rootDirectory)



        if(viewModel._isGallerySelfCall) {
            fragmentBinding.fabBtnGallerySelf.isVisible = false
        }
*/

        fragmentBinding.fabBtnGalleryDelete.setOnClickListener { clickDeleteBtn() }

        /*
        fragmentBinding.fabBtnGallerySelf.setOnClickListener {
               setSelfCallData()
               it.findNavController().navigate(R.id.galleryFragment,  requireArguments())
        }
         */
    }


    private fun clickDeleteBtn() {

        val index = fragmentBinding.viewPagerGallery.currentItem

        viewModel.imageList.getOrNull(index)?.let {
            MaterialAlertDialogBuilder(requireContext())
                .apply {
                    setIcon(getResourceID(PUBLIC_CALLER, ALERT_IMAGE))
                    setTitle(context.getString(R.string.Dialog_Delete_Title))
                    setMessage(context.getString(R.string.Dialog_Delete_Message))
                    setNeutralButton(CANCEL) { _, _ -> }
                    setPositiveButton(OK) { _, _ -> deletePagerItem(index) }
                }.create().show()
        }
    }


    private fun deletePagerItem(index:Int ) {
        val mediaFile = viewModel.imageList[index]
        mediaFile.delete()
        MediaScannerConnection.scanFile(
            requireContext(), arrayOf(mediaFile.absolutePath), null, null)
        viewModel.imageList.removeAt(index)

        fragmentBinding.viewPagerGallery.apply {
            offscreenPageLimit = 2
            adapter = ImagePagerAdapter(this@GalleryFragment)
            TabLayoutMediator( fragmentBinding.galleryFragmentViewPagerTab, this) { tab, position ->
                tab.text = (position+1).toString()
            }.attach()
        }


        if(viewModel.imageList.size == 0) {
           fragmentBinding.fabBtnGalleryDelete.isVisible = false

            if(viewModel._isGallerySelfCall) {
                initSelfCallData()
                findNavController().navigateUp()
            }

        }



    }



}