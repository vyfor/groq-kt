@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.vyfor.groqkt.api.audio.transcription

import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.GroqDsl
import io.github.vyfor.groqkt.api.audio.AudioResponseFormat

/**
 * Data used for translating audio into English.
 *
 * @param file The audio file object (not file name) translate, in one of these formats: flac, mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.
 * @param language The language of the input audio. Supplying the input language in ISO-639-1 format will improve accuracy and latency.
 * @param model The model to use.
 * @param prompt An optional text to guide the model's style or continue a previous audio segment. The prompt should be in English.
 * @param responseFormat The format of the transcript output.
 * @param temperature The sampling temperature, between 0 and 1. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. If set to 0, the model will use log probability to automatically increase the temperature until certain thresholds are hit.
 * @param timestampGranularities The timestamp granularities to populate for this transcription. [responseFormat] must be set to [AudioResponseFormat.VERBOSE_JSON] to use timestamp granularities. Either or both of these options are supported. Note: There is no additional latency for segment timestamps, but generating word timestamps incurs additional latency.
 */
data class AudioTranscriptionRequest(
  val file: String,
  val language: String? = null,
  val model: GroqModel,
  val prompt: String? = null,
  val responseFormat: AudioResponseFormat? = null,
  val temperature: Double? = null,
  val timestampGranularities: List<TimestampGranularity>? = null
)  {
  companion object {
    const val ENDPOINT = "audio/transcriptions"
  }
  
  /**
   * Convenient builder for [AudioTranscriptionRequest].
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
    var language: String? = null
    var model: GroqModel? = null
    var prompt: String? = null
    var responseFormat: AudioResponseFormat? = null
    var temperature: Double? = null
    var timestampGranularities: List<TimestampGranularity>? = null
    
    fun timestampGranularities(vararg granularities: TimestampGranularity) = apply {
      this.timestampGranularities = granularities.toList()
    }
    
    fun build(): AudioTranscriptionRequest {
      return AudioTranscriptionRequest(
        requireNotNull(file),
        language,
        requireNotNull(model),
        prompt,
        responseFormat,
        temperature,
        timestampGranularities
      )
    }
  }
}

enum class TimestampGranularity(val value: String) {
  WORD("word"),
  SEGMENT("segment"),
}