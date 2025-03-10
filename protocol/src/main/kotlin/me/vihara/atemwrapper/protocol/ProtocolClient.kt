package me.vihara.atemwrapper.protocol

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel

abstract class ProtocolClient(private val hostName: String?, private val port: Int?) {
    private var socket: Socket? = null
    private val pingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val commandScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var readJob: Job? = null
    private var writeJob: Job? = null
    private var pingJob: Job? = null
    private val writeChannel = Channel<String>(Channel.UNLIMITED)
    private var ackDeferred: CompletableDeferred<Boolean>? = null

    /**
     * Called when the client successfully connects to the server.
     */
    abstract fun onConnect()

    /**
     * Called when the client disconnects from the server.
     */
    abstract fun onDisconnect()

    /**
     * Called when an error occurs during communication with the server.
     */
    abstract fun onError(e: Throwable)

    /**
     * Connects to the server using the provided host name and port.
     */
    fun connect() {
        if (hostName == null || port == null) {
            println("Host name or port is null")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val selectorManager = SelectorManager(Dispatchers.IO)
                socket = aSocket(selectorManager).tcp().connect(hostName, port)

                socket?.let {
                    startReading(it)
                    startWriting(it)
                    onConnect()
                }
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    handleError(e)
                    onError(e)
                }
            }
        }
    }

    /**
     * Disconnects from the server and cleans up resources.
     */
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

    /**
     * Starts reading data from the socket.
     */
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

    /**
     * Starts writing data to the socket.
     */
    private fun startWriting(socket: Socket) {
        val output = socket.openWriteChannel(autoFlush = true)
        startPing(socket)
        writeJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                for (message in writeChannel) {
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

    /**
     * Processes incoming data from the socket.
     */
    private fun processIncomingData(data: ByteArray) {
        val message = data.decodeToString()
        //println("Received message: $message")
        when {
            message.contains("ACK") -> ackDeferred?.complete(true)
            message.contains("NAK") -> ackDeferred?.complete(false)
            else -> handleData(data)
        }
    }

    /**
     * Starts sending periodic PING messages to the server.
     */
    private fun startPing(socket: Socket) {
        pingJob = pingScope.launch {
            try {
                while (true) {
                    val ack = sendCommand("PING:\n\n")
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

    /**
     * Sends a command to the server and waits for an acknowledgment.
     */
    suspend fun sendCommand(block: String): Boolean {
        ackDeferred = CompletableDeferred()
        writeChannel.send(block)
        return ackDeferred?.await() ?: false
    }

    /**
     * Sends a command to the server and invokes a callback when an acknowledgment is received.
     */
    fun sendCommand(block: String, onAck: (Boolean) -> Unit) {
        ackDeferred = CompletableDeferred()
        commandScope.launch {
            try {
                writeChannel.send(block)
                val ackReceived = ackDeferred?.await() ?: false
                onAck(ackReceived)
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    handleError(e)
                    onError(e)
                }
            }
        }
    }

    /**
     * Reads available data from the channel.
     */
    private suspend fun ByteReadChannel.readAvailable(): ByteArray {
        val buffer = ByteArray(1024)
        val bytesRead = readAvailable(buffer)
        return if (bytesRead > 0) buffer.copyOf(bytesRead) else byteArrayOf()
    }

    /**
     * Handles errors that occur during communication with the server.
     */
    protected open fun handleError(e: Throwable) {
        println("Error occurred: ${e.message}")
    }

    /**
     * Processes incoming data based on the protocol.
     */
    protected abstract fun handleData(data: ByteArray)
}