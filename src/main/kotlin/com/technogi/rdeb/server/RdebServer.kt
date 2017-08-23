package com.technogi.rdeb.server

import com.technogi.rdeb.client.Event
import io.vertx.core.Verticle
import java.time.Instant

interface RdebServer : Verticle {

    fun postEvent(event: Event, broadcast: Boolean = false, handler: (Boolean) -> Unit)

    fun getNextEvent(clientId: String, handler: (Event?) -> Unit)

    fun getNextBroadcast(from: Instant, handler: (Array<Event>) -> Unit)

    fun markEvent(event: Event, clientId: String, handler: (Boolean) -> Unit)

}