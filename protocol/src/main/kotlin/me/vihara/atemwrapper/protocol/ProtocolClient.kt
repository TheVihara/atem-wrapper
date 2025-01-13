package me.vihara.atemwrapper.protocol

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers

interface ProtocolClient {
    var socket: Socket?

    suspend fun connect(hostName: String, port: Int): Socket {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = aSocket(selectorManager).tcp().connect(hostName, port)

        socket.openWriteChannel(autoFlush = true)
        socket.openReadChannel()

        return socket
    }

    fun disconnect() {
        socket?.close()
    }
}