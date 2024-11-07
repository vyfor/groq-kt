@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.vyfor.groqkt.api.audio.translation

import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.shared.GroqDsl
import io.github.vyfor.groqkt.api.audio.AudioResponseFormat

/**
 * Data used for translating audio into English.
 *
 * @param file The audio file object (not file name) translate, in one of these formats: flac, mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.
 * @param model The model to use.
 * @param prompt An optional text to guide the model's style or continue a previous audio segment. The prompt should be in English.
 * @param responseFormat The format of the transcript output.
 * @param temperature The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use log probability to automatically increase the temperature until certain thresholds are hit.
 */
data class AudioTranslationRequest(
  val file: String,
  val model: GroqModel,
  val prompt: String? = null,
  val responseFormat: AudioResponseFormat? = null,
  val temperature: Double? = null,
)  {
  companion object {
    const val ENDPOINT = "audio/translations"
  }
  
  /**
   * Convenient builder for [AudioTranslationRequest].
   *
   * @property file The audio file object (not file name) translate, in one of these formats: flac, mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.
   * @property model The model to use.
   * @property prompt An optional text to guide the model's style or continue a previous audio segment. The prompt should be in English.
   * @property responseFormat The format of the transcript output.
   * @property temperature The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use log probability to automatically increase the temperature until certain thresholds are hit.
   */
  @GroqDsl
  class Builder {
    var file: String? = null
    var model: GroqModel? = null
    var prompt: String? = null
    var responseFormat: AudioResponseFormat? = null
    var temperature: Double? = null
    
    fun build(): AudioTranslationRequest {
      return AudioTranslationRequest(
        requireNotNull(file),
        requireNotNull(model),
        prompt,
        responseFormat,
        temperature,
      )
    }
  }
}
