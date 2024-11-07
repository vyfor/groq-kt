package io.github.vyfor.groqkt.api.audio.transcription

import kotlinx.serialization.Serializable

/**
 * Audio transcription object.
 *
 * Nullable fields are only present with [io.github.vyfor.groqkt.api.audio.AudioResponseFormat.VERBOSE_JSON] response format.
 *
 * @param text The transcribed text.
 * @param duration The duration of the audio in seconds.
 * @param language The detected language of the input audio.
 * @param segments The transcribed segments of the audio.
 * @param task The task that was performed on the audio.
 */
@Serializable
data class AudioTranscription(
  val text: String,
  val duration: Double?,
  val language: String?,
  val segments: List<TranscriptionSegment>?,
  val task: String?,
)

/**
 * Transcription segment object.
 *
 * @param avgLogprob The average log probability of the segment.
 * @param compressionRatio The compression ratio of the segment.
 * @param end The end time of the segment in seconds.
 * @param id The ID of the segment.
 * @param noSpeechProb The no speech probability of the segment.
 * @param seek The seek time of the segment in seconds.
 * @param start The start time of the segment in seconds.
 * @param temperature The temperature of the segment.
 * @param text The transcribed text of the segment.
 * @param tokens The tokens of the segment.
 */
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