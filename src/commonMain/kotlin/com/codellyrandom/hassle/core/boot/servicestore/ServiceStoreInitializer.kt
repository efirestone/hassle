package com.codellyrandom.hassle.core.boot.servicestore

import co.touchlab.kermit.Logger
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.communicating.GetServicesCommand

internal class ServiceStoreInitializer(
    private val apiClient: HomeAssistantApiClientImpl,
    private val session: WebSocketSession,
    private val serviceStore: ServiceStore,
) {
    private val logger = Logger
    private val getServicesCommand = GetServicesCommand()

    suspend fun initialize() {
        val id = sendServicesRequest()
        logger.i { "Requested registered Home Assistant services" }
        storeServices(consumeServicesResponse(id))
    }

    private suspend fun consumeServicesResponse(id: Int) =
        session.consumeSingleMessage<ServicesResponse>(id)

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
