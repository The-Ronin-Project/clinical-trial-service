package com.projectronin.clinical.trial.server.util

import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions

/**
 * Makes a copy of the supplied [instance] with the [changedValues]. This method assumes that the supplied instance is a
 * data class and relies on the format of its [copy](https://kotlinlang.org/docs/data-classes.html#copying) method.
 */
fun <T : Any> copy(
    instance: T,
    changedValues: Map<String, Any?>,
): T {
    val copyFunction =
        instance::class.memberFunctions.firstOrNull { it.name == "copy" } as? KFunction<T>
            ?: throw IllegalStateException("Supplied instance does not represent a data class and cannot be copied")
    val parameters = copyFunction.parameters.filter { it.name != null }.associateBy { it.name }
    val copyArguments =
        mapOf(copyFunction.instanceParameter!! to instance) + changedValues.mapKeys { parameters[it.key]!! }
    return copyFunction.callBy(copyArguments)
}
