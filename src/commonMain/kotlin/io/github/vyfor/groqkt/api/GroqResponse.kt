package io.github.vyfor.groqkt.api

import io.github.vyfor.groqkt.api.chat.ChatCompletionUsage
import io.github.vyfor.groqkt.api.shared.XGroq
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.time.Duration

@Serializable(GroqResponseSerializer::class)
sealed class GroqResponseType<out T> {
  data class Ok<out T>(val data: T, val usage: ChatCompletionUsage?,  val xGroq: XGroq?) : GroqResponseType<T>()
  data class Error(val error: GroqError) : GroqResponseType<Nothing>()
}

@Serializable
data class GroqResponse<T>(
  val data: T,
  val xGroq: XGroq?,
) {
  var ratelimit: GroqRatelimit? = null
}

data class GroqStreamingResponse<T>(
  val ratelimit: GroqRatelimit?,
  val data: Flow<Result<T>>,
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

class GroqResponseSerializer<T>(private val dataSerializer: KSerializer<T>) : KSerializer<GroqResponseType<T>> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Response") {
    element("Ok", dataSerializer.descriptor)
    element("Error", buildClassSerialDescriptor("Error") {
      element<String>("message")
    })
  }
  
  override fun deserialize(decoder: Decoder): GroqResponseType<T> {
    require(decoder is JsonDecoder)
    
    val element = decoder.decodeJsonElement()
    if (element is JsonObject && "error" in element)
      return GroqResponseType.Error(
        element["error"]!!.jsonObject.run {
          GroqError(
            get("message")!!.jsonPrimitive.content,
            get("type")!!.jsonPrimitive.content
          )
        }
      )
    
    return GroqResponseType.Ok(
      decoder.json.decodeFromJsonElement(dataSerializer, element),
      element.jsonObject["usage"]?.jsonObject?.let {
        decoder.json.decodeFromJsonElement(ChatCompletionUsage.serializer(), it)
      },
      element.jsonObject["x_groq"]?.let {
        decoder.json.decodeFromJsonElement(XGroq.serializer(), it)
      }
    )
  }
  
  override fun serialize(encoder: Encoder, value: GroqResponseType<T>) {
    error("Unreachable")
  }
}