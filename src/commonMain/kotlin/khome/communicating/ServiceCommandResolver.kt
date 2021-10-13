package khome.communicating

import khome.entities.State
import khome.values.EntityId

typealias ServiceCommandResolverFunction<S> = (EntityId, S) -> ServiceCommand

@Suppress("FunctionName")
fun <S : State<*>> ServiceCommandResolver(resolverFunction: ServiceCommandResolverFunction<S>): ServiceCommandResolver<S> =
    ServiceCommandResolverImpl(resolverFunction)

interface ServiceCommandResolver<S> {
    fun resolve(entityId: EntityId, desiredState: S): ServiceCommand
}

internal class ServiceCommandResolverImpl<S>(private val resolverFunction: ServiceCommandResolverFunction<S>) : ServiceCommandResolver<S> {
    override fun resolve(entityId: EntityId, desiredState: S): ServiceCommand = resolverFunction(entityId, desiredState)
}
