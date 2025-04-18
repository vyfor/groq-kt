package io.github.vyfor.groqkt

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class GroqModelTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGroqModelSerialization() {
        // Test that GroqModel is serialized to a string
        val model = GroqModel("gemma2-9b-it")
        val serialized = json.encodeToString(model)
        
        // The serialized value should be a JSON string containing just the model ID
        assertEquals("\"gemma2-9b-it\"", serialized)
        
        // Test with a predefined constant
        val predefinedModel = GroqModel.GEMMA_2_9B_IT
        val serializedPredefined = json.encodeToString(predefinedModel)
        assertEquals("\"gemma2-9b-it\"", serializedPredefined)
    }

    @Test
    fun testGroqModelDeserialization() {
        // Test that a string is deserialized to a GroqModel
        val modelString = "\"gemma2-9b-it\""
        val deserialized = json.decodeFromString<GroqModel>(modelString)
        
        // The deserialized value should be a GroqModel with the correct ID
        assertEquals("gemma2-9b-it", deserialized.id)
        assertEquals(GroqModel.GEMMA_2_9B_IT, deserialized)
    }
}