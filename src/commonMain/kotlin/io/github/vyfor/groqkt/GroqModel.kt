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
        // Production models
        val GEMMA_2_9B_IT = GroqModel("gemma2-9b-it")
        val LLAMA_3_3_70B_VERSATILE = GroqModel("llama-3.3-70b-versatile")
        val LLAMA_3_1_8B_INSTANT = GroqModel("llama-3.1-8b-instant")
        val LLAMA_GUARD_3_8B = GroqModel("llama-guard-3-8b")
        val LLAMA_3_70B_8192 = GroqModel("llama3-70b-8192")
        val LLAMA_3_8B_8192 = GroqModel("llama3-8b-8192")
        val MIXTRAL_8X7B_32768 = GroqModel("mixtral-8x7b-32768")
        val WHISPER_LARGE_V3 = GroqModel("whisper-large-v3")
        val WHISPER_LARGE_V3_TURBO = GroqModel("whisper-large-v3-turbo")
        val DISTIL_WHISPER_LARGE_V3_EN = GroqModel("distil-whisper-large-v3-en")

        // Preview models
        val ALLAM_2_7B = GroqModel("allam-2-7b")
        val DEEPSEEK_R1_DISTILL_LLAMA_70B = GroqModel("deepseek-r1-distill-llama-70b")
        val LLAMA_4_MAVERICK_17B_128E_INSTRUCT = GroqModel("meta-llama/llama-4-maverick-17b-128e-instruct")
        val LLAMA_4_SCOUT_17B_16E_INSTRUCT = GroqModel("meta-llama/llama-4-scout-17b-16e-instruct")
        val MISTRAL_SABA_24B = GroqModel("mistral-saba-24b")
        val PLAYAI_TTS = GroqModel("playai-tts")
        val PLAYAI_TTS_ARABIC = GroqModel("playai-tts-arabic")
        val QWEN_QWQ_32B = GroqModel("qwen-qwq-32b")

        // Preview systems
        val COMPOUND_BETA = GroqModel("compound-beta")
        val COMPOUND_BETA_MINI = GroqModel("compound-beta-mini")
    }
}
