package com.ozlembasabakar.remind.data.firebase

import com.ozlembasabakar.remind.domain.model.Vocabulary

expect suspend fun fetchFirestoreRestWords(): List<Vocabulary>?
