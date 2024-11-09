package io.github.vyfor.groqkt.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Models(
    @SerialName("object") val obj: String,
    val data: List<Model>,
)
