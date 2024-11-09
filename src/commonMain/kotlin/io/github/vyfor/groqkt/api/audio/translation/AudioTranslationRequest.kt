@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.vyfor.groqkt.api.audio.translation

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
 * @param file The audio file object (not file name) to translate, in one of these formats: flac,
 *   mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.
 * @param model The model to use.
 * @param prompt An optional text to guide the model's style or continue a previous audio segment.
 *   The prompt should be in English.
 * @param responseFormat The format of the transcript output.
 * @param temperature The sampling temperature, between 0 and 1. Higher values like 0.8 will make
 *   the output more random, while lower values like 0.2 will make it more focused and
 *   deterministic. If set to 0, the model will use log probability to automatically increase the
 *   temperature until certain thresholds are hit.
 */
@Serializable
data class AudioTranslationRequest(
    val file: ByteArray,
    val model: GroqModel,
    val prompt: String? = null,
    val responseFormat: AudioResponseFormat? = null,
    val temperature: Double? = null,
) {
  var filename: String = "audio.mp3"

  companion object {
    const val ENDPOINT = "audio/translations"
  }

  /**
   * Convenient builder for [AudioTranslationRequest].
   *
   * @property file The audio file object (not file name) to translate, in one of these formats:
   *   flac, mp3, mp4, mpeg, mpga, m4a, ogg, wav, or webm.
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
    var model: GroqModel? = null
    var prompt: String? = null
    var responseFormat: AudioResponseFormat? = null
    var temperature: Double? = null

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

    fun build(): AudioTranslationRequest {
      return AudioTranslationRequest(
              requireNotNull(file) { "file must be set" },
              requireNotNull(model) { "model must be set" },
              prompt,
              responseFormat,
              temperature,
          )
          .apply {
            this.filename = requireNotNull(this@Builder.filename) { "filename must be set" }
          }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false

    other as AudioTranslationRequest

    if (!file.contentEquals(other.file)) return false
    if (model != other.model) return false
    if (prompt != other.prompt) return false
    if (responseFormat != other.responseFormat) return false
    if (temperature != other.temperature) return false

    return true
  }

  override fun hashCode(): Int {
    var result = file.contentHashCode()
    result = 31 * result + model.hashCode()
    result = 31 * result + (prompt?.hashCode() ?: 0)
    result = 31 * result + (responseFormat?.hashCode() ?: 0)
    result = 31 * result + (temperature?.hashCode() ?: 0)
    return result
  }
}
