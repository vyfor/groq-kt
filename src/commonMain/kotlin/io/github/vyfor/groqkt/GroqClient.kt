@file:Suppress("DuplicatedCode", "unused")

package io.github.vyfor.groqkt

import io.github.vyfor.groqkt.api.GroqRatelimit
import io.github.vyfor.groqkt.api.GroqResponse
import io.github.vyfor.groqkt.api.GroqResponseType
import io.github.vyfor.groqkt.api.GroqStreamingResponse
import io.github.vyfor.groqkt.api.audio.transcription.AudioTranscription
import io.github.vyfor.groqkt.api.audio.transcription.AudioTranscriptionRequest
import io.github.vyfor.groqkt.api.audio.translation.AudioTranslation
import io.github.vyfor.groqkt.api.audio.translation.AudioTranslationRequest
import io.github.vyfor.groqkt.api.chat.ChatCompletion
import io.github.vyfor.groqkt.api.chat.ChatCompletionRequest
import io.github.vyfor.groqkt.api.chat.StreamingChatCompletion
import io.github.vyfor.groqkt.api.model.Model
import io.github.vyfor.groqkt.api.model.Models
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/**
 * A client used to interact with the Groq API.
 *
 * @param apiKey The API key obtained from [here](https://console.groq.com/keys).
 * @param config Lambda used to configure the [GroqClient].
 */
