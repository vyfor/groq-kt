@file:Suppress("DuplicatedCode", "unused")

package io.github.vyfor.groqkt

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
import io.github.vyfor.groqkt.util.parse
import io.github.vyfor.groqkt.util.parseHeaders
import io.github.vyfor.groqkt.util.validate
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray

/**
 * A client used to interact with the Groq API.
 *
 * @param apiKey The API key obtained from [here](https://console.groq.com/keys).
 * @param config Lambda used to configure the [GroqClient].
 */
@Suppress("MemberVisibilityCanBePrivate")
class GroqClient(
    apiKey: String,
    config: GroqConfigBuilder.() -> Unit = {},
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
                    .build(),
            )
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
                          val chunk = config.json.decodeFromString<StreamingChatCompletion>(line)
                          emit(Result.success(chunk))
                        }
                      }
                    }
                    .flowOn(Dispatchers.Default)
                    .catch { e -> emit(Result.failure(e)) },
            )
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
                    .build(),
            )
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
                          val chunk = config.json.decodeFromString<StreamingChatCompletion>(line)
                          emit(Result.success(chunk))
                        }
                      }
                    }
                    .flowOn(Dispatchers.Default)
                    .catch { e -> emit(Result.failure(e)) },
            )
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
                    },
                )
                append("model", data.model.id)
                data.prompt?.let { append("prompt", it) }
                data.responseFormat?.let { append("response_format", it.name) }
                data.temperature?.let { append("temperature", it.toString()) }
              },
          ) {
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
                      },
                  )
                  append("model", data.model.id)
                  data.prompt?.let { append("prompt", it) }
                  data.responseFormat?.let { append("response_format", it.name) }
                  data.temperature?.let { append("temperature", it.toString()) }
                }
              },
          ) {
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
                      },
                  )
                }
                append("model", data.model.id)
                data.language?.let { append("language", it) }
                data.prompt?.let { append("prompt", it) }
                data.responseFormat?.let { append("response_format", it.name) }
                data.temperature?.let { append("temperature", it.toString()) }
                data.timestampGranularities?.let {
                  append(
                      "timestamp_granularities",
                      config.json.encodeToString(
                          buildJsonArray { it.forEach { enum -> add(JsonPrimitive(enum.value)) } }
                              .toString(),
                      ),
                  )
                }
              },
          ) {
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
                        },
                    )
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
                        config.json.encodeToString(
                            buildJsonArray { it.forEach { enum -> add(JsonPrimitive(enum.value)) } }
                                .toString(),
                        ),
                    )
                  }
                }
              },
          ) {
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
  }
}
