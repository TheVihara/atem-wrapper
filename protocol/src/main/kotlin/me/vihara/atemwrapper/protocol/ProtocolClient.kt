package me.vihara.atemwrapper.protocol

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

abstract class ProtocolClient(private val hostName: String, private val port: Int) {
    private var socket: Socket? = null
    private val pingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val commandScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var readJob: Job? = null
    private var writeJob: Job? = null
    private var pingJob: Job? = null
    private val writeChannel = Channel<String>(Channel.UNLIMITED)
    private var ackDeferred: CompletableDeferred<Boolean>? = null

    abstract fun onConnect()
    abstract fun onDisconnect()
    abstract fun onError(e: Throwable)

    fun connect() {
        CoroutineScope(Dispatchers.IO).launch {
            val selectorManager = SelectorManager(Dispatchers.IO)
            socket = withContext(Dispatchers.IO) {
                aSocket(selectorManager).tcp().connect(hostName, port)
            }

            socket?.let {
                startReading(it)
                startWriting(it)
                onConnect()
            }
        }

        /*
                return socket ?: throw IllegalStateException("Socket failed to connect")
        */
    }

    fun disconnect() {
        pingScope.cancel()
        commandScope.cancel()
        writeChannel.close()
        pingJob?.cancel()
        readJob?.cancel()
        writeJob?.cancel()
        socket?.close()
        socket = null
        onDisconnect()
    }

    private fun startReading(socket: Socket) {
        val input = socket.openReadChannel()
        readJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (!input.isClosedForRead) {
                    val data = input.readAvailable()
                    if (data.isNotEmpty()) {
                        processIncomingData(data)
                    }
                }
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    handleError(e)
                    onError(e)
                }
            } finally {
                disconnect()
            }
        }
    }

    private fun startWriting(socket: Socket) {
        val output = socket.openWriteChannel(autoFlush = true)
        startPing(socket)
        writeJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                for (message in writeChannel) {
                    //println("Writing $message")
                    output.writeStringUtf8(message)
                }
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    handleError(e)
                    onError(e)
                }
            }
        }
    }

    private fun processIncomingData(data: ByteArray) {
        val message = data.decodeToString()
        //println("Received message: $message")
        when {
            message.contains("ACK") -> ackDeferred?.complete(true)
            message.contains("NAK") -> ackDeferred?.complete(false)
            else -> handleData(data)
        }
    }

    private fun startPing(socket: Socket) {
        pingJob = pingScope.launch {
            try {
                while (true) {
                    //println("PINGING")
                    val ack = sendCommand("PING:\n\n")
                    //println("Ping result: $ack")
                    delay(1000)
                }
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    handleError(e)
                    onError(e)
                }
            }
        }
    }

    suspend fun sendCommand(block: String): Boolean {
        ackDeferred = CompletableDeferred()
        //println("Sending command: $block")
        writeChannel.send(block)
        val result = ackDeferred?.await() ?: false
        //println("Command result: $result")
        return result
    }

    fun sendCommand(block: String, onAck: (Boolean) -> Unit) {
        ackDeferred = CompletableDeferred()
        commandScope.launch {
            try {
                //println("Sending command: $block")
                writeChannel.send(block)
                val ackReceived = ackDeferred?.await() ?: false
                //println("Command result: $ackReceived")
                onAck(ackReceived)
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    handleError(e)
                    onError(e)
                }
            }
        }
    }

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