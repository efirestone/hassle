package hassemble.extending.serviceCalls.notifications

import hassemble.HomeAssistantApiClient
import hassemble.HomeAssistantApiClientImpl
import hassemble.communicating.CreatePersistentNotificationServiceCommand
import hassemble.communicating.DismissPersistentNotificationServiceCommand
import hassemble.communicating.MarkReadPersistentNotificationServiceCommand

suspend fun HomeAssistantApiClient.createPersistentNotification(message: String, title: String? = null, notificationId: String? = null) =
    (this as HomeAssistantApiClientImpl).send(
        CreatePersistentNotificationServiceCommand(
            CreatePersistentNotificationServiceCommand.ServiceData(title, message, notificationId)
        )
    )

suspend fun HomeAssistantApiClient.dismissPersistentNotification(id: String) =
    (this as HomeAssistantApiClientImpl).send(
        DismissPersistentNotificationServiceCommand(
            DismissPersistentNotificationServiceCommand.ServiceData(id)
        )
    )

suspend fun HomeAssistantApiClient.markPersistentNotificationAsRead(id: String) =
    (this as HomeAssistantApiClientImpl).send(
        MarkReadPersistentNotificationServiceCommand(
            MarkReadPersistentNotificationServiceCommand.ServiceData(id)
        )
    )
