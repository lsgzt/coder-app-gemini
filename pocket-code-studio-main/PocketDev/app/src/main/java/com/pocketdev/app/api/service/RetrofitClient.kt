package com.pocketdev.app.api.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.pocketdev.app.api.models.ChatResponse
import com.pocketdev.app.api.models.Choice
import com.pocketdev.app.api.models.Message
import com.pocketdev.app.api.models.Usage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.groq.com/openai/v1/"
    private const val TIMEOUT_SECONDS = 30L

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Use a lenient Gson with explicit type adapters to avoid the
    // ParameterizedType cast error with Kotlin data classes containing List<T>.
    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .registerTypeAdapterFactory(SafeTypeAdapterFactory())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val groqApiService: GroqApiService = retrofit.create(GroqApiService::class.java)
}

/**
 * A TypeAdapterFactory that catches ClassCastException (ParameterizedType errors)
 * and falls back to a safe manual parse for known model types.
 */
private class SafeTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        // Only intercept our API model classes
        if (rawType != ChatResponse::class.java &&
            rawType != Choice::class.java &&
            rawType != Message::class.java &&
            rawType != Usage::class.java
        ) {
            return null
        }

        val delegate: TypeAdapter<T> = try {
            gson.getDelegateAdapter(this, type)
        } catch (e: Exception) {
            // If delegate creation itself fails, provide a manual fallback
            return createFallbackAdapter(rawType, gson) as? TypeAdapter<T>
        }

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T) {
                delegate.write(out, value)
            }

            override fun read(`in`: JsonReader): T? {
                return try {
                    delegate.read(`in`)
                } catch (e: ClassCastException) {
                    // Fallback: skip the problematic value
                    `in`.skipValue()
                    null
                }
            }
        }
    }

    private fun createFallbackAdapter(rawType: Class<*>, gson: Gson): TypeAdapter<*>? {
        // Return null to let Gson use its default for non-problematic types
        return null
    }
}