@Suppress("MemberVisibilityCanBePrivate")
class GroqClient(
    apiKey: String,
    config: GroqConfigBuilder.() -> Unit,
) : AutoCloseable {
  private val config = GroqConfigBuilder(apiKey).apply(config).build()

  /**
   * Create a model response for the given chat conversation.
   *
   * @param data The chat completion request
   * @return Result<GroqResponse<ChatCompletion>>
   */
  suspend fun chat(data: ChatCompletionRequest) =
      config.client
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
  suspend fun chat(block: ChatCompletionRequest.Builder.() -> Unit) =
      config.client
          .post(ChatCompletionRequest.ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(
                ChatCompletionRequest.Builder()
                    .apply {
                      block()
                      stream = false
                    }
                    .build())
          }
          .parse<ChatCompletion>()

  /**
   * Stream a model response for the given chat conversation.
   *
   * @param data The chat completion request
   * @return GroqStreamingResponse<StreamingChatCompletion>
   */
  suspend fun chatStreaming(data: ChatCompletionRequest) =
      config.client
          .post(ChatCompletionRequest.ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(data.apply { stream = true })
          }
          .run {
            GroqStreamingResponse(
                parseHeaders(),
                flow {
                      bodyAsChannel().apply {
                        while (!isClosedForRead) {
                          val line =
                              readUTF8Line()?.removePrefix("data: ")?.takeIf { it.startsWith("{") }
                                  ?: continue
                          val chunk = json.decodeFromString<StreamingChatCompletion>(line)
                          emit(Result.success(chunk))
                        }
                      }
                    }
                    .flowOn(Dispatchers.Default)
                    .catch { e -> emit(Result.failure(e)) })
          }

  /**
   * Stream a model response for the given chat conversation.
   *
   * @param block The chat completion request
   * @return GroqStreamingResponse<StreamingChatCompletion>
   */
  suspend fun chatStreaming(block: ChatCompletionRequest.Builder.() -> Unit) =
      config.client
          .post(ChatCompletionRequest.ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(
                ChatCompletionRequest.Builder()
                    .apply {
                      block()
                      stream = true
                    }
                    .build())
          }
          .run {
            GroqStreamingResponse(
                parseHeaders(),
                flow {
                      bodyAsChannel().apply {
                        while (!isClosedForRead) {
                          val line =
                              readUTF8Line()?.removePrefix("data: ")?.takeIf { it.startsWith("{") }
                                  ?: continue
                          val chunk = json.decodeFromString<StreamingChatCompletion>(line)
                          emit(Result.success(chunk))
                        }
                      }
                    }
                    .flowOn(Dispatchers.Default)
                    .catch { e -> emit(Result.failure(e)) })
          }

  /**
   * Translate an audio file
   *
   * @param data The audio to translate
   * @return Result<GroqResponse<AudioTranslation>>
   */
  suspend fun translateAudio(data: AudioTranslationRequest) =
      config.client
          .submitFormWithBinaryData(
              AudioTranslationRequest.ENDPOINT,
              formData {
                append(
                    "file",
                    data.file,
                    Headers.build {
                      append(
                          HttpHeaders.ContentDisposition,
                          "filename=\"${data.filename.encodeURLPathPart()}\"")
                    })
                append("model", data.model.id)
                data.prompt?.let { append("prompt", it) }
                data.responseFormat?.let { append("response_format", it.name) }
                data.temperature?.let { append("temperature", it.toString()) }
              }) {
                contentType(ContentType.MultiPart.FormData)
              }
          .parse<AudioTranslation>()

  /**
   * Translate an audio file
   *
   * @param block The audio to translate
   * @return Result<GroqResponse<AudioTranslation>>
   */
  suspend fun translateAudio(block: AudioTranslationRequest.Builder.() -> Unit) =
      config.client
          .submitFormWithBinaryData(
              AudioTranslationRequest.ENDPOINT,
              formData {
                AudioTranslationRequest.Builder().apply(block).build().let { data ->
                  append(
                      "file",
                      data.file,
                      Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${data.filename.encodeURLPathPart()}\"")
                      })
                  append("model", data.model.id)
                  data.prompt?.let { append("prompt", it) }
                  data.responseFormat?.let { append("response_format", it.name) }
                  data.temperature?.let { append("temperature", it.toString()) }
                }
              }) {
                contentType(ContentType.MultiPart.FormData)
              }
          .parse<AudioTranslation>()

  /**
   * Transcribe an audio file
   *
   * @param data The audio to transcribe
   * @return Result<GroqResponse<AudioTranscription>>
   */
  suspend fun transcribeAudio(data: AudioTranscriptionRequest) =
      config.client
          .submitFormWithBinaryData(
              AudioTranscriptionRequest.ENDPOINT,
              formData {
                data.file?.let {
                  append(
                      "file",
                      it,
                      Headers.build {
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${data.filename.encodeURLPathPart()}\"")
                      })
                }
                append("model", data.model.id)
                data.language?.let { append("language", it) }
                data.prompt?.let { append("prompt", it) }
                data.responseFormat?.let { append("response_format", it.name) }
                data.temperature?.let { append("temperature", it.toString()) }
                data.timestampGranularities?.let {
                  append(
                      "timestamp_granularities",
                      json.encodeToString(
                          buildJsonArray { it.forEach { enum -> add(JsonPrimitive(enum.value)) } }
                              .toString()))
                }
              }) {
                contentType(ContentType.MultiPart.FormData)
              }
          .parse<AudioTranscription>()

  /**
   * Transcribe an audio file
   *
   * @param block The audio to transcribe
   * @return Result<GroqResponse<AudioTranscription>>
   */
  suspend fun transcribeAudio(block: AudioTranscriptionRequest.Builder.() -> Unit) =
      config.client
          .submitFormWithBinaryData(
              AudioTranscriptionRequest.ENDPOINT,
              formData {
                AudioTranscriptionRequest.Builder().apply(block).build().let { data ->
                  data.file?.let {
                    append(
                        "file",
                        it,
                        Headers.build {
                          append(
                              HttpHeaders.ContentDisposition,
                              "filename=\"${data.filename.encodeURLPathPart()}\"")
                        })
                  }
                  append("model", data.model.id)
                  data.url?.let { append("url", it) }
                  data.language?.let { append("language", it) }
                  data.prompt?.let { append("prompt", it) }
                  data.responseFormat?.let { append("response_format", it.name) }
                  data.temperature?.let { append("temperature", it.toString()) }
                  data.timestampGranularities?.let {
                    append(
                        "timestamp_granularities",
                        json.encodeToString(
                            buildJsonArray { it.forEach { enum -> add(JsonPrimitive(enum.value)) } }
                                .toString()))
                  }
                }
              }) {
                contentType(ContentType.MultiPart.FormData)
              }
          .parse<AudioTranscription>()

  /**
   * Fetch a specific model
   *
   * @param model The model to fetch
   * @return Result<GroqResponse<Model>>
   */
  suspend fun fetchModel(model: String) = config.client.get("models/$model").parse<Model>()

  /**
   * Fetch a specific model
   *
   * @param model The model to fetch
   * @return Result<GroqResponse<Model>>
   */
  suspend fun fetchModel(model: GroqModel) = config.client.get("models/${model.id}").parse<Model>()

  /**
   * Fetch the list of available models
   *
   * @return List<Model>
   */
  suspend fun fetchModels() = config.client.get("models").validate<Models, List<Model>> { data }

  override fun close() {
    config.client.close()
  }

  companion object {
    const val BASE_URL: String = "https://api.groq.com/openai/v1/"
    @OptIn(ExperimentalSerializationApi::class)
    private val json: Json = Json {
      explicitNulls = false
      encodeDefaults = true
      ignoreUnknownKeys = true
      namingStrategy = JsonNamingStrategy.SnakeCase
      classDiscriminatorMode = ClassDiscriminatorMode.NONE
    }

    private suspend inline fun <reified T> HttpResponse.validate(): Result<T> {
      return if (status.isSuccess()) Result.success(body<T>())
      else Result.failure(ResponseException(this, status.description))
    }

    private suspend inline fun <reified T, R> HttpResponse.validate(block: T.() -> R): Result<R> {
      return if (status.isSuccess()) Result.success(block(body<T>()))
      else Result.failure(ResponseException(this, status.description))
    }

    private suspend inline fun <reified T> HttpResponse.parse(): Result<GroqResponse<T>> {
      return when (val result = body<GroqResponseType<T>>()) {
        is GroqResponseType.Ok ->
            Result.success(
                GroqResponse(data = result.data, xGroq = result.xGroq).apply {
                  ratelimit = parseHeaders()
                })
        is GroqResponseType.Error -> Result.failure(result.error)
      }
    }

    private fun HttpResponse.parseHeaders(): GroqRatelimit? {
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
            resetTokens = resetTokens ?: Duration.INFINITE)
      }
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
