@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.vyfor.groqkt

import io.github.vyfor.groqkt.GroqClient.Companion.BASE_URL
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

/**
 * Groq configuration class.
 *
 * @property json The JSON serializer.
 * @property client The HTTP client.
 */
data class GroqConfig(
    val json: Json,
    val client: HttpClient,
)

/**
 * Groq configuration builder class.
 *
 * @param apiKey The Groq API key obtained from [here](https://console.groq.com/keys).
 * @property client The HTTP client. Do not modify unless you know what you're doing.
 */
class GroqConfigBuilder(
    apiKey: String,
) {
    @OptIn(ExperimentalSerializationApi::class)
    private val json: Json =
        Json {
            explicitNulls = false
            encodeDefaults = true
            ignoreUnknownKeys = true
            namingStrategy = JsonNamingStrategy.SnakeCase
            classDiscriminatorMode = ClassDiscriminatorMode.NONE
        }
    var client: HttpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }

            install(HttpRequestRetry) {
                retryIf(1) { _, response ->
                    when (response.status.value) {
                        // retry on server errors
                        in 500..599 -> {
                            constantDelay()
                            true
                        }

                        // handle ratelimits (retry-after)
                        HttpStatusCode.TooManyRequests.value -> {
                            constantDelay(
                                response.headers["retry-after"]?.toLongOrNull()?.times(1000) ?: 0,
                                0,
                                false,
                            )
                            true
                        }
                        else -> false
                    }
                }
            }

            install(DefaultRequest) {
                url(BASE_URL)
                header("Authorization", "Bearer $apiKey")
            }
        }

    internal fun build(): GroqConfig =
        GroqConfig(
            json,
            client,
        )
}
