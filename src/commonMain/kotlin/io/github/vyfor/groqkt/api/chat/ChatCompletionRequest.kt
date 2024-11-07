@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.vyfor.groqkt.api.chat

import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.shared.GroqDsl
import io.github.vyfor.groqkt.api.chat.CompletionToolChoice.Auto
import io.github.vyfor.groqkt.api.chat.CompletionToolChoice.Function
import io.github.vyfor.groqkt.api.chat.CompletionToolChoice.None
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

/**
 * Data used for creating a model response for the given chat conversation.
 *
 * @param frequencyPenalty Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
 * @param functionCall Controls which (if any) function is called by the model.
 * @param functions A list of functions the model may generate JSON inputs for.
 * @param maxTokens The maximum number of tokens that can be generated in the chat completion. The total length of input tokens and generated tokens is limited by the model's context length.
 * @param messages A list of messages comprising the conversation so far.
 * @param model The model to use for chat completion.
 * @param n How many chat completion choices to generate for each input message. Note that the current moment, only a value of `n = 1` is supported. Other values will result in a 400 response.
 * @param parallelToolCalls Whether to enable parallel function calling during tool use.
 * @param presencePenalty Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
 * @param responseFormat The format in which the response should be returned. When using JSON mode, you must also instruct the model to produce JSON yourself via a system or user message.
 * @param seed If specified, our system will make the best effort to sample deterministically, such that repeated requests with the same seed and parameters should return the same result. Determinism is not guaranteed, and you should refer to the `system_fingerprint` response parameter to monitor changes in the backend.
 * @param stop Up to 4 sequences where the API will stop generating further tokens. The returned text will not contain the stop sequence.
 * @param stream If set, partial message deltas will be sent.
 * @param streamOptions Options for streaming response. Only set this when you set [stream] to true.
 * @param temperature What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. It is generally recommended to alter either this or [topP] but not both.
 * @param toolChoice Controls which (if any) tool is called by the model.
 * @param tools A list of tools the model may call. Use this to provide a list of functions the model may generate JSON inputs for. A max of 128 functions are supported.
 * @param topP An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with [topP] probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered. It is generally recommended to alter either this or temperature but not both.
 * @param user A unique identifier representing your end-user, which can help Groq monitor and detect abuse.
 */
