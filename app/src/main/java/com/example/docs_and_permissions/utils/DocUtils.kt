package com.example.docs_and_permissions.utils

import android.os.Environment
import android.print.PrintAttributes
import android.util.Log
import android.webkit.WebView
import print.PdfPrint
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.UUID

private const val PDF_FOLDER_NAME = "/pdf_output"
private const val RESOLUTION_DOCUMENT = 600
private const val JOB_NAME = "TaxReceipts_Doc"
private const val ERROR_TAG = "Error"
const val PDF = "pdf"
const val RECEIPT_NAME = "receipt_"

fun generateUUID(): UUID = UUID.randomUUID()

fun createWebPrintJob(
    webView: WebView,
    uuid: UUID,
    callback: PdfPrint.PrintAdapterCallback
): File? {
    return try {
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.NA_LEGAL)
            .setResolution(
                PrintAttributes.Resolution(
                    PDF,
                    PDF,
                    RESOLUTION_DOCUMENT,
                    RESOLUTION_DOCUMENT
                )
            )
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
        val path =
            Environment.getExternalStoragePublicDirectory(PDF_FOLDER_NAME)

        val pdfPrint = PdfPrint(attributes, callback)
        pdfPrint.print(
            webView.createPrintDocumentAdapter(JOB_NAME),
            path,
            "$RECEIPT_NAME$uuid.pdf"
        )
        path
    } catch (e: Exception) {
        Log.e(ERROR_TAG, "Create pdf failed ${e.localizedMessage}")
        null
    }
}

internal fun getFileContent(filePath: String): String {
    val file = File(filePath)
    val reader = BufferedReader(FileReader(file))
    val content = StringBuilder()
    var line: String?

    while (reader.readLine().also { line = it } != null) {
        content.append(line).append("\n")
    }

    reader.close()
    return content.toString()
}

internal fun formatXml(xml: String): String {
    val lines = xml.split("\n")
    val formattedLines = mutableListOf<String>()

    for (line in lines) {
        val trimmedLine = line.trim()
        if (trimmedLine.startsWith("<") || trimmedLine.startsWith("</"))
            formattedLines.add(trimmedLine)
    }

    return formattedLines.joinToString("\n")
}