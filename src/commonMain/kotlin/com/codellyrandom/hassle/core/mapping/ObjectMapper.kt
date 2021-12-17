package com.codellyrandom.hassle.core.mapping

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ObjectMapper(
    private val delegate: Json = makeJson(),
    private val logger: Logger = Logger(config = LoggerConfig.default)
) {
    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    fun <Target : Any> fromJson(json: JsonElement, type: KClass<Target>): Target {
        try {
            return delegate.decodeFromJsonElement(
                deserializer = type.serializer(),
                element = json
            )
        } catch (e: Throwable) {
            logger.e(e) { "Exception converting from JSON" }
            throw e
        }
    }
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun <Target : Any> fromJson(json: String, type: KType): Target {
        try {
            return delegate.decodeFromString(
                deserializer = delegate.serializersModule.serializer(type) as KSerializer<Target>,
                string = json
            )
        } catch (e: Throwable) {
            logger.e(e) { "Exception converting from JSON" }
            throw e
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun <Destination : Any> toJson(from: Destination, type: KType): String {
        try {
            return delegate.encodeToString(
                serializer = delegate.serializersModule.serializer(type) as KSerializer<Destination>,
                value = from
            )
        } catch (e: Throwable) {
            logger.e(e) { "Exception converting to JSON" }
            throw e
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified Target : Any> fromJson(json: String): Target = fromJson(json, typeOf<Target>())
    inline fun <reified Target : Any> fromJson(json: JsonElement): Target = fromJson(json, Target::class)

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified Destination : Any> toJson(value: Destination): String =
        toJson(value, type = typeOf<Destination>())
}

private fun makeJson(builder: SerializersModuleBuilder.() -> Unit = {}) =
    Json {
        encodeDefaults = true
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            builder(this)
        }
    }
