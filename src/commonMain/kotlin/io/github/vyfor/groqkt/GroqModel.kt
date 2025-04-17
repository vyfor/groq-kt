@file:Suppress("unused")

package io.github.vyfor.groqkt

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class GroqModel(
    val id: String,
) {
    companion object {
        val DISTIL_WHISPER_LARGE_V3_EN = GroqModel("distil-whisper-large-v3-en")
        val GEMMA_2_9B_IT = GroqModel("gemma2-9b-it")
        val GEMMA_7B_IT = GroqModel("gemma-7b-it")
        val LLAMA_3_GROQ_70B_8192_TOOL_USE_PREVIEW = GroqModel("llama3-groq-70b-8192-tool-use-preview")
        val LLAMA_3_GROQ_8B_8192_TOOL_USE_PREVIEW = GroqModel("llama3-groq-8b-8192-tool-use-preview")
        val LLAMA_3_1_70B_VERSATILE = GroqModel("llama-3.1-70b-versatile")
        val LLAMA_3_1_8B_INSTANT = GroqModel("llama-3.1-8b-instant")
        val LLAMA_3_2_1B_PREVIEW = GroqModel("llama-3.2-1b-preview")
        val LLAMA_3_2_3B_PREVIEW = GroqModel("llama-3.2-3b-preview")
        val LLAMA_3_2_11B_VISION_PREVIEW = GroqModel("llama-3.2-11b-vision-preview")
        val LLAMA_3_2_90B_VISION_PREVIEW = GroqModel("llama-3.2-90b-vision-preview")
        val LLAMA_GUARD_3_8B = GroqModel("llama-guard-3-8b")
        val LLAMA_3_70B_8192 = GroqModel("llama3-70b-8192")
        val LLAMA_3_8B_8192 = GroqModel("llama3-8b-8192")
        val MIXTRAL_8X7B_32768 = GroqModel("mixtral-8x7b-32768")
        val WHISPER_LARGE_V3 = GroqModel("whisper-large-v3")
        val WHISPER_LARGE_V3_TURBO = GroqModel("whisper-large-v3-turbo")

        // todo: remove after 10/28/24
        @Deprecated(
            "Deprecated in the Groq API",
            ReplaceWith("LLAMA_3_2_11B_VISION_PREVIEW"),
            DeprecationLevel.WARNING
        )
        val LLAVA_V1_5_7B_4096_PREVIEW = GroqModel("llava-v1.5-7b-4096-preview")
    }
}
