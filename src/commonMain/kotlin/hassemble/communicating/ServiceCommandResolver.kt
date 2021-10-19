package hassemble.communicating

import hassemble.entities.State
import hassemble.values.EntityId

internal typealias ServiceCommandResolverFunction<S> = (EntityId, S) -> ServiceCommand

@Suppress("FunctionName")
internal fun <S : State<*>> ServiceCommandResolver(resolverFunction: ServiceCommandResolverFunction<S>): ServiceCommandResolver<S> =
    ServiceCommandResolverImpl(resolverFunction)

internal interface ServiceCommandResolver<S> {
    fun resolve(entityId: EntityId, desiredState: S): ServiceCommand
}

internal class ServiceCommandResolverImpl<S>(private val resolverFunction: ServiceCommandResolverFunction<S>) : ServiceCommandResolver<S> {
    override fun resolve(entityId: EntityId, desiredState: S): ServiceCommand = resolverFunction(entityId, desiredState)
}
