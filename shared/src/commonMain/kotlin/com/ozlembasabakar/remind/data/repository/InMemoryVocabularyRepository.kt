package com.ozlembasabakar.remind.data.repository

import com.ozlembasabakar.remind.domain.model.Article
import com.ozlembasabakar.remind.domain.model.GrammarBreakdown
import com.ozlembasabakar.remind.domain.model.SrsStatus
import com.ozlembasabakar.remind.domain.model.TenseExample
import com.ozlembasabakar.remind.domain.model.Vocabulary
import com.ozlembasabakar.remind.domain.model.WordType
import com.ozlembasabakar.remind.domain.repository.VocabularyRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max

class InMemoryVocabularyRepository : VocabularyRepository {
    private val mutex = Mutex()

    private val cards = mutableListOf(
        Vocabulary(
            id = "1",
            germanWord = "Tisch",
            article = Article.DER,
            wordType = WordType.NOUN,
            turkishTranslation = "Masa",
            imageUrl = "https://images.unsplash.com/photo-1530018607912-eff2daa1bac4?w=500",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3",
            grammar = GrammarBreakdown(
                pluralForm = "die Tische"
            ),
            examples = listOf(
                TenseExample(
                    tenseName = "Nominativ",
                    germanSentence = "Der Tisch steht im Wohnzimmer.",
                    turkishTranslation = "Masa oturma odasında duruyor.",
                    targetWord = "Tisch",
                    targetWordArticle = Article.DER
                ),
                TenseExample(
                    tenseName = "Plural",
                    germanSentence = "Die Tische sind sehr groß.",
                    turkishTranslation = "Masalar çok büyük.",
                    targetWord = "Tische",
                    targetWordArticle = Article.PLURAL
                )
            )
        ),
        Vocabulary(
            id = "2",
            germanWord = "Katze",
            article = Article.DIE,
            wordType = WordType.NOUN,
            turkishTranslation = "Kedi",
            imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3",
            grammar = GrammarBreakdown(
                pluralForm = "die Katzen"
            ),
            examples = listOf(
                TenseExample(
                    tenseName = "Präsens",
                    germanSentence = "Die Katze schläft auf dem Sofa.",
                    turkishTranslation = "Kedi koltukta uyuyor.",
                    targetWord = "Katze",
                    targetWordArticle = Article.DIE
                )
            )
        ),
        Vocabulary(
            id = "3",
            germanWord = "Buch",
            article = Article.DAS,
            wordType = WordType.NOUN,
            turkishTranslation = "Kitap",
            imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3",
            grammar = GrammarBreakdown(
                pluralForm = "die Bücher"
            ),
            examples = listOf(
                TenseExample(
                    tenseName = "Präsens",
                    germanSentence = "Das Buch ist sehr interessant.",
                    turkishTranslation = "Kitap çok ilginç.",
                    targetWord = "Buch",
                    targetWordArticle = Article.DAS
                )
            )
        ),
        Vocabulary(
            id = "4",
            germanWord = "gehen",
            article = Article.NONE,
            wordType = WordType.VERB,
            turkishTranslation = "Gitmek",
            imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=500",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3",
            grammar = GrammarBreakdown(
                prasens = "geht",
                prateritum = "ging",
                perfekt = "ist gegangen"
            ),
            examples = listOf(
                TenseExample(
                    tenseName = "Präsens",
                    germanSentence = "Ich gehe heute in die Schule.",
                    turkishTranslation = "Bugün okula gidiyorum.",
                    targetWord = "gehe",
                    targetWordArticle = Article.NONE
                ),
                TenseExample(
                    tenseName = "Präteritum",
                    germanSentence = "Er ging gestern nach Hause.",
                    turkishTranslation = "Dün eve gitti.",
                    targetWord = "ging",
                    targetWordArticle = Article.NONE
                ),
                TenseExample(
                    tenseName = "Perfekt",
                    germanSentence = "Wir sind ins Kino gegangen.",
                    turkishTranslation = "Sinemaya gittik.",
                    targetWord = "gegangen",
                    targetWordArticle = Article.NONE
                )
            )
        ),
        Vocabulary(
            id = "5",
            germanWord = "Kinder",
            article = Article.PLURAL,
            wordType = WordType.NOUN,
            turkishTranslation = "Çocuklar",
            imageUrl = "https://images.unsplash.com/photo-1485546246426-74dc88dec4d9?w=500",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3",
            grammar = GrammarBreakdown(
                pluralForm = "die Kinder (Singular: das Kind)"
            ),
            examples = listOf(
                TenseExample(
                    tenseName = "Plural",
                    germanSentence = "Die Kinder spielen im Park.",
                    turkishTranslation = "Çocuklar parkta oynuyor.",
                    targetWord = "Kinder",
                    targetWordArticle = Article.PLURAL
                )
            )
        )
    )

    private val reviewedIds = mutableSetOf<String>()

    override suspend fun getNextDueCard(): Vocabulary? = mutex.withLock {
        // Return first card that hasn't been completed in this current deck session
        cards.firstOrNull { it.id !in reviewedIds } ?: cards.firstOrNull()
    }

    override suspend fun getRemainingCardsCount(): Int = mutex.withLock {
        max(0, cards.size - reviewedIds.size)
    }

    override suspend fun updateSrsStatus(id: String, rating: SrsStatus.Rating) = mutex.withLock {
        val index = cards.indexOfFirst { it.id == id }
        if (index != -1) {
            val current = cards[index]
            val srs = current.srsStatus
            val newRepetitions = if (rating == SrsStatus.Rating.HARD) 0 else srs.repetitions + 1
            val multiplier = when (rating) {
                SrsStatus.Rating.EASY -> 1.5
                SrsStatus.Rating.GOOD -> 1.2
                SrsStatus.Rating.HARD -> 0.8
            }
            val newInterval = max(1.0, srs.intervalDays * multiplier)
            val updatedSrs = srs.copy(
                repetitions = newRepetitions,
                intervalDays = newInterval
            )
            cards[index] = current.copy(srsStatus = updatedSrs)
            reviewedIds.add(id)
        }
    }

    override suspend fun getAllCards(): List<Vocabulary> = mutex.withLock {
        cards.toList()
    }
}
