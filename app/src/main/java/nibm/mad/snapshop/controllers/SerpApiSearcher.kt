package nibm.mad.snapshop.controllers

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import nibm.mad.snapshop.models.ProductMatch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Searches SerpApi Google Lens using a direct image URL.
 * Extracts title, link, source, and source_icon into a usable list.
 * Logs the titles of the top 5 results.
 * * @return A list of ProductMatch objects. Returns an empty list if the request fails.
 */
suspend fun searchImageWithSerpApi(
    imageUrl: String,
    apiKey: String = "3a58c91a238bf0fd0b99a8e70a2cd3fd2a4215afeeb3084a115aaccbb48c9b99"
): List<ProductMatch> = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // 1. Build the URL with all necessary query parameters
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("serpapi.com")
            .addPathSegment("search")
            .addQueryParameter("api_key", apiKey)
            .addQueryParameter("engine", "google_lens")
            .addQueryParameter("url", imageUrl)
            .addQueryParameter("type", "all")
            .addQueryParameter("hl", "en")
            .addQueryParameter("country", "lk")
            .addQueryParameter("safe", "off")
            .addQueryParameter("q", "Buy+in+Sri+Lanka")
            .build()

        // 2. Formulate the GET request
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        // 3. Execute the network transaction
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("SerpApi", "Request failed with code: ${response.code}")
                return@withContext emptyList()
            }

            val responseString = response.body?.string() ?: return@withContext emptyList()

            // 4. Parse the JSON manually to extract exactly what we need
            val jsonElement = Json.parseToJsonElement(responseString).jsonObject
            val visualMatchesArray = jsonElement["visual_matches"]?.jsonArray ?: return@withContext emptyList()

            val extractedProducts = mutableListOf<ProductMatch>()

            // 5. Build the primary array of objects
            for (item in visualMatchesArray) {
                val matchObj = item.jsonObject

                val title = matchObj["title"]?.jsonPrimitive?.content ?: "Unknown Title"
                val link = matchObj["link"]?.jsonPrimitive?.content ?: ""
                val source = matchObj["source"]?.jsonPrimitive?.content ?: ""
                val sourceIcon = matchObj["source_icon"]?.jsonPrimitive?.content ?: ""

                extractedProducts.add(ProductMatch(title, link, source, sourceIcon))
            }

            // 6. Create the secondary array (Top 5 Titles Only) and log it
            val top5Titles = extractedProducts.take(5).map { it.title }
            Log.d("SerpApi_Top5", "Top 5 Titles: $top5Titles")

            // Return the full list of parsed objects for later processing in the app
            return@withContext extractedProducts
        }
    } catch (e: IOException) {
        Log.e("SerpApi", "Network error during search", e)
        return@withContext emptyList()
    } catch (e: Exception) {
        Log.e("SerpApi", "Unexpected error during parsing", e)
        return@withContext emptyList()
    }
}