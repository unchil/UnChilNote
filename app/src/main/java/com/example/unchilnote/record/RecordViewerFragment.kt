package com.example.unchilnote.record

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.unchilnote.R
import com.example.unchilnote.data.RepositoryProvider
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.databinding.RecordViewerFragmentBinding
import com.example.unchilnote.utils.*
import com.example.unchilnote.write.WriteFragmentDirections
import com.google.android.material.snackbar.Snackbar
import java.io.File


class RecordViewerFragment internal constructor(): Fragment() {

    private val logTag = RecordViewerFragment::class.java.name
    private val shareViewModel: SharedViewModel by activityViewModels()
    private val parentViewModel: RecordViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var _fragmentBinding:  RecordViewerFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!

    var nowPlayer: MediaPlayer? = null
   // var beforePlayingFile: String? = null

    var isPaly = false
    var isConvert = false

    private  lateinit var recordFile: String



    companion object {
        fun newInstance(recordFile: File) =
            RecordViewerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FILENAME, recordFile.absolutePath)
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments ?: return
        recordFile = args.getString(ARG_FILENAME)!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _fragmentBinding = RecordViewerFragmentBinding.inflate(inflater, container, false)


        fragmentBinding.recordSeekBarPlaying.apply {
            progress = 0
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekbar: SeekBar, position: Int, fromUser: Boolean) {
                    if(fromUser) {
                        seekbar.progress = position
                        nowPlayer?.seekTo(position)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    //         viewModel.player!!.seekTo(viewModel.player!!.currentPosition)
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    //        viewModel.player!!.seekTo(viewModel.player!!.currentPosition)
                }
            })
        }
        if( File(recordFile).parent == parentViewModel.recordDirectory )
            fragmentBinding.recordEditTextTextMultiLine.isEnabled = false

        fragmentBinding.recordEditTextTextMultiLine.setText(parentViewModel.recordTextMap[File(recordFile).name])



        fragmentBinding.recordToolbar.setOnMenuItemClickListener{
            when(it.itemId) {
                R.id.menu_record_play -> {
                    isPaly = !isPaly
                    if(isPaly) {
                        it.setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PAUSE_IMAGE))
                        startPlaying(it)
                    }
                    else {
                        it.setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PLAY_IMAGE))
                        pausePlaying()
                    }
                    true
                }
                R.id.menu_record_convert -> {
                    isConvert = !isConvert
                    setEditText()
                    true
                }

                else -> { false }
            }
        }

        return fragmentBinding.root
    }



    private fun setEditText( ) {
        parentViewModel.apply {
            _snackbar.value = getString(R.string.SnackBarMessage_Record_Convert)
            BizLogic.launchDataLoad( viewModelScope, _progressBarVisible, _snackbar) {
                fragmentBinding.recordEditTextTextMultiLine.apply {
                    val convText = getConvText(recordFile, requireContext())
                    val newText = this.text.toString()  + convText
                    setText(newText)
                    shareViewModel.replaceRecordTextMap(File(recordFile).name, newText)
                }
            }
        }
    }

    override fun onDestroyView() {
        shareViewModel.replaceRecordTextMap(
            File(recordFile).name, fragmentBinding.recordEditTextTextMultiLine.text.toString()
        )

        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments ?: return
    }

    fun startPlaying ( item: MenuItem) {

        nowPlayer = MediaPlayer().apply {
            setDataSource(requireContext(), File(recordFile).toUri())
            prepare()


            if(shareViewModel.playListStat.containsKey(recordFile)) {
                seekTo( shareViewModel.playListStat[recordFile]?:0 )
                start()
            } else {
                shareViewModel.playListStat[recordFile] = 0
                start()
            }

            fragmentBinding.recordSeekBarPlaying.max = duration
            fragmentBinding.recordSeekBarPlaying.progress = currentPosition

            fragmentBinding.recordTextViewPlayingTime.text =
                ConvFormat.makeMediaPlayerDurationTime(
                    fragmentBinding.recordSeekBarPlaying.progress,
                    fragmentBinding.recordSeekBarPlaying.max
                )

        }

        val mediaSeekBarHandler = Handler(Looper.getMainLooper())

        val updatePlayTime: Runnable = object : Runnable {
            override fun run() {

                val it: MenuItem = item

                val seekbar: SeekBar = fragmentBinding.recordSeekBarPlaying
                val textview: TextView = fragmentBinding.recordTextViewPlayingTime

                nowPlayer?.apply {

                    seekbar.max = duration
                    seekbar.progress = currentPosition
                    textview.text =
                        ConvFormat.makeMediaPlayerDurationTime(
                            fragmentBinding.recordSeekBarPlaying.progress,
                            fragmentBinding.recordSeekBarPlaying.max)

                    if( duration !=0 && duration == currentPosition) {
                        it.setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, PLAY_IMAGE))
                        isPaly = false
                        shareViewModel.playListStat.replace(recordFile, 0)
                        releasePlayer()
                    }
                }
                mediaSeekBarHandler.postDelayed(this, 100)
            }
        }

        mediaSeekBarHandler.postDelayed(updatePlayTime, 100)

    }




    private fun releasePlayer() {

        nowPlayer?.apply {
            stop()
            reset()
            release()
        }
        nowPlayer = null
    }

    private fun pausePlaying(){
        nowPlayer?.apply {
            pause()
            shareViewModel.playListStat.replace(recordFile, currentPosition)
            reset()
            release()
        }
        nowPlayer = null
    }




}

