package com.example.docs_and_permissions.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.docs_and_permissions.R
import com.example.docs_and_permissions.utils.interactor.DocumentBottomSheetInteractor
import kotlinx.coroutines.launch
import java.io.File

class ExampleFragment : Fragment() {

    @Inject
    lateinit var interactor: DocumentBottomSheetInteractor

    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerReceiver()
    }

    private fun registerReceiver() = viewLifecycleOwner.lifecycleScope.launch {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isAdded.not()) return
                if (interactor.isAdded(childFragmentManager)) return
                val format = intent?.getStringExtra(OPEN_DOCUMENT_IN_APP_FORMAT) ?: return
                val path = intent.getStringExtra(OPEN_DOCUMENT_IN_APP_PATH) ?: return
                val name = intent.getStringExtra(OPEN_DOCUMENT_IN_APP_NAME) ?: return
                val supportedUiDocs =
                    requireContext().resources.getStringArray(FilerKitR.array.supported_doc_ui_formats)
                val supportedDocs =
                    requireContext().resources.getStringArray(FilerKitR.array.supported_doc_formats)
                if (supportedUiDocs.contains(format) || supportedDocs.contains(format)) {
                    interactor.provideFragment(childFragmentManager, format, path, name)
                } else {
                    toOpenSuggestion(path)
                }
            }
        }
        val intentFilter = IntentFilter(OPEN_DOCUMENT_IN_APP)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun toOpenSuggestion(path: String) {
        val file = File(path)
        val fileProvider = requireContext().packageName + ".fileProvider"
        val uri = FileProvider.getUriForFile(requireContext(), fileProvider, file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(uri, "application/*")
        startActivity(
            Intent.createChooser(
                intent,
                getString(R.string.document_open_title)
            )
        )
    }


    companion object {
        private val TAG = this::class.simpleName.toString()
        private const val OPEN_DOCUMENT_IN_APP = "open_document_in_app"
        private const val OPEN_DOCUMENT_IN_APP_PATH = "open_document_in_app_path"
        private const val OPEN_DOCUMENT_IN_APP_NAME = "open_document_in_app_name"
        private const val OPEN_DOCUMENT_IN_APP_FORMAT = "open_document_in_app_format"
    }
}