package com.korebit.rigel

import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

/**
 * Null-safe replacement for [ArgumentMatchers.any]: the Java matcher returns `null`, which Kotlin
 * rejects when the stubbed parameter is non-null.
 */
@Suppress("UNCHECKED_CAST")
fun <T> any(): T {
    ArgumentMatchers.any<T>()
    return null as T
}

/** `Mockito.when` under a name that is not a Kotlin keyword. */
fun <T> whenever(methodCall: T): OngoingStubbing<T> = Mockito.`when`(methodCall)
