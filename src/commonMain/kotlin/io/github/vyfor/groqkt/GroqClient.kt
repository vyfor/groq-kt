@file:Suppress("unused")

package io.github.vyfor.groqkt

import io.github.vyfor.groqkt.api.CompleteGroqResponse
import io.github.vyfor.groqkt.api.GroqRatelimit
import io.github.vyfor.groqkt.api.GroqResponse
import io.github.vyfor.groqkt.api.audio.transcription.AudioTranscription
import io.github.vyfor.groqkt.api.audio.transcription.AudioTranscriptionRequest
import io.github.vyfor.groqkt.api.audio.translation.AudioTranslation
import io.github.vyfor.groqkt.api.audio.translation.AudioTranslationRequest
import io.github.vyfor.groqkt.api.chat.ChatCompletion
import io.github.vyfor.groqkt.api.chat.ChatCompletionRequest
import io.github.vyfor.groqkt.api.chat.StreamingChatCompletion
import io.github.vyfor.groqkt.api.model.Model
import io.github.vyfor.groqkt.api.model.Models
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Suppress("MemberVisibilityCanBePrivate")
@OptIn(ExperimentalSerializationApi::class, ExperimentalStdlibApi::class)
class GroqClient(
  private val apiKey: String,
  private val client: HttpClient = HttpClient {
    install(ContentNegotiation) {
      json(
        Json {
          explicitNulls = false
          encodeDefaults = true
          ignoreUnknownKeys = true
          namingStrategy = JsonNamingStrategy.SnakeCase
        }
      )
    }
    
    install(HttpRequestRetry) {
      retryIf(1) { _, response ->
        when(response.status.value) {
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
               false
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
) : AutoCloseable {
  
  /**
   * Create a model response for the given chat conversation.
   *
   * @param data The chat completion request
   * @return Result<GroqResponse<ChatCompletion>>
   */
  suspend fun chat(data: ChatCompletionRequest) = client
    .post(ChatCompletionRequest.ENDPOINT) {
      contentType(ContentType.Application.Json)
      setBody(data.apply { stream = false })
    }
    .parse<ChatCompletion>()
  
  /**
   * Create a model response for the given chat conversation.
   *
   * @param block The chat completion request
   * @return Result<GroqResponse<ChatCompletion>>
   */
  suspend fun chat(block: ChatCompletionRequest.Builder.() -> Unit) = client
    .post(ChatCompletionRequest.ENDPOINT) {
      contentType(ContentType.Application.Json)
      setBody(ChatCompletionRequest.Builder().apply {
        block()
        stream = false
      })
    }
    .parse<ChatCompletion>()
  
  /**
   * Stream a model response for the given chat conversation.
   *
   * @param data The chat completion request
   * @return Result<GroqResponse<ChatCompletion>>
   */
  suspend fun chatStreaming(data: ChatCompletionRequest) = flow {
    client
      .post(ChatCompletionRequest.ENDPOINT) {
        contentType(ContentType.Application.Json)
        setBody(data.apply { stream = true })
      }
      .bodyAsChannel()
      .apply {
        while (!isClosedForRead) {
          val line = readUTF8Line() ?: break
          val chunk = Json.decodeFromString<StreamingChatCompletion>(line)
          emit(chunk)
        }
      }
  }.flowOn(Dispatchers.IO)
  
  /**
   * Stream a model response for the given chat conversation.
   *
   * @param block The chat completion request
   * @return Result<GroqResponse<ChatCompletion>>
   */
  suspend fun chatStreaming(block: ChatCompletionRequest.Builder.() -> Unit) = flow {
    client
      .post(ChatCompletionRequest.ENDPOINT) {
        contentType(ContentType.Application.Json)
        setBody(ChatCompletionRequest.Builder().apply {
          block()
          stream = true
        })
      }
      .bodyAsChannel()
      .apply {
        while (!isClosedForRead) {
          val line = readUTF8Line() ?: break
          val chunk = Json.decodeFromString<StreamingChatCompletion>(line)
          emit(chunk)
        }
      }
  }.flowOn(Dispatchers.IO)
  
  /**
   * Translate an audio file
   *
   * @param data The audio to translate
   * @return Result<GroqResponse<AudioTranslation>>
   */
  suspend fun translateAudio(data: AudioTranslationRequest) = client
    .post(AudioTranslationRequest.ENDPOINT) {
      contentType(ContentType.MultiPart.FormData)
      setBody(data)
    }
    .parse<AudioTranslation>()
  
  /**
   * Translate an audio file
   *
   * @param block The audio to translate
   * @return Result<GroqResponse<AudioTranslation>>
   */
  suspend fun translateAudio(block: AudioTranslationRequest.Builder.() -> Unit) = client
    .post(AudioTranslationRequest.ENDPOINT) {
      contentType(ContentType.MultiPart.FormData)
      setBody(AudioTranslationRequest.Builder().apply(block).build())
    }
    .parse<AudioTranslation>()
  
  
  /**
   * Transcribe an audio file
   *
   * @param data The audio to transcribe
   * @return Result<GroqResponse<AudioTranscription>>
   */
  suspend fun transcribeAudio(data: AudioTranscriptionRequest) = client
    .post(AudioTranscriptionRequest.ENDPOINT) {
      contentType(ContentType.MultiPart.FormData)
      setBody(data)
    }
    .parse<AudioTranscription>()
  
  /**
   * Transcribe an audio file
   *
   * @param block The audio to transcribe
   * @return Result<GroqResponse<AudioTranscription>>
   */
  suspend fun transcribeAudio(block: AudioTranscriptionRequest.Builder.() -> Unit) = client
    .post(AudioTranslationRequest.ENDPOINT) {
      contentType(ContentType.MultiPart.FormData)
      setBody(AudioTranscriptionRequest.Builder().apply(block).build())
    }
    .parse<AudioTranscription>()
  
  /**
   * Fetch a specific model
   *
   * @param model The model to fetch
   * @return Result<GroqResponse<Model>>
   */
  suspend fun fetchModel(model: String) = client
    .get("models/$model")
    .parse<Model>()
  
  /**
   * Fetch a specific model
   *
   * @param model The model to fetch
   * @return Result<GroqResponse<Model>>
   */
  suspend fun fetchModel(model: GroqModel) = client
    .get("models/${model.id}")
    .body<Model>()
  
  /**
   * Fetch the list of available models
   * @return List<Model>
   */
  suspend fun fetchModels() = client
    .get("models")
    .body<Models>()
    .data
  
  override fun close() {
    client.close()
  }
  
  companion object {
    const val BASE_URL: String = "https://api.groq.com/openai/v1/"
    
    private suspend fun <T> HttpResponse.parse(): Result<GroqResponse<T>> {
      val body = body<CompleteGroqResponse<T>>()
      return if (body.error == null) Result.success(
        GroqResponse(
          data = body.data,
          xGroq = body.xGroq,
        ).apply {
          val limitRequests = headers["x-ratelimit-limit-requests"]?.toIntOrNull()
          val limitTokens = headers["x-ratelimit-limit-tokens"]?.toIntOrNull()
          val remainingRequests = headers["x-ratelimit-remaining-requests"]?.toIntOrNull()
          val remainingTokens = headers["x-ratelimit-remaining-tokens"]?.toIntOrNull()
          val resetRequests = headers["x-ratelimit-reset-requests"]?.parseDuration()
          val resetTokens = headers["x-ratelimit-reset-tokens"]?.parseDuration()
          
          if (limitRequests != null)
            ratelimit = GroqRatelimit(
              limitRequests = limitRequests,
              limitTokens = limitTokens ?: -1,
              remainingRequests = remainingRequests ?: -1,
              remainingTokens = remainingTokens ?: -1,
              resetRequests = resetRequests ?: Duration.INFINITE,
              resetTokens = resetTokens ?: Duration.INFINITE
            )
        }
      ) else Result.failure(body.error)
    }
    
    private fun String.parseDuration(): Duration {
      return when {
        /* 123ms */
        contains("ms") -> {
          val millis = substring(0, length - 2).toLongOrNull()
          millis?.milliseconds ?: Duration.ZERO
        }
        /* 1m2.34s */
        contains("m") && contains("s") -> {
          val mins = substringBefore("m").toLongOrNull()
          val secs = substringAfter("m").substringBefore("s").toDoubleOrNull()
          mins?.minutes?.let { secs?.seconds?.plus(it) } ?: Duration.ZERO
        }
        /* 1s */
        contains("s") -> {
          val secs = replace("s", "").toDoubleOrNull()
          secs?.seconds ?: Duration.ZERO
        }
        else -> {
          Duration.ZERO
        }
      }
    }
  }
}