package com.codellyrandom.hassle.communicating

import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.values.EntityId

internal typealias ServiceCommandResolverFunction<S> = (EntityId, S) -> ServiceCommand

internal fun <S : State<*>> ServiceCommandResolver(resolverFunction: ServiceCommandResolverFunction<S>): ServiceCommandResolver<S> =
    ServiceCommandResolverImpl(resolverFunction)

internal interface ServiceCommandResolver<S> {
    fun resolve(entityId: EntityId, desiredState: S): ServiceCommand
}

internal class ServiceCommandResolverImpl<S>(private val resolverFunction: ServiceCommandResolverFunction<S>) : ServiceCommandResolver<S> {
    override fun resolve(entityId: EntityId, desiredState: S): ServiceCommand = resolverFunction(entityId, desiredState)
}
