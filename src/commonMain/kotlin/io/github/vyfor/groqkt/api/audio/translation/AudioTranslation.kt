package io.github.vyfor.groqkt.api.audio.translation

import kotlinx.serialization.Serializable

/**
 * Audio translation object.
 *
 * @param text The translated text.
 */
@Serializable
data class AudioTranslation(
    val text: String,
)
