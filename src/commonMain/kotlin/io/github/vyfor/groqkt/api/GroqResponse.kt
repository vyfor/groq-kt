package io.github.vyfor.groqkt.api

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class CompleteGroqResponse<T>(
  val data: T,
  val xGroq: XGroq?,
  val error: GroqError?,
)

@Serializable
data class GroqResponse<T>(
  val data: T,
  val xGroq: XGroq?,
) {
  var ratelimit: GroqRatelimit? = null
}

@Serializable
data class XGroq(
  val id: String,
)

@Serializable
data class GroqRatelimit(
  val limitRequests: Int,
  val limitTokens: Int,
  val remainingRequests: Int,
  val remainingTokens: Int,
  val resetRequests: Duration,
  val resetTokens: Duration,
)

/**
 * The primary container for error details.
 *
 * @param message A descriptive message explaining the nature of the error, intended to aid developers in diagnosing the problem.
 * @param type A classification of the error type, such as `invalid_request_error`, indicating the general category of the problem encountered.
 */
@Serializable
data class GroqError(
  override val message: String,
  val type: String,
) : Throwable(message)

@DslMarker
annotation class GroqDsl