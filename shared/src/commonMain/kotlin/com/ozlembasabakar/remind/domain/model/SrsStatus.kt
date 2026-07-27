package com.ozlembasabakar.remind.domain.model

data class SrsStatus(
    val repetitions: Int = 0,
    val intervalDays: Double = 1.0,
    val easeFactor: Double = 2.5,
    val lastReviewedAtEpochMs: Long = 0L,
    val nextReviewAtEpochMs: Long = 0L
) {
    enum class Rating {
        HARD, GOOD, EASY
    }
}