@Serializable
data class ChatCompletionRequest(
  val frequencyPenalty: Double? = null,
  @Deprecated("Deprecated in the Groq API", ReplaceWith("toolChoice"))
  val functionCall: CompletionFunctionCallType? = null,
  val functions: List<CompletionFunction>? = null,
  /* val logitBias: GroqLogitBias? = null, */           // not supported
  /* val logprobs: Boolean? = null, */                  // not supported
  val maxTokens: Int? = null,
  val messages: List<CompletionMessage>,
  val model: GroqModel,
  val n: Int? = null,
  val parallelToolCalls: Boolean? = null,
  var presencePenalty: Double? = null,
  val responseFormat: CompletionResponseFormat? = null,
  val seed: Int? = null,
  val stop: List<String>? = null,
  var stream: Boolean? = null,
  val streamOptions: CompletionStreamOptions? = null,
  var temperature: Double? = null,
  val toolChoice: CompletionToolChoice? = null,
  val tools: List<CompletionTool>? = null,
  /* val topLogprobs: Int? = null, */                   // not supported
  var topP: Double? = null,
  val user: String? = null,
) {
  companion object {
    const val ENDPOINT = "chat/completions"
  }
  
  init {
    require(n == null || n == 1) { "Currently only n = 1 is supported." }
    require(streamOptions == null || stream == true) { "streamOptions must have stream = true." }
    require(tools == null || tools.size <= 128) { "Currently only up to 128 tools are supported." }
    require(messages.isNotEmpty()) { "messages must not be empty." }
    presencePenalty = presencePenalty?.coerceIn(-2.0, 2.0)
    temperature = temperature?.coerceIn(-2.0, 2.0)
    topP = topP?.coerceIn(0.0, 1.0)
  }
  
  /**
   * Convenient builder for `ChatCompletionRequest`.
   *
   * @property frequencyPenalty Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
   * @property functionCall Controls which (if any) function is called by the model.
   * @property functions A list of functions the model may generate JSON inputs for.
   * @property maxTokens The maximum number of tokens that can be generated in the chat completion. The total length of input tokens and generated tokens is limited by the model's context length.
   * @property messages A list of messages comprising the conversation so far.
   * @property model The model to use for chat completion.
   * @property n How many chat completion choices to generate for each input message. Note that the current moment, only a value of `n = 1` is supported. Other values will result in a 400 response.
   * @property parallelToolCalls Whether to enable parallel function calling during tool use.
   * @property presencePenalty Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
   * @property responseFormat The format in which the response should be returned. When using JSON mode, you must also instruct the model to produce JSON yourself via a system or user message.
   * @property seed If specified, our system will make the best effort to sample deterministically, such that repeated requests with the same seed and parameters should return the same result. Determinism is not guaranteed, and you should refer to the `system_fingerprint` response parameter to monitor changes in the backend. TODO system_fingerprint
   * @property stop Up to 4 sequences where the API will stop generating further tokens. The returned text will not contain the stop sequence.
   * @property stream If set, partial message deltas will be sent.
   * @property streamOptions Options for streaming response. Only set this when you set [stream] to true.
   * @property temperature What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. It is generally recommended to alter either this or [topP] but not both.
   * @property toolChoice Controls which (if any) tool is called by the model.
   * @property tools A list of tools the model may call. Use this to provide a list of functions the model may generate JSON inputs for. A max of 128 functions are supported.
   * @property topP An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with [topP] probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered. It is generally recommended to alter either this or temperature but not both.
   * @property user A unique identifier representing your end-user, which can help Groq monitor and detect abuse.
   */
  @GroqDsl
  class Builder {
    var frequencyPenalty: Double? = null
    var functionCall: CompletionFunctionCallType? = null
    var functions: MutableList<CompletionFunction>? = null
    var maxTokens: Int? = null
    var messages: MutableList<CompletionMessage>? = null
    var model: GroqModel? = null
    var n: Int? = null
    var parallelToolCalls: Boolean? = null
    var presencePenalty: Double? = null
    var responseFormat: CompletionResponseFormat? = null
    var seed: Int? = null
    var stop: MutableList<String>? = null
    var stream: Boolean? = null
    var streamOptions: CompletionStreamOptions? = null
    var temperature: Double? = null
    var toolChoice: CompletionToolChoice? = null
    var tools: MutableList<CompletionTool>? = null
    var topP: Double? = null
    var user: String? = null
    
    fun noFunctionCalls() {
      functionCall = CompletionFunctionCallType.None
    }
    
    fun autoFunctionCalls() {
      functionCall = CompletionFunctionCallType.Auto
    }
    
    fun functionCall(name: String) {
      functionCall = CompletionFunctionCallType.Call(name)
    }
    
    fun function(block: CompletionFunction.Builder.() -> Unit) {
      CompletionFunction.Builder().apply(block).build().apply {
        functions?.add(this) ?: run {
          functions = mutableListOf(this)
        }
      }
    }
    
    fun messages(block: ChatCompletionMessageBuilder.() -> Unit) {
      messages = ChatCompletionMessageBuilder().apply(block).messages
    }
    
    fun stops(vararg sequences: String) {
      stop = sequences.toMutableList()
    }
    
    fun tool(tool: CompletionFunction.Builder.() -> Unit) {
      CompletionTool(CompletionFunction.Builder().apply(tool).build()).apply {
        tools?.add(this) ?: run {
          tools = mutableListOf(this)
        }
      }
    }
    
    fun build(): ChatCompletionRequest {
      return ChatCompletionRequest(
        frequencyPenalty,
        functionCall,
        functions,
        maxTokens,
        requireNotNull(messages) { "messages must be set" },
        requireNotNull(model) { "model must be set" },
        n,
        parallelToolCalls,
        presencePenalty,
        responseFormat,
        seed,
        stop,
        stream,
        streamOptions,
        temperature,
        toolChoice,
        tools,
        topP,
        user
      )
    }
  }
}

@GroqDsl
class ChatCompletionMessageBuilder {
  var messages: MutableList<CompletionMessage> = mutableListOf()
  
  fun system(content: String, name: String? = null) { messages.add(CompletionMessage.System(content, name)) }
  fun text(content: String) { messages.add(CompletionMessage.User(UserMessageType.Text(content))) }
  fun image(image: String) { messages.add(CompletionMessage.User(UserMessageType.Array(imageContent = UserMessageImage(UserMessageImage.Image(url = image))))) }
  fun user(content: String?, image: String?, name: String? = null) { messages.add(CompletionMessage.User(UserMessageType.Array(UserMessageText(content), UserMessageImage(UserMessageImage.Image(url = image))), name)) }
  fun assistant(content: String, name: String? = null, functionCall: CompletionFunctionCall? = null, toolCalls: List<CompletionToolCall>? = null) { messages.add(CompletionMessage.Assistant(content, functionCall, name, toolCalls)) }
  fun tool(content: String, toolCallId: String) { messages.add(CompletionMessage.Tool(content, toolCallId)) }
  @Deprecated("Deprecated in the Groq API", ReplaceWith("tool"))
  @Suppress("DEPRECATION")
  fun function(content: String, name: String) { messages.add(CompletionMessage.Function(content, name)) }
}

