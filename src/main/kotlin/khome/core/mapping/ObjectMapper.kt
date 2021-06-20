package khome.core.mapping

import khome.core.koin.KhomeComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

interface ObjectMapperInterface {
    fun <Target : Any> fromJson(json: String, type: KClass<Target>): Target
    fun <Target : Any> fromJson(json: JsonElement, type: KClass<Target>): Target
    fun <Destination : Any> toJson(from: Destination): String
}

class ObjectMapper(private val delegate: Json) : KhomeComponent, ObjectMapperInterface {
    @OptIn(ExperimentalSerializationApi::class, kotlinx.serialization.InternalSerializationApi::class)
    override fun <Target : Any> fromJson(json: JsonElement, type: KClass<Target>): Target = delegate.decodeFromJsonElement(
        deserializer = type.serializer(),
        element = json
    )
    @OptIn(ExperimentalSerializationApi::class)
    override fun <Target : Any> fromJson(json: String, type: KClass<Target>): Target = delegate.decodeFromString(
        deserializer = delegate.serializersModule.getContextual(type)!!,
        string = json
    )
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun <Destination : Any> toJson(from: Destination): String = delegate.encodeToString(
        serializer = delegate.serializersModule.getContextual(from::class)!! as KSerializer<Destination>,
        value = from
    )
}

inline fun <reified Target : Any> ObjectMapperInterface.fromJson(json: String): Target = fromJson(json, Target::class)
inline fun <reified Target : Any> ObjectMapperInterface.fromJson(json: JsonElement): Target = fromJson(json, Target::class)
