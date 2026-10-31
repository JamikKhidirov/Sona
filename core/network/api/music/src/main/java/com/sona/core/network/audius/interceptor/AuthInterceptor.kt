package com.sona.core.network.audius.interceptor

import com.sona.core.common.auth.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.token()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
