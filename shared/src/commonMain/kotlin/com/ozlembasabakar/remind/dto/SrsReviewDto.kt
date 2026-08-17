package com.ozlembasabakar.remind.dto

import kotlinx.serialization.Serializable

@Serializable
data class SrsReviewRequestDto(
    val wordId: String,
    val rating: String
)
