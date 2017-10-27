package com.technogi.rdeb.server.codecs

import com.technogi.rdeb.client.Event
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import org.apache.commons.lang3.SerializationUtils.serialize
import org.apache.commons.lang3.SerializationUtils.deserialize

class EventCodec : MessageCodec<Event, Event> {

    override fun encodeToWire(buffer: Buffer?, event: Event?) {
        buffer?.appendBytes(serialize(event))
    }

    override fun decodeFromWire(pos: Int, buffer: Buffer?): Event {
        return deserialize(buffer?.bytes)
    }

    override fun transform(event: Event?) = event

    override fun systemCodecID(): Byte =-1

    override fun name() = "EVENT_CODEC"
}