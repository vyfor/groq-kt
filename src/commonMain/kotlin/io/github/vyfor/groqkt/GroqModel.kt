@file:Suppress("unused")

package io.github.vyfor.groqkt

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(ModelSerializer::class)
enum class GroqModel(val id: String) {
  DISTIL_WHISPER_LARGE_V3_EN("distil-whisper-large-v3-en"),
  GEMMA_2_9B_IT("gemma2-9b-it"),
  GEMMA_7B_IT("gemma-7b-it"),
  LLAMA_3_GROQ_70B_8192_TOOL_USE_PREVIEW("llama3-groq-70b-8192-tool-use-preview"),
  LLAMA_3_GROQ_8B_8192_TOOL_USE_PREVIEW("llama3-groq-8b-8192-tool-use-preview"),
  LLAMA_3_1_70B_VERSATILE("llama-3.1-70b-versatile"),
  LLAMA_3_1_8B_INSTANT("llama-3.1-8b-instant"),
  LLAMA_3_2_1B_PREVIEW("llama-3.2-1b-preview"),
  LLAMA_3_2_3B_PREVIEW("llama-3.2-3b-preview"),
  LLAMA_3_2_11B_VISION_PREVIEW("llama-3.2-11b-vision-preview"),
  LLAMA_3_2_90B_VISION_PREVIEW("llama-3.2-90b-vision-preview"),
  LLAMA_GUARD_3_8B("llama-guard-3-8b"),
  LLAMA_3_70B_8192("llama3-70b-8192"),
  LLAMA_3_8B_8192("llama3-8b-8192"),
  MIXTRAL_8X7B_32768("mixtral-8x7b-32768"),
  WHISPER_LARGE_V3("whisper-large-v3"),
  WHISPER_LARGE_V3_TURBO("whisper-large-v3-turbo"),
  // todo: remove after 10/28/24
  @Deprecated("Deprecated in the Groq API", ReplaceWith("LLAMA_3_2_11B_VISION_PREVIEW"), DeprecationLevel.WARNING)
  LLAVA_V1_5_7B_4096_PREVIEW("llava-v1.5-7b-4096-preview"),
}

private object ModelSerializer : KSerializer<GroqModel> {
  override val descriptor = PrimitiveSerialDescriptor("GroqModel", PrimitiveKind.STRING)
  
  override fun deserialize(decoder: Decoder): GroqModel {
    val id = decoder.decodeString()
    return GroqModel.entries.firstOrNull { it.id == id } ?: error("Model '$id' is not supported")
  }
  
  override fun serialize(encoder: Encoder, value: GroqModel) {
    encoder.encodeString(value.id)
  }
}