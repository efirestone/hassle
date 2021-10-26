package com.codellyrandom.hassle.communicating

import com.codellyrandom.hassle.extending.serviceCalls.notifications.MobileNotificationData
import com.codellyrandom.hassle.values.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

// - Base Command

@Serializable(Command.Companion::class)
internal sealed class Command(
    // The generated serializer won't include this ID unless we also include it
    // in our subclasses, hence the "open" modifier.
    open var id: Int? = null
) {
    companion object : JsonContentPolymorphicSerializer<Command>(Command::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Command> {
            // We don't support deserializing commands (only serializing them),
            // so we don't care about mapping JSON elements back to deserializers.
            throw IllegalStateException("Commands do not support deserialization")
        }
    }

    constructor() : this(null)
}

// - Basic Commands

@Serializable
internal class SubscribeEventsCommand(
    override var id: Int? = null,
    @SerialName("event_type")
    val eventType: EventType,
    val type: String = "subscribe_events"
) : Command(id)

@Serializable
internal class GetStatesCommand(
    override var id: Int? = null,
    val type: String = "get_states"
) : Command(id)

@Serializable
internal class GetServicesCommand(
    override var id: Int? = null,
    val type: String = "get_services"
) : Command(id)

// - Service Command

@Serializable
internal sealed class ServiceCommand(
    override var id: Int? = null,
    var domain: Domain,
    var service: Service,
    val type: String = "call_service"
) : Command(id) {
    @Serializable
    class Target(
        @SerialName("entity_id")
        val entityId: EntityId
    )

    constructor(domain: String, service: String) : this(null, Domain(domain), Service(service))
}

// - Basic Service Commands

@Serializable
internal class TurnOnServiceCommand(
    val target: Target
) : ServiceCommand("light", "turn_on") {
    constructor(target: EntityId) : this(Target(target))

    init {
        this.domain = target.entityId.domain
    }
}

@Serializable
internal class TurnOffServiceCommand(
    val target: Target
) : ServiceCommand("light", "turn_off") {
    constructor(target: EntityId) : this(Target(target))

    init {
        this.domain = target.entityId.domain
    }
}

// - Climate Service Commands

@Serializable
internal class SetHvacPresetModeServiceCommand(
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
internal class SetTemperatureServiceCommand(
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
internal class SetCoverPositionServiceCommand(
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
internal class OpenCoverServiceCommand(
    val target: Target
) : ServiceCommand("cover", "open_cover") {
    constructor(target: EntityId) : this(Target(target))
}

@Serializable
internal class CloseCoverServiceCommand(
    val target: Target
) : ServiceCommand("cover", "close_cover") {
    constructor(target: EntityId) : this(Target(target))
}

// - Light Service Commands

@Serializable
internal class TurnOnLightServiceCommand(
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
internal class SetDateTimeServiceCommand<DateType>(
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
internal class SelectOptionServiceCommand(
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
internal class SetValueServiceCommand<ValueType>(
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
internal class PlayMediaServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData
) : ServiceCommand("media_player", "play_media") {
    @Serializable
    class ServiceData(
        @SerialName("media_content_type")
        val mediaContentType: MediaContentType,
        @SerialName("media_content_id")
        val mediaContentId: MediaContentId
    )

    constructor(target: EntityId, mediaContentType: MediaContentType, mediaContentId: MediaContentId) : this(
        Target(target),
        ServiceData(mediaContentType, mediaContentId)
    )
}

@Serializable
internal class PauseMediaServiceCommand(
    val target: Target
) : ServiceCommand("media_player", "media_pause") {
    constructor(target: EntityId) : this(Target(target))
}

@Serializable
internal class MuteVolumeServiceCommand(
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
internal class SetVolumeServiceCommand(
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
internal class SetSeekPositionServiceCommand(
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
internal class SetMediaSourceServiceCommand(
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
internal class ResumeMediaServiceCommand(
    val target: Target
) : ServiceCommand("media_player", "media_play") {
    constructor(target: EntityId) : this(Target(target))
}

// - Persistent Notification Service Commands

@Serializable
internal class CreatePersistentNotificationServiceCommand(
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
internal class DismissPersistentNotificationServiceCommand(
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
internal class MarkReadPersistentNotificationServiceCommand(
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
internal class SendNotificationServiceCommand(
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