package nibm.mad.snapshop.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun extractMainObject(context: Context, sourceUri: Uri): Uri? = withContext(Dispatchers.IO) {
    var originalBitmap: Bitmap? = null
    var croppedBitmap: Bitmap? = null

    try {
        originalBitmap = loadBitmap(context, sourceUri) ?: return@withContext null

        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()

        val detector = ObjectDetection.getClient(options)

        val detectedObjects = detector.use { detector ->
            detector.process(InputImage.fromBitmap(originalBitmap, 0)).await()
        }

        if (detectedObjects.isEmpty()) return@withContext null

        val boundingBox = detectedObjects
            .maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
            ?.boundingBox ?: return@withContext null

        val safeLeft   = boundingBox.left.coerceIn(0, originalBitmap.width - 1)
        val safeTop    = boundingBox.top.coerceIn(0, originalBitmap.height - 1)
        val safeWidth  = boundingBox.width()
            .coerceAtMost(originalBitmap.width - safeLeft)
            .coerceAtLeast(1)
        val safeHeight = boundingBox.height()
            .coerceAtMost(originalBitmap.height - safeTop)
            .coerceAtLeast(1)

        croppedBitmap = Bitmap.createBitmap(originalBitmap, safeLeft, safeTop, safeWidth, safeHeight)

        val outputFile = File(context.cacheDir, "cropped_obj_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        Uri.fromFile(outputFile)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        if (croppedBitmap !== originalBitmap) croppedBitmap?.recycle()
        originalBitmap?.recycle()
    }
}

private fun loadBitmap(context: Context, uri: Uri): Bitmap? = try {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.isMutableRequired = true
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}
