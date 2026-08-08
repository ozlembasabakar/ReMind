package com.ozlembasabakar.remind.data.remote

import com.ozlembasabakar.remind.domain.model.Article
import com.ozlembasabakar.remind.domain.model.GrammarBreakdown
import com.ozlembasabakar.remind.domain.model.TenseExample
import com.ozlembasabakar.remind.domain.model.Vocabulary
import com.ozlembasabakar.remind.domain.model.WordType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

actual suspend fun fetchFirestoreRestWords(): List<Vocabulary>? = withContext(Dispatchers.IO) {
    runCatching {
        println("Fetching words from Firestore REST API on JVM Desktop...")
        val url = "https://firestore.googleapis.com/v1/projects/remind-98595/databases/(default)/documents/words?pageSize=300"
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            println("REST fetch failed with status HTTP ${response.statusCode()}")
            return@withContext null
        }

        val jsonObj = Json.parseToJsonElement(response.body()).jsonObject
        val documents = jsonObj["documents"]?.jsonArray ?: return@withContext emptyList()

        val resultList = mutableListOf<Vocabulary>()

        for (doc in documents) {
            val docObj = doc.jsonObject
            val name = docObj["name"]?.jsonPrimitive?.content ?: ""
            val docId = name.substringAfterLast('/')

            val fields = docObj["fields"]?.jsonObject ?: continue

            val germanWord = getStringField(fields, "germanWord") ?: continue
            val articleStr = getStringField(fields, "article") ?: "NONE"
            val wordTypeStr = getStringField(fields, "wordType") ?: "NOUN"
            val turkishTranslation = getStringField(fields, "turkishTranslation") ?: ""
            val imageUrl = getStringField(fields, "imageUrl")
            val audioUrl = getStringField(fields, "audioUrl")

            val article = runCatching { Article.valueOf(articleStr.uppercase()) }.getOrDefault(Article.NONE)
            val wordType = runCatching { WordType.valueOf(wordTypeStr.uppercase()) }.getOrDefault(WordType.NOUN)

            val grammarObj = getMapFields(fields, "grammar")
            val grammar = grammarObj?.let { g ->
                GrammarBreakdown(
                    pluralForm = getStringField(g, "pluralForm"),
                    prasens = getStringField(g, "prasens"),
                    prateritum = getStringField(g, "prateritum") ?: getStringField(g, "pastTense"),
                    perfekt = getStringField(g, "perfekt") ?: getStringField(g, "perfectTense")
                )
            }

            val examplesList = getArrayValues(fields, "examples")
            val examples = examplesList.mapNotNull { exFields ->
                val german = getStringField(exFields, "germanSentence") ?: getStringField(exFields, "sentenceGerman") ?: ""
                val turkish = getStringField(exFields, "turkishTranslation") ?: getStringField(exFields, "sentenceTurkish") ?: ""
                if (german.isEmpty() && turkish.isEmpty()) null
                else {
                    val target = getStringField(exFields, "targetWord") ?: germanWord
                    val targetArtStr = getStringField(exFields, "targetWordArticle") ?: "NONE"
                    val targetArt = runCatching { Article.valueOf(targetArtStr.uppercase()) }.getOrDefault(Article.NONE)
                    TenseExample(
                        germanSentence = german,
                        turkishTranslation = turkish,
                        targetWord = target,
                        targetWordArticle = targetArt
                    )
                }
            }

            resultList.add(
                Vocabulary(
                    id = docId,
                    germanWord = germanWord,
                    article = article,
                    wordType = wordType,
                    turkishTranslation = turkishTranslation,
                    imageUrl = imageUrl,
                    audioUrl = audioUrl,
                    grammar = grammar,
                    examples = examples
                )
            )
        }

        println("REST API successfully loaded ${resultList.size} cards from Cloud Firestore!")
        resultList
    }.getOrElse { e ->
        println("REST API fetch error: ${e.message}")
        null
    }
}

private fun getStringField(fields: JsonObject, fieldName: String): String? {
    val field = fields[fieldName]?.jsonObject ?: return null
    return field["stringValue"]?.jsonPrimitive?.contentOrNull
}

private fun getMapFields(fields: JsonObject, fieldName: String): JsonObject? {
    val field = fields[fieldName]?.jsonObject ?: return null
    val mapValue = field["mapValue"]?.jsonObject ?: return null
    return mapValue["fields"]?.jsonObject
}

private fun getArrayValues(fields: JsonObject, fieldName: String): List<JsonObject> {
    val field = fields[fieldName]?.jsonObject ?: return emptyList()
    val arrayValue = field["arrayValue"]?.jsonObject ?: return emptyList()
    val values = arrayValue["values"]?.jsonArray ?: return emptyList()
    return values.mapNotNull { item ->
        val mapVal = item.jsonObject["mapValue"]?.jsonObject
        mapVal?.get("fields")?.jsonObject
    }
}
