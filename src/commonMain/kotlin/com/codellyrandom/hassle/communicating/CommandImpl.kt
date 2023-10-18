package com.codellyrandom.hassle.communicating

import com.codellyrandom.hassle.Command
import com.codellyrandom.hassle.extending.serviceCalls.notifications.MobileNotificationData
import com.codellyrandom.hassle.values.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

// - Base Command

@Serializable(CommandImpl.Companion::class)
internal sealed class CommandImpl(
    // The generated serializer won't include this ID unless we also include it
    // in our subclasses, hence the "open" modifier.
    override var id: Int? = null,
) : Command {
    companion object : JsonContentPolymorphicSerializer<CommandImpl>(CommandImpl::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CommandImpl> {
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
) : CommandImpl(id) {
    val type: String = "subscribe_events"

    override fun copy(id: Int?) = SubscribeEventsCommand(id = id, eventType = eventType)
}

@Serializable
internal class GetStatesCommand(
    override var id: Int? = null,
) : CommandImpl(id) {
    val type: String = "get_states"

    override fun copy(id: Int?) = GetStatesCommand(id = id)
}

@Serializable
internal class GetServicesCommand(
    override var id: Int? = null,
) : CommandImpl(id) {
    val type: String = "get_services"

    override fun copy(id: Int?) = GetServicesCommand(id = id)
}

// - Service Command

@Serializable
internal sealed class ServiceCommand(
    override var id: Int? = null,
    var domain: Domain,
    var service: Service,
) : CommandImpl(id) {
    val type: String = "call_service"

    @Serializable
    class Target(
        @SerialName("entity_id")
        val entityId: EntityId,
    )

    constructor(domain: String, service: String) : this(null, Domain(domain), Service(service))
}

// - Basic Service Commands

@Serializable
internal class TurnOnServiceCommand(
    val target: Target,
) : ServiceCommand("light", "turn_on") {
    constructor(target: EntityId) : this(Target(target))

    init {
        this.domain = target.entityId.domain
    }

    override fun copy(id: Int?) = TurnOnServiceCommand(target = target).also { it.id = id }
}

@Serializable
internal class TurnOffServiceCommand(
    val target: Target,
) : ServiceCommand("light", "turn_off") {
    constructor(target: EntityId) : this(Target(target))

    init {
        this.domain = target.entityId.domain
    }

    override fun copy(id: Int?) = TurnOffServiceCommand(target = target).also { it.id = id }
}

// - Climate Service Commands

@Serializable
internal class SetHvacPresetModeServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("climate", "set_preset_mode") {
    @Serializable
    class ServiceData(
        @SerialName("preset_mode")
        val presetMode: PresetMode,
    )

    constructor(target: EntityId, presetMode: PresetMode) : this(Target(target), ServiceData(presetMode))

    override fun copy(id: Int?) = SetHvacPresetModeServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class SetTemperatureServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("climate", "set_temperature") {
    @Serializable
    class ServiceData(
        val temperature: Temperature,
        @SerialName("hvac_mode")
        val hvacMode: HvacMode,
    )

    constructor(target: EntityId, temperature: Temperature, hvacMode: HvacMode = HvacMode("heat")) : this(
        Target(target),
        ServiceData(temperature, hvacMode),
    )

    override fun copy(id: Int?) = SetTemperatureServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

// - Cover Service Commands

@Serializable
internal class SetCoverPositionServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("cover", "set_cover_position") {
    @Serializable
    class ServiceData(
        val position: Position,
    )

    constructor(target: EntityId, position: Position) : this(
        Target(target),
        ServiceData(position),
    )

    override fun copy(id: Int?) = SetCoverPositionServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class OpenCoverServiceCommand(
    val target: Target,
) : ServiceCommand("cover", "open_cover") {
    constructor(target: EntityId) : this(Target(target))

    override fun copy(id: Int?) = OpenCoverServiceCommand(target = target).also { it.id = id }
}

@Serializable
internal class CloseCoverServiceCommand(
    val target: Target,
) : ServiceCommand("cover", "close_cover") {
    constructor(target: EntityId) : this(Target(target))

    override fun copy(id: Int?) = CloseCoverServiceCommand(target = target).also { it.id = id }
}

// - Light Service Commands

@Serializable
internal class TurnOnLightServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
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
        val xyColor: XYColor? = null,
    )

    constructor(target: EntityId, serviceData: ServiceData) : this(Target(target), serviceData)

    override fun copy(id: Int?) = TurnOnLightServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

// - Input Service Commands

@Serializable
internal class SetDateTimeServiceCommand<DateType>(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData<DateType>,
) : ServiceCommand("input_datetime", "set_datetime") {
    @Serializable
    class ServiceData<DateType>(
        val date: DateType,
    )

    constructor(target: EntityId, date: DateType) : this(Target(target), ServiceData(date))

    override fun copy(id: Int?) = SetDateTimeServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class SelectOptionServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("input_select", "select_option") {
    @Serializable
    class ServiceData(
        val option: Option,
    )

    constructor(target: EntityId, option: Option) : this(Target(target), ServiceData(option))

    override fun copy(id: Int?) = SelectOptionServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class SetValueServiceCommand<ValueType>(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData<ValueType>,
) : ServiceCommand("input_number", "set_value") {
    @Serializable
    class ServiceData<ValueType>(
        val value: ValueType,
    )

    constructor(target: EntityId, value: ValueType) : this(Target(target), ServiceData(value))

    init {
        this.domain = target.entityId.domain
    }

    override fun copy(id: Int?) = SetValueServiceCommand(target = target, serviceData = serviceData).also { it.id = id }
}

// - Media PLayer Service Commands

@Serializable
internal class PlayMediaServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("media_player", "play_media") {
    @Serializable
    class ServiceData(
        @SerialName("media_content_type")
        val mediaContentType: MediaContentType,
        @SerialName("media_content_id")
        val mediaContentId: MediaContentId,
    )

    constructor(target: EntityId, mediaContentType: MediaContentType, mediaContentId: MediaContentId) : this(
        Target(target),
        ServiceData(mediaContentType, mediaContentId),
    )

    override fun copy(id: Int?) = PlayMediaServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class PauseMediaServiceCommand(
    val target: Target,
) : ServiceCommand("media_player", "media_pause") {
    constructor(target: EntityId) : this(Target(target))

    override fun copy(id: Int?) = PauseMediaServiceCommand(target = target).also { it.id = id }
}

@Serializable
internal class MuteVolumeServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("media_player", "volume_mute") {
    @Serializable
    class ServiceData(
        @SerialName("is_volume_muted")
        val isVolumeMuted: Mute,
    )

    constructor(target: EntityId, isMuted: Mute) : this(Target(target), ServiceData(isMuted))

    override fun copy(id: Int?) = MuteVolumeServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class SetVolumeServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("media_player", "volume_set") {
    @Serializable
    class ServiceData(
        @SerialName("volume_level")
        val volumeLevel: VolumeLevel,
    )

    constructor(target: EntityId, volumeLevel: VolumeLevel) : this(Target(target), ServiceData(volumeLevel))

    override fun copy(id: Int?) = SetVolumeServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class SetSeekPositionServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("media_player", "seek_position") {
    @Serializable
    class ServiceData(
        @SerialName("seek_position")
        val seekPosition: MediaPosition,
    )

    constructor(target: EntityId, seekPosition: MediaPosition) : this(Target(target), ServiceData(seekPosition))

    override fun copy(id: Int?) = SetSeekPositionServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class SetMediaSourceServiceCommand(
    val target: Target,
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("media_player", "select_source") {
    @Serializable
    class ServiceData(
        val source: MediaSource,
    )

    constructor(target: EntityId, source: MediaSource) : this(Target(target), ServiceData(source))

    override fun copy(id: Int?) = SetMediaSourceServiceCommand(target = target, serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class ResumeMediaServiceCommand(
    val target: Target,
) : ServiceCommand("media_player", "media_play") {
    constructor(target: EntityId) : this(Target(target))

    override fun copy(id: Int?) = ResumeMediaServiceCommand(target = target).also { it.id = id }
}

// - Persistent Notification Service Commands

@Serializable
internal class CreatePersistentNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("persistent_notification", "create") {
    @Serializable
    class ServiceData(
        val title: String? = null,
        val message: String,
        @SerialName("notification_id")
        val notificationId: String?,
    )

    override fun copy(id: Int?) = CreatePersistentNotificationServiceCommand(serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class DismissPersistentNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("persistent_notification", "dismiss") {
    @Serializable
    class ServiceData(
        @SerialName("notification_id")
        val notificationId: String,
    )

    override fun copy(id: Int?) = DismissPersistentNotificationServiceCommand(serviceData = serviceData)
        .also { it.id = id }
}

@Serializable
internal class MarkReadPersistentNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("persistent_notification", "mark_read") {
    @Serializable
    class ServiceData(
        @SerialName("notification_id")
        val notificationId: String,
    )

    override fun copy(id: Int?) = MarkReadPersistentNotificationServiceCommand(serviceData = serviceData)
        .also { it.id = id }
}

// - Remote Notification Service Commands

@Serializable
internal class SendNotificationServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData,
) : ServiceCommand("notify", "device") {
    @Serializable
    class ServiceData(
        val title: String? = null,
        val message: String? = null,
        val data: MobileNotificationData? = null,
    )

    constructor(
        device: Device,
        title: String? = null,
        message: String? = null,
        messageBuilder: (MobileNotificationData.() -> Unit)? = null,
    ) : this(ServiceData(title, message, messageBuilder?.let { MobileNotificationData().apply(it) })) {
        this.service = Service.fromDevice(device)
    }

    override fun copy(id: Int?) = SendNotificationServiceCommand(serviceData = serviceData)
        .also { it.id = id }
}
