package khome.extending.serviceCalls.notifications

import khome.HomeAssistantApiClient
import khome.communicating.SendNotificationServiceCommand
import khome.communicating.ServiceCommand
import khome.values.Device
import khome.values.EntityId
import khome.values.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val REQUEST_LOCATION_UPDATE = "request_location_update"

// - Public API

suspend fun HomeAssistantApiClient.notifyMobileApp(device: Device, message: String, title: String? = null) =
    send(SendNotificationServiceCommand(device, message, title))

suspend fun HomeAssistantApiClient.notifyMobileApp(device: Device, messageBuilder: MobileNotificationData.() -> Unit) =
    send(SendNotificationServiceCommand(device, messageBuilder = messageBuilder))

suspend fun HomeAssistantApiClient.notifyMobileApp(vararg devices: Device, title: String, message: String) =
    devices.forEach { device -> notifyMobileApp(device, message, title) }

suspend fun HomeAssistantApiClient.requestLocationUpdate(device: Device) =
    notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE)

suspend fun HomeAssistantApiClient.requestLocationUpdate(vararg devices: Device) =
    devices.forEach { device -> notifyMobileApp(device, message = REQUEST_LOCATION_UPDATE) }

// - Service Commands

// - Types

//@Serializable
//class MobileNotificationData {
//    var title: String? = null
//    lateinit var message: String
//    private val data: MessageData =
//        MessageData()
//    fun data(builder: MessageData.() -> Unit) = data.apply(builder)
//}

@Serializable
class MobileNotificationData {
    lateinit var subtitle: String
    private val push: PushData = PushData()
    var apnsHeaders: ApnsHeaders? = null
    var presentationOptions: List<PresentationOptions>? = null
    private var attachment: AttachmentData? = null

    private var actionData: MapActionData? = null
    var entityId: EntityId? = null

    enum class PresentationOptions {
        ALERT, BATCH, SOUND
    }

    fun push(builder: PushData.() -> Unit) = push.apply(builder)
    fun mapActionData(builder: MapActionData.() -> Unit) {
        actionData = MapActionData().apply(builder)
    }

    fun attachment(url: String? = null, contentType: String? = null, hideThumbnail: Boolean = false) {
        attachment = AttachmentData(
            url = url,
            contentType = contentType,
            hideThumbnail = hideThumbnail
        )
    }
}

@Serializable
data class AttachmentData(
    val url: String?,
    @SerialName("content-type")
    val contentType: String?,
    @SerialName("hide-thumbnail")
    val hideThumbnail: Boolean?
)

@Serializable
class PushData {
    @SerialName("thread-id")
    var threadId: String? = null
    private var sound: SoundData? = null
    var badge: Int? = null
    var category: String? = null

    fun sound(name: String = "default", critical: Int? = null, volume: Double? = null) {
        sound = SoundData(name, critical, volume)
    }
}

@Serializable
data class SoundData(
    var name: String,
    var critical: Int?,
    var volume: Double?
)

// https://companion.home-assistant.io/docs/notifications/dynamic-content/#map
@Serializable
data class MapActionData(
    var latitude: String? = null,
    var longitude: String? = null,
    @SerialName("second_latitude")
    var secondLatitude: String? = null,
    @SerialName("second_longitude")
    var secondLongitude: String? = null,
    @SerialName("shows_line_between_points")
    var showsLineBetweenPoints: Boolean? = null,
    @SerialName("shows_compass")
    var showsCompass: Boolean? = null,
    @SerialName("shows_points_of_interest")
    var showsPointOfInterest: Boolean? = null,
    @SerialName("shows_scale")
    var showsScale: Boolean? = null,
    @SerialName("shows_traffic")
    var showsTraffic: Boolean? = null,
    @SerialName("shows_users_location")
    var showsUsersLocation: Boolean? = null
)

@Serializable
data class ApnsHeaders(
    @SerialName("apns-collapse-id")
    var id: String? = null
)
