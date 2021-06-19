package khome.core.boot.servicestore

import co.touchlab.kermit.Kermit
import khome.KhomeSession
import khome.communicating.CALLER_ID

internal class ServiceStoreInitializerImpl(
    private val khomeSession: KhomeSession,
    private val serviceStore: ServiceStoreInterface
) : ServiceStoreInitializer {
    private val logger = Kermit()
    private val servicesRequest =
        ServicesRequest(CALLER_ID.incrementAndGet())

    override suspend fun initialize() {
        sendServicesRequest()
        logger.i { "Requested registered homeassistant services" }
        storeServices(consumeServicesResponse())
    }

    private suspend fun consumeServicesResponse() =
        khomeSession.consumeSingleMessage<ServicesResponse>()

    private suspend fun sendServicesRequest() =
        khomeSession.callWebSocketApi(servicesRequest)

    private fun storeServices(servicesResponse: ServicesResponse) =
        servicesResponse.let { response ->
            when (response.success) {
                false -> logger.e { "Could not fetch services from homeassistant" }
                true -> {
                    response.result.forEach { (domain, services) ->
                        val serviceList = mutableListOf<String>()
                        services.forEach { (name, _) ->
                            serviceList += name
                            logger.d { "Fetched service: $name in domain: $domain from homeassistant" }
                        }
                        serviceStore[domain] = serviceList
                    }
                }
            }
            logger.i { "Stored homeassistant services in local service store" }
        }
}

interface ServiceStoreInitializer {
    suspend fun initialize()
}
