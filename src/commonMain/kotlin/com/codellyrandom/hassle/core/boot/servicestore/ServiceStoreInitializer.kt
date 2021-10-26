package com.codellyrandom.hassle.core.boot.servicestore

import co.touchlab.kermit.Kermit
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.communicating.GetServicesCommand

internal class ServiceStoreInitializer(
    private val apiClient: HomeAssistantApiClientImpl,
    private val session: WebSocketSession,
    private val serviceStore: ServiceStore
) {
    private val logger = Kermit()
    private val getServicesCommand = GetServicesCommand()

    suspend fun initialize() {
        sendServicesRequest()
        logger.i { "Requested registered Home Assistant services" }
        storeServices(consumeServicesResponse())
    }

    private suspend fun consumeServicesResponse() =
        session.consumeSingleMessage<ServicesResponse>()

    private suspend fun sendServicesRequest() =
        apiClient.send(getServicesCommand)

    private fun storeServices(servicesResponse: ServicesResponse) =
        servicesResponse.let { response ->
            when (response.success) {
                false -> logger.e { "Could not fetch services from Home Assistant" }
                true -> {
                    response.result.forEach { (domain, services) ->
                        val serviceList = mutableListOf<String>()
                        services.forEach { (name, _) ->
                            serviceList += name
                            logger.d { "Fetched service: $name in domain: $domain from Home Assistant" }
                        }
                        serviceStore[domain] = serviceList
                    }
                }
            }
            logger.i { "Stored Home Assistant services in local service store" }
        }
}
