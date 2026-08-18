package com.ozlembasabakar.remind.data.remote

import com.ozlembasabakar.remind.defaultBaseUrl

object ApiConfig {
    var baseUrl: String = defaultBaseUrl
        private set

    fun init(customBaseUrl: String) {
        baseUrl = customBaseUrl.trimEnd('/')
    }
}
