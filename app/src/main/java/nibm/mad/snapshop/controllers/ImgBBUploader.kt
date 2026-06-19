package nibm.mad.snapshop.controllers

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Uploads a local image file Uri to ImgBB and returns the direct image link.
 * Runs completely asynchronously on Dispatchers.IO.
 * * @return Direct image URL string if successful, null otherwise.
 */
suspend fun uploadImageToImgBB(
    context: Context,
    imageUri: Uri,
    apiKey: String = "" // Replace with your actual ImgBB API Key
): String? = withContext(Dispatchers.IO) {
    try {
        // 1. Resolve and open the local image stream to read raw bytes
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri) ?: return@withContext null
        val imageBytes = inputStream.use { it.readBytes() }

        // 2. Build the Multi-part form body containing the image payload
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "upload_${System.currentTimeMillis()}.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        // 3. Formulate the POST API request
        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$apiKey")
            .post(requestBody)
            .build()

        // 4. Execute the network transaction
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null

            val responseString = response.body?.string() ?: return@withContext null

            // 5. Safely extract the direct 'url' string using Kotlinx Serialization
            val jsonElement = Json.parseToJsonElement(responseString).jsonObject
            val dataObject = jsonElement["data"]?.jsonObject
            val directUrl = dataObject?.get("url")?.jsonPrimitive?.content

            return@withContext directUrl
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return@withContext null
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}