package khome.core.mapping

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

class ObjectMapper(private val delegate: Json) {
    @OptIn(ExperimentalSerializationApi::class, kotlinx.serialization.InternalSerializationApi::class)
    fun <Target : Any> fromJson(json: JsonElement, type: KClass<Target>): Target = delegate.decodeFromJsonElement(
        deserializer = type.serializer(),
        element = json
    )
    @OptIn(ExperimentalSerializationApi::class)
    fun <Target : Any> fromJson(json: String, type: KClass<Target>): Target = delegate.decodeFromString(
        deserializer = delegate.serializersModule.getContextual(type)!!,
        string = json
    )
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun <Destination : Any> toJson(from: Destination): String = delegate.encodeToString(
        serializer = delegate.serializersModule.getContextual(from::class)!! as KSerializer<Destination>,
        value = from
    )

    inline fun <reified Target : Any> fromJson(json: String): Target = fromJson(json, Target::class)
    inline fun <reified Target : Any> fromJson(json: JsonElement): Target = fromJson(json, Target::class)
}
