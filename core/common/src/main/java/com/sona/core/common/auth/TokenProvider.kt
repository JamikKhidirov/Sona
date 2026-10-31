package com.sona.core.common.auth

/** Provides the optional Audius OAuth bearer token to the network layer. */
interface TokenProvider {
    fun token(): String?
}
