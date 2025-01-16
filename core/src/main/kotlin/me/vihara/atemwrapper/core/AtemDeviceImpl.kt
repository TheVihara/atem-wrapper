package me.vihara.atemwrapper.core

import me.vihara.atemwrapper.api.device.AtemDevice
import me.vihara.atemwrapper.api.device.event.impl.AtemRouteChangeEvent
import me.vihara.atemwrapper.protocol.ProtocolClient

class AtemDeviceImpl(
    hostName: String,
    port: Int
) : ProtocolClient(hostName, port), AtemDevice {

    private var protocolVersion: Float = 1.0f
    private var modelName: String = "Default Model"
    private var videoInputs: Int = 4
    private var videoOutputs: Int = 2
    private var extraData: Map<String, Any> = emptyMap()
    private var routing: Array<Int?> = arrayOfNulls(videoOutputs)
    private var outputLocks: Array<Boolean> = Array(videoOutputs) { false }

    override fun getProtocolVersion(): Float = protocolVersion
    override fun getModelName(): String = modelName
    override fun getVideoInputs(): Int = videoInputs
    override fun getVideoOutputs(): Int = videoOutputs
    override fun getExtraData(): Map<String, Any> = extraData
    override fun getRouting(): Array<Int?> = routing
    override fun getOutputLocks(): Array<Boolean> = outputLocks

    override fun setRoute(output: Int, input: Int) {
        if (output in routing.indices && input in 0 until videoInputs) {
            routing[output] = input
            println("Route set: Output $output -> Input $input")
        } else {
            println("Invalid route: Output $output or Input $input is out of bounds")
        }
    }

    override fun setOutputLock(output: Int) {
        if (output in outputLocks.indices) {
            outputLocks[output] = true
            println("Output $output locked")
        } else {
            println("Invalid output: $output is out of bounds")
        }
    }

    override fun handleData(data: ByteArray) {
        val received = data.decodeToString()
        val lines = received.split("\n")
        var currentSection = ""
        val parsedData = mutableMapOf<String, Any>()

        for (line in lines) {
            when {
                line.startsWith("Version:") -> {
                    parsedData["Version"] = line.split(":")[1].trim()
                }
                line.startsWith("Model name:") -> {
                    parsedData["Model name"] = line.split(":")[1].trim()
                }
                line.startsWith("Video inputs:") -> {
                    parsedData["Video inputs"] = line.split(":")[1].trim().toInt()
                }
                line.startsWith("Video outputs:") -> {
                    parsedData["Video outputs"] = line.split(":")[1].trim().toInt()
                }
                line.startsWith("INPUT LABELS:") -> {
                    currentSection = "INPUT LABELS"
                    parsedData["Input Labels"] = mutableListOf<String>()
                }
                line.startsWith("OUTPUT LABELS:") -> {
                    currentSection = "OUTPUT LABELS"
                    parsedData["Output Labels"] = mutableListOf<String>()
                }
                currentSection == "INPUT LABELS" -> {
                    (parsedData["Input Labels"] as MutableList<String>).add(line)
                }
                currentSection == "OUTPUT LABELS" -> {
                    (parsedData["Output Labels"] as MutableList<String>).add(line)
                }
            }
        }

        EventManager.INSTANCE.fireEvent(AtemRouteChangeEvent(1, 2, 3))
        println("Parsed data: $parsedData")
    }

    override fun handleError(e: Throwable) {
        println("Protocol error: ${e.message}")
    }
}
