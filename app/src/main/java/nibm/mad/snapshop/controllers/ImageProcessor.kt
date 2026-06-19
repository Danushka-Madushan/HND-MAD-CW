package nibm.mad.snapshop.controllers

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
        // 1. Load bitmap once — reused for both ML Kit input and cropping
        //    (avoids the original double-decode: fromFilePath + getBitmap)
        originalBitmap = loadBitmap(context, sourceUri) ?: return@withContext null

        // 2. Configure detector
        //    enableMultipleObjects() is required: without it, SINGLE_IMAGE_MODE returns at most
        //    1 object, making maxByOrNull a no-op. With it, we get up to 5 candidates to compare.
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .build()

        val detector = ObjectDetection.getClient(options)

        // 3. Run detection; close detector immediately after to release native resources
        val detectedObjects = detector.use { detector ->
            detector.process(InputImage.fromBitmap(originalBitmap, 0)).await()
        }

        if (detectedObjects.isEmpty()) return@withContext null

        // 4. Pick the most prominent object by bounding box area
        val boundingBox = detectedObjects
            .maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
            ?.boundingBox ?: return@withContext null

        // 5. Clamp bounding box strictly within bitmap bounds
        //    coerceIn guards the origin; coerceAtLeast(1) prevents a zero-size crop crash
        val safeLeft   = boundingBox.left.coerceIn(0, originalBitmap.width - 1)
        val safeTop    = boundingBox.top.coerceIn(0, originalBitmap.height - 1)
        val safeWidth  = boundingBox.width()
            .coerceAtMost(originalBitmap.width - safeLeft)
            .coerceAtLeast(1)
        val safeHeight = boundingBox.height()
            .coerceAtMost(originalBitmap.height - safeTop)
            .coerceAtLeast(1)

        // 6. Crop
        croppedBitmap = Bitmap.createBitmap(originalBitmap, safeLeft, safeTop, safeWidth, safeHeight)

        // 7. Save to cache and return URI
        val outputFile = File(context.cacheDir, "cropped_obj_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { out ->
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        Uri.fromFile(outputFile)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        // Recycle both bitmaps to release native memory promptly.
        // Guard against the rare case createBitmap() returns the same instance.
        if (croppedBitmap !== originalBitmap) croppedBitmap?.recycle()
        originalBitmap?.recycle()
    }
}

/**
 * Loads a software-backed, mutable [Bitmap] from [uri].
 * Mutable is required so that [InputImage.fromBitmap] and [Bitmap.createBitmap] work correctly.
 */
private fun loadBitmap(context: Context, uri: Uri): Bitmap? = try {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
        decoder.isMutableRequired = true   // forces software (ARGB_8888) — never hardware
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}