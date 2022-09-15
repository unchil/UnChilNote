package com.example.unchilnote.write

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Point
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.unchilnote.MemoViewModelFactory
import com.example.unchilnote.R
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.databinding.WriteFragmentBinding
import com.example.unchilnote.gallery.GalleryFragment
import com.example.unchilnote.googlemap.MapsFragment
import com.example.unchilnote.record.RecordFragment
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.ResourceUtils.Companion.createTagChips
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout

class WriteFragment : Fragment()  {

    private val logTag = WriteFragment::class.java.name
    private val shareViewModel: SharedViewModel by activityViewModels()
    lateinit var viewModel: WriteViewModel
    private var  recordingFileName: String? = null
    private var _fragmentBinding:  WriteFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragmentBinding = WriteFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this, MemoViewModelFactory( requireContext().applicationContext)).get(
            WriteViewModel::class.java)

        viewModel.setGalleryFragmentArgs(requireContext())

        createTagChips(fragmentBinding.chipWriteFragment.chipGroup, requireContext())

        fragmentBinding.bottomWrite.bottomAppBar
            .replaceMenu(ResourceUtils
                .getResourceID(PUBLIC_CALLER, WRITE_MENU))

        setMenuProcess(null)

        fragmentBinding.bottomWrite.bottomAppBar.setOnMenuItemClickListener{
            when(it.itemId) {
                R.id.menu_write_photo -> {
                    this.findNavController()
                        .navigate(WriteFragmentDirections.actionWriteFragmentToCameraFragment())
                    true
                }
                else -> {
                    setMenuProcess(it)
                    true
                }
            }
        }

        fragmentBinding.writeFragmentMapFrame.setOnTouchListener( View.OnTouchListener(
            fun(view: View, event: MotionEvent): Boolean {
                drawMap(event)
                return true
            }) )


        if (shareViewModel.googleMap == null) {
            replaceContainer(GOOGLE_MAP)
            replaceContainer(SNAPSHOT)
        }



        viewModel.apply {
            snackbar.observe(viewLifecycleOwner) {
                it?.let {
                    MessageUtils.msgToSnackBar(
                        fragmentBinding.root, null, it, Snackbar_LENGTH, false
                    )
                    clearSnackbarMessage()
                }
            }
            progressBarVisible.observe(viewLifecycleOwner){
                fragmentBinding.writeProgressBar.visibility =
                    if(it) View.VISIBLE else View.GONE
            }
        }

        BizLogic.setBottomSheetBehavior(BottomSheetBehavior.from(fragmentBinding.writeFragmentBottomSheetTab))

        fragmentBinding.tabWriteFragment.writeTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    replaceContainer(it.text as String)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { }
            override fun onTabReselected(tab: TabLayout.Tab?) { }
        })

        fragmentBinding.bottomWrite.bottomFab.apply {
            contentDescription = SAVE
            setImageResource( ResourceUtils.getResourceID(PUBLIC_CALLER, SAVE))

            setOnClickListener {

                when (viewModel.isChipsDisplay) {
                    true -> {
                        viewModel.isChipsDisplay = !viewModel.isChipsDisplay
                        setTagsCollapsed()
                    }
                    false -> {
                        viewModel.saveMemo(requireContext(),  fragmentBinding.chipWriteFragment.chipGroup)
                        it.findNavController().navigate(WriteFragmentDirections.actionWriteFragmentToListFragment())
                    }
                }
            }
        }

        return fragmentBinding.root
    }



    @SuppressLint("MissingPermission")
    fun setMenuProcess(it: MenuItem?) {

        it?.let {
            viewModel.apply {
                when (it) {
                    fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_SECRET_IDX] -> {
                        isSecret = !isSecret
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
                    fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_PIN_IDX] -> {
                        isPin = !isPin
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
                    fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_RECORD_IDX] -> {
                        isOnRecording = !isOnRecording
                        when (isOnRecording) {
                            true -> {
                                startRecording()
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Record_On)
                                it.setIcon(
                                    ResourceUtils.getResourceID(
                                        PUBLIC_CALLER,
                                        MIC_OFF_IMAGE
                                    )
                                )
                            }
                            false -> {
                                stopRecording()
                                replaceContainer(RECORD)
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Record_Off)
                                it.setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, MIC_ON_IMAGE))
                            }
                        }
                    }
                    fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_SNAPSHOT_IDX] -> {
                        isDrawingOnMap = !isDrawingOnMap
                        googleMap?.uiSettings?.setAllGesturesEnabled(!isDrawingOnMap)
                        googleMap?.isMyLocationEnabled = !isDrawingOnMap

                        when (isDrawingOnMap) {
                            true -> {
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Drawing_On)
                                it.setIcon(
                                    ResourceUtils.getResourceID(
                                        PUBLIC_CALLER,
                                        SNAPSHOT_IMAGE
                                    )
                                )
                                fragmentBinding.writeFragmentMapFrame.visibility = View.VISIBLE
                            }
                            false -> {
                                makeSnapShot(SNAPSHOT_DRAWING, this@WriteFragment)
                                _snackbar.value =
                                    getString(R.string.SnackBarMessage_Drawing_Off)
                                it.setIcon(
                                    ResourceUtils.getResourceID(
                                        PUBLIC_CALLER,
                                        DRAWING_IMAGE
                                    )
                                )
                                fragmentBinding.writeFragmentMapFrame.visibility = View.INVISIBLE
                            }
                        }
                    }
                    fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_LABEL_IDX] -> {
                        isChipsDisplay = !isChipsDisplay
                        when (isChipsDisplay) {
                            true ->  setTagsExpended()
                            false -> setTagsCollapsed()
                        }
                    }
                    else -> {
                        return
                    }
                }
            }
        }

        if( it == null ) {
            viewModel.apply {

                when (isSecret) {
                    true -> {
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_SECRET_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, SECRET_LOCK_IMAGE))
                    }
                    false -> {
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_SECRET_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, SECRET_OPEN_IMAGE))
                    }
                }
                when (isPin) {
                    true -> {
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_PIN_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PIN_ON_IMAGE))
                    }
                    false -> {
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_PIN_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PIN_OFF_IMAGE))
                    }
                }

                when (isOnRecording) {
                    true -> {
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_RECORD_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, MIC_OFF_IMAGE))
                    }
                    false -> {
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_RECORD_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, MIC_ON_IMAGE))
                    }
                }

                when (isDrawingOnMap) {
                    true -> {
                        fragmentBinding.writeFragmentMapFrame.visibility = View.VISIBLE
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_SNAPSHOT_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, SNAPSHOT_IMAGE))
                    }
                    false -> {
                        fragmentBinding.writeFragmentMapFrame.visibility = View.INVISIBLE
                        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_SNAPSHOT_IDX]
                            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, DRAWING_IMAGE))
                    }
                }

                when(isChipsDisplay) {
                    true -> fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_LABEL_IDX]
                        .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, CHECK))
                    false -> fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_LABEL_IDX]
                        .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, LABEL_IMAGE))
                }
            }
        }
    }

    private fun setTagsCollapsed() {

        fragmentBinding.bottomWrite.bottomFab.apply {
            contentDescription = SAVE
            setImageResource(ResourceUtils.getResourceID(PUBLIC_CALLER, SAVE))
        }

        BottomSheetBehavior.from(fragmentBinding.writeFragmentBottomSheetChip).state =
            BottomSheetBehavior.STATE_COLLAPSED
        viewModel._snackbar.value =
            getString(R.string.SnackBarMessage_Label_Ok)

        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_LABEL_IDX]
            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, LABEL_IMAGE))
    }

    private fun setTagsExpended() {
        fragmentBinding.bottomWrite.bottomFab.apply {
            contentDescription = LABEL
            setImageResource( ResourceUtils.getResourceID(PUBLIC_CALLER, LABEL_IMAGE))
        }

        BottomSheetBehavior.from(fragmentBinding.writeFragmentBottomSheetChip).state =
            BottomSheetBehavior.STATE_EXPANDED
        viewModel._snackbar.value =
            getString(R.string.SnackBarMessage_Label_Set)

        fragmentBinding.bottomWrite.bottomAppBar.menu[WRITE_MENU_LABEL_IDX]
            .setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, CHECK))

    }

    fun replaceContainer(containerName: String) {
        childFragmentManager.commit {
            when(containerName) {
                GOOGLE_MAP -> {
                    replace<MapsFragment>( fragmentBinding.writeFragmentGoogleMapFragmentContainer.id )
                 }
                SNAPSHOT -> {
                    replace<GalleryFragment>( fragmentBinding.tabWriteFragment.writeTabFragmentContainer.id,
                            args = viewModel.galleryFragmentArgs_Map)
                    fragmentBinding.tabWriteFragment.writeTab.getTabAt(TAB_MAP_INDEX)?.select()
                }
                RECORD -> {
                    replace<RecordFragment>( fragmentBinding.tabWriteFragment.writeTabFragmentContainer.id,
                            args = viewModel.recordFragmentArgs_record)
                    fragmentBinding.tabWriteFragment.writeTab.getTabAt(TAB_RECORD_INDEX)?.select()
                }
                PHOTO -> {
                    replace<GalleryFragment>( fragmentBinding.tabWriteFragment.writeTabFragmentContainer.id,
                            args = viewModel.galleryFragmentArgs_Photo)
                    fragmentBinding.tabWriteFragment.writeTab.getTabAt(TAB_PHOTO_INDEX)?.select()
                }
            }
            setReorderingAllowed(true)
        }
    }

    fun drawPolylineToMap(latLng: LatLng){
        viewModel.apply {
            googleMap?.let {
                val options =  PolylineOptions().width(5F).color(Color.BLUE).geodesic(true)
                _latLngList.add(latLng)
                options.addAll(_latLngList)
                polyLineOptions.add(options)


                it.addPolyline(options)
                _latLngList.clear()
                _latLngList.add(latLng)

            }
        }
    }

    fun drawMap( event: MotionEvent){


        viewModel.apply {
            googleMap?.let {
                if(isDrawingOnMap) {
                    var x = Math.round(event.x)
                    var y = Math.round(event.y)

                    var latLng = it.projection.fromScreenLocation(Point(x, y))

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            _latLngList.clear()
                            _latLngList.add(latLng)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            drawPolylineToMap( latLng)
                        }
                        MotionEvent.ACTION_UP -> {

                        }
                    }
                }
            }
        }
    }

    fun startRecording() {
        val recordingFile = FileUtils.createFile(viewModel.cacheRecordFolder, FILENAME_FORMAT, EXTENSION_RECORD)
        recordingFileName = recordingFile.name
        viewModel.apply {
            _progressBarVisible.value = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewModel.recorder = MediaRecorder(requireContext()).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                setOutputFile(recordingFile)
                prepare()
                start()
            }
        } else {
            viewModel.recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setOutputFormat(MediaRecorder.OutputFormat.OGG)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                }else {
                    setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                    setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                }

                setOutputFile(recordingFile)
                prepare()
                start()
            }
        }
    }

    fun stopRecording() {
        viewModel.apply {
            _progressBarVisible.value = false
            recordingFileName?.let {
                putRecordTextMap(it, ConvFormat.makeRecordText(it))
                recordingFileName = null
            }
            recorder.apply {
                stop()
                reset()
                release()
            }
        }
    }



}