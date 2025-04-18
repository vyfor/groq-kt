# 📚 Groq Kotlin Library 

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.vyfor/groq-kt)

**An idiomatic [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) library for the [Groq](https://groq.com/) API.**

## 💎 Features
- 🚀 Built with [Ktor](https://ktor.io/) for seamless networking
- 🎨 Complete and documented API for chat completions, audio transcription, and translation, including tool support and function calling
- ⚡ Real-time streaming responses via Kotlin Flows
- 🧩 Rich, idiomatic DSL for clean and expressive syntax
- 🔒 Ensures required validations are met before request submission
- 🔧 Allows specifying default values for API requests in client configuration
- ⏳ Automatically handles rate limiting and retries on failed requests
- 📱 Supports multiple platforms:
  - Android
  - iOS
  - JavaScript
  - JVM
  - Linux
  - macOS
  - Windows
  - WebAssembly
  - tvOS, watchOS

## 🔌 Requirements
- Java 8 or higher (only for use within the JVM environment)

## ⚙️ Installation

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.vyfor/groq-kt)

Add these dependencies to your project:
```kotlin
dependencies {
    implementation("io.github.vyfor:groq-kt:$version")
    /* required */
    implementation("io.ktor:ktor-client-$engine:$version")
}
```

For the list of supported engines, see [Ktor Client Engines](https://ktor.io/docs/client-engines.html#platforms).

## 🧩 Usage

### Initialization
```kotlin
/* It is recommended to use an environment variable for the API key */
val apiKey = System.getenv("GROQ_API_KEY")        // JVM
val apiKey = process.env.GROQ_API_KEY             // JS
val apiKey = getenv("GROQ_API_KEY")!!.toKString() // Native

val client = GroqClient(apiKey)
```

### Specifying default values
You can configure default values for requests. These values will be automatically applied to every request made with a DSL function.
```kotlin
val client = GroqClient(apiKey) {
  defaults {
    chatCompletion {
      model = GroqModel.LLAMA_3_8B_8192
    }
    
    audioTranscription {
      format = AudioResponseFormat.VERBOSE_JSON
    }
  }
}
```

### Chat completion
```kotlin
val response = client.chat {
    model = GroqModel.LLAMA_3_8B_8192

    messages {
        system("You are a helpful assistant.")
        text("What is the capital of Germany?")
    }
}
```

### Streaming
```kotlin
val response = client.chatStreaming {
    model = GroqModel("$VISION_MODEL")

    messages {
        user(
          "Describe what you see in the image.",
          "https://example.com/image.png"
        )
    }
}.data.collect { chunk ->
    println(chunk)
}
```

### Audio transcription
```kotlin
val response = client.transcribe {
    model = GroqModel("$TRANSCRIPTION_MODEL")

    file("path/to/audio.mp3")
    /* or */
    url = "https://example.com/audio.mp3"
}
```

### Audio translation
> [!NOTE]
> Does not seem to be supported by the API yet.
```kotlin
val response = client.translate {
    model = GroqModel("$TRANSLATION_MODEL")

    file("path/to/audio.mp3")
    /* or */
    url = "https://example.com/audio.mp3"
}
```

## ⚖️ License
`groq-kt` is licensed under the [MIT License](./LICENSE).

The project is not affiliated with [Groq](https://groq.com/) in any way.

## 📚 Documentation

The REST API documentation can be found on [console.groq.com](https://console.groq.com/docs).

## 🌱 Contributing
This project is in beta and hasn't undergone excessive testing. Contributions of any kind are welcome, just make sure you read the [Contribution Guidelines](./.github/CONTRIBUTING.md) first. You can also contact me directly on Discord (**[vyfor](https://discord.com/users/446729269872427018)**) if you have any questions.
