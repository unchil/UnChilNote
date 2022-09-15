package com.example.unchilnote.camera

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.window.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.unchilnote.R
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.databinding.CameraContainerBinding
import com.example.unchilnote.databinding.CameraFragmentBinding
import com.example.unchilnote.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class CameraFragment : Fragment() {
    private val logTag = CameraFragment::class.java.name
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _fragmentBinding:  CameraFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!
    var cameraContainerBinding: CameraContainerBinding? = null

    lateinit var windowManager: WindowManager
    lateinit var outputDirectory: File
    lateinit var displayManager: DisplayManager
    var displayId: Int = -1
    var checkedMeteringButtonId: Int = -1
    var cameraExecutor  = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private var camera: Camera? = null


    val displayListener = object: DisplayManager.DisplayListener {
        override fun onDisplayAdded(_displayId: Int) = Unit
        override fun onDisplayRemoved(_displayId: Int) = Unit
        override fun onDisplayChanged(_displayId: Int) = view?.let {
            if (_displayId == displayId) {
                Log.d(logTag, "Rotation changed: ${it.display.rotation}")
                imageCapture?.targetRotation = it.display.rotation
                imageAnalyzer?.targetRotation = it.display.rotation
            }
        } ?: Unit
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = CameraFragmentBinding.inflate(inflater, container, false)

        outputDirectory = FileUtils.getOutputDirectory(requireContext(), PHOTO, isCache = true)
        displayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener( displayListener, null)


        windowManager = WindowManager(requireContext())

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentBinding.viewFinder.post {
            setUpCameraUI()
            cameraExecutor = Executors.newSingleThreadExecutor()
            displayId = fragmentBinding.viewFinder.display.displayId
            setUpCamera()
            setUpPreviewViewTouchListener()
        }
    }

    override fun onDestroyView() {
        _fragmentBinding = null
        super.onDestroyView()

        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        bindCameraUseCases()
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProvider = cameraProviderFuture.get()
        cameraProviderFuture.addListener( {bindCameraUseCases()},
            ContextCompat.getMainExecutor(requireContext()) )
    }

    private fun getMeteringFlag() :Int {
        return when(checkedMeteringButtonId) {
            cameraContainerBinding?.radioBtnAf?.id -> FocusMeteringAction.FLAG_AF
            cameraContainerBinding?.radioBtnAe?.id -> FocusMeteringAction.FLAG_AE
            cameraContainerBinding?.radioBtnAwe?.id -> FocusMeteringAction.FLAG_AWB
            else -> FocusMeteringAction.FLAG_AF
        }
    }
    private fun setUpPreviewViewTouchListener() {
        fragmentBinding.viewFinder.setOnTouchListener { view, motionEvent ->
            return@setOnTouchListener when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.performClick()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val factory =  fragmentBinding.viewFinder.meteringPointFactory
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)
                    val action = FocusMeteringAction.Builder(point, getMeteringFlag())
                        .setAutoCancelDuration(AUTO_CANCEL_DURATION, TimeUnit.SECONDS)
                        .build()
                    camera?.cameraControl?.startFocusAndMetering(action)
                    view.performClick()
                    true
                }
                else -> false
            }
        }
    }


    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)

        return if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE))
            AspectRatio.RATIO_4_3
        else AspectRatio.RATIO_16_9

    }


    private fun bindCameraUseCases() {
        val viewFinder: PreviewView = fragmentBinding.viewFinder

        val metrics = windowManager.getCurrentWindowMetrics().bounds
        Log.d(logTag, "Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Log.d(logTag, "Preview aspect ratio: ${screenAspectRatio}")

        val rotation = viewFinder.display.rotation

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
                    //Log.d(logTag, "Average luminosity: $luma")
                })
            }

        cameraProvider.unbindAll()

        preview.setSurfaceProvider(viewFinder.surfaceProvider)

        try {
            camera = cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer)

        } catch (e: Exception) {
            Log.d(logTag, "Use case binding failed:[${e.message}]")
        }


    }

    private fun setUpCameraUI() {

        cameraContainerBinding?.root?.let {
            fragmentBinding.root.removeView(it)
        }
        cameraContainerBinding = CameraContainerBinding.inflate(
            LayoutInflater.from(requireContext()),
            fragmentBinding.root,
            true

        ).also { cameraContainerBinding ->
            checkedMeteringButtonId = cameraContainerBinding.radioGrpMetering.checkedRadioButtonId

            lifecycleScope.launch(Dispatchers.Main) {
                outputDirectory.listFiles { file ->
                    arrayOf(JPG_CHECK).contains(file.extension.uppercase(Locale.ROOT))
                }?.maxOrNull()?.let {
                    setGalleryThumbnail(Uri.fromFile(it))
                }
            }

            cameraContainerBinding.imgBtnShutter.setOnClickListener {
                onClickShutter()
            }
            cameraContainerBinding.imgBtnLensSwitch.setOnClickListener {
                onClickLensSwitch()
            }
            cameraContainerBinding.imgBtnGallery.setOnClickListener {
                onClickGallery()
            }

            /*
            cameraContainerBinding.imgBtnReplyAll.setOnClickListener {
                findNavController().navigateUp()
            }
            */
            cameraContainerBinding.imgBtnFlash.setOnClickListener {
                onClickFlash( it as ImageButton)
            }
            cameraContainerBinding.radioGrpMetering.setOnCheckedChangeListener { radioGroup, _ ->
                checkedMeteringButtonId = radioGroup.checkedRadioButtonId
            }
        }
    }

    private fun onClickLensSwitch() {
        lensFacing = if ( CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        bindCameraUseCases()
    }

    private fun onClickGallery() {

        if(outputDirectory.listFiles()?.isNotEmpty() == true) {
            /* val args = Bundle().apply { putString(ARG_ROOTDIR, outputDirectory.absolutePath) }
            owner.findNavController().navigate(R.id.action_cameraFragment_to_galleryFragment, args) */
        //    shareViewModel.parentFragmentName = this::class.java.name
            sharedViewModel.apply {
                selfCallMap.put(outputDirectory.absolutePath, true)
            }

            findNavController().navigate(
                CameraFragmentDirections.actionCameraFragmentToGalleryFragment(outputDirectory.absolutePath))
        }
    }




    fun onClickFlash(flashBtn: ImageButton){

        camera?.cameraInfo?.hasFlashUnit()?.let {
            when(it) {
                true -> {
                    when (camera?.cameraInfo?.torchState?.value) {
                        TorchState.ON -> {
                            camera?.cameraControl?.enableTorch(false)
                            flashBtn.setImageResource(
                                ResourceUtils.getResourceID(
                                    this::onClickFlash.name,
                                    FLASH_OFF_IMAGE
                                )
                            )
                        }
                        TorchState.OFF -> {
                            camera?.cameraControl?.enableTorch(true)
                            flashBtn.setImageResource(
                                ResourceUtils.getResourceID(
                                    this::onClickFlash.name,
                                    FLASH_ON_IMAGE
                                )
                            )
                        }
                    }
                }
                false -> flashBtn.setImageResource(
                    ResourceUtils.getResourceID(
                        this::onClickFlash.name,
                        FLASH_NO_IMAGE
                    )
                )
            }
        }
    }

    private fun onClickShutter() {

        imageCapture?.let {

            val photoFile =
                FileUtils.createFile(outputDirectory, FILENAME_FORMAT, EXTENSION_PHOTO)

            val metadata = ImageCapture.Metadata().apply {
                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            it.takePicture(outputOptions, cameraExecutor, onImageSaveCallback)

            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Display flash animation to indicate that photo was captured
                fragmentBinding.root.postDelayed({
                    fragmentBinding.root.foreground = ColorDrawable(Color.WHITE)
                    fragmentBinding.root.postDelayed(
                        { fragmentBinding.root.foreground = null },
                        ANIMATION_FAST_MILLIS
                    )
                }, ANIMATION_SLOW_MILLIS)
            }
        }
    }




    val onImageSaveCallback = object: ImageCapture.OnImageSavedCallback {

        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

            Log.d(logTag, "Photo capture succeeded: ${outputFileResults.savedUri}")
            // val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
            val savedUri = outputFileResults.savedUri

            savedUri?.let {
                // We can only change the foreground Drawable using API level 23+ API
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Update the gallery thumbnail with latest picture taken
                    lifecycleScope.launch(Dispatchers.Main) {
                        setGalleryThumbnail(it)
                    }
                }
                // Implicit broadcasts will be ignored for devices running API level >= 24
                // so if you only target API level 24+ you can remove this statement
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    requireActivity().sendBroadcast(
                        Intent(android.hardware.Camera.ACTION_NEW_PICTURE, savedUri)
                    )
                }

                // If the folder selected is an external media directory, this is
                // unnecessary but otherwise other apps will not be able to access our
                // images unless we scan them using [MediaScannerConnection]
                val mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(it.toFile().extension)

                MediaScannerConnection.scanFile(
                    requireContext(),
                    arrayOf(it.toFile().absolutePath),
                    arrayOf(mimeType)
                ) { _, uri ->
                    Log.d(logTag, "Image capture scanned into media store: $uri")
                }
            }
        }

        override fun onError(e: ImageCaptureException) {
            Log.d(logTag, "Photo capture failed: ${e.message}", e)
        }

    }


    private fun setGalleryThumbnail(uri: Uri) {
        cameraContainerBinding?.let {
            val galleryBtn = it.imgBtnGallery
            galleryBtn.post {
                galleryBtn.setPadding(
                    resources.getDimension(R.dimen.stroke_small).toInt())
            }
            try {
                Glide.with(this)
                    .load(uri)
                    .override(THUMBNAIL_IMG_SIZE_W, THUMBNAIL_IMG_SIZE_H)
                    .placeholder(ResourceUtils.getResourceID(PUBLIC_CALLER, LOADING_IMAGE))
                    .error(ResourceUtils.getResourceID(PUBLIC_CALLER, ERROR_IMAGE))
                    .apply(RequestOptions.centerCropTransform())
                    .into(galleryBtn)

            } catch (e: Exception) {
                Log.d(logTag, "setGalleryThumbnail Glide failed:[${e.message}]")
            }

        }
    }


    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: Analyzer method implementation must
         * call image.close() on received images when finished using them. Otherwise, new images
         * may not be received or the camera may stall, depending on back pressure setting.
         *
         */
        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }

            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()

            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }


}