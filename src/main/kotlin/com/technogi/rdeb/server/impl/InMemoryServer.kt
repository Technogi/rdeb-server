package com.technogi.rdeb.server.impl

import com.technogi.rdeb.client.Event
import com.technogi.rdeb.server.RdebServer
import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import java.time.Instant
import java.util.*

class InMemoryServer : RdebServer, AbstractVerticle() {

    val log = LoggerFactory.getLogger(InMemoryServer::class.java)

    val unprocessedEvents = Collections.synchronizedList(mutableListOf<InMemoryServerEvent>())
    val processingEvents = Collections.synchronizedList(mutableListOf<InMemoryServerEvent>())
    val broadcasts = Collections.synchronizedList(mutableListOf<InMemoryServerEvent>())
    val eventClientMap = mutableMapOf<String, Long>()
    override fun postEvent(event: Event, broadcast: Boolean, handler: (Boolean) -> Unit) {
        if (broadcast) {
            broadcasts.add(InMemoryServerEvent(event, true))
            handler(true)
        } else {
            unprocessedEvents.add(InMemoryServerEvent(event))
            handler(true)
        }
    }

    override fun getNextEvent(clientId: String, handler: (Event?) -> Unit) {
        val pointer = eventClientMap.getOrDefault(clientId, 0)
        var event:InMemoryServerEvent? = null
        synchronized(unprocessedEvents){
            unprocessedEvents.sortBy { it.createdOn }
            event = unprocessedEvents.find({ it.createdOn > pointer })
            if(event!=null){
                eventClientMap.put(clientId,event!!.createdOn)
            }
        }
        handler(event)
    }

    override fun getNextBroadcast(from: Instant, handler: (Array<Event>) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markEvent(event: Event, clientId: String, handler: (Boolean) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() {
        log.info("InMemory RDEB Server deployed")
    }
}

class InMemoryServerEvent(event: Event, val broadcast: Boolean = false) : Event() {
    val createdOn: Long
    val uuid = UUID.randomUUID()

    init {
        this.type = event.type
        this.props = event.props
        createdOn = Instant.now().toEpochMilli()
    }
}