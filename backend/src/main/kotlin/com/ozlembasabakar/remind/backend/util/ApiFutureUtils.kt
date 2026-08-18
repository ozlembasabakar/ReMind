package com.ozlembasabakar.remind.backend.util

import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.ApiFutures
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> ApiFuture<T>.await(): T = suspendCancellableCoroutine { continuation ->
    ApiFutures.addCallback(this, object : ApiFutureCallback<T> {
        override fun onSuccess(result: T?) {
            @Suppress("UNCHECKED_CAST")
            continuation.resume(result as T)
        }

        override fun onFailure(t: Throwable) {
            continuation.resumeWithException(t)
        }
    }, MoreExecutors.directExecutor())

    continuation.invokeOnCancellation {
        this.cancel(true)
    }
}