/**
 * Controls which function is called by the model.
 *
 * @property None No function is called.
 * @property Auto Let the model pick between generating a message or calling a function.
 * @property Function Call the specified function.
 */
@Serializable(CompletionFunctionCallType.Serializer::class)
sealed class CompletionFunctionCallType(val name: String) {
  /**
   * The model will not call a function and instead generates a message.
   */
  data object None : CompletionFunctionCallType("none")
  
  /**
   * The model can pick between generating a message or calling a function.
   */
  data object Auto : CompletionFunctionCallType("auto")
  
  /**
   * Forces the model to call the specified function.
   *
   * @param functionName The name of the function to call.
   */
  data class Call(val functionName: String) : CompletionFunctionCallType(functionName)
  
  internal companion object Serializer : KSerializer<CompletionFunctionCallType> {
    override val descriptor = PrimitiveSerialDescriptor("GroqFunctionCallType", PrimitiveKind.STRING)
    
    override fun deserialize(decoder: Decoder): CompletionFunctionCallType {
      error("Unreachable")
    }
    
    override fun serialize(encoder: Encoder, value: CompletionFunctionCallType) {
      require(encoder is JsonEncoder)
      return when (value) {
        is Call -> encoder.encodeJsonElement(
          buildJsonObject {
            put("name", value.functionName)
          }
        )
        else -> encoder.encodeString(value.name)
      }
    }
  }
}

/**
 * A function that can be called by the model.
 *
 * @param name The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
 * @param description A description of what the function does, used by the model to choose when and how to call the function.
 * @param parameters The parameters the functions accepts, described as a JSON Schema object. See the docs on tool use for examples, and the JSON Schema reference for documentation about the format.
 *
 * @sample io.github.vyfor.groqkt.api.chat.CompletionFunction.exampleGroqFunction
 */
@Serializable
data class CompletionFunction(
  val name: String,
  val description: String? = null,
  val parameters: JsonObject? = null,
) {
  init {
    require(name.length <= 64) { "Function name must be <= 64" }
  }
  
  @GroqDsl
  class Builder {
    var name: String? = null
    var description: String? = null
    var parameters: JsonObject? = null
    
    fun build() = CompletionFunction(requireNotNull(name) { "function name is required" }, description, parameters)
  }
  
  companion object {
    private fun exampleGroqFunction() = CompletionFunction(
      name = "get_current_weather",
      description = "Get the current weather in a given location",
      parameters = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
          putJsonObject("location") {
            put("type", "string")
            put("description", "The location to get the weather for")
          }
          putJsonObject("unit") {
            put("type", "string")
            putJsonArray("enum") {
              add("celsius")
              add("fahrenheit")
            }
          }
        }
        putJsonArray("required") {
          add("location")
        }
      }
    )
  }
}

/**
 * Message object used for chat completion.
 */
@Serializable(CompletionMessage.Serializer::class)
//@Serializable
sealed class CompletionMessage(val role: String) {
  constructor() : this("")
  
  /**
   * System message
   *
   * @param content The content of the message.
   * @param name An optional name for the participant. Provides the model information to differentiate between participants of the same role.
   */
  @Serializable
  data class System(
    val content: String,
    val name: String? = null,
  ) : CompletionMessage("system")
  
  /**
   * User message
   *
   * @param content The content of the message.
   * @param name An optional name for the participant. Provides the model information to differentiate between participants of the same role.
   */
  @Serializable
  data class User(
    val content: UserMessageType,
    val name: String? = null,
  ) : CompletionMessage("user")
  
  /**
   * Assistant message
   *
   * @param content The contents of the assistant message. Required unless [toolCalls] or [functionCall] is specified.
   * @param functionCall The name and arguments of a function that should be called, as generated by the model.
   * @param name An optional name for the participant. Provides the model information to differentiate between participants of the same role.
   * @param toolCalls The tool calls generated by the model, such as function calls.
   */
  @Serializable
  data class Assistant(
    val content: String? = null,
    @Deprecated("Deprecated in the Groq API. Use `toolCalls` instead")
    val functionCall: CompletionFunctionCall? = null,
    val name: String? = null,
    val toolCalls: List<CompletionToolCall>? = null,
  ) : CompletionMessage("assistant")
  
