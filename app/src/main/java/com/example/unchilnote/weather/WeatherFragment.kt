package com.example.unchilnote.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.unchilnote.MemoViewModelFactory
import com.example.unchilnote.data.dataset.*
import com.example.unchilnote.databinding.WeatherFragmentBinding
import com.example.unchilnote.utils.MessageUtils
import com.example.unchilnote.utils.ResourceUtils
import com.example.unchilnote.utils.WEATHER_CALLER
import com.google.android.material.snackbar.Snackbar

class WeatherFragment : Fragment() {

    private val logTag = WeatherFragment::class.java.name

    lateinit var viewModel: WeatherViewModel
    private var _fragmentBinding: WeatherFragmentBinding? = null
    val fragmentBinding get() = _fragmentBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragmentBinding = WeatherFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this, MemoViewModelFactory( requireContext().applicationContext)).get(
            WeatherViewModel::class.java)
        viewModel.apply {
            parentFragmentName = requireParentFragment()::class.java.name
            getCurrentLocation(true, requireContext())
        }


        viewModel.apply {
            progressBarVisible.observe(viewLifecycleOwner){
                fragmentBinding.weatherProgressBar.visibility =
                    if(it) View.VISIBLE else View.GONE
            }
            snackbar.observe(viewLifecycleOwner) {
                it?.let {
                    MessageUtils.msgToSnackBar(
                        fragmentBinding.root, null, it, Snackbar.LENGTH_LONG, false
                    )
                    clearSnackbarMessage()
                }
            }
            currentWeather.observe(viewLifecycleOwner) {
                weatherDataBinding( fragmentBinding , it)
            }

        }


        return fragmentBinding.root
    }


    private fun weatherDataBinding(fragmentBinding: WeatherFragmentBinding, item: CURRENTWEATHER_TBL){

        fragmentBinding.weatherTextViewHeadline.apply {
            text = item.toTextHeadLine()
        }

        fragmentBinding.weatherImageViewIcon.apply {
            setImageResource(ResourceUtils.getResourceID(WEATHER_CALLER, item.icon))
        }

        fragmentBinding.weatherTextViewSun.apply {
            text = item.toTextSun()
        }

        fragmentBinding.weatherTextViewTemp.apply {
            text = item.toTextTemp()
        }

        fragmentBinding.weatherTextViewWeather.apply {
            text = item.toTextWeather()
        }

        fragmentBinding.detailWeatherTextViewWind.apply {
            text = item.toTextWind()
        }


    }


}