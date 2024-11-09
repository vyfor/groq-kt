package io.github.vyfor.groqkt.api.shared

import io.github.vyfor.groqkt.api.chat.ChatCompletionUsage
import kotlinx.serialization.Serializable

@Serializable
data class XGroq(
    val id: String,
    val usage: ChatCompletionUsage?,
)
