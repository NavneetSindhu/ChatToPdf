import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun sharePdf(context: Context, pdfFile: File) {
    // 1. Ask the FileProvider for a temporary, secure URI
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider", // This MUST match the authority in your Manifest
        pdfFile
    )

    // 2. Create the Intent (the message to the Android system)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        // 3. Grant temporary permission to whoever receives this intent
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // 4. Wrap it in a Chooser so the beautiful bottom sheet pops up
    val chooserIntent = Intent.createChooser(shareIntent, "Share your PDF")

    // We need this flag if we are launching from outside a traditional Activity context
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(chooserIntent)
}

fun viewPdf(context: Context, pdfFile: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
    )

    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Try to open it. If they don't have a PDF viewer installed, handle the crash gracefully!
    try {
        context.startActivity(viewIntent)
    } catch (e: Exception) {
        // Show a Toast saying "No PDF viewer installed!"
    }
}