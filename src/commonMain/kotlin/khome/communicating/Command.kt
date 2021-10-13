package khome.communicating

import khome.extending.serviceCalls.notifications.MobileNotificationData
import khome.values.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

// - Base Command

@Serializable(Command.Companion::class)
sealed class Command {
    // Use a custom serializer so that we don't add an unnecessary class descriminator field into the JSON.
    companion object : JsonContentPolymorphicSerializer<Command>(Command::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Command> {
            // We don't support deserializing commands (only serializing them),
            // so we don't care about mapping JSON elements back to deserializers.
            throw IllegalStateException("Commands do not support deserialization")
        }
    }

    var id: Int? = null
}

// - Basic Commands

@Serializable
internal class SubscribeEventCommand(
    @SerialName("event_type")
    val eventType: EventType,
    val type: String = "subscribe_events"
) : Command()

// - Service Command

@Serializable
sealed class ServiceCommand(
    var domain: Domain,
    var service: Service,
    val type: String = "call_service"
) : Command() {
    @Serializable
    class Target(
        @SerialName("entity_id")
        val entityId: EntityId
    )

    constructor(domain: String, service: String) : this(Domain(domain), Service(service))
}

// - Basic Service Commands

@Serializable
class TurnOnServiceCommand(
    val target: Target
) : ServiceCommand("light", "turn_on") {
    constructor(target: EntityId) : this(Target(target))

    init {
        this.domain = target.entityId.domain
    }
}

@Serializable
class TurnOffServiceCommand(
    val target: Target
) : ServiceCommand("light", "turn_off") {
    constructor(target: EntityId) : this(Target(target))

    init {
        this.domain = target.entityId.domain
    }
}

// - Climate Service Commands

@Serializable
class SetHvacPresetModeServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("climate", "set_preset_mode") {
    @Serializable
    class ServiceData(
        @SerialName("preset_mode")
        val presetMode: PresetMode
    )

    constructor(target: EntityId, presetMode: PresetMode) : this(Target(target), ServiceData(presetMode))
}

@Serializable
class SetTemperatureServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("climate", "set_temperature") {
    @Serializable
    class ServiceData(
        val temperature: Temperature,
        @SerialName("hvac_mode")
        val hvacMode: HvacMode
    )

    constructor(target: EntityId, temperature: Temperature, hvacMode: HvacMode = HvacMode("heat")) : this(
        Target(target),
        ServiceData(temperature, hvacMode)
    )
}

// - Cover Service Commands

@Serializable
class SetCoverPositionServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("cover", "set_cover_position") {
    @Serializable
    class ServiceData(
        val position: Position
    )

    constructor(target: EntityId, position: Position) : this(
        Target(target),
        ServiceData(position)
    )
}

@Serializable
class OpenCoverServiceCommand(
    val target: Target
) : ServiceCommand("cover", "open_cover") {
    constructor(target: EntityId) : this(Target(target))
}

@Serializable
class CloseCoverServiceCommand(
    val target: Target
) : ServiceCommand("cover", "close_cover") {
    constructor(target: EntityId) : this(Target(target))
}

// - Light Service Commands

@Serializable
class TurnOnLightServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("light", "turn_on") {
    @Serializable
    class ServiceData(
        val brightness: Brightness? = null,
        @SerialName("color_name")
        val colorName: ColorName? = null,
        @SerialName("color_temp")
        val colorTemp: ColorTemperature? = null,
        @SerialName("hs_color")
        val hsColor: HSColor? = null,
        val kelvin: ColorTemperature? = null,
        @SerialName("rgb_color")
        val rgbColor: RGBColor? = null,
        @SerialName("xy_color")
        val xyColor: XYColor? = null
    )

    constructor(target: EntityId, serviceData: ServiceData) : this(Target(target), serviceData)
}

// - Input Service Commands

