package me.vihara.atemwrapper.protocol

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

abstract class ProtocolClient(private val hostName: String, private val port: Int) {
    private var socket: Socket? = null
    private var readJob: Job? = null
    private var writeJob: Job? = null
    private val writeChannel = Channel<String>(Channel.UNLIMITED)
    private var ackDeferred: CompletableDeferred<Boolean>? = null

    suspend fun connect(): Socket {
        val selectorManager = SelectorManager(Dispatchers.IO)
        socket = withContext(Dispatchers.IO) {
            aSocket(selectorManager).tcp().connect(hostName, port)
        }

        socket?.let {
            startReading(it)
            startWriting(it)
            startPing()
        }

        return socket!!
    }

    fun disconnect() {
        readJob?.cancel()
        writeJob?.cancel()
        writeChannel.close()
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
                        processIncomingData(data)
                    }
                }
            } catch (e: Throwable) {
                handleError(e)
            } finally {
                disconnect()
            }
        }
    }

    private fun startWriting(socket: Socket) {
        val output = socket.openWriteChannel(autoFlush = true)
        writeJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                for (message in writeChannel) {
                    println("Writing $message")
                    output.writeStringUtf8(message)
                }
            } catch (e: Throwable) {
                handleError(e)
            }
        }
    }

    private fun processIncomingData(data: ByteArray) {
        val message = data.decodeToString()
        println("$message je msg")
        if (message.contains("ACK")) {
            ackDeferred?.complete(true)
        } else if (message.contains("NAK")) {
            ackDeferred?.complete(false)
        } else {
            handleData(data)
        }
    }

    private suspend fun startPing() {
        while (true) {
            delay(1000)
            sendCommand("PING:\n\n")
        }
    }

    fun sendCommand(block: String) {
        val termination = if (block.endsWith("\n\n")) "" else "\n"
        ackDeferred = CompletableDeferred()
        runBlocking {
            println("Sending $block")
            writeChannel.send(block + termination)
            ackDeferred?.await()
        }
    }

    fun sendCommand(block: String, onAck: (Boolean) -> Unit) {
        val termination = if (block.endsWith("\n\n")) "" else "\n"
        ackDeferred = CompletableDeferred()
        runBlocking {
            writeChannel.send(block + termination)
            val ackReceived = ackDeferred?.await() ?: false
            onAck(ackReceived)
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