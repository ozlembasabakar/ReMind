package com.ozlembasabakar.remind.data.remote

import com.ozlembasabakar.remind.domain.model.Vocabulary

expect suspend fun fetchFirestoreRestWords(): List<Vocabulary>?
