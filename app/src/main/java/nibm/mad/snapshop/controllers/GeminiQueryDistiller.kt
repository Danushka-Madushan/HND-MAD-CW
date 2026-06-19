package nibm.mad.snapshop.controllers

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Sends the top 5 product titles to the Gemini API to distill them into a single clean search query.
 * Uses structured JSON outputs to guarantee a reliable schema response.
 *
 * @param top5Titles The list of the top 5 titles extracted from your SerpApi response.
 * @return The final distilled plain English query string, or null if the request fails.
 */
suspend fun distillProductQuery(
    top5Titles: List<String>,
    apiKey: String = "", // Your Gemini API Key
    modelName: String = "gemini-3.1-flash-lite" // Recommended robust model for structured text processing
): String? = withContext(Dispatchers.IO) {
    try {
        // Re-use safe timeouts for AI generation processing latency blocks
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // 1. Build the titles JSON array safely to handle quotes/special characters
        val datasetJsonArray = buildJsonArray {
            top5Titles.forEach { add(it) }
        }

        val promptText = """
            Analyze these chaotic product titles extracted from Google Lens and distill them into a single, clean English search query (8 words maximum) optimized for e-commerce search engines. Remove noise words like buy, cheap, online, or store names. Translate foreign language concepts (e.g., "Soporte Celular") into standard English terms. Format the output in standard Title Case (e.g., 'The Lord of the Rings'), capitalizing the first letter of major descriptive words while keeping minor words, prepositions, and measurement units lowercase.

            Dataset:
            $datasetJsonArray
        """.trimIndent()

        // 2. Programmatically build the exact JSON request hierarchy structure
        val requestBodyJson = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", promptText)
                        })
                    })
                })
            })
            put("systemInstruction", buildJsonObject {
                put("parts", buildJsonArray {
                    add(buildJsonObject {
                        put("text", "You are an automated backend extractor. Output ONLY valid JSON matching the requested schema with no extra characters, text, markdown code blocks, or formatting.")
                    })
                })
            })
            put("generationConfig", buildJsonObject {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
                put("responseSchema", buildJsonObject {
                    put("type", "OBJECT")
                    put("properties", buildJsonObject {
                        put("distillated_query", buildJsonObject {
                            put("type", "STRING")
                            put("description", "The final distilled e-commerce search query in plain English.")
                        })
                    })
                    put("required", buildJsonArray { add("distillated_query") })
                })
            })
        }

        // 3. Prepare the HTTP network package
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // 4. Fire the network call
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e("GeminiDistiller", "API call failed with code: ${response.code}")
                return@withContext null
            }

            val responseString = response.body?.string() ?: return@withContext null

            // 5. Navigate the standard Gemini payload structure to find the text string response
            val rootJson = Json.parseToJsonElement(responseString).jsonObject
            val candidates = rootJson["candidates"]?.jsonArray ?: return@withContext null
            val firstCandidate = candidates.getOrNull(0)?.jsonObject ?: return@withContext null
            val content = firstCandidate["content"]?.jsonObject ?: return@withContext null
            val parts = content["parts"]?.jsonArray ?: return@withContext null
            val innerJsonString = parts.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content ?: return@withContext null

            // 6. Parse the structured inner JSON text generated by Gemini to grab the final field
            val distilledJsonObj = Json.parseToJsonElement(innerJsonString).jsonObject
            val distilledQuery = distilledJsonObj["distillated_query"]?.jsonPrimitive?.content

            return@withContext distilledQuery
        }

    } catch (e: IOException) {
        Log.e("GeminiDistiller", "Network communication failure", e)
        return@withContext null
    } catch (e: Exception) {
        Log.e("GeminiDistiller", "Unexpected exception while processing distillation", e)
        return@withContext null
    }
}