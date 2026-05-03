package com.example.chattopdf.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import androidx.core.graphics.createBitmap

fun enhanceBitmap(sourceBitmap: Bitmap):Bitmap{
    val resultBitmap = createBitmap(
        sourceBitmap.width,
        sourceBitmap.height,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(resultBitmap)
    val contrast = 1.2f
    val brightness = 20f

    val colorMatrix = ColorMatrix(
        floatArrayOf(
            // R, G, B, A, Offset(Brightness)
            contrast, 0f, 0f, 0f, brightness, // Red channel
            0f, contrast, 0f, 0f, brightness, // Green channel
            0f, 0f, contrast, 0f, brightness, // Blue channel
            0f, 0f, 0f, 1f, 0f          // Alpha channel (keep it fully opaque)
        )
    )

    val paint = Paint()
    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)

    canvas.drawBitmap(sourceBitmap,0f,0f,paint)
    return resultBitmap
}


fun uriToBitmap(uri: Uri,contentResolver: ContentResolver):Bitmap{
    val source = ImageDecoder.createSource(contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        // Force Android to load this into standard memory, not the GPU!
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        decoder.isMutableRequired = true
    }
}