  /**
   * Tool message
   *
   * @param content The content of the message.
   * @param toolCallId Tool call that this message is responding to.
   */
  @Serializable
  data class Tool(
    val content: String,
    val toolCallId: String,
  ) : CompletionMessage("tool")
  
  /**
   * Function message
   *
   * @param content The content of the message.
   * @param name The name of the function to call.
   */
  @Deprecated("Deprecated in the Groq API")
  @Serializable
  data class Function(
    val content: String,
    val name: String,
  ) : CompletionMessage("function")
  
  fun system(content: String, name: String? = null) = System(content, name)
  fun text(content: String) = User(UserMessageType.Text(content))
  fun image(image: String) = User(UserMessageType.Array(imageContent = UserMessageImage(UserMessageImage.Image(url = image))))
  fun user(content: String?, image: String?, name: String? = null) = User(UserMessageType.Array(UserMessageText(content), UserMessageImage(UserMessageImage.Image(url = image))), name)
  fun assistant(content: String, name: String? = null, functionCall: CompletionFunctionCall? = null, toolCalls: List<CompletionToolCall>? = null) = Assistant(content, functionCall, name, toolCalls)
  fun tool(content: String, toolCallId: String) = Tool(content, toolCallId)
  @Deprecated("Deprecated in the Groq API", ReplaceWith("tool"))
  @Suppress("DEPRECATION")
  fun function(content: String, name: String) = Function(content, name)
  
  class Serializer : KSerializer<CompletionMessage> {
    override val descriptor = buildClassSerialDescriptor("GroqChatCompletionMessage") {
      element<String>("role")
      element<String>("content")
      element<String>("name")
      element<String>("toolCallId")
      element<String>("functionName")
      element<String>("toolName")
      element<UserMessageText>("textContent")
      element<UserMessageImage>("imageContent")
      element<CompletionFunctionCall>("functionCall")
      element<List<CompletionToolCall>>("toolCalls")
    }
    
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("DEPRECATION")
    override fun serialize(encoder: Encoder, value: CompletionMessage) {
      require(encoder is JsonEncoder)
      val root = encoder.beginStructure(descriptor)
      root.encodeStringElement(descriptor, 0, value.role)
      
      when (value) {
        is System -> {
          root.encodeStringElement(descriptor, 1, value.content)
          value.name?.let { root.encodeStringElement(descriptor, 2, it) }
        }
        is User -> {
          when (value.content) {
            is UserMessageType.Text -> {
              root.encodeStringElement(descriptor, 1, value.content.content)
              value.name?.let { root.encodeStringElement(descriptor, 2, it) }
            }
            is UserMessageType.Array -> {
              val contentArray = encoder.beginStructure(descriptor)
              
              contentArray.encodeNullableSerializableElement(descriptor, 6, UserMessageText.serializer(), value.content.textContent)
              contentArray.encodeNullableSerializableElement(descriptor, 7, UserMessageImage.serializer(), value.content.imageContent)
              
              contentArray.endStructure(descriptor)
              
              value.name?.let { root.encodeStringElement(descriptor, 2, it) }
            }
          }
        }
        is Assistant -> {
          value.content?.let { root.encodeStringElement(descriptor, 1, it) }
          value.functionCall?.let { root.encodeSerializableElement(descriptor, 8, CompletionFunctionCall.serializer(), it) }
          value.name?.let { root.encodeStringElement(descriptor, 2, it) }
          value.toolCalls?.let { root.encodeSerializableElement(descriptor, 9, ListSerializer(CompletionToolCall.serializer()), it) }
        }
        is Tool -> {
          root.encodeStringElement(descriptor, 1, value.content)
          root.encodeStringElement(descriptor, 3, value.toolCallId)
        }
        is Function -> {
          root.encodeStringElement(descriptor, 1, value.content)
          root.encodeStringElement(descriptor, 2, value.name)
        }
      }
      
      root.endStructure(descriptor)
    }
    
    override fun deserialize(decoder: Decoder): CompletionMessage {
      error("Unreachable")
    }
  }
}

/**
 * The kind of message content.
 */
@Serializable
sealed class UserMessageType {
  /**
   * Text content
   *
   * @param content The content of the message.
   */
  @Serializable
  data class Text(val content: String) : UserMessageType()
  
