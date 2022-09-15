package com.example.unchilnote.gallery


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.unchilnote.R
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.ResourceUtils.Companion.getResourceID
import java.io.File

class ImageViewer internal constructor() : Fragment() {
    private val logTag = ImageViewer::class.java.name
    private lateinit var imageView: ImageView
    private var scaleTouchCheck = false
    private var scaleFactor = 1.0f

    private var _scaleGestureDetector: ScaleGestureDetector? = null
    private val scaleGestureDetector get() = _scaleGestureDetector!!


    private var velocityTracker: VelocityTracker? = null
    private var eventTouchCheck = false
    private var startX = 0f
    private var startY = 0f

    companion object {
        fun newInstance( parentName: String,  logTag: String, imageFile: File) = ImageViewer().apply {

            /* arguments = Bundle().apply {
                putString(ARG_FILENAME, imageFile.absolutePath)
                putString(ARG_LOGTAG, logTag)
                putString(ARG_PARENT, parentName)
            } */
            arguments = ImageViewerArgs(
                fileName = imageFile.absolutePath,
                logTag = logTag,
                parentfragment = parentName
            ).toBundle()


        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        imageView = ImageView(context)

        imageView.setOnClickListener {
            val GalleryFragment = parentFragment as GalleryFragment
            GalleryFragment.apply {
                if(!viewModel._isGallerySelfCall) {
                    setSelfCallData()
                    it.findNavController().navigate(R.id.galleryFragment,  requireArguments())
                }
            }
        }

        return imageView
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
/*
        val parentName = args.getString(ARG_PARENT) ?: return
        val logTag = args.getString(ARG_LOGTAG) ?: Thread.currentThread().name
        val resource = args.getString(ARG_FILENAME)?.let { File(it) }
            ?: getResourceID(PUBLIC_CALLER, ERROR_IMAGE)
*/
        val parentName = ImageViewerArgs.fromBundle(args).parentfragment
        val logTag = ImageViewerArgs.fromBundle(args).logTag
        val resource = ImageViewerArgs.fromBundle(args).fileName.let { File(it) }

        try {
            Glide.with(view)
                .load(resource)
                .placeholder(getResourceID(PUBLIC_CALLER, LOADING_IMAGE))
                .error(getResourceID(PUBLIC_CALLER, ERROR_IMAGE))
                .apply(RequestOptions.centerCropTransform())
                .into(view as ImageView)

        } catch (e: Exception) {
            Log.d(logTag, "${this::onViewCreated.name} Glide failed:[${e.message}]")
        }

/*
        _scaleGestureDetector = ScaleGestureDetector(context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleTouchCheck = true
                    scaleFactor *= detector.scaleFactor
                    // 최소 0.5, 최대 2배
                    scaleFactor = max(SCALE_MAX, min(scaleFactor, SCALE_MIN))
                    imageView.scaleX = scaleFactor
                    imageView.scaleY = scaleFactor
                    return scaleTouchCheck
                }

                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    scaleTouchCheck = true
                    return scaleTouchCheck
                }

                override fun onScaleEnd(detector: ScaleGestureDetector?) {
                    scaleTouchCheck = false
                }
        })


        imageView.setOnTouchListener( object : View.OnTouchListener {

            val owner = when(parentName) {
                ::GalleryFragment.name -> parentFragment as GalleryFragment
                else -> parentFragment as GalleryFragment
            }

            @SuppressLint("Recycle")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                eventTouchCheck = false
                event?.let { it ->
                    scaleGestureDetector.onTouchEvent(it)

                    when (it.getActionMasked()) {
                        MotionEvent.ACTION_DOWN -> {
                            velocityTracker?.clear()
                            velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                            velocityTracker?.addMovement(it)
                            startX = it.x
                            startY = it.y
                            eventTouchCheck = true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            var chkValue = 0
                            velocityTracker?.apply {
                                addMovement(it)
                                computeCurrentVelocity(VELOCITY_CURRENT)
                                chkValue = Math.abs(this.getXVelocity(it.getPointerId(it.actionIndex)).toInt())
                            }
                            owner.galleryFragmentBinding.viewPagerGallery.isUserInputEnabled =
                                chkValue >= VELOCITY_ENABLE_SWIPE_MIN

                            imageView.x = imageView.x + (it.x - startX)
                            imageView.y = imageView.y + (it.y - startY)
                            eventTouchCheck = true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            owner.galleryFragmentBinding.viewPagerGallery.isUserInputEnabled = true
                            velocityTracker?.recycle()
                            velocityTracker = null
                            eventTouchCheck = false
                        }
                    }
                }
                return eventTouchCheck
            } // onTouch
        })
*/

    } // onCreateView



}