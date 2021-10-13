package khome.extending.events

import khome.HomeAssistantApiClient
import khome.events.EventHandlerFunction
import khome.values.EventType
import kotlinx.serialization.SerialName

fun HomeAssistantApiClient.attachIosActionHandler(eventHandler: EventHandlerFunction<IosActionEventData>) =
    attachEventHandler(EventType.ACTION_FIRED, eventHandler)

data class IosActionEventData(
    @SerialName("sourceDeviceID")
    val sourceDeviceID: String,

    @SerialName("actionID")
    val actionID: String,

    @SerialName("actionName")
    val actionName: String,

    @SerialName("sourceDeviceName")
    val sourceDeviceName: String,

    @SerialName("sourceDevicePermanentID")
    val sourceDevicePermanentID: String,

    @SerialName("triggerSource")
    val triggerSource: String
)

data class IosNotificationActionEventData<AD>(
    @SerialName("sourceDeviceName")
    val sourceDeviceName: String,

    @SerialName("sourceDeviceID")
    val sourceDeviceID: String,

    @SerialName("actionName")
    val actionName: String,

    @SerialName("sourceDevicePermanentID")
    val sourceDevicePermanentID: String?,

    @SerialName("textInput")
    val textInput: String?,

    @SerialName("action_data")
    val actionData: AD?
)

fun <AD> HomeAssistantApiClient.attachIosNotificationActionHandler(eventHandler: EventHandlerFunction<IosNotificationActionEventData<AD>>) =
    attachEventHandler(EventType.NOTIFICATION_ACTION_FIRED, eventHandler)

//internal enum class IosEventType(val value: String) {
//    ACTION_FIRED("ios.action_fired"),
//    NOTIFICATION_ACTION_FIRED("ios.notification_action_fired")
//}

//internal val IosEventType.eventType
//    get() = EventType(this.value)
