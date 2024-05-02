package com.example.docs_and_permissions.utils

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import com.example.docs_and_permissions.R
import com.example.docs_and_permissions.databinding.DocumentBottomSheetLayoutBinding
import com.example.docs_and_permissions.utils.interactor.DocumentBottomSheetInteractorImpl.Companion.DOCUMENT_FORMAT_KEY
import com.example.docs_and_permissions.utils.interactor.DocumentBottomSheetInteractorImpl.Companion.DOCUMENT_NAME_KEY
import com.example.docs_and_permissions.utils.interactor.DocumentBottomSheetInteractorImpl.Companion.DOCUMENT_PATH_KEY
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import print.PdfPrint
import java.io.File

class DocumentBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DocumentBottomSheetLayoutBinding? = null
    private var uri: Uri? = null
    private val binding get() = _binding

    private val uiFormats by lazy {
        this.resources.getStringArray(R.array.supported_doc_ui_formats)
    }

    private val callback = object : PdfPrint.PrintAdapterCallback {
        override fun callback(tag: Int) {
            sendDocument()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Component_BottomSheetDialog_Light_Resizing)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = DocumentBottomSheetLayoutBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener {
                val bottomSheetDialog = it as BottomSheetDialog
                setupBehavior(bottomSheetDialog)
            }
        }

    private fun setupBehavior(bottomSheetDialog: BottomSheetDialog) {
        val view = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        view.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(view)
        behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        setUpDocument()
    }

    private fun setUpDocument() = with(binding) {
        this ?: return@with
        val format = arguments?.getString(DOCUMENT_FORMAT_KEY)
        val path = arguments?.getString(DOCUMENT_PATH_KEY)
        val name = arguments?.getString(DOCUMENT_NAME_KEY)
        progressBar.visibility = View.GONE
        context ?: return@with
        val file = File(path ?: return@with)

        val fileProvider = requireContext().packageName + ".fileProvider"
        val uri = FileProvider.getUriForFile(requireContext(), fileProvider, file)
        title.text = name
        readyButton.setOnClickListener { dismiss() }
        shareIcon.setOnClickListener { shareDocument(uri) }
        when {
            format == TXT_FORMAT || format == RTF_FORMAT -> displayTextFormat(file)
            format == HTML_FORMAT -> displayHtmlFormat(file)
            format == XML_FORMAT -> displayXmlFormat(file)
            uiFormats.contains(format) -> {
                val bitmap = BitmapFactory.decodeFile(path)
                documentImage.setImageBitmap(bitmap)
            }

            format == PDF_FORMAT -> {
                val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                val screenHeight = Resources.getSystem().displayMetrics.heightPixels
                val layoutParams = pdfView.layoutParams
                layoutParams.width = screenWidth
                layoutParams.height = screenHeight
                pdfView.layoutParams = layoutParams
                pdfView.visibility = View.VISIBLE
                displayPdfFormat(file)
            }
        }
    }

    private fun sendDocument() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(intent)
    }

    private fun displayXmlFormat(file: File) = with(binding) {
        this ?: return@with
        webView.visibility = View.VISIBLE
        val xmlContent = getFileContent(file.path)
        val content = formatXml(xmlContent)
        val formattedXml = content
            .replace("<", "&lt;")
            .replace(">", "&gt;")
        webView.loadData(formattedXml, "text/html", "UTF-8")
    }

    private fun displayHtmlFormat(file: File) = with(binding) {
        this ?: return@with
        webView.visibility = View.VISIBLE
        val content = getFileContent(file.path)
        webView.loadData(content, "text/html", "UTF-8")
    }

    private fun displayTextFormat(file: File) = with(binding) {
        this ?: return@with
        webView.visibility = View.VISIBLE
        webView.loadUrl("${Uri.fromFile(file)}")
    }

    private fun displayPdfFormat(file: File) = with(binding) {
        this ?: return@with
        pdfView.fromFile(file)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAnnotationRendering(false)
            .scrollHandle(null)
            .load()
    }

    private fun shareDocument(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.type = "application/*"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, getString(R.string.document_send_title)))
    }

    private fun createDocument() = with(binding.webView) {
        val uuid = generateUUID()
        val file = createWebPrintJob(this, uuid, callback)
        val fullPath = File("${file}/$RECEIPT_NAME$uuid.$PDF")
        uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName,
            fullPath
        )
    }

    companion object {
        private const val RTF_FORMAT = "text/rtf"
        private const val XML_FORMAT = "text/xml"
        private const val HTML_FORMAT = "text/html"
        private const val TXT_FORMAT = "text/plain"
        private const val PDF_FORMAT = "application/pdf"

        internal fun getInstance() = DocumentBottomSheet()
    }
}