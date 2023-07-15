package com.codellyrandom.hassle.extending.commands

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.await
import com.codellyrandom.hassle.communicating.ConfigEntityRegistrationListCommand
import com.codellyrandom.hassle.values.EntityRegistration

suspend fun HomeAssistantApiClient.getEntityRegistrations(): List<EntityRegistration> =
    await(ConfigEntityRegistrationListCommand())
