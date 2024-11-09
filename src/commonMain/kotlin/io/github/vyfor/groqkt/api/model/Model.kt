package io.github.vyfor.groqkt.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Model(
    val id: String,
    @SerialName("object") val obj: String,
    val created: Long,
    val ownedBy: String,
    val active: Boolean,
    val contextWindow: Long,
    // not documented
    /* val publicApps */
)
