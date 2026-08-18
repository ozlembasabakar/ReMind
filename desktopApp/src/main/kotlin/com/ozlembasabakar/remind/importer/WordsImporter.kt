package com.ozlembasabakar.remind.importer

import com.google.auth.oauth2.GoogleCredentials
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

const val PROJECT_ID = "remind-98595"
const val FIRESTORE_URL = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents/words"

private val logger = LoggerFactory.getLogger("WordsImporter")

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val inputPath = args.firstOrNull()
        ?: System.getenv("WORDS_FILE_PATH")
    val wordsFile = File(inputPath)

    val userHomeCredentials = File(System.getProperty("user.home"), ".credentials/remind-service-account-key.json")
    val customKeyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    val candidateKeys = listOfNotNull(
        customKeyPath?.let { File(it) },
        File("serviceAccountKey.json"),
        File("service-account-key.json"),
        if (userHomeCredentials.exists()) userHomeCredentials else null
    )
    val serviceAccountFile = candidateKeys.firstOrNull { it.exists() }

    if (!wordsFile.exists()) {
        logger.error("words.json not found at {}", wordsFile.absolutePath)
        return
    }
    if (serviceAccountFile == null || !serviceAccountFile.exists()) {
        logger.error(
            "Service account key not found. Tried paths: {}",
            candidateKeys.map { it.absolutePath })
        return
    }

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        allowTrailingComma = true
    }

    val rawText = wordsFile.readText(Charsets.UTF_8)
    logger.info("Read words.json ({} characters)", rawText.length)

    val objectStrings = mutableListOf<String>()
    var depth = 0
    var startIndex = -1
    var inString = false
    var isEscaped = false

    for (i in rawText.indices) {
        val char = rawText[i]

        if (isEscaped) {
            isEscaped = false
            continue
        }

        if (char == '\\' && inString) {
            isEscaped = true
            continue
        }

        if (char == '"') {
            inString = !inString
            continue
        }

        if (!inString) {
            if (char == '{') {
                if (depth == 0) {
                    startIndex = i
                }
                depth++
            } else if (char == '}') {
                depth--
                if (depth == 0 && startIndex != -1) {
                    objectStrings.add(rawText.substring(startIndex, i + 1))
                    startIndex = -1
                }
            }
        }
    }

    logger.info("Extracted {} JSON objects from file.", objectStrings.size)

    logger.info("Authenticating with Google Service Account...")
    val credentials = GoogleCredentials.fromStream(FileInputStream(serviceAccountFile))
        .createScoped(listOf("https://www.googleapis.com/auth/datastore"))
    credentials.refresh()
    val token = credentials.accessToken.tokenValue
    logger.info("Authentication successful!")

    var uploaded = 0
    var skipped = 0

    for ((index, objStr) in objectStrings.withIndex()) {
        val sanitizedStr = objStr.replace(Regex("""("(?:[^"\\]|\\.)*")\s*\n\s*("(?:\w+)"\s*:)""")) { matchResult ->
            "${matchResult.groupValues[1]},\n${matchResult.groupValues[2]}"
        }

        val element = try {
            json.parseToJsonElement(sanitizedStr)
        } catch (e: Exception) {
            try {
                json.parseToJsonElement(objStr)
            } catch (e2: Exception) {
                logger.warn("Skipping malformed item #{}: {}", index, e2.message)
                skipped++
                continue
            }
        }

        if (element !is JsonObject) {
            skipped++
            continue
        }

        val germanWord = element["germanWord"]?.jsonPrimitive?.contentOrNull ?: run {
            skipped++
            continue
        }
        val article = element["article"]?.jsonPrimitive?.contentOrNull ?: "NONE"
        val wordType = element["wordType"]?.jsonPrimitive?.contentOrNull ?: "NOUN"
        val turkishTranslation = element["turkishTranslation"]?.jsonPrimitive?.contentOrNull ?: ""
        val imageUrl = element["imageUrl"]?.jsonPrimitive?.contentOrNull

        val fieldsMap = mutableMapOf<String, JsonObject>(
            "germanWord" to stringField(germanWord),
            "article" to stringField(article),
            "wordType" to stringField(wordType),
            "turkishTranslation" to stringField(turkishTranslation)
        )
        if (imageUrl != null) fieldsMap["imageUrl"] = stringField(imageUrl)

        element["grammar"]?.let { gElem ->
            if (gElem is JsonObject) {
                val gMap = mutableMapOf<String, JsonObject>()
                gElem["pluralForm"]?.jsonPrimitive?.contentOrNull?.let { gMap["pluralForm"] = stringField(it) }
                gElem["prasens"]?.jsonPrimitive?.contentOrNull?.let { gMap["prasens"] = stringField(it) }
                gElem["prateritum"]?.jsonPrimitive?.contentOrNull?.let { gMap["prateritum"] = stringField(it) }
                gElem["perfekt"]?.jsonPrimitive?.contentOrNull?.let { gMap["perfekt"] = stringField(it) }
                gElem["pastTense"]?.jsonPrimitive?.contentOrNull?.let { gMap["pastTense"] = stringField(it) }
                gElem["perfectTense"]?.jsonPrimitive?.contentOrNull?.let { gMap["perfectTense"] = stringField(it) }
                fieldsMap["grammar"] = mapField(gMap)
            }
        }

        element["examples"]?.let { exElem ->
            val exArray = when (exElem) {
                is JsonArray -> exElem
                is JsonObject -> JsonArray(listOf(exElem))
                else -> null
            }
            if (!exArray.isNullOrEmpty()) {
                val exList = mutableListOf<JsonObject>()
                exArray.forEach { exItem ->
                    if (exItem is JsonObject) {
                        val exMap = mutableMapOf<String, JsonObject>()
                        exItem["germanSentence"]?.jsonPrimitive?.contentOrNull?.let { exMap["germanSentence"] = stringField(it) }
                        exItem["turkishTranslation"]?.jsonPrimitive?.contentOrNull?.let { exMap["turkishTranslation"] = stringField(it) }
                        exItem["sentenceGerman"]?.jsonPrimitive?.contentOrNull?.let { exMap["sentenceGerman"] = stringField(it) }
                        exItem["sentenceTurkish"]?.jsonPrimitive?.contentOrNull?.let { exMap["sentenceTurkish"] = stringField(it) }
                        exItem["targetWord"]?.jsonPrimitive?.contentOrNull?.let { exMap["targetWord"] = stringField(it) }
                        exItem["targetWordArticle"]?.jsonPrimitive?.contentOrNull?.let { exMap["targetWordArticle"] = stringField(it) }
                        exList.add(mapField(exMap))
                    }
                }
                if (exList.isNotEmpty()) {
                    fieldsMap["examples"] = arrayField(exList)
                }
            }
        }

        val body = JsonObject(mapOf("fields" to JsonObject(fieldsMap))).toString()

        val success = postToFirestore(token, body)
        if (success) {
            uploaded++
            if (uploaded % 50 == 0) {
                logger.info("Uploaded {} / {} words...", uploaded, objectStrings.size)
            }
        } else {
            logger.warn("Failed to upload '{}'", germanWord)
            skipped++
        }
    }

    logger.info("FINISHED! Total Uploaded to Firestore: {} | Skipped: {}", uploaded, skipped)
}

private fun stringField(value: String): JsonObject = JsonObject(mapOf("stringValue" to JsonPrimitive(value)))
private fun mapField(map: Map<String, JsonObject>): JsonObject = JsonObject(mapOf("mapValue" to JsonObject(mapOf("fields" to JsonObject(map)))))
private fun arrayField(list: List<JsonObject>): JsonObject = JsonObject(mapOf("arrayValue" to JsonObject(mapOf("values" to JsonArray(list)))))

private fun postToFirestore(accessToken: String, jsonBody: String): Boolean {
    return try {
        val url = java.net.URI.create(FIRESTORE_URL).toURL()
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $accessToken")
        conn.setRequestProperty("Content-Type", "application/json; utf-8")
        conn.doOutput = true

        conn.outputStream.use { os ->
            val input = jsonBody.toByteArray(StandardCharsets.UTF_8)
            os.write(input, 0, input.size)
        }

        val responseCode = conn.responseCode
        responseCode in 200..299
    } catch (e: Exception) {
        logger.error("HTTP Error: {}", e.message)
        false
    }
}
