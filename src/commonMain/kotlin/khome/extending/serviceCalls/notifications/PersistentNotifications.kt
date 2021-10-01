package khome.extending.serviceCalls.notifications

import khome.HassConnection
import khome.values.domain
import khome.values.service

val PERSISTENT_NOTIFICATION = "persistent_notification".domain
val CREATE = "create".service
val DISMISS = "dismiss".service
val MARK_READ = "mark_read".service

suspend fun HassConnection.createPersistentNotification(message: String, title: String? = null, notificationId: String? = null) =
    callService(
        PERSISTENT_NOTIFICATION,
        CREATE,
        PersistentNotificationMessage(
            message,
            title,
            notificationId
        )
    )

suspend fun HassConnection.dismissPersistentNotification(id: String) =
    callService(
        PERSISTENT_NOTIFICATION,
        DISMISS,
        PersistentNotificationId(id)
    )

suspend fun HassConnection.markPersistentNotificationAsRead(id: String) =
    callService(
        PERSISTENT_NOTIFICATION,
        MARK_READ,
        PersistentNotificationId(id)
    )

internal data class PersistentNotificationMessage(
    val message: String,
    val title: String?,
    val notificationId: String?
)

internal data class PersistentNotificationId(val notificationId: String)
