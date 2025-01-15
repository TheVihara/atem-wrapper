package me.vihara.atemwrapper.protocol

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*

abstract class ProtocolClient(private val hostName: String, private val port: Int) {
    var socket: Socket? = null
    var readJob: Job? = null

    suspend fun connect(): Socket {
        val selectorManager = SelectorManager(Dispatchers.IO)
        socket = withContext(Dispatchers.IO) {
            aSocket(selectorManager).tcp().connect(hostName, port)
        }

        socket?.let { startReading(it) }
        return socket!!
    }

    fun disconnect() {
        readJob?.cancel()
        socket?.close()
        socket = null
    }

    private fun startReading(socket: Socket) {
        val input = socket.openReadChannel()
        readJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (!isClosedForRead(input)) {
                    val data = input.readAvailable()
                    if (data.isNotEmpty()) {
                        handleData(data)
                    }
                }
            } catch (e: Throwable) {
                handleError(e)
            } finally {
                disconnect()
            }
        }
    }

    private fun isClosedForRead(input: ByteReadChannel): Boolean = input.isClosedForRead

    private suspend fun ByteReadChannel.readAvailable(): ByteArray {
        val buffer = ByteArray(1024)
        val bytesRead = readAvailable(buffer)
        return if (bytesRead > 0) buffer.copyOf(bytesRead) else byteArrayOf()
    }

    protected abstract fun handleData(data: ByteArray)

    protected open fun handleError(e: Throwable) {
        println("Error occurred: ${e.message}")
    }
}
