package khome.extending.serviceCalls.notifications

import khome.HomeAssistantApiClient
import khome.communicating.CreatePersistentNotificationServiceCommand
import khome.communicating.DismissPersistentNotificationServiceCommand
import khome.communicating.MarkReadPersistentNotificationServiceCommand
import khome.communicating.ServiceCommand
import khome.values.Domain
import khome.values.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun HomeAssistantApiClient.createPersistentNotification(message: String, title: String? = null, notificationId: String? = null) =
    send(
        CreatePersistentNotificationServiceCommand(
            CreatePersistentNotificationServiceCommand.ServiceData(title, message, notificationId)
        )
    )

suspend fun HomeAssistantApiClient.dismissPersistentNotification(id: String) =
    send(
        DismissPersistentNotificationServiceCommand(
            DismissPersistentNotificationServiceCommand.ServiceData(id)
        )
    )

suspend fun HomeAssistantApiClient.markPersistentNotificationAsRead(id: String) =
    send(
        MarkReadPersistentNotificationServiceCommand(
            MarkReadPersistentNotificationServiceCommand.ServiceData(id)
        )
    )
