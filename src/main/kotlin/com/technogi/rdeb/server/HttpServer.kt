package com.technogi.rdeb.server

import com.technogi.rdeb.client.Constants
import com.technogi.rdeb.client.Event
import com.technogi.rdeb.server.codecs.EventCodec
import com.technogi.rdeb.server.impl.InMemoryServer
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

class HttpServer : AbstractVerticle() {

    val CONTENT_TYPE_EVENT = "application/rdeb-event"
    val CONTENT_TYPE_BROADCAST = "application/rdeb-broadcast"

    val log = LoggerFactory.getLogger(HttpServer::class.java)

    lateinit var rdebServer: RdebServer

    override fun start() {

        rdebServer = InMemoryServer()
        vertx.eventBus().registerCodec(EventCodec())
        vertx.deployVerticle(rdebServer)

        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        router.post().handler(BodyHandler.create())
        router.post().handler { ctx ->
            post(ctx) {
                if (it) ctx.response().setStatusCode(201).end("OK")
                else ctx.response().setStatusCode(500).end("ERROR")
            }
        }

        router.get().handler { ctx ->
            get(ctx) {

            }
        }
        server.requestHandler({ router.accept(it) }).listen(8080)

    }

    fun post(ctx: RoutingContext, handler: (Boolean) -> Unit) {
        val type = ctx.request().getHeader("content-type")
        when (type) {
            CONTENT_TYPE_BROADCAST ->
                rdebServer.postEvent(Json.decodeValue(ctx.body, Event::class.java), broadcast = true, handler = handler)

            CONTENT_TYPE_EVENT ->
                rdebServer.postEvent(Json.decodeValue(ctx.body, Event::class.java), handler = handler)
            else -> {
                ctx.response().statusCode = 406
                ctx.response().end()
            }
        }
    }

    fun get(ctx: RoutingContext, handler: (Event) -> Unit) {
        val clientId = ctx.request().headers().get(Constants.HTTP_CLIENT_HEADER)
        if (clientId == null) {
            ctx.response().setStatusCode(406).end(JsonObject().put("desc", "Missing client").encode())
        } else {
            rdebServer.getNextEvent(clientId) { event ->
                if (event == null) ctx.response().setStatusCode(404).end();
                else ctx.response()
                  .setStatusCode(200)
                  .end(JsonObject()
                    .put("type", event.type)
                    .put("props", event.props).encode())
            }
        }
    }
}