package com.codellyrandom.hassle.communicating

import com.codellyrandom.hassle.Command
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

class AllCommandsTest {
    /**
     * Compares the results of calling sealedSubclasses with the manually maintained allCommands() list.
     * We keep the manual list because sealedSubclasses isn't available in Kotlin/Native, and so we need
     * something to use there. If/when K/N supports sealedSubclasses we can use that to create the allCommands()
     * list and remove this test.
     */
    @Test
    fun allCommandsIsComplete() {
        val commands = mutableListOf<KClass<out Command>>()

        CommandImpl::class.sealedSubclasses.forEach { commandClass: KClass<out CommandImpl> ->
            when (commandClass) {
                ServiceCommand::class -> {
                    commands.addAll(ServiceCommand::class.sealedSubclasses)
                }

                else -> {
                    commands.add(commandClass)
                }
            }
        }

        assertEquals(commands.toSet(), allCommands().toSet())
    }
}
