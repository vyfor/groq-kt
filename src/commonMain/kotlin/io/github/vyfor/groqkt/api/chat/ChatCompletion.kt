package io.github.vyfor.groqkt.api.chat

import io.github.vyfor.groqkt.GroqModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Chat completion object.
 *
 * @property id Unique identifier for the chat completion.
 * @property obj Type of object returned, typically `chat.completion`.
 * @property created Timestamp of when the chat completion was created.
 * @property model The model used for generating the completion.
 * @property choices List of choices containing the generated responses.
 * @property usage Token usage details for the chat completion.
 * @property systemFingerprint System fingerprint for tracking.
 */
@Serializable
data class ChatCompletion(
  val id: String,
  @SerialName("object")
  val obj: String,
  val created: Long,
  val model: GroqModel,
  val choices: List<ChatCompletionChoice>,
  val usage: ChatCompletionUsage?,
  val systemFingerprint: String? = null,
)

/**
 * Streaming chat completion object.
 *
 * @property id Unique identifier for the streaming chat completion.
 * @property obj Type of object returned, typically `chat.completion.chunk`.
 * @property created Timestamp of when the streaming chat completion was created.
 * @property model The model used for generating the completion.
 * @property choices List of choices containing the generated responses, streamed incrementally.
 * @property usage Token usage details for the streaming chat completion.
 * @property systemFingerprint System fingerprint for tracking.
 */
@Serializable
data class StreamingChatCompletion(
  val id: String,
  @SerialName("object")
  val obj: String,
  val created: Long,
  val model: GroqModel,
  val choices: List<StreamingChatCompletionChoice>,
  val usage: ChatCompletionUsage?,
  val systemFingerprint: String? = null,
)

/**
 * Chat completion choice.
 *
 * @property index Index of the choice in the list of choices.
 * @property message The message content of the choice.
 * @property finishReason Reason for the completion's termination, e.g., `stop`.
 */
@Serializable
data class ChatCompletionChoice(
  val index: Int,
  val message: ChatCompletionMessage,
  val finishReason: String,
)

/**
 * Chat completion message returned by the model.
 *
 * @property role The role of the message sender, e.g., `assistant`.
 * @property content The content of the message.
 */
@Serializable
data class ChatCompletionMessage(
  val role: String,
  val content: String,
)

/**
 * Streaming chat completion choice.
 *
 * @property index Index of the choice in the list of choices.
 * @property delta The message content, streamed incrementally.
 * @property finishReason Optional reason for the completion's termination.
 */
@Serializable
data class StreamingChatCompletionChoice(
  val index: Int,
  val delta: ChatCompletionDelta,
  val finishReason: String?,
)

/**
 * Delta (incremental update) in a streaming chat completion.
 *
 * @property role Optional role of the message sender, which may not be present in every delta.
 * @property content The content of the message, streamed incrementally.
 */
@Serializable
data class ChatCompletionDelta(
  val role: String?,
  val content: String?,
)

/**
 * Token usage details.
 *
 * @property queueTime Time taken to process the request in seconds.
 * @property promptTokens Number of tokens used in the input prompt.
 * @property promptTime Time taken to generate the prompt in seconds.
 * @property completionTokens Number of tokens used in the completion response.
 * @property completionTime Time taken to generate the completion response in seconds.
 * @property totalTokens Total number of tokens used.
 * @property totalTime Total time taken in seconds.
 */
@Serializable
data class ChatCompletionUsage(
  val queueTime: Double,
  val promptTokens: Int,
  val promptTime: Double,
  val completionTokens: Int,
  val completionTime: Double,
  val totalTokens: Int,
  val totalTime: Double,
)
