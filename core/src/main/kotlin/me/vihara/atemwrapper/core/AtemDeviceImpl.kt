package me.vihara.atemwrapper.core

import me.vihara.atemwrapper.api.device.AtemDevice
import me.vihara.atemwrapper.api.device.AtemLock
import me.vihara.atemwrapper.api.device.AtemStatus
import me.vihara.atemwrapper.api.event.impl.AtemInputLabelChangeEvent
import me.vihara.atemwrapper.api.event.impl.AtemLockChangeEvent
import me.vihara.atemwrapper.api.event.impl.AtemOutputLabelChangeEvent
import me.vihara.atemwrapper.api.event.impl.AtemOutputRouteChangeEvent
import me.vihara.atemwrapper.protocol.ProtocolClient
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

open class AtemDeviceImpl(
    val hostName: String?,
    port: Int?
) : ProtocolClient(hostName, port), AtemDevice {

    companion object {
        private var logger: Logger = Logger.getLogger(AtemDeviceImpl::class.java.name)
    }

    private var status: AtemStatus = AtemStatus.DISCONNECTED
    private var protocolVersion: Float = 0f
    private var info: AtemDevice.Info = AtemDevice.Info()
    private var configuration: String = ""
    private var outputLabels: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    private var inputLabels: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    private var videoInputStatus: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    private var videoOutputRouting: ConcurrentHashMap<Int, Int> = ConcurrentHashMap()
    private var videoOutputLocks: ConcurrentHashMap<Int, AtemLock> = ConcurrentHashMap()

    override fun getStatus(): AtemStatus? = status
    override fun getProtocolVersion(): Float = protocolVersion
    override fun getInfo(): AtemDevice.Info = info
    override fun getConfiguration(): String = configuration
    override fun getVideoInputStatus(): ConcurrentHashMap<Int, String> = videoInputStatus
    override fun getOutputLabels(): ConcurrentHashMap<Int, String> = outputLabels
    override fun getInputLabels(): ConcurrentHashMap<Int, String> = inputLabels
    override fun getVideoOutputRouting(): ConcurrentHashMap<Int, Int> = videoOutputRouting
    override fun getVideoOutputLocks(): ConcurrentHashMap<Int, AtemLock> = videoOutputLocks

    override fun setInputLabel(input: Int, label: String) {
        val oldLabel = inputLabels.getOrDefault(input, "None")
        val command = """
            INPUT LABELS:
            $input $label
            
        """.trimIndent()
        sendCommand(command) { ack ->
            if (ack) {
                //println("ACK")
                EventManager.fireEvent(
                    AtemInputLabelChangeEvent(
                        this,
                        input,
                        oldLabel,
                        label
                    )
                )
            } else {
                //println("NAK")
            }
        }
    }

    override fun setOutputLabel(output: Int, label: String) {
        val oldLabel = outputLabels.getOrDefault(output, "None")
        val command = """
            OUTPUT LABELS:
            $output $label
            
        """.trimIndent()
        sendCommand(command) { ack ->
            if (ack) {
                //println("ACK")
                EventManager.fireEvent(
                    AtemOutputLabelChangeEvent(
                        this,
                        output,
                        oldLabel,
                        label
                    )
                )
            } else {
                //println("NAK")
            }
        }
    }

    override fun setOutputRoute(output: Int, input: Int) {
        val oldInput = videoOutputRouting.getOrDefault(output, -1)
        val command = """
            VIDEO OUTPUT ROUTING:
            $output $input
            
        """.trimIndent()
        sendCommand(command) { ack ->
            if (ack) {
                //println("ACK")
                EventManager.fireEvent(
                    AtemOutputRouteChangeEvent(
                        this,
                        output,
                        oldInput,
                        input
                    )
                )
            } else {
                //println("NAK")
            }
        }
    }

    override fun setOutputLock(output: Int, lock: AtemLock) {
        val oldLock = videoOutputLocks.getOrDefault(output, AtemLock.LOCKED)
        val forceCommand = """
            INPUT LABELS:
            $output F
            
        """.trimIndent()
        val actualCommand = """
            INPUT LABELS:
            $output ${lock.id}
            
        """.trimIndent()
        sendCommand(forceCommand) { ack ->
            if (ack) {
                //println("ACK")
                sendCommand(actualCommand) { ack ->
                    if (ack) {
                        //println("ACK")
                        EventManager.fireEvent(
                            AtemLockChangeEvent(
                                this,
                                output,
                                oldLock,
                                lock
                            )
                        )
                    } else {
                        //println("NAK")
                    }
                }
            } else {
                //println("NAK")
            }
        }
    }

    override fun onConnect() {
        status = AtemStatus.CONNECTED
    }

    override fun onDisconnect() {
        status = AtemStatus.DISCONNECTED
    }

    override fun onError(e: Throwable) {
        status = AtemStatus.ERROR
        logger.log(Level.SEVERE, hostName, e)
    }

    override fun handleData(data: ByteArray) {
        val received = data.decodeToString()

        val lines = received.split("\n")
        var currentSection: String? = null
        val parsedData = mutableMapOf<String, Any>()
        val currentContent = StringBuilder()

        for (line in lines) {
            if (!line.endsWith(":") && !line.isUppercase()) {
                if (currentContent.isNotEmpty()) currentContent.append("\n")
                currentContent.append(line)
            } else {
                if (currentSection != null) {
                    val parser = ParserRegistry.getParser<Any>(currentSection)
                    if (parser != null) {
                        val parsedSection = parser.parse(currentContent.toString())
                        parsedData[currentSection] = parsedSection
                    }
                }

                currentSection = line.removeSuffix(":").trim()
                currentContent.clear()
            }
        }

        if (currentSection != null && currentContent.isNotEmpty()) {
            val parser = ParserRegistry.getParser<Any>(currentSection)
            if (parser != null) {
                val parsedSection = parser.parse(currentContent.toString())
                parsedData[currentSection] = parsedSection
            }
        }

        parsedData.forEach { (section, value) ->
            fieldUpdaters[section]?.invoke(value)
        }

        //println("HANDLED DATA : " + toString() + " ----------------------------------")
    }

    override fun handleError(e: Throwable) {
        println("Protocol error: ${e.message}")
    }

    fun String.isUppercase(): Boolean {
        if (this.any { it.isDigit() }) return false
        return this == this.uppercase()
    }

    private val fieldUpdaters: Map<String, (Any) -> Unit> = mapOf(
        "PROTOCOL PREAMBLE" to { value -> if (value is Float) protocolVersion = value },
        "VIDEOHUB DEVICE" to { value -> if (value is AtemDevice.Info) info = value },
        "CONFIGURATION" to { value -> if (value is String) configuration = value },
        "OUTPUT LABELS" to { value -> if (value is Map<*, *>) handleOutputLabelUpdate(value as Map<Int, String>) },
        "INPUT LABELS" to { value -> if (value is Map<*, *>) handleInputLabelUpdate(value as Map<Int, String>) },
        "VIDEO INPUT STATUS" to { value -> if (value is Map<*, *>) videoInputStatus.putAll(value as Map<Int, String>) },
        "VIDEO OUTPUT ROUTING" to { value -> if (value is Map<*, *>) handleRouteUpdate(value as Map<Int, Int>) },
        "VIDEO OUTPUT LOCKS" to { value -> if (value is Map<*, *>) handleLockUpdate(value as Map<Int, AtemLock>) }
    )

    private fun handleRouteUpdate(value: Map<Int, Int>) {
        value.entries.stream()
            .filter { e ->
                e.value != videoOutputRouting.get(e.key)
            }.forEach {
                val event = AtemOutputRouteChangeEvent(
                    this,
                    it.key,
                    videoOutputRouting.get(it.key)!!,
                    it.value
                )

                EventManager.fireEvent(event)
            }

        videoOutputRouting.putAll(value)
    }

    private fun handleOutputLabelUpdate(value: Map<Int, String>) {
        value.entries.stream()
            .filter { e ->
                e.value != outputLabels.get(e.key)
            }.forEach {
                val event = AtemOutputLabelChangeEvent(
                    this,
                    it.key,
                    outputLabels.get(it.key)!!,
                    it.value
                )

                EventManager.fireEvent(event)
            }

        outputLabels.putAll(value)
    }

    private fun handleInputLabelUpdate(value: Map<Int, String>) {
        value.entries.stream()
            .filter { e ->
                e.value != inputLabels.get(e.key)
            }.forEach {
                val event = AtemInputLabelChangeEvent(
                    this,
                    it.key,
                    inputLabels.get(it.key)!!,
                    it.value
                )

                EventManager.fireEvent(event)
            }

        inputLabels.putAll(value)
    }

    private fun handleLockUpdate(value: Map<Int, AtemLock>) {
        value.entries.stream()
            .filter { e ->
                e.value != videoOutputLocks.get(e.key)
            }.forEach {
                val event = AtemLockChangeEvent(
                    this,
                    it.key,
                    videoOutputLocks.get(it.key)!!,
                    it.value
                )

                EventManager.fireEvent(event)
            }

        videoOutputLocks.putAll(value)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("AtemDeviceImpl Details:\n")
        sb.append("Protocol Version: $protocolVersion\n")
        sb.append("Info: $info\n")
        sb.append("Configuration: $configuration\n")

        sb.append("Output Labels:\n")
        outputLabels.forEach { (key, value) ->
            sb.append("  Output $key: $value\n")
        }

        sb.append("Input Labels:\n")
        inputLabels.forEach { (key, value) ->
            sb.append("  Input $key: $value\n")
        }

        sb.append("Video Input Status:\n")
        videoInputStatus.forEach { (key, value) ->
            sb.append("  Input $key: $value\n")
        }

        sb.append("Video Output Routing:\n")
        videoOutputRouting.forEach { (output, input) ->
            sb.append("  Output $output -> Input $input\n")
        }

        sb.append("Video Output Locks:\n")
        videoOutputLocks.forEach { (output, lock) ->
            sb.append("  Output $output: $lock\n")
        }

        return sb.toString()
    }
}
