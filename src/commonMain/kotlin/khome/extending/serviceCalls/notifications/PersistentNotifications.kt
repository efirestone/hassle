package khome.extending.serviceCalls.notifications

import khome.HassConnection
import khome.callService
import khome.values.Domain
import khome.values.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val PERSISTENT_NOTIFICATION = Domain("persistent_notification")

suspend fun HassConnection.createPersistentNotification(message: String, title: String? = null, notificationId: String? = null) =
    callService(
        PERSISTENT_NOTIFICATION,
        Service("create"),
        PersistentNotificationMessage(
            message,
            title,
            notificationId
        )
    )

suspend fun HassConnection.dismissPersistentNotification(id: String) =
    callService(
        PERSISTENT_NOTIFICATION,
        Service("dismiss"),
        PersistentNotificationId(id)
    )

suspend fun HassConnection.markPersistentNotificationAsRead(id: String) =
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
