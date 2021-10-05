package khome.core.mapping

import khome.communicating.ServiceCommand
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class ObjectMapper(val delegate: Json = makeJson()) {
    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    fun <Target : Any> fromJson(json: JsonElement, type: KClass<Target>): Target = delegate.decodeFromJsonElement(
        deserializer = type.serializer(),
        element = json
    )
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun <Target : Any> fromJson(json: String, type: KType): Target = delegate.decodeFromString(
        deserializer = delegate.serializersModule.serializer(type) as KSerializer<Target>,
        string = json
    )
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun <Destination : Any> toJson(from: Destination): String = delegate.encodeToString(
        serializer = delegate.serializersModule.getContextual(from::class)!! as KSerializer<Destination>,
        value = from
    )

    /**
     * Convert a value that has a generic parameter into a JSON string.
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun <Destination : Any> toJson(from: Destination, parameterType: KType): String {
        val serializersModule = delegate.serializersModule
        val parameterSerializer = serializersModule.serializer(parameterType)
        val serializer = delegate.serializersModule.getContextual(
            from::class,
            listOf(parameterSerializer)
        ) as KSerializer<Destination>
        return delegate.encodeToString(
            serializer = serializer,
            value = from
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified Target : Any> fromJson(json: String): Target = fromJson(json, typeOf<Target>())
    inline fun <reified Target : Any> fromJson(json: JsonElement): Target = fromJson(json, Target::class)
}

private fun makeJson(builder: SerializersModuleBuilder.() -> Unit = {}) =
    Json {
        isLenient = true
        prettyPrint = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(ServiceCommand::class) { args ->
                ServiceCommand.serializer(args[0])
            }
            builder(this)
        }
    }
