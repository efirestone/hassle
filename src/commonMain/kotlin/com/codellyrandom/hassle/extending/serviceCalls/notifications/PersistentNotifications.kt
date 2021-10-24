package com.codellyrandom.hassle.extending.serviceCalls.notifications

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.communicating.CreatePersistentNotificationServiceCommand
import com.codellyrandom.hassle.communicating.DismissPersistentNotificationServiceCommand
import com.codellyrandom.hassle.communicating.MarkReadPersistentNotificationServiceCommand

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
