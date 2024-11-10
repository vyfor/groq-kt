@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.vyfor.groqkt

import io.github.vyfor.groqkt.GroqClient.Companion.BASE_URL
import io.github.vyfor.groqkt.api.audio.transcription.AudioTranscriptionRequest
import io.github.vyfor.groqkt.api.audio.translation.AudioTranslationRequest
import io.github.vyfor.groqkt.api.chat.ChatCompletionRequest
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * Groq configuration class.
 *
 * @property json The JSON serializer.
 * @property client The HTTP client.
 * @property defaults The default values for the [GroqClient]. These values are applied to every
 *   request made using a DSL function.
 */
data class GroqConfig(
    val json: Json,
    val client: HttpClient,
    val defaults: GroqDefaults?,
)

/**
 * Default values for use with the [GroqClient]. These values are applied to every request made
 * using a DSL function.
 *
 * @property chatCompletion The default values for [ChatCompletionRequest].
 * @property audioTranslation The default values for [AudioTranslationRequest].
 * @property audioTranscription The default values for [AudioTranscriptionRequest].
 */
data class GroqDefaults(
    val chatCompletion: (ChatCompletionRequest.Builder.() -> Unit)? = null,
    val audioTranslation: (AudioTranslationRequest.Builder.() -> Unit)? = null,
    val audioTranscription: (AudioTranscriptionRequest.Builder.() -> Unit)? = null,
)

/**
 * Groq configuration builder class.
 *
 * @param apiKey The Groq API key obtained from [here](https://console.groq.com/keys).
 * @property client The HTTP client. Do not modify unless you know what you're doing.
 */
class GroqConfigBuilder(
    apiKey: String,
) {
  @OptIn(ExperimentalSerializationApi::class)
  private val json: Json = Json {
    explicitNulls = false
    encodeDefaults = true
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase
    classDiscriminatorMode = ClassDiscriminatorMode.NONE
  }
  private var defaults: GroqDefaults? = null
  var client: HttpClient = HttpClient {
    install(ContentNegotiation) { json(json) }

    install(HttpRequestRetry) {
      retryIf(1) { _, response ->
        when (response.status.value) {
          // retry on server errors
          in 500..599 -> {
            constantDelay()
            true
          }

          // handle ratelimits (retry-after)
          HttpStatusCode.TooManyRequests.value -> {
            constantDelay(
                response.headers["retry-after"]?.toLongOrNull()?.times(1000) ?: 0,
                0,
                false,
            )
            true
          }
          else -> false
        }
      }
    }

    install(DefaultRequest) {
      url(BASE_URL)
      header("Authorization", "Bearer $apiKey")
    }
  }

  /**
   * Sets the default values for the [GroqClient]. These values are applied to every request made
   * using a DSL function.
   *
   * @param block The default values for the [GroqClient].
   */
  fun defaults(block: GroqDefaultsBuilder.() -> Unit) {
    defaults = GroqDefaultsBuilder().apply(block).build()
  }

  internal fun build(): GroqConfig =
      GroqConfig(
          json,
          client,
          defaults,
      )
}

/**
 * Groq defaults builder class.
 *
 * @property chatCompletion The default values for [ChatCompletionRequest].
 * @property audioTranslation The default values for [AudioTranslationRequest].
 * @property audioTranscription The default values for [AudioTranscriptionRequest].
 */
class GroqDefaultsBuilder {
  private var chatCompletion: (ChatCompletionRequest.Builder.() -> Unit)? = null
  private var audioTranslation: (AudioTranslationRequest.Builder.() -> Unit)? = null
  private var audioTranscription: (AudioTranscriptionRequest.Builder.() -> Unit)? = null

  fun chatCompletion(block: ChatCompletionRequest.Builder.() -> Unit) {
    chatCompletion = block
  }

  fun audioTranslation(block: AudioTranslationRequest.Builder.() -> Unit) {
    audioTranslation = block
  }

  fun audioTranscription(block: AudioTranscriptionRequest.Builder.() -> Unit) {
    audioTranscription = block
  }

  internal fun build(): GroqDefaults =
      GroqDefaults(
          chatCompletion,
          audioTranslation,
          audioTranscription,
      )
}
