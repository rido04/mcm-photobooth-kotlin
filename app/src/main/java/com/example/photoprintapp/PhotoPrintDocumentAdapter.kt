package com.example.photoprintapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import java.io.FileOutputStream
import java.io.IOException

class PhotoPrintDocumentAdapter(
    private val context: Context,
    private val bitmap: Bitmap
) : PrintDocumentAdapter() {

    private var pageHeight: Int = 0
    private var pageWidth: Int = 0

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        // Hitung ukuran halaman
        val mediaSize = newAttributes?.mediaSize
        pageHeight = mediaSize?.heightMils?.times(72)?.div(1000) ?: 792 // Default A4
        pageWidth = mediaSize?.widthMils?.times(72)?.div(1000) ?: 612

        val info = PrintDocumentInfo.Builder("photo_print.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_PHOTO)
            .setPageCount(1)
            .build()

        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        var pdfDocument: PdfDocument? = null
        
        try {
            // Buat PDF document
            pdfDocument = PdfDocument()
            
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
                pdfDocument.close()
                return
            }

            // Draw bitmap ke canvas
            drawBitmapOnCanvas(page.canvas)
            pdfDocument.finishPage(page)

            // Write PDF ke file
            try {
                pdfDocument.writeTo(FileOutputStream(destination?.fileDescriptor))
                callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            } catch (e: IOException) {
                callback?.onWriteFailed(e.message)
            }
        } finally {
            pdfDocument?.close()
        }
    }

    private fun drawBitmapOnCanvas(canvas: Canvas) {
        // Scale bitmap agar fit di halaman
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()
        
        val scaleX = pageWidth / bitmapWidth
        val scaleY = pageHeight / bitmapHeight
        val scale = minOf(scaleX, scaleY)
        
        val scaledWidth = bitmapWidth * scale
        val scaledHeight = bitmapHeight * scale
        
        // Center bitmap di halaman
        val left = (pageWidth - scaledWidth) / 2
        val top = (pageHeight - scaledHeight) / 2
        
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        
        canvas.save()
        canvas.translate(left, top)
        canvas.scale(scale, scale)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        canvas.restore()
    }
}
