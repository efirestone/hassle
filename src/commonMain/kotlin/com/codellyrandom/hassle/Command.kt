package com.codellyrandom.hassle

import com.codellyrandom.hassle.communicating.CommandImpl
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = CommandImpl.Companion::class)
interface Command {
    val id: Int?

    fun copy(id: Int?): Command

//    companion object : KSerializer<Command> {
//        @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
//        override val descriptor: SerialDescriptor = buildSerialDescriptor("Command")
//
//        //        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CommandImpl> {
////            // We don't support deserializing commands (only serializing them),
////            // so we don't care about mapping JSON elements back to deserializers.
////            throw IllegalStateException("Commands do not support deserialization")
////        }
//        override fun deserialize(decoder: Decoder): Command {
//            TODO("Not yet implemented")
//        }
//
//        override fun serialize(encoder: Encoder, value: Command) {
//            TODO("Not yet implemented")
//        }
//    }
}
