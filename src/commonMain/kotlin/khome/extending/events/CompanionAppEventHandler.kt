package khome.extending.events

import khome.KhomeApplication
import khome.events.EventHandlerFunction
import khome.extending.events.IosEventType.ACTION_FIRED
import khome.extending.events.IosEventType.NOTIFICATION_ACTION_FIRED
import khome.values.EventType
import kotlinx.serialization.SerialName

fun KhomeApplication.attachIosActionHandler(eventHandler: EventHandlerFunction<IosActionEventData>) =
    attachEventHandler(ACTION_FIRED.eventType, eventHandler)

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

fun <AD> KhomeApplication.attachIosNotificationActionHandler(eventHandler: EventHandlerFunction<IosNotificationActionEventData<AD>>) =
    attachEventHandler(NOTIFICATION_ACTION_FIRED.eventType, eventHandler)

internal enum class IosEventType(val value: String) {
    ACTION_FIRED("ios.action_fired"),
    NOTIFICATION_ACTION_FIRED("ios.notification_action_fired")
}

internal val IosEventType.eventType
    get() = EventType(this.value)
