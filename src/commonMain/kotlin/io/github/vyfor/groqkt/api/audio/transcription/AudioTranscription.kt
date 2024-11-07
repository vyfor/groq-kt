package io.github.vyfor.groqkt.api.audio.transcription

import kotlinx.serialization.Serializable

/**
 * Audio transcription object.
 *
 * @param text The transcribed text.
 */
@Serializable
data class AudioTranscription(
  val duration: Double,
  val language: String?,
  val segments: List<TranscriptionSegment>,
  val text: String,
  val task: String,
)

@Serializable
data class TranscriptionSegment(
  val avgLogprob: Double,
  val compressionRatio: Double,
  val end: Double,
  val id: Int,
  val noSpeechProb: Double,
  val seek: Double,
  val start: Double,
  val temperature: Double,
  val text: String,
  val tokens: List<Int>,
)