package com.example.chattopdf.utils

import android.content.Context
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// A4 Dimensions in PDF points (1/72 of an inch)
private const val A4_WIDTH = 595f
private const val A4_HEIGHT = 842f

suspend fun generatePdfFromImages(context: Context, uris: List<Uri>): File? {
    return withContext(Dispatchers.IO) {
        if (uris.isEmpty()) return@withContext null

        val pdfDocument = PdfDocument()

        for ((index, uri) in uris.withIndex()) {
            // 1. Get the original bitmap
            val originalBitmap = uriToBitmap(uri, context.contentResolver)

            // 2. Enhance it (Contrast & Brightness)
            val enhancedBitmap = enhanceBitmap(originalBitmap)

            // 3. Create the PDF Page
            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH.toInt(), A4_HEIGHT.toInt(), index + 1).create()
            val page = pdfDocument.startPage(pageInfo)

            // --- THE SCALING MATH ---

            // Find how much we need to shrink the image to fit the width and height
            val scaleX = A4_WIDTH / enhancedBitmap.width
            val scaleY = A4_HEIGHT / enhancedBitmap.height

            // Pick the SMALLER scale factor to ensure the whole image fits on the page
            val scale = minOf(scaleX, scaleY)

            // Calculate the final size of the image on the PDF
            val scaledWidth = enhancedBitmap.width * scale
            val scaledHeight = enhancedBitmap.height * scale

            // Calculate where to draw the image so it sits perfectly in the center
            val leftPosition = (A4_WIDTH - scaledWidth) / 2f
            val topPosition = (A4_HEIGHT - scaledHeight) / 2f

            // Create a Matrix to apply our math
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            matrix.postTranslate(leftPosition, topPosition)

            // 4. Draw the image using our calculated matrix
            page.canvas.drawBitmap(enhancedBitmap, matrix, null)

            pdfDocument.finishPage(page)

            // 5. Clean up memory so the app doesn't crash on large batches!
            originalBitmap.recycle()
            if (enhancedBitmap != originalBitmap) enhancedBitmap.recycle()
        }

        // 6. Save the document to a temporary file
        val outputFile = File(context.cacheDir, "PDFDidi_Document_${System.currentTimeMillis()}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(outputFile))
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            pdfDocument.close()
        }

        return@withContext outputFile
    }
}