package me.vihara.atemwrapper.core

import me.vihara.atemwrapper.api.device.AtemDevice
import me.vihara.atemwrapper.api.device.AtemLock
import me.vihara.atemwrapper.api.event.impl.AtemOutputRouteChangeEvent
import me.vihara.atemwrapper.protocol.ProtocolClient
import java.util.concurrent.ConcurrentHashMap

class AtemDeviceImpl(
    hostName: String,
    port: Int
) : ProtocolClient(hostName, port), AtemDevice {

    private var protocolVersion: Float = 0f
    private var info: AtemDevice.Info = AtemDevice.Info()
    private var configuration: String = ""
    private var outputLabels: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    private var inputLabels: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    private var videoInputStatus: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    private var videoOutputRouting: ConcurrentHashMap<Int, Int> = ConcurrentHashMap()
    private var videoOutputLocks: ConcurrentHashMap<Int, AtemLock> = ConcurrentHashMap()

    override fun getProtocolVersion(): Float = protocolVersion
    override fun getInfo(): AtemDevice.Info = info
    override fun getConfiguration(): String = configuration
    override fun getVideoInputStatus(): ConcurrentHashMap<Int, String> = videoInputStatus
    override fun getOutputLabels(): ConcurrentHashMap<Int, String> = outputLabels
    override fun getInputLabels(): ConcurrentHashMap<Int, String> = inputLabels
    override fun getVideoOutputRouting(): ConcurrentHashMap<Int, Int> = videoOutputRouting
    override fun getVideoOutputLocks(): ConcurrentHashMap<Int, AtemLock> = videoOutputLocks

    override fun setInputLabel(output: Int, label: String) {

    }

    override fun setOutputLabel(output: Int, label: String) {

    }

    override fun setOutputRoute(output: Int, input: Int) {
        val oldInput = videoOutputRouting.getOrDefault(output, -1)
        val command = """
            VIDEO OUTPUT ROUTING:
            $output $input
            
        """.trimIndent()
        sendCommand(command) { ack ->
            if (ack) {
                println("ACK")
                EventManager.fireEvent(
                    AtemOutputRouteChangeEvent(
                        output,
                        oldInput,
                        input
                    )
                )
            } else {
                println("NAK")
            }
        }
    }

    override fun setOutputLock(output: Int, lock: AtemLock) {

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
        "OUTPUT LABELS" to { value -> if (value is Map<*, *>) outputLabels = ConcurrentHashMap(value as Map<Int, String>) },
        "INPUT LABELS" to { value -> if (value is Map<*, *>) inputLabels = ConcurrentHashMap(value as Map<Int, String>) },
        "VIDEO INPUT STATUS" to { value -> if (value is Map<*, *>) videoInputStatus = ConcurrentHashMap(value as Map<Int, String>) },
        "VIDEO OUTPUT ROUTING" to { value -> if (value is Map<*, *>) videoOutputRouting = ConcurrentHashMap(value as Map<Int, Int>) },
        "VIDEO OUTPUT LOCKS" to { value -> if (value is Map<*, *>) videoOutputLocks = ConcurrentHashMap(value as Map<Int, AtemLock>) }
    )

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
