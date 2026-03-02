package com.shadowforgedmmo.engine.event

typealias Receiver<E> = (E) -> Unit

class Signal<E : Event> {
    private val receivers = mutableSetOf<Receiver<E>>()

    operator fun plusAssign(receiver: Receiver<E>) {
        receivers += receiver
    }

    operator fun minusAssign(receiver: Receiver<E>) {
        receivers -= receiver
    }

    fun emit(event: E) = receivers.toList().forEach { it(event) }
}
