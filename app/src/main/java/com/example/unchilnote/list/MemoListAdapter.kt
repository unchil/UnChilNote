package com.example.unchilnote.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.unchilnote.R
import com.example.unchilnote.data.dataset.MEMO_TBL
import com.example.unchilnote.databinding.ItemBinding
import com.example.unchilnote.utils.*
import com.example.unchilnote.utils.FileUtils.Companion.getOutputDirectory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class MemoListAdapter (/*private val onClickListener: OnClickListener */) :  ListAdapter<MEMO_TBL,
        MemoListAdapter.ItemViewHolder>( DiffCallback ) {

    private val logTag = MemoListAdapter::class.java.name
    private lateinit var currentItem : MEMO_TBL

    companion object DiffCallback : DiffUtil.ItemCallback<MEMO_TBL>() {
        override fun areItemsTheSame(oldItem: MEMO_TBL, newItem: MEMO_TBL): Boolean {
            return oldItem === newItem
        }
        override fun areContentsTheSame(oldItem: MEMO_TBL, newItem: MEMO_TBL): Boolean {
            return oldItem.id == newItem.id
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val holder = ItemViewHolder( ItemBinding.inflate(
            LayoutInflater.from(parent.context)), logTag)

        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        currentItem = getItem(position)
        holder.bind(currentItem)

        /*
        holder.itemView.setOnClickListener {
          currentItem = getItem(position)
          FragmentManager.findFragment<ListFragment>(it).viewModel.setMemoItem(currentItem)
          onClickListener.onClick(isSecret = currentItem.isSecret)
        }
        */

        holder.binding.imageViewItemMap.setOnClickListener {
            currentItem = getItem(position)
            val listFragment = FragmentManager.findFragment<ListFragment>(it)

            listFragment.apply {
                viewModel.setMemoItem(currentItem)

                when(currentItem.isSecret) {
                    true -> {
                        viewModel.biometricCheckType = BIOMETRIC_CHECK_TYPE_VIEW
                        getBiometricPrompt().authenticate(BizLogic.getBiometricPromptInfo(it.resources))
                    }
                    false -> {
                        findNavController().navigate(ListFragmentDirections.actionListFragmentToDetailFragment())
                    }
                }
            }
        }

        holder.binding.itemBack.imgBtnShare.setOnClickListener {
            currentItem = getItem(position)
            val listFragment = FragmentManager.findFragment<ListFragment>(it)
            listFragment.apply {
                when (currentItem.isSecret) {
                    true -> {
                        viewModel.biometricCheckType = BIOMETRIC_CHECK_TYPE_SHARE
                        viewModel.setMemoItem( currentItem )
                        getBiometricPrompt().authenticate(BizLogic.getBiometricPromptInfo(resources = it.resources))
                    }
                    false -> {
                        viewModel.setMemoItem( currentItem ).apply {
                            // Waiting for setting Memo Item in Coroutinescope
                            Thread.sleep(500L)
                        }
                        shareItem( viewModel, it.context )
                    }
                }
            }
        }


        holder.binding.itemBack.imgBtnDelete.setOnClickListener {

            currentItem = getItem(position)
            val listFragment = FragmentManager.findFragment<ListFragment>(it)
            listFragment.apply {
                viewModel.setMemoItem(currentItem)
                when(currentItem.isSecret) {
                    true -> {
                        viewModel.biometricCheckType = BIOMETRIC_CHECK_TYPE_DELETE
                        getBiometricPrompt().authenticate(BizLogic.getBiometricPromptInfo(resources = it.resources))
                    }
                    false -> {
                        deleteItem(viewModel, it.context )
                    }
                }
            }
        }

    }

    class ItemViewHolder (val binding: ItemBinding , val logTag: String):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MEMO_TBL) {

            when(item.isSecret) {
                true -> binding.imageViewItemIsSecret.setImageResource(ResourceUtils.getResourceID(
                    PUBLIC_CALLER, SECRET_LOCK_IMAGE ))
                false -> binding.imageViewItemIsSecret.setImageResource(ResourceUtils.getResourceID(
                    PUBLIC_CALLER, SECRET_OPEN_IMAGE))
            }

            when(item.isPin) {
                true -> binding.imageViewItemPinDrop.setImageResource(ResourceUtils.getResourceID(
                    PUBLIC_CALLER, PIN_ON_IMAGE))
                false -> binding.imageViewItemPinDrop.visibility = View.INVISIBLE
            }

            binding.textViewItemWriteTime.text = item.title

            binding.textViewItemAttachCnt.text = item.desc

            binding.textViewItemTag.text = item.snippets

            val outputDirectory =  getOutputDirectory(binding.imageViewItemMap.context, SNAPSHOT, false)

            CoroutineScope(Dispatchers.Main).launch {
                outputDirectory.listFiles { file ->
                    file.name == item.snapshot
                }?.first()?.let {
                    try {
                        Glide.with(binding.root)
                            .load(Uri.fromFile(it))
                            .override(LIST_IMG_SIZE_W, LIST_IMG_SIZE_H)
                            .placeholder(ResourceUtils.getResourceID(PUBLIC_CALLER, LOADING_IMAGE))
                            .error(ResourceUtils.getResourceID(PUBLIC_CALLER, ERROR_IMAGE))
                            .apply(RequestOptions.centerCropTransform())
                            .into(binding.imageViewItemMap )

                    } catch (e: Exception) {
                        Log.d(logTag, "Glide failed:[${e.message}]")
                    }
                }
            }
        }
    }

    fun deleteItem(viewModel: ListViewModel, context: Context ) {
        MaterialAlertDialogBuilder(context)
            .apply {
                setIcon(ResourceUtils.getResourceID(PUBLIC_CALLER, ALERT_IMAGE))
                setTitle(context.getString(R.string.Dialog_Delete_Title))
                setMessage(context.getString(R.string.Dialog_Delete_Message))
                setNeutralButton(CANCEL) { _, _ -> }
                setPositiveButton(OK) { _, _ -> viewModel.deleteMemo(currentItem, context) }

            }.create().show()
    }

     fun shareItem(viewModel: ListViewModel, context: Context) {

        val subject = currentItem.title

        var memoText = currentItem.desc + "\n" + currentItem.snippets + "\n\n"

         viewModel.getMemoFileTbl().filter {
            it.type == RECORD
        }.forEach {
            memoText = memoText + it.text + "\n"
        }

        val attachmentArray = arrayListOf<Uri>()

         viewModel.getMemoFileTbl().forEach {
             when(it.type) {
                 SNAPSHOT -> {
                     attachmentArray.add( FileProvider.getUriForFile(
                         context,
                         FILEPROVIDER_AUTHORITY,
                         File( getOutputDirectory(context, SNAPSHOT, false), it.fileName )
                     ))
                 }
                 PHOTO -> {
                     attachmentArray.add( FileProvider.getUriForFile(
                         context,
                         FILEPROVIDER_AUTHORITY,
                         File( getOutputDirectory(context, PHOTO, false), it.fileName)
                     ))
                 }
                 RECORD -> {
                     attachmentArray.add( FileProvider.getUriForFile(
                         context,
                         FILEPROVIDER_AUTHORITY,
                         File(getOutputDirectory(context, RECORD, false), it.fileName)
                     ))
                 }
             }
         }
         composeShare( context, subject, memoText,  attachmentArray)

    }

    private fun composeShare( context: Context,
                              subject: String,
                              text: String,
                              attachment: ArrayList<Uri>) {

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, subject)

            putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachment)
        }
        if( intent.resolveActivity(context.packageManager) != null) {
            ContextCompat.startActivity(context, intent, null)
        }
    }

    /*
    class OnClickListener(val clickListener: (isSecret:Boolean) -> Unit) {
        fun onClick(isSecret:Boolean) = clickListener(isSecret)
    }
*/

}