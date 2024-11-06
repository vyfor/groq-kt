package io.github.vyfor.groqkt.api.audio.transcription

import kotlinx.serialization.Serializable

/**
 * Audio transcription object.
 *
 * @param text The transcribed text.
 */
@Serializable
data class AudioTranscription(
  val text: String,
)