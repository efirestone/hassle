package khome.extending.serviceCalls.notifications

import khome.HomeAssistantApiClient
import khome.callService
import khome.values.Domain
import khome.values.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val PERSISTENT_NOTIFICATION = Domain("persistent_notification")

suspend fun HomeAssistantApiClient.createPersistentNotification(message: String, title: String? = null, notificationId: String? = null) =
    callService(
        PERSISTENT_NOTIFICATION,
        Service("create"),
        PersistentNotificationMessage(
            message,
            title,
            notificationId
        )
    )

suspend fun HomeAssistantApiClient.dismissPersistentNotification(id: String) =
    callService(
        PERSISTENT_NOTIFICATION,
        Service("dismiss"),
        PersistentNotificationId(id)
    )

suspend fun HomeAssistantApiClient.markPersistentNotificationAsRead(id: String) =
    callService(
        PERSISTENT_NOTIFICATION,
        Service("mark_read"),
        PersistentNotificationId(id)
    )

@Serializable
internal data class PersistentNotificationMessage(
    val message: String,
    val title: String?,
    @SerialName("notification_id")
    val notificationId: String?
)

internal data class PersistentNotificationId(val notificationId: String)
