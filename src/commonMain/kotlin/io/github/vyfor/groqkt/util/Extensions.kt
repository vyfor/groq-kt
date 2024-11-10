@file:Suppress("unused", "NAME_SHADOWING")

package io.github.vyfor.groqkt.util

import io.github.vyfor.groqkt.api.GroqRatelimit
import io.github.vyfor.groqkt.api.GroqResponse
import io.github.vyfor.groqkt.api.GroqResponseType
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal inline fun <reified T> T.applyDefaults(noinline defaults: (T.() -> Unit)?): T =
    defaults?.let { defaults -> apply(defaults) } ?: this

internal suspend inline fun <reified T> HttpResponse.validate(): Result<T> =
    if (status.isSuccess()) {
      Result.success(body<T>())
    } else {
      Result.failure(ResponseException(this, status.description))
    }

internal suspend inline fun <reified T, R> HttpResponse.validate(block: T.() -> R): Result<R> =
    if (status.isSuccess()) {
      Result.success(block(body<T>()))
    } else {
      Result.failure(ResponseException(this, status.description))
    }

internal suspend inline fun <reified T> HttpResponse.parse(): Result<GroqResponse<T>> =
    when (val result = body<GroqResponseType<T>>()) {
      is GroqResponseType.Ok ->
          Result.success(
              GroqResponse(
                      data = result.data,
                      xGroq = result.xGroq,
                  )
                  .apply { ratelimit = parseHeaders() },
          )
      is GroqResponseType.Error -> Result.failure(result.error)
    }

internal fun HttpResponse.parseHeaders(): GroqRatelimit? {
  val limitRequests = headers["x-ratelimit-limit-requests"]?.toIntOrNull()
  val limitTokens = headers["x-ratelimit-limit-tokens"]?.toIntOrNull()
  val remainingRequests = headers["x-ratelimit-remaining-requests"]?.toIntOrNull()
  val remainingTokens = headers["x-ratelimit-remaining-tokens"]?.toIntOrNull()
  val resetRequests = headers["x-ratelimit-reset-requests"]?.parseDuration()
  val resetTokens = headers["x-ratelimit-reset-tokens"]?.parseDuration()

  return limitRequests?.let {
    GroqRatelimit(
        limitRequests = limitRequests,
        limitTokens = limitTokens ?: -1,
        remainingRequests = remainingRequests ?: -1,
        remainingTokens = remainingTokens ?: -1,
        resetRequests = resetRequests ?: Duration.INFINITE,
        resetTokens = resetTokens ?: Duration.INFINITE,
    )
  }
}

internal fun String.parseDuration(): Duration =
    when {
      // 123ms
      contains("ms") -> {
        val millis = substring(0, length - 2).toLongOrNull()
        millis?.milliseconds ?: Duration.ZERO
      }
      // 1m2.34s
      contains("m") && contains("s") -> {
        val mins = substringBefore("m").toLongOrNull()
        val secs = substringAfter("m").substringBefore("s").toDoubleOrNull()
        mins?.minutes?.let { secs?.seconds?.plus(it) } ?: Duration.ZERO
      }
      // 1s
      contains("s") -> {
        val secs = replace("s", "").toDoubleOrNull()
        secs?.seconds ?: Duration.ZERO
      }
      else -> {
        Duration.ZERO
      }
    }
