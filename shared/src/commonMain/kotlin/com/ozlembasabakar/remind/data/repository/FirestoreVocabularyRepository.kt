package com.ozlembasabakar.remind.data.repository

import com.ozlembasabakar.remind.data.remote.dto.WordDto
import com.ozlembasabakar.remind.data.remote.dto.toDomain
import com.ozlembasabakar.remind.data.remote.fetchFirestoreRestWords
import com.ozlembasabakar.remind.data.remote.initializeFirebase
import com.ozlembasabakar.remind.domain.model.Article
import com.ozlembasabakar.remind.domain.model.GrammarBreakdown
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.model.TenseExample
import com.ozlembasabakar.remind.domain.model.Vocabulary
import com.ozlembasabakar.remind.domain.model.WordType
import com.ozlembasabakar.remind.domain.repository.VocabularyRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FirestoreVocabularyRepository(
    private val fallbackRepository: VocabularyRepository = InMemoryVocabularyRepository()
) : VocabularyRepository {

    private val mutex = Mutex()
    private var cachedCards: List<Vocabulary> = emptyList()
    private val reviewedIds = mutableSetOf<String>()

    private suspend fun ensureLoaded(): List<Vocabulary> = mutex.withLock {
        if (cachedCards.isNotEmpty()) return@withLock cachedCards

        try {
            initializeFirebase()
            println("Fetching collection 'words' from Cloud Firestore...")
            val snapshot = Firebase.firestore.collection("words").get()
            println("Firestore snapshot documents count: ${snapshot.documents.size}")

            val loaded = mutableListOf<Vocabulary>()
            for (doc in snapshot.documents) {
                val vocab = try {
                    val dto: WordDto = doc.data()
                    dto.toDomain(doc.id)
                } catch (e: Exception) {
                    try {
                        val germanWord: String = doc.get("germanWord") ?: ""
                        if (germanWord.isEmpty()) null
                        else {
                            val articleStr: String = doc.get("article") ?: "NONE"
                            val wordTypeStr: String = doc.get("wordType") ?: "NOUN"
                            val turkishTranslation: String = doc.get("turkishTranslation") ?: ""
                            val imageUrl: String? = doc.get("imageUrl")
                            val audioUrl: String? = doc.get("audioUrl")

                            val article = runCatching { Article.valueOf(articleStr.uppercase()) }.getOrDefault(Article.NONE)
                            val wordType = runCatching { WordType.valueOf(wordTypeStr.uppercase()) }.getOrDefault(WordType.NOUN)

                            val grammarMap: Map<String, String?>? = runCatching { doc.get<Map<String, String?>?>("grammar") }.getOrNull()
                            val grammar = grammarMap?.let { g ->
                                GrammarBreakdown(
                                    pluralForm = g["pluralForm"],
                                    prasens = g["prasens"],
                                    prateritum = g["prateritum"] ?: g["pastTense"],
                                    perfekt = g["perfekt"] ?: g["perfectTense"]
                                )
                            }

                            val examplesList: List<Map<String, String?>>? = runCatching { doc.get<List<Map<String, String?>>?>("examples") }.getOrNull()
                            val examples = examplesList?.mapNotNull { map ->
                                val german = map["germanSentence"] ?: map["sentenceGerman"] ?: ""
                                val turkish = map["turkishTranslation"] ?: map["sentenceTurkish"] ?: ""
                                if (german.isEmpty() && turkish.isEmpty()) null
                                else {
                                    val target = map["targetWord"] ?: germanWord
                                    val targetArtStr = map["targetWordArticle"] ?: "-"
                                    val targetArt = runCatching { Article.valueOf(targetArtStr.uppercase()) }.getOrDefault(Article.NONE)
                                    TenseExample(
                                        germanSentence = german,
                                        turkishTranslation = turkish,
                                        targetWord = target,
                                        targetWordArticle = targetArt
                                    )
                                }
                            } ?: emptyList()

                            Vocabulary(
                                id = doc.id,
                                germanWord = germanWord,
                                article = article,
                                wordType = wordType,
                                turkishTranslation = turkishTranslation,
                                imageUrl = imageUrl,
                                audioUrl = audioUrl,
                                grammar = grammar,
                                examples = examples
                            )
                        }
                    } catch (inner: Exception) {
                        println("Failed to decode doc '${doc.id}': ${inner.message}")
                        null
                    }
                }

                if (vocab != null) {
                    loaded.add(vocab)
                }
            }

            if (loaded.isNotEmpty()) {
                println("Successfully loaded ${loaded.size} cards from Cloud Firestore!")
                cachedCards = loaded.shuffled()
                return@withLock cachedCards
            } else {
                println("Loaded document list is empty after decoding.")
            }
        } catch (e: Exception) {
            println("Firestore collection fetch EXCEPTION: ${e::class.simpleName} - ${e.message}")
        }

        val restCards = fetchFirestoreRestWords()
        if (!restCards.isNullOrEmpty()) {
            println("Successfully loaded ${restCards.size} cards via Firestore REST API!")
            cachedCards = restCards.shuffled()
            return@withLock cachedCards
        }

        println("Firestore returned no cards or threw an exception. Falling back to local memory.")
        val fallbackCards = fallbackRepository.getAllCards().shuffled()
        cachedCards = fallbackCards
        fallbackCards
    }

    override suspend fun getNextDueCard(): Vocabulary? {
        val cards = ensureLoaded()
        return mutex.withLock {
            cards.firstOrNull { it.id !in reviewedIds } ?: cards.firstOrNull()
        }
    }

    override suspend fun getRemainingCardsCount(): Int {
        val cards = ensureLoaded()
        return mutex.withLock {
            maxOf(0, cards.size - reviewedIds.size)
        }
    }

    override suspend fun updateSrsStatus(id: String, rating: SrsStatus.Rating) {
        mutex.withLock {
            reviewedIds.add(id)
        }
        runCatching {
            fallbackRepository.updateSrsStatus(id, rating)
        }
    }

    override suspend fun getAllCards(): List<Vocabulary> {
        return ensureLoaded()
    }
}
