@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package io.github.vyfor.groqkt.api.audio.transcription

import io.github.vyfor.groqkt.GroqModel
import io.github.vyfor.groqkt.api.audio.AudioResponseFormat
import io.github.vyfor.groqkt.api.shared.GroqDsl
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable

/**
 * Data used for translating audio into English.
 *
 * @param file The audio file object (not file name) to transcribe, in one of these formats: flac,
 *   mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.
 * @param language The language of the input audio. Supplying the input language in ISO-639-1 format
 *   will improve accuracy and latency.
 * @param model The model to use.
 * @param prompt An optional text to guide the model's style or continue a previous audio segment.
 *   The prompt should be in English.
 * @param responseFormat The format of the transcript output.
 * @param temperature The sampling temperature, between 0 and 1. Higher values like 0.8 will make
 *   the output more random, while lower values like 0.2 will make it more focused and
 *   deterministic. If set to 0, the model will use log probability to automatically increase the
 *   temperature until certain thresholds are hit.
 * @param timestampGranularities The timestamp granularities to populate for this transcription.
 *   [responseFormat] must be set to [AudioResponseFormat.VERBOSE_JSON] to use timestamp
 *   granularities. Either or both of these options are supported. Note: There is no additional
 *   latency for segment timestamps, but generating word timestamps incurs additional latency.
 */
@Serializable
data class AudioTranscriptionRequest(
    val file: ByteArray? = null,
    val url: String? = null,
    val language: String? = null,
    val model: GroqModel,
    val prompt: String? = null,
    val responseFormat: AudioResponseFormat? = null,
    val temperature: Double? = null,
    val timestampGranularities: List<TimestampGranularity>? = null,
) {
  var filename: String = "audio.mp3"

  init {
    require(file != null || url != null) { "either file or url must be set" }
  }

  companion object {
    const val ENDPOINT = "audio/transcriptions"
  }

  /**
   * Convenient builder for [AudioTranscriptionRequest].
   *
   * Only one of [file] or [url] must be set.
   *
   * @property file The audio file object (not file name) to transcribe, in one of these formats:
   *   flac, mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.
   * @property filename The filename of the audio file to transcribe.
   * @property url The URL of the audio file to transcribe.
   * @property model The model to use.
   * @property prompt An optional text to guide the model's style or continue a previous audio
   *   segment. The prompt should be in English.
   * @property responseFormat The format of the transcript output.
   * @property temperature The sampling temperature, between 0 and 1. Higher values like 0.8 will
   *   make the output more random, while lower values like 0.2 will make it more focused and
   *   deterministic. If set to 0, the model will use log probability to automatically increase the
   *   temperature until certain thresholds are hit.
   */
  @GroqDsl
  class Builder {
    var file: ByteArray? = null
    var filename: String? = null
    var url: String? = null
    var language: String? = null
    var model: GroqModel? = null
    var prompt: String? = null
    var responseFormat: AudioResponseFormat? = null
    var temperature: Double? = null
    var timestampGranularities: List<TimestampGranularity>? = null

    /**
     * Sets the `file` property by reading the contents of a file specified by its file path.
     *
     * @param file The file path as a [String].
     */
    fun file(file: String) = apply {
      this.filename = file.substringAfterLast(SystemPathSeparator)
      this.file = SystemFileSystem.source(Path(file)).buffered().readByteArray()
    }

    /**
     * Sets the `file` property by reading the contents of a file specified by its [Path].
     *
     * @param file The file as a [Path].
     */
    fun file(file: Path) = apply {
      this.filename = file.name
      this.file = SystemFileSystem.source(file).buffered().readByteArray()
    }

    /**
     * Sets the `file` property by reading the contents of a file from a [Source].
     *
     * @param file The file source as a [Source].
     */
    fun file(file: Source) = apply { this.file = file.readByteArray() }

    /**
     * Sets the `file` property by reading the contents of a file from a [ByteReadChannel].
     *
     * @param file The file as a [ByteReadChannel].
     */
    suspend fun file(file: ByteReadChannel) = apply { this.file = file.toByteArray() }

    /** Sets one or more [timestampGranularities]. */
    fun timestampGranularities(vararg granularities: TimestampGranularity) = apply {
      this.timestampGranularities = granularities.toList()
    }

    fun build(): AudioTranscriptionRequest =
        AudioTranscriptionRequest(
                file,
                url,
                language,
                requireNotNull(model) { "model must be set" },
                prompt,
                responseFormat,
                temperature,
                timestampGranularities,
            )
            .apply {
              this.filename = requireNotNull(this@Builder.filename) { "filename must be set" }
            }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as AudioTranscriptionRequest

    if (!file.contentEquals(other.file)) return false
    if (language != other.language) return false
    if (model != other.model) return false
    if (prompt != other.prompt) return false
    if (responseFormat != other.responseFormat) return false
    if (temperature != other.temperature) return false
    if (timestampGranularities != other.timestampGranularities) return false

    return true
  }

  override fun hashCode(): Int {
    var result = file.contentHashCode()
    result = 31 * result + (language?.hashCode() ?: 0)
    result = 31 * result + model.hashCode()
    result = 31 * result + (prompt?.hashCode() ?: 0)
    result = 31 * result + (responseFormat?.hashCode() ?: 0)
    result = 31 * result + (temperature?.hashCode() ?: 0)
    result = 31 * result + (timestampGranularities?.hashCode() ?: 0)
    return result
  }
}

enum class TimestampGranularity(
    val value: String,
) {
  WORD("word"),
  SEGMENT("segment"),
}
