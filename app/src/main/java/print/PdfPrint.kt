package print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.print.PrintDocumentInfo
import android.util.Log
import java.io.File

class PdfPrint(
    private val printAttributes: PrintAttributes,
    private val callback: PrintAdapterCallback
) {
    fun print(printAdapter: PrintDocumentAdapter, path: File, fileName: String) {
        printAdapter.onLayout(null, printAttributes, null, object : LayoutResultCallback() {
            override fun onLayoutFinished(info: PrintDocumentInfo, changed: Boolean) {
                printAdapter.onWrite(
                    arrayOf<PageRange>(PageRange.ALL_PAGES),
                    getOutputFile(path, fileName),
                    CancellationSignal(),
                    object : WriteResultCallback() {
                        override fun onWriteFinished(pages: Array<PageRange>) {
                            super.onWriteFinished(pages)
                            callback.callback(DOCUMENT_PRINT_FINISHED)
                        }
                    })
            }
        }, null)
    }

    private fun getOutputFile(path: File, fileName: String): ParcelFileDescriptor? {
        if (!path.exists()) path.mkdirs()
        val file = File(path, fileName)
        try {
            file.createNewFile()
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open ParcelFileDescriptor", e)
        }
        return null
    }

    private companion object {
        private val TAG = PdfPrint::class.java.toString()
        private const val DOCUMENT_PRINT_FINISHED = 0
    }

    public interface PrintAdapterCallback {
        fun callback(tag: Int)
    }
}