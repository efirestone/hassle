package khome.core.mapping

import co.touchlab.kermit.Kermit
import khome.communicating.ServiceCommand
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
    private val logger: Kermit = Kermit()
) {
    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    fun <Target : Any> fromJson(json: JsonElement, type: KClass<Target>): Target = delegate.decodeFromJsonElement(
        deserializer = type.serializer(),
        element = json
    )
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

    /**
     * Convert a value that has a generic parameter into a JSON string.
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Suppress("UNCHECKED_CAST")
    fun <Destination : Any> toJsonWithParameter(from: Destination, parameterType: KType): String {
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

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified Destination : Any> toJson(value: Destination): String =
        toJson(value, type = typeOf<Destination>())
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