@Serializable
class SetDateTimeServiceCommand<DateType>(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData<DateType>
) : ServiceCommand("input_datetime", "set_datetime") {
    @Serializable
    class ServiceData<DateType>(
        val date: DateType
    )

    constructor(target: EntityId, date: DateType) : this(Target(target), ServiceData(date))
}

@Serializable
class SelectOptionServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("input_select", "select_option") {
    @Serializable
    class ServiceData(
        val option: Option
    )

    constructor(target: EntityId, option: Option) : this(Target(target), ServiceData(option))
}

@Serializable
class SetValueServiceCommand<ValueType>(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData<ValueType>
) : ServiceCommand("input_number", "set_value") {
    @Serializable
    class ServiceData<ValueType>(
        val value: ValueType
    )

    constructor(target: EntityId, value: ValueType) : this(Target(target), ServiceData(value))

    init {
        this.domain = target.entityId.domain
    }
}

// - Media PLayer Service Commands

@Serializable
class PlayMediaServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("media_player","play_media") {
    @Serializable
    class ServiceData(
        @SerialName("media_content_id")
        val mediaContentId: MediaContentId
    )

    constructor(target: EntityId, mediaContentId: MediaContentId) : this(
        Target(target),
        ServiceData(mediaContentId)
    )
}

@Serializable
class PauseMediaServiceCommand(
    val target: Target
) : ServiceCommand("media_player", "media_pause") {
    constructor(target: EntityId) : this(Target(target))
}

@Serializable
class MuteVolumeServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("media_player", "volume_mute") {
    @Serializable
    class ServiceData(
        @SerialName("is_volume_muted")
        val isVolumeMuted: Mute
    )

    constructor(target: EntityId, isMuted: Mute) : this(Target(target), ServiceData(isMuted))
}

@Serializable
class SetVolumeServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("media_player", "volume_set") {
    @Serializable
    class ServiceData(
        @SerialName("volume_level")
        val volumeLevel: VolumeLevel
    )

    constructor(target: EntityId, volumeLevel: VolumeLevel) : this(Target(target), ServiceData(volumeLevel))
}

@Serializable
class SetSeekPositionServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("media_player", "seek_position") {
    @Serializable
    class ServiceData(
        @SerialName("seek_position")
        val seekPosition: MediaPosition
    )

    constructor(target: EntityId, seekPosition: MediaPosition) : this(Target(target), ServiceData(seekPosition))
}

@Serializable
class SetMediaSourceServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("media_player", "select_source") {
    @Serializable
    class ServiceData(
        val source: MediaSource
    )

    constructor(target: EntityId, source: MediaSource) : this(Target(target), ServiceData(source))
}

@Serializable
class ResumeMediaServiceCommand(
    val target: Target
) : ServiceCommand("media_player", "media_play") {
    constructor(target: EntityId) : this(Target(target))
}

// - Persistent Notification Service Commands

@Serializable
class CreatePersistentNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("persistent_notification", "create") {
    @Serializable
    class ServiceData(
        val title: String? = null,
        val message: String,
        @SerialName("notification_id")
        val notificationId: String?
    )
}

@Serializable
class DismissPersistentNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("persistent_notification", "dismiss") {
    @Serializable
    class ServiceData(
        @SerialName("notification_id")
        val notificationId: String
    )
}

@Serializable
class MarkReadPersistentNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("persistent_notification", "mark_read") {
    @Serializable
    class ServiceData(
        @SerialName("notification_id")
        val notificationId: String
    )
}

// - Remote Notification Service Commands

@Serializable
class SendNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("notify", "device") {
    @Serializable
    class ServiceData(
        val title: String? = null,
        val message: String? = null,
        val data: MobileNotificationData? = null
    )

    constructor(
        device: Device,
        title: String? = null,
        message: String? = null,
        messageBuilder: (MobileNotificationData.() -> Unit)? = null
    ) : this(ServiceData(title, message, messageBuilder?.let { MobileNotificationData().apply(it) })) {
        this.service = Service.fromDevice(device)
    }
}
