package com.example.unchilnote.googlemap

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.example.unchilnote.MemoViewModelFactory
import com.example.unchilnote.R
import com.example.unchilnote.data.SharedViewModel
import com.example.unchilnote.data.dataset.CURRENTLOCATION_TBL
import com.example.unchilnote.data.dataset.MarkerItem
import com.example.unchilnote.data.dataset.toLatLng
import com.example.unchilnote.databinding.MapsFragmentBinding
import com.example.unchilnote.utils.FileUtils
import com.example.unchilnote.utils.MAP_LAYER_MENU
import com.example.unchilnote.utils.MessageUtils.Companion.msgToSnackBar
import com.example.unchilnote.utils.PUBLIC_CALLER
import com.example.unchilnote.utils.ResourceUtils.Companion.getResourceID
import com.example.unchilnote.utils.SNAPSHOT_DEFAULT
import com.example.unchilnote.write.WriteFragment
import com.example.unchilnote.write.WriteViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.utils.collection.addMarker


class MapsFragment : Fragment() , OnMapsSdkInitializedCallback {
//class MapsFragment : Fragment(){

    private val logTag = MapsFragment::class.java.name
    lateinit var viewModel: MapsViewModel
    private val shareViewModel: SharedViewModel by activityViewModels()
    private var _fragmentBinding:  MapsFragmentBinding? = null
    private lateinit var currentLocation: CURRENTLOCATION_TBL

    val fragmentBinding get() = _fragmentBinding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(requireContext().applicationContext, MapsInitializer.Renderer.LATEST, this)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragmentBinding = MapsFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this,
            MemoViewModelFactory( requireContext().applicationContext))
            .get( MapsViewModel::class.java)

        if( savedInstanceState == null && shareViewModel.googleMap == null) {

            lifecycle.coroutineScope.launchWhenCreated {

                val mapFragment =  childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
                mapFragment.awaitMap().let { map ->
                    shareViewModel.googleMap = map
                    initMap(map)
                }
            }

        }

        if(savedInstanceState != null) {
            lifecycle.coroutineScope.launchWhenCreated {

                val mapFragment = childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment

                mapFragment.awaitMap().let { map ->
                    shareViewModel.googleMap = map
                    map.mapType = shareViewModel.googleMapType
                    initMap(map)
                }
            }
        }

        fragmentBinding.imgBtnMapsLayer.setOnClickListener {
            showMenu( it, getResourceID(PUBLIC_CALLER, MAP_LAYER_MENU))
        }

        return fragmentBinding.root
    }

    private fun showMenu(view: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            shareViewModel.googleMapType = when(menuItem.itemId) {
                R.id.menu_map_layer_normal -> GoogleMap.MAP_TYPE_NORMAL
                R.id.menu_map_layer_hybrid -> GoogleMap.MAP_TYPE_HYBRID
                R.id.menu_map_layer_terrain -> GoogleMap.MAP_TYPE_TERRAIN
                else -> GoogleMap.MAP_TYPE_NONE
            }
            shareViewModel.googleMapZoom = when(shareViewModel.googleMapType) {
                GoogleMap.MAP_TYPE_HYBRID, GoogleMap.MAP_TYPE_TERRAIN -> 14F
                else -> 18F
            }
            shareViewModel.googleMap?.let {
                it.mapType = shareViewModel.googleMapType
                it.moveCamera( CameraUpdateFactory.newLatLngZoom(
                    currentLocation.toLatLng(), shareViewModel.googleMapZoom)
                )
            }

            true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }


    private fun initMap(map: GoogleMap) {

        map.isMyLocationEnabled = true

        viewModel.currentLocation.observe(viewLifecycleOwner) { currentLocationTbl ->
            currentLocation = currentLocationTbl
            map.moveCamera( CameraUpdateFactory.newLatLngZoom(
                currentLocationTbl.toLatLng(), shareViewModel.googleMapZoom)
            )
        }

        when (requireParentFragment()) {
            is WriteFragment -> processWriteFragment(map)
            else -> showMapLayers(map)
        }
    }

    private fun processWriteFragment(map:GoogleMap) {
        val writeViewModel: WriteViewModel by viewModels(ownerProducer = { requireParentFragment() })
        FileUtils.clearDefaultFile(requireContext())
        writeViewModel.apply {
            if (this.isDrawingOnMap) {
                polyLineOptions.forEach {
                    map.addPolyline(it)
                }
            } else {
                map.setOnMapLoadedCallback {
                    makeSnapShot(SNAPSHOT_DEFAULT, fragment = null)
                }
            }
            googleMap = map
        }
    }

    private fun showMapLayers(map: GoogleMap) {
        // Shared object managers - used to support multiple layer types on the map simultaneously
        val markerManager = MarkerManager(map)
        addClusters(map, markerManager)
     //   addMarker(markerManager)
    }

    private fun addClusters(map: GoogleMap, markerManager: MarkerManager) {

        val clusterManager = ClusterManager<MarkerItem>( requireContext(), map, markerManager )

        map.setOnCameraIdleListener(clusterManager)

        clusterManager.addItems( viewModel.markerList )

        clusterManager.markerCollection.setOnMarkerClickListener { marker ->

            clusterManager.algorithm.items.forEachIndexed { index, myItem ->

                marker.title?.let { title ->
                    if( title == myItem.title ) {
                        viewModel.setMemoItem(index)
                        view?.let { view ->
                            myItem.desc?.let { desc ->
                                msgToSnackBar(view, null,
                                    desc, Snackbar.LENGTH_LONG, false )
                            }
                        }
                    }
                }
            }
            false
        }

        clusterManager.markerCollection.setOnInfoWindowClickListener {
            findNavController().navigate(
                MapsFragmentDirections.actionMapsFragmentToDetailFragment()
            )
        }

    }


    private fun addMarker(markerManager: MarkerManager) {
        // Unclustered marker - instead of adding to the map directly, use the MarkerManager
        val markerCollection: MarkerManager.Collection = markerManager.newCollection()
        viewModel.currentLocation.observe(viewLifecycleOwner) {
            markerCollection.addMarker {
                position(
                    LatLng( it.latitude.toDouble(),
                        it.longitude.toDouble()))
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                title("Current Location")
            }
        }

        view?.let {
            markerCollection.setOnMarkerClickListener { marker ->
                msgToSnackBar(
                    it,
                    null,
                    "MarkerManager Marker clicked: " + marker.position.toString(),
                    Snackbar.LENGTH_LONG,
                    false )
                false
            }
        }

    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
/*
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d( logTag, "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d( logTag, "The legacy version of the renderer is used.")
        }

 */
    }
}