package khome.core.boot.servicestore

import co.touchlab.kermit.Kermit
import khome.WebSocketSession
import khome.communicating.CALLER_ID

internal class ServiceStoreInitializer(
    private val session: WebSocketSession,
    private val serviceStore: ServiceStoreInterface
) {
    private val logger = Kermit()
    private val servicesRequest =
        ServicesRequest(CALLER_ID.incrementAndGet())

    suspend fun initialize() {
        sendServicesRequest()
        logger.i { "Requested registered Home Assistant services" }
        storeServices(consumeServicesResponse())
    }

    private suspend fun consumeServicesResponse() =
        session.consumeSingleMessage<ServicesResponse>()

    private suspend fun sendServicesRequest() =
        session.callWebSocketApi(servicesRequest)

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
