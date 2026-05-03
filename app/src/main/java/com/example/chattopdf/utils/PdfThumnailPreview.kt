import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

fun getPdfThumbnail(pdfFile: File): Bitmap? {
    try {
        // 1. Open the file specifically for reading
        val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fileDescriptor)

        // 2. Grab the very first page (Index 0)
        val page = renderer.openPage(0)

        // 3. Create a blank Bitmap to hold the image
        // (We scale it up slightly so it looks crisp on high-res phone screens)
        val bitmap = Bitmap.createBitmap(
            page.width * 2,
            page.height * 2,
            Bitmap.Config.ARGB_8888
        )

        // Fill the background with white (otherwise transparent PDFs will look black)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        // 4. Draw the PDF page onto our Bitmap
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        // 5. Clean up memory! Very important!
        page.close()
        renderer.close()
        fileDescriptor.close()

        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}