  /**
   * Array of content parts
   *
   * @param textContent The text content of the message.
   * @param imageContent The image content of the message.
   */
  @Serializable
  data class Array(val textContent: UserMessageText = UserMessageText(), val imageContent: UserMessageImage = UserMessageImage()) : UserMessageType() {
    init {
      require(textContent.type != null || imageContent.type != null) {
        "Either textContent or imageContent must be specified"
      }
    }
  }
}

/**
 * The text content of a user message.
 *
 * @param text The text content.
 * @param type The type of the content part.
 */
@Serializable
data class UserMessageText(
  val text: String? = null,
  val type: String? = "text",
)

/**
 * The image content of a user message.
 *
 * @param imageUrl
 * @param type The type of the content part.
 */
@Serializable
data class UserMessageImage(
  val imageUrl: Image? = null,
  val type: String? = "image_url",
) {
  /**
   * Image object.
   *
   * @param detail Specifies the detail level of the image.
   * @param url Either a URL of the image or the base64 encoded image data.
   */
  @Serializable
  data class Image(
    val detail: String? = null,
    val url: String? = null,
  )
}

/**
 * A function call generated by the model.
 *
 * @param arguments The arguments to call the function with, as generated by the model in JSON format. Note that the model does not always generate valid JSON, and may hallucinate parameters not defined by your function schema. Validate the arguments in your code before calling your function.
 * @param name The name of the function to call.
 */
@Serializable
data class CompletionFunctionCall(
  val arguments: String,
  val name: String,
)

/**
 * A tool call generated by the model.
 *
 * @param function The function that the model called.
 * @param id The ID of the tool call.
 * @param type The type of the tool. Currently, only `function` is supported.
 */
@Serializable
data class CompletionToolCall(
  val function: CompletionFunctionCall,
  val id: String,
  val type: String = "function",
)

/**
 * The format of the model's response.
 *
 * @param type The type of the response.
 */
@Serializable
data class CompletionResponseFormat(
  val type: CompletionResponseFormatType,
)

/**
 * The response format type.
 *
 * @property TEXT Outputs the model's response as standard text.
 * @property JSON_OBJECT Enables JSON mode, which guarantees the message the model generates is valid JSON.
 */
@Serializable
enum class CompletionResponseFormatType {
  @SerialName("text")
  TEXT,
  @SerialName("json_object")
  JSON_OBJECT,
}

/**
 * Options for streaming response.
 */
@Serializable
data class CompletionStreamOptions(
  /**
   * Unused.
   */
  val includeUsages: Boolean? = null,
)

/**
 * Controls which tool is called by the model.
 *
 * @property None No tool is called.
 * @property Auto Let the model pick between generating a message or calling a function/tool.
 * @property Function Call the specified function/tool.
 */
@Serializable(CompletionToolChoice.Serializer::class)
sealed class CompletionToolChoice(val name: String) {
  constructor() : this("auto")
  
  /**
   * The model will not call a function and instead generates a message.
   */
  @Serializable
  data object None : CompletionToolChoice("none")
  
  /**
   * The model can pick between generating a message or calling a function.
   */
  @Serializable
  data object Auto : CompletionToolChoice("auto")
  
  /**
   * Forces the model to call the specified tool.
   *
   * @param functionName The name of the tool.
   * @param functionType The type of the tool. Currently, only `function` is supported.
   */
  @Serializable
  data class Function(val functionName: String, val functionType: String? = "function") : CompletionToolChoice(functionName)
  
  internal companion object Serializer : KSerializer<CompletionToolChoice> {
    override val descriptor = PrimitiveSerialDescriptor("GroqToolChoice", PrimitiveKind.STRING)
    
    override fun deserialize(decoder: Decoder): CompletionToolChoice {
      error("Unreachable")
    }
    
    override fun serialize(encoder: Encoder, value: CompletionToolChoice) {
      require(encoder is JsonEncoder)
      return when (value) {
        is Function -> encoder.encodeJsonElement(
          buildJsonObject {
            value.functionType?.let { type -> put("type", type) }
            putJsonObject("function") {
              put("name", value.functionName)
            }
          }
        )
        else -> encoder.encodeString(value.name)
      }
    }
  }
}

/**
 * A tool specification.
 *
 * @param function The function that the model can call.
 * @param type The type of the tool. Currently, only `function` is supported.
 */
@Serializable
data class CompletionTool(
  val function: CompletionFunction? = null,
  val type: String? = "function",